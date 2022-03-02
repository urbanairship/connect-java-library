/*
Copyright 2015-2022 Airship and Contributors
*/

package com.urbanairship.connect.client.consume;

import com.google.common.base.Optional;
import com.urbanairship.connect.java8.Consumer;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides the mechanism through which communication happens during request/response lifecycle of consuming a stream
 * of events.
 *
 * Intended usage requires that once the caller has received the call to {@link ConnectCallback#connected(StatusAndHeaders)} the
 * caller should call {@link #consumeBody(Consumer)}. The handler will *not* read any of the response body until
 * {@link #consumeBody(Consumer)} is called.  This is done so that the caller can deal with any special handling that may be
 * needed based on response status code and/or headers before receiving the streamed body.
 */
public final class MobileEventStreamResponseHandler implements AsyncHandler<Boolean> {

    private final AtomicBoolean stop = new AtomicBoolean(false);



    private final CountDownLatch consumeLatch = new CountDownLatch(1);
    private final Semaphore consumePermit = new Semaphore(1);

    private final ConnectCallback connectCallback;

    private final AtomicReference<Throwable> error = new AtomicReference<>(null);

    private volatile boolean connected = false;
    private volatile int statusCode;
    private volatile String statusMessage;
    private volatile Consumer<byte[]> receiver = null;

    public MobileEventStreamResponseHandler(ConnectCallback connectCallback) {
        this.connectCallback = connectCallback;
    }

    @Override
    public void onThrowable(Throwable t) {
        if (!connected) {
            connectCallback.error(t);
        }

        error.set(t);
        try {
            stop();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted stopping handler on error receipt!", e);
        }
    }

    @Override
    public State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
        statusCode = responseStatus.getStatusCode();
        statusMessage = responseStatus.getStatusText();
        return State.CONTINUE;
    }

    @Override
    public State onHeadersReceived(HttpHeaders headers) throws Exception {
        Map<String, String> copied = new HashMap<>();

        for (Map.Entry<String, String> entry : headers.entries()) {
            copied.put(entry.getKey(), entry.getValue());
        }

        StatusAndHeaders statusAndHeaders = new StatusAndHeaders(statusCode, statusMessage, copied);

        connected = true;
        connectCallback.connected(statusAndHeaders);
        return State.CONTINUE;
    }

    public void consumeBody(Consumer<byte[]> receiver) {
        this.receiver = receiver;
        consumeLatch.countDown();
    }

    @Override
    public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
        consumeLatch.await();

        if (!consumePermit.tryAcquire()) {
            return State.ABORT;
        }

        try {
            receiver.accept(bodyPart.getBodyPartBytes());
        }
        finally {
            consumePermit.release();
        }

        return stop.get() ? State.ABORT : State.CONTINUE;
    }

    @Override
    public Boolean onCompleted() throws Exception {
        return Boolean.TRUE;
    }

    public Optional<Throwable> getError() {
        return Optional.fromNullable(error.get());
    }

    public void stop() throws InterruptedException {
        if (stop.compareAndSet(false, true)) {
            consumePermit.acquire();

            // Trip the consume latch in case consumeBody was never called since we may have received a body part
            // asynchronously, in which case the onBodyPartReceived call will be stuck waiting on the consumeLatch
            consumeLatch.countDown();
        }
    }
}
