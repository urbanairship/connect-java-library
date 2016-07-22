package com.urbanairship.connect.client;

import com.google.common.base.Optional;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.urbanairship.connect.client.model.StartPosition;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class Stream extends UnmodifiableIterator<String> implements AutoCloseable {

    private static final Logger log = LogManager.getLogger(Stream.class);

    private final AtomicReference<SourceExit> sourceExit = new AtomicReference<>(null);

    private final ExecutorService threads;
    private final BlockingQueue<String> eventQueue;
    private final StreamConsumeTask consumeTask;

    private String next = null;

    public Stream(StreamQueryDescriptor descriptor, Optional<StartPosition> startingPosition) {
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

        consumeTask = builder.build();
        Future<?> handle = threads.submit(consumeTask);
        threads.submit(new SourceWatcher(handle));
    }

    @Override
    public boolean hasNext() {
        if (next != null) {
            return true;
        }

        Optional<String> result = computeNext();
        if (result.isPresent()) {
            next = result.get();
        }

        return next != null;
    }

    @Override
    public String next() {
        if (next == null) {
            throw new NoSuchElementException();
        }

        return next;
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

    private Optional<String> computeNext() {
        String event = null;
        while (event == null) {

            SourceExit exit = this.sourceExit.get();
            if (exit != null) {
                // Source is no longer providing data
                if (exit.error.isPresent()) {
                    throw new RuntimeException("Underlying stream connection failed", exit.error.get());
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

        return Optional.fromNullable(event);
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
