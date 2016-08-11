/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.consume;

import com.google.common.base.Optional;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.urbanairship.connect.java8.Consumer;

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
    public STATE onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
        statusCode = responseStatus.getStatusCode();
        statusMessage = responseStatus.getStatusText();
        return STATE.CONTINUE;
    }

    @Override
    public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
        Map<String, List<String>> copied = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : headers.getHeaders().entrySet()) {
            copied.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        StatusAndHeaders statusAndHeaders = new StatusAndHeaders(statusCode, statusMessage, copied);

        connected = true;
        connectCallback.connected(statusAndHeaders);
        return STATE.CONTINUE;
    }

    public void consumeBody(Consumer<byte[]> receiver) {
        this.receiver = receiver;
        consumeLatch.countDown();
    }

    @Override
    public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
        consumeLatch.await();

        if (!consumePermit.tryAcquire()) {
            return STATE.ABORT;
        }

        try {
            receiver.accept(bodyPart.getBodyPartBytes());
        }
        finally {
            consumePermit.release();
        }

        return stop.get() ? STATE.ABORT : STATE.CONTINUE;
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
