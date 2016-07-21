/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.ning.http.client.AsyncHttpClient;
import com.urbanairship.connect.java8.Consumer;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class for handling {@link com.urbanairship.connect.client.MobileEventStream} interactions.
 * Includes basic stream connection/consumption and reconnection on retryable errors.
 *
 * Proper use of this class requires that only a single call ever be made to the {@link #run()} method. The {@link #stop()}
 * method can be called by any thread, but should not be called before {@link #run()} is called.
 *
 * MobileEventConsumeTask objects cannot be reused.
 */
public final class MobileEventConsumeTask implements Runnable {

    private static final Logger log = LogManager.getLogger(MobileEventConsumeTask.class);

    private final Supplier<Optional<String>> latestOffsetProvider;
    private final ConnectClientConfiguration config;
    private final AsyncHttpClient asyncClient;
    private final Consumer<String> eventReceiver;
    private final StreamQueryDescriptor streamQueryDescriptor;
    private final StreamSupplier supplier;

    private final AtomicBoolean active = new AtomicBoolean(true);
    private final CountDownLatch done = new CountDownLatch(1);

    private final Object streamLock = new Object();
    private volatile MobileEventStream mobileEventStream;

    public static Builder newBuilder() {
        return new Builder();
    }

    private MobileEventConsumeTask(Consumer<String> consumer,
                                   AsyncHttpClient client,
                                   Supplier<Optional<String>> latestOffsetProvider,
                                   StreamQueryDescriptor streamQueryDescriptor,
                                   Configuration config,
                                   StreamSupplier supplier) {
        this.latestOffsetProvider = latestOffsetProvider;
        this.config = new ConnectClientConfiguration(config);
        this.asyncClient = client;
        this.eventReceiver = consumer;
        this.streamQueryDescriptor = streamQueryDescriptor;
        this.supplier = supplier;
    }

    /**
     * Begins the process of consuming from the stream by interacting {@link MobileEventStream}. The call will block
     * until {@link #stop()} is called.
     */
    @Override
    public void run() {
        try {
            stream();
        }
        finally {
            asyncClient.close();
            done.countDown();
        }
    }

    private void stream() {
        String appKey = streamQueryDescriptor.getCreds().getAppKey();
        while (active.get()) {

            Optional<String> startingOffset = latestOffsetProvider.get();
            try (MobileEventStream newMobileEventStream = supplier.get(streamQueryDescriptor, asyncClient, eventReceiver, config.mesUrl)) {
                transitionToReading(startingOffset, newMobileEventStream);
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

    private void transitionToReading(Optional<String> startingOffset, MobileEventStream newMobileEventStream) throws InterruptedException {
        // The streamLock sync is used to ensure consistency between the stop method and the swap of the mobileEventStream
        // resource in the case of a race. We want to ensure we only active and begin reading from the stream if a
        // stop signal has not been received. Note, it's ok to call MobileEventStream.read() even if the MobileEventStream
        // has been closed. Therefore, the possible race where we get in the sync block, swap the stream resource and
        // between exiting the sync block and the read call, a stop call occurs and closes the stream resource is ok.
        synchronized (streamLock) {
            if (!active.get()) {
                return;
            }

            mobileEventStream = newMobileEventStream;
        }

        mobileEventStream.read(startingOffset);
    }

    /**
     * Stops the task and causes the {@link #run()} method to exit.
     */
    public void stop() {
        log.info("Shutting down stream handler for app " + streamQueryDescriptor.getCreds().getAppKey());
        if (!active.compareAndSet(true, false)) {
            return;
        }

        // The streamLock sync is used to guard the potential race between a call to stop and an iteration inside the
        // stream method. We want to ensure that if the mobileEventStream resource is setup, we close it. The streamLock
        // is used on swapping that resource and so we know it cannot change and its state is consistent inside the sync.
        synchronized (streamLock) {
            if (mobileEventStream != null) {
                try {
                    mobileEventStream.close();
                }
                catch (Exception e) {
                    throw new RuntimeException("Failed to shutdown stream and stop gracefully", e);
                }
            }
        }

        try {
            done.await();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
        Builder setStreamSupplier(StreamSupplier supplier) {
            this.supplier = supplier;
            return this;
        }

        public MobileEventConsumeTask build() {
            Preconditions.checkNotNull(consumer, "Event consumer must be provided");
            Preconditions.checkNotNull(latestOffsetProvider, "Offset manager must be provided");
            Preconditions.checkNotNull(streamQueryDescriptor, "Stream query descriptor must be provided");
            Preconditions.checkNotNull(config, "Configuration must be provided");

            if (client == null) {
                client = StreamUtils.buildHttpClient(new ConnectClientConfiguration(config));
            }

            return new MobileEventConsumeTask(
                    consumer,
                    client,
                    latestOffsetProvider,
                    streamQueryDescriptor,
                    config,
                    supplier
            );
        }
    }

    // Default StreamSupplier implementation
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
