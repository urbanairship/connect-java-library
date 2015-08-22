package com.urbanairship.connect.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.ning.http.client.AsyncHttpClient;
import com.urbanairship.connect.client.model.responses.Event;
import com.urbanairship.connect.client.offsets.OffsetManager;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * A class for handling {@link com.urbanairship.connect.client.MobileEventStream} interactions.
 * Includes basic stream connection/consumption, reconnection, and offset tracking.
 */
public class StreamHandler extends AbstractExecutionThreadService {

    private static final Logger log = LogManager.getLogger(StreamHandler.class);

    private final OffsetManager offsetManager;
    private final ConnectClientConfiguration config;
    private final AtomicBoolean doConsume;
    private final AsyncHttpClient asyncClient;
    private final RawEventReceiver rawEventReceiver;
    private final StreamDescriptor baseStreamDescriptor;
    private final StreamSupplier supplier;
    private MobileEventStream stream;

    /**
     * StreamHandler builder
     * @return Builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    private StreamHandler(Consumer<Event> consumer,
                         AsyncHttpClient client,
                         OffsetManager offsetManager,
                         StreamDescriptor baseStreamDescriptor,
                         Configuration config,
                         StreamSupplier supplier) {
        this.offsetManager = offsetManager;
        this.config = new ConnectClientConfiguration(config);
        this.asyncClient = client;
        this.doConsume = new AtomicBoolean(true);
        this.rawEventReceiver = new RawEventReceiver(consumer);
        this.baseStreamDescriptor = baseStreamDescriptor;
        this.supplier = supplier;
    }

    /**
     * Gets the implemented offset manager.
     * @return OffsetManager
     */
    public OffsetManager getOffsetManager() {
        return offsetManager;
    }

    /**
     * Gets the library configuration.
     *
     * @return ConnectClientConfiguration
     */
    public ConnectClientConfiguration getConfig() {
        return config;
    }

    /**
     * Gets the DoConsume flag indicating whether or not the handler should
     * continue to consume / reconnect to a stream.
     *
     * @return AtomicBoolean
     */
    public AtomicBoolean getDoConsume() {
        return doConsume;
    }

    /**
     * Get the Http client.
     *
     * @return AsyncHttpClient
     */
    public AsyncHttpClient getAsyncClient() {
        return asyncClient;
    }

    /**
     * Get the event receiver.
     *
     * @return RawEventReceiver
     */
    public RawEventReceiver getRawEventReceiver() {
        return rawEventReceiver;
    }

    /**
     * Get the base stream descriptor.
     *
     * @return StreamDescriptor
     */
    public StreamDescriptor getBaseStreamDescriptor() {
        return baseStreamDescriptor;
    }

    /**
     * Runs the stream handler by setting doConsume to {@code true} and creating a
     * {@link com.urbanairship.connect.client.MobileEventStream} instance.  The handler will
     * continue to consume or create new {@link com.urbanairship.connect.client.MobileEventStream} instances
     * until either the handler is stopped or the reconnect attempt limit is reached.
     */
    @Override
    public void run() {
        doConsume.set(true);
        stream();
    }

