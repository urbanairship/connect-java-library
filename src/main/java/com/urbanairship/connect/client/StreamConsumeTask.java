/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ning.http.client.AsyncHttpClient;
import com.urbanairship.connect.client.model.GsonUtil;
import com.urbanairship.connect.client.model.StartPosition;
import com.urbanairship.connect.java8.Consumer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A class for handling {@link StreamConnection} interactions and expose the data received from the Urban Airship Connect
 * API out through a {@link BlockingQueue} provided by the user. Includes basic stream connection/consumption and
 * reconnection on retryable errors.
 *
 * Proper use of this class requires that only a single call ever be made to the {@link #run()} method. The {@link #stop()}
 * method can be called by any thread, but should not be called before {@link #run()} is called.
 *
 * StreamConsumeTask objects cannot be reused.
 */
public final class StreamConsumeTask implements Runnable {

    private static final Logger log = LogManager.getLogger(StreamConsumeTask.class);

    private final AsyncHttpClient http;
    private final StreamQueryDescriptor streamQueryDescriptor;
    private final Optional<StartPosition> initialPosition;
    private final StreamConnectionSupplier supplier;
    private final boolean manageHttpLifecycle;

    private final EnqueuingConsumer consumer;

    private final AtomicBoolean active = new AtomicBoolean(true);
    private final CountDownLatch done = new CountDownLatch(1);

    private final Object streamLock = new Object();
    private volatile StreamConnection streamConnection;

    public static Builder newBuilder() {
        return new Builder();
    }

    private StreamConsumeTask(AsyncHttpClient client,
                              StreamQueryDescriptor streamQueryDescriptor,
                              BlockingQueue<String> targetQueue,
                              Optional<StartPosition> initialPosition,
                              StreamConnectionSupplier supplier,
                              boolean manageHttpLifecycle) {
        this.http = client;
        this.streamQueryDescriptor = streamQueryDescriptor;
        this.initialPosition = initialPosition;
        this.supplier = supplier;
        this.manageHttpLifecycle = manageHttpLifecycle;

        this.consumer = new EnqueuingConsumer(GsonUtil.getGson(), targetQueue);
    }

    /**
     * Begins the process of consuming from the stream by interacting {@link StreamConnection}. The call will block
     * until {@link #stop()} is called.
     */
    @Override
    public void run() {
        try {
            stream();
        }
        finally {
            if (manageHttpLifecycle) {
                http.close();
            }

            done.countDown();
        }
    }

    private void stream() {
        String appKey = streamQueryDescriptor.getCreds().getAppKey();
        while (active.get()) {

            Optional<StartPosition> position = getPosition();
            try (StreamConnection newStreamConnection = supplier.get(streamQueryDescriptor, http, consumer)) {
                transitionToReading(position, newStreamConnection);
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

    private Optional<StartPosition> getPosition() {
        Optional<Long> lastOffset = consumer.get();
        if (lastOffset.isPresent()) {
            return Optional.of(StartPosition.offset(lastOffset.get()));
        }

        return initialPosition;
    }

    private void transitionToReading(Optional<StartPosition> position, StreamConnection newStreamConnection) throws InterruptedException {
        // The streamLock sync is used to ensure consistency between the stop method and the swap of the streamConnection
        // resource in the case of a race. We want to ensure we only active and begin reading from the stream if a
        // stop signal has not been received. Note, it's ok to call StreamConnection.read() even if the StreamConnection
        // has been closed. Therefore, the possible race where we get in the sync block, swap the stream resource and
        // between exiting the sync block and the read call, a stop call occurs and closes the stream resource is ok.
        synchronized (streamLock) {
            if (!active.get()) {
                return;
            }

            streamConnection = newStreamConnection;
        }

        streamConnection.read(position);
    }

    /**
     * Stops the task and causes the {@link #run()} method to exit.
     */
    public void stop() {
        if (!active.compareAndSet(true, false)) {
            log.debug("Ignoring call to stop as initial call has already occurred");
            return;
        }

        log.info("Shutting down stream handler for app " + streamQueryDescriptor.getCreds().getAppKey());

        // The streamLock sync is used to guard the potential race between a call to stop and an iteration inside the
        // stream method. We want to ensure that if the streamConnection resource is setup, we close it. The streamLock
        // is used on swapping that resource and so we know it cannot change and its state is consistent inside the sync.
        synchronized (streamLock) {
            if (streamConnection != null) {
                try {
                    streamConnection.close();
                }
                catch (Exception e) {
                    throw new RuntimeException("Failed to shutdown stream and stop gracefully", e);
                }
            }
        }
    }

    public static final class Builder {

        private StreamConnectionSupplier supplier = new MobileEventStreamConnectionSupplier();

        private StreamQueryDescriptor streamQueryDescriptor = null;
        private Optional<StartPosition> initialPosition = Optional.absent();
        private BlockingQueue<String> targetQueue = null;

        private AsyncHttpClient http = null;

        private Builder() {}

        /**
         * Specify the queue into which received events will be placed upon receipt.
         *
         * @param targetQueue BlockingQueue to put received events into.
         */
        public Builder setTargetQueue(BlockingQueue<String> targetQueue) {
            this.targetQueue = targetQueue;
            return this;
        }

        /**
         * Specify the parameters for the stream request.
         *
         * @param descriptor stream specification
         */
        public Builder setStreamQueryDescriptor(StreamQueryDescriptor descriptor) {
            this.streamQueryDescriptor = descriptor;
            return this;
        }

        /**
         * Optionally specify the starting position for stream consumption.
         *
         * @param position starting position for consume.
         */
        public Builder setStartingPosition(StartPosition position) {
            this.initialPosition = Optional.of(position);
            return this;
        }

        /**
         * Optionally set the http client that will be used for connecting to the API endpoint. If the client is not
         * specified, the default client specified by {@link HttpClientUtil#defaultHttpClient()} will be used.
         *
         * This is exposed to provide the ability to override the HTTP client settings. In most cases, this is not
         * necessary.
         *
         * If a client is provided externally by using this method, the task will NOT close it when the task exits
         * meaning that it is the responsibility of the caller to manage the HTTP client's lifecycle. If no external
         * HTTP client is specified via this method (and thus the library default client is used) the task will handle
         * lifecycle management of the client.
         *
         * @param http the HTTP client to use for transport
         */
        public Builder setHttpClient(AsyncHttpClient http) {
            this.http = http;
            return this;
        }

        @VisibleForTesting
        Builder setStreamConnectionSupplier(StreamConnectionSupplier supplier) {
            this.supplier = supplier;
            return this;
        }

        public StreamConsumeTask build() {
            Preconditions.checkNotNull(streamQueryDescriptor, "Stream query descriptor must be provided");
            Preconditions.checkNotNull(targetQueue, "Target queue must be provided");

            boolean manageHttpLifecycle = false;
            if (http == null) {
                http = HttpClientUtil.defaultHttpClient();
                manageHttpLifecycle = true;
            }

            return new StreamConsumeTask(
                    http,
                    streamQueryDescriptor,
                    targetQueue,
                    initialPosition,
                    supplier,
                    manageHttpLifecycle
            );
        }
    }

    // Default StreamConnectionSupplier implementation
    private static class MobileEventStreamConnectionSupplier implements StreamConnectionSupplier {
        @Override
        public StreamConnection get(StreamQueryDescriptor descriptor,
                                    AsyncHttpClient client,
                                    Consumer<String> eventConsumer) {
            return new StreamConnection(descriptor, client, eventConsumer, Constants.API_URL);
        }
    }

    private final class EnqueuingConsumer implements Consumer<String>, Supplier<Optional<Long>> {

        private final AtomicLong lastOffset = new AtomicLong(-1L);

        private final Gson gson;
        private final BlockingQueue<String> targetQueue;

        public EnqueuingConsumer(Gson gson, BlockingQueue<String> targetQueue) {
            this.gson = gson;
            this.targetQueue = targetQueue;
        }

        @Override
        public void accept(String event) {
            long offset = getOffset(event);

            // Possible that a reconnection reset the stream to our last offset and thus we could get an event we've
            // seen already since the stream starts at the last recorded offset
            if (lastOffset.get() > -1L && lastOffset.get() == offset) {
                return;
            }

            try {
                while (active.get()) {
                    if (targetQueue.offer(event, 1, TimeUnit.SECONDS)) {
                        lastOffset.set(offset);
                        break;
                    }

                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private long getOffset(String event) {
            JsonObject obj = gson.fromJson(event, JsonObject.class);
            return obj.get("offset").getAsLong();
        }

        @Override
        public Optional<Long> get() {
            if (lastOffset.get() == -1L) {
                return Optional.absent();
            }

            return Optional.of(lastOffset.get());
        }
    }
}
