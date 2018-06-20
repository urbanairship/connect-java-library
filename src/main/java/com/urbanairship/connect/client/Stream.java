package com.urbanairship.connect.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.urbanairship.connect.client.model.StreamQueryDescriptor;
import com.urbanairship.connect.client.model.request.StartPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Wraps up the the low level classes that interact with the Urban Airship Connect API and exposes the data received
 * from the API as an {@link java.util.Iterator} that is open ended and behaves conceptually as a "stream" of events.
 *
 * Note this class is {@link AutoCloseable} and so proper use should look something like:
 * <pre>
 *     try (Stream stream = new Stream(...)) {
 *         while (stream.hasNext()) {
 *              String event = stream.next();
 *         }
 *     }
 * </pre>
 */
public final class Stream extends AbstractIterator<String> implements ConnectStreamApi {

    private static final Logger log = LoggerFactory.getLogger(Stream.class);

    private final AtomicReference<SourceExit> sourceExit = new AtomicReference<>(null);

    private final ExecutorService threads;
    private final BlockingQueue<String> eventQueue;
    private final StreamConsumeTask consumeTask;

    public Stream(StreamQueryDescriptor descriptor, Optional<StartPosition> startingPosition) {
        this(descriptor, startingPosition, Optional.<StreamConnectionSupplier>absent());
        log.debug("Starting Stream. Filters: " + descriptor.getFilters()
                + " Subset: " + descriptor.getSubset()
                + " Starting Position " + startingPosition);
    }

    @VisibleForTesting
    public Stream(StreamQueryDescriptor descriptor,
                  Optional<StartPosition> startingPosition,
                  Optional<StreamConnectionSupplier> connSupplier) {
        // TODO: size limit configured?
        eventQueue = new LinkedBlockingQueue<>(100);
        threads = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder()
                .setDaemon(false)
                .setNameFormat("Stream iteration thread %d")
                .build());

        StreamConsumeTask.Builder builder = StreamConsumeTask.newBuilder()
                .setTargetQueue(eventQueue)
                .setStreamQueryDescriptor(descriptor);

        if (startingPosition.isPresent()) {
            builder.setStartingPosition(startingPosition.get());
        }

        if (connSupplier.isPresent()) {
            builder.setStreamConnectionSupplier(connSupplier.get());
        }

        consumeTask = builder.build();
        Future<?> handle = threads.submit(consumeTask);
        threads.submit(new SourceWatcher(handle));
    }

    @Override
    public void close() throws Exception {
        try {
            consumeTask.stop();
        }
        finally {
            threads.shutdown();
        }
    }

    @Override
    public String computeNext() {
        String event = null;
        while (event == null) {

            SourceExit exit = this.sourceExit.get();
            if (exit != null) {
                // Source is no longer providing data
                if (exit.error.isPresent()) {
                    throw Throwables.propagate(exit.error.get());
                }

                break;
            }

            try {
                event = eventQueue.poll(1, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return event == null ? endOfData() : event;
    }

    private static final class SourceExit {

        private final Optional<Throwable> error;

        public SourceExit(Optional<Throwable> error) {
            this.error = error;
        }
    }

    private final class SourceWatcher implements Runnable {
        private final Future<?> handle;

        public SourceWatcher(Future<?> handle) {
            this.handle = handle;
        }

        @Override
        public void run() {
            Throwable error = null;
            try {
                handle.get();
            }
            catch (InterruptedException e) {
                log.info("Source watcher interrupted!", e);
                Thread.currentThread().interrupt();
            }
            catch (ExecutionException e) {
                error = e.getCause();
            }
            finally {
                sourceExit.set(new SourceExit(Optional.fromNullable(error)));
            }
        }
    }
}
