/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.ning.http.client.AsyncHttpClient;
import com.urbanairship.connect.java8.Consumer;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class for handling {@link com.urbanairship.connect.client.MobileEventStream} interactions.
 * Includes basic stream connection/consumption, reconnection, and offset tracking.
 */
public final class MobileEventConsumerService extends AbstractExecutionThreadService {

    private static final Logger log = LogManager.getLogger(MobileEventConsumerService.class);

    private final Supplier<Optional<String>> latestOffsetProvider;
    private final ConnectClientConfiguration config;
    private final AtomicBoolean doConsume;
    private final AsyncHttpClient asyncClient;
    private final Consumer<String> eventReceiver;
    private final StreamQueryDescriptor streamQueryDescriptor;
    private final StreamSupplier supplier;

    private volatile MobileEventStream mobileEventStream;

    public static Builder newBuilder() {
        return new Builder();
    }

    private MobileEventConsumerService(Consumer<String> consumer,
                                       AsyncHttpClient client,
                                       Supplier<Optional<String>> latestOffsetProvider,
                                       StreamQueryDescriptor streamQueryDescriptor,
                                       Configuration config,
                                       StreamSupplier supplier) {
        this.latestOffsetProvider = latestOffsetProvider;
        this.config = new ConnectClientConfiguration(config);
        this.asyncClient = client;
        this.doConsume = new AtomicBoolean(true);
        this.eventReceiver = consumer;
        this.streamQueryDescriptor = streamQueryDescriptor;
        this.supplier = supplier;
    }

    /**
     * Gets the DoConsume flag indicating whether or not the handler should
     * continue to consume / reconnect to a stream.
     *
     * @return AtomicBoolean
     */
    @VisibleForTesting
    AtomicBoolean getDoConsume() {
        return doConsume;
    }

    /**
     * Runs the stream handler by setting doConsume to {@code true} and creating a
     * {@link com.urbanairship.connect.client.MobileEventStream} instance.  The handler will
     * continue to consume or create new {@link com.urbanairship.connect.client.MobileEventStream} instances
     * until the handler is stopped.
     */
    @Override
    protected void run() {
        doConsume.set(true);
        try {
            stream();
        }
        finally {
            asyncClient.close();
        }
    }

    private void stream() {
        String appKey = streamQueryDescriptor.getCreds().getAppKey();
        while (doConsume.get()) {

            Optional<String> startingOffset = latestOffsetProvider.get();
            try (MobileEventStream newMobileEventStream = supplier.get(streamQueryDescriptor, asyncClient, eventReceiver, config.mesUrl)) {
                mobileEventStream = newMobileEventStream;
                mobileEventStream.read(startingOffset);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            catch (ConnectionException e) {
                throw e;
            }
            catch (Throwable throwable) {
                log.error("Error encountered while consuming stream for app " + appKey, throwable);
            }
        }
    }

    @Override
    public void triggerShutdown() {
        log.info("Shutting down stream handler for app " + streamQueryDescriptor.getCreds().getAppKey());
        doConsume.set(false);

        if (mobileEventStream != null) {
            try {
                mobileEventStream.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static final class Builder{

        private StreamSupplier supplier = new MobileEventStreamSupplier();
        private AsyncHttpClient client = null;

        private Consumer<String> consumer;
        private Supplier<Optional<String>> latestOffsetProvider;
        private StreamQueryDescriptor streamQueryDescriptor;
        private Configuration config;

        private Builder() {}

        public Builder setConsumer(Consumer<String> consumer) {
            this.consumer = consumer;
            return this;
        }

        public Builder setLatestOffsetProvider(Supplier<Optional<String>> latestOffsetProvider) {
            this.latestOffsetProvider = latestOffsetProvider;
            return this;
        }

        public Builder setStreamQueryDescriptor(StreamQueryDescriptor descriptor) {
            this.streamQueryDescriptor = descriptor;
            return this;
        }

        public Builder setConfig(Configuration config) {
            this.config = config;
            return this;
        }

        @VisibleForTesting
        Builder setSupplier(StreamSupplier supplier) {
            this.supplier = supplier;
            return this;
        }

        @VisibleForTesting
        Builder setClient(AsyncHttpClient client) {
            this.client = client;
            return this;
        }

        public MobileEventConsumerService build() {
            Preconditions.checkNotNull(consumer, "Event consumer must be provided");
            Preconditions.checkNotNull(latestOffsetProvider, "Offset manager must be provided");
            Preconditions.checkNotNull(streamQueryDescriptor, "Stream query descriptor must be provided");
            Preconditions.checkNotNull(config, "Configuration must be provided");

            if (client == null) {
                client = StreamUtils.buildHttpClient(new ConnectClientConfiguration(config));
            }

            return new MobileEventConsumerService(
                    consumer,
                    client,
                    latestOffsetProvider,
                    streamQueryDescriptor,
                    config,
                    supplier
            );
        }
    }

    // Default Supplier implementation
    private static class MobileEventStreamSupplier implements StreamSupplier {
        @Override
        public MobileEventStream get(StreamQueryDescriptor descriptor,
                                     AsyncHttpClient client,
                                     Consumer<String> eventConsumer,
                                     String url) {
            return new MobileEventStream(descriptor, client, eventConsumer, url);
        }
    }
}