    private void stream() {

        int consumptionAttempt = 0;

        try {
            while (doConsume.get()) {
                boolean connected = false;

                // if this is not the original consumption attempt, create a new StreamDescriptor with the most recent offset
                final StreamDescriptor descriptor;
                if (consumptionAttempt == 0) {
                    descriptor = baseStreamDescriptor;
                } else {
                    descriptor = StreamUtils.buildNewDescriptor(baseStreamDescriptor, offsetManager);
                }

                // create a new MobileEventStream
                try (MobileEventStream newStream = supplier.get(descriptor, asyncClient, rawEventReceiver, config.mesUrl)) {
                    stream = newStream;

                    // connect to the MobileEventStream
                    log.info("Connecting to stream for app " + baseStreamDescriptor.getCreds().getAppKey());
                    connected = connectWithRetries(stream);

                    // if connection attempts fail, exit the consumption loop.
                    if (!connected) {
                        // TODO add handling to let connection error bubble up
                        break;
                    }

                    // consume from the MobileEventStream
                    log.info("Consuming from stream for app " + baseStreamDescriptor.getCreds().getAppKey());
                    stream.consume(config.maxAppStreamConsumeTime, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Throwable throwable) {
                    log.error("Error encountered while consuming stream for app " + baseStreamDescriptor.getCreds().getAppKey(), throwable);
                } finally {
                    log.info("Ending stream handling for app " + baseStreamDescriptor.getCreds().getAppKey());

                    // update consumption attempt
                    consumptionAttempt += 1;

                    if (connected) {
                        // update offset
                        log.debug("Updating offset for app " + baseStreamDescriptor.getCreds().getAppKey());
                        offsetManager.update(rawEventReceiver.get());
                    }
                }
            }
        } finally {
            // close the HTTP client
            asyncClient.close();
        }
    }

    private boolean connectWithRetries(MobileEventStream stream) throws InterruptedException {

        int baseConnectionAttempts = 0;
        long backoff = config.mesReconnectBackoffTime;

        for (int connectionAttempts = baseConnectionAttempts; connectionAttempts < config.maxConnectionAttempts; connectionAttempts++) {

            try {
                // have the thread sleep before trying to reconnect
                if (connectionAttempts > baseConnectionAttempts) {
                    Thread.sleep(backoff);
                }

                // update the backoff value for each connection attempt
                backoff += backoff * connectionAttempts;
                if (backoff > config.maxConnectionAttempts * config.mesReconnectBackoffTime) {
                    backoff = config.maxConnectionAttempts * config.mesReconnectBackoffTime;
                }

                stream.connect(config.appStreamConnectTimeout, TimeUnit.MILLISECONDS);
                return true;
            } catch (InterruptedException e) {
                throw e;
            } catch (Throwable throwable) {
                log.error("Error encountered while connecting to stream for app " + baseStreamDescriptor.getCreds().getAppKey(), throwable);
            }
        }

        // if the reconnection retry limit is reached without connection, exit.
        log.error("Handler failed to reconnect after " + config.maxConnectionAttempts + " attempts, exiting.");
        return false;
    }

    /**
     * Stops any stream handling by setting doConsume to {@code false}.
     */
    @Override
    public void triggerShutdown() {
        log.info("Shutting down stream handler for app " + baseStreamDescriptor.getCreds().getAppKey());
        doConsume.set(false);

        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static final class Builder{
        private Consumer<Event> consumer;
        private AsyncHttpClient client;
        private OffsetManager offsetManager;
        private StreamDescriptor baseStreamDescriptor;
        private Configuration config;
        private StreamSupplier supplier = new MobileEventStreamSupplier();

        private Builder() {}

        /**
         * Set the Event consumer.
         *
         * @param consumer Consumer<>Event</>
         * @return Builder
         */
        public Builder setConsumer(Consumer<Event> consumer) {
            this.consumer = consumer;
            return this;
        }

        /**
         * Set the HTTP client.
         *
         * @param client AsyncHttpClient
         * @return Builder
         */
        public Builder setClient(AsyncHttpClient client) {
            this.client = client;
            return this;
        }

        /**
         * Set the offset manager.
         *
         * @param offsetManager OffsetManager
         * @return Builder
         */
        public Builder setOffsetManager(OffsetManager offsetManager) {
            this.offsetManager = offsetManager;
            return this;
        }

        /**
         * Set the base stream descriptor.
         *
         * @param descriptor StreamDescriptor
         * @return Builder
         */
        public Builder setBaseStreamDescriptor(StreamDescriptor descriptor) {
            this.baseStreamDescriptor = descriptor;
            return this;
        }

        /**
         * Set any config overrides.
         *
         * @param config Configuration
         * @return Builder
         */
        public Builder setConfig(Configuration config) {
            this.config = config;
            return this;
        }

        /**
         * Set the stream supplier.  Not intended for non-testing use.
         *
         * @param supplier Supplier
         * @return Builder
         */
        @VisibleForTesting
        public Builder setSupplier(StreamSupplier supplier) {
            this.supplier = supplier;
            return this;
        }

        /**
         * Build the StreamHandler object.
         *
         * @return StreamHandler
         */
        public StreamHandler build() {
            return new StreamHandler(consumer,
                client,
                offsetManager,
                baseStreamDescriptor,
                config,
                supplier);
        }
    }

    // Straightforward Supplier implementation
    private static class MobileEventStreamSupplier implements StreamSupplier {
        @Override
        public MobileEventStream get(StreamDescriptor descriptor,
                    AsyncHttpClient client,
                    Consumer<String> eventConsumer,
                    String url) {
            return new MobileEventStream(descriptor, client, eventConsumer, url);
        }
    }
}
