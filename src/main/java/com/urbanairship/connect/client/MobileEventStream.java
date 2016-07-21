/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HttpHeaders;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.cookie.Cookie;
import com.ning.http.client.cookie.CookieDecoder;
import com.urbanairship.connect.client.consume.MobileEventStreamBodyConsumer;
import com.urbanairship.connect.client.consume.MobileEventStreamConnectFuture;
import com.urbanairship.connect.client.consume.MobileEventStreamResponseHandler;
import com.urbanairship.connect.client.consume.StatusAndHeaders;
import com.urbanairship.connect.client.model.GsonUtil;
import com.urbanairship.connect.java8.Consumer;
import sun.net.www.protocol.http.HttpURLConnection;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides the abstraction through which events are stream from the mobile event stream endpoint to a caller.
 *
 * Usage should follow the pattern:
 * <pre>
 *    try (MobileEventStream stream = new MobileEventStream(...)) {
 *        stream.read()
 *    }
 * </pre>
 */
public class MobileEventStream implements AutoCloseable {

    public static final String X_UA_APPKEY = "X-UA-Appkey";
    public static final String ACCEPT_HEADER = "application/vnd.urbanairship+x-ndjson; version=3;";

    private static final Gson GSON = GsonUtil.getGson();

    private final StreamQueryDescriptor descriptor;
    private final AsyncHttpClient client;
    private final Consumer<String> eventConsumer;
    private final String url;

    private final AtomicBoolean gate = new AtomicBoolean(false);

    private volatile Connection connection = null;
    private volatile CountDownLatch bodyConsumeLatch = null;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public MobileEventStream(StreamQueryDescriptor descriptor,
                             AsyncHttpClient client,
                             Consumer<String> eventConsumer,
                             String url) {
        this.descriptor = descriptor;
        this.client = client;
        this.eventConsumer = eventConsumer;
        this.url = url;
    }

    public void read(Optional<String> startingOffset) throws ConnectionException, InterruptedException {
        if (!gate.compareAndSet(false, true)) {
            throw new IllegalStateException("Stream is already consuming!");
        }

        connect(startingOffset);
        consume();
    }

    private void connect(Optional<String> startingOffset) throws InterruptedException {
        try {
            connection = connect(Collections.<Cookie>emptyList(), startingOffset);
        }
        catch (ExecutionException e) {
            throw new RuntimeException("Failure attempting to connect to mobile event stream for app " + getAppKey(), e);
        }
    }

    private void consume() throws InterruptedException {
        bodyConsumeLatch = new CountDownLatch(1);
        connection.consume(bodyConsumeLatch);

        try {
            bodyConsumeLatch.await();

            Optional<Throwable> error = connection.getConsumeError();
            if (error.isPresent()) {
                throw new RuntimeException("Error occurred consuming stream for app " + getAppKey(), error.get());
            }
        }
        finally {
            cleanup();
        }
    }

    private void cleanup() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        if (bodyConsumeLatch != null) {
            bodyConsumeLatch.countDown();
        }

        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public void close() throws Exception {
        cleanup();
    }

    private Connection connect(Collection<Cookie> cookies, Optional<String> startingOffset) throws InterruptedException, ExecutionException {

        AsyncHttpClient.BoundRequestBuilder request = buildRequest(cookies, startingOffset);

        MobileEventStreamConnectFuture connectFuture = new MobileEventStreamConnectFuture();
        Consumer<byte[]> consumer = new MobileEventStreamBodyConsumer(eventConsumer);
        MobileEventStreamResponseHandler responseHandler = new MobileEventStreamResponseHandler(consumer, connectFuture);

        ListenableFuture<Boolean> future = request.execute(responseHandler);

        StatusAndHeaders statusAndHeaders;
        try {
            statusAndHeaders = connectFuture.get();
        }
        catch (InterruptedException | ExecutionException e) {
            responseHandler.stop();
            future.done();
            throw e;
        }

        int status = statusAndHeaders.getStatusCode();
        if (status == HttpURLConnection.HTTP_OK) {
            return new Connection(future, responseHandler);
        }

        // At this point, we know we don't want to consume anything else on the response - even if there was something there
        responseHandler.stop();
        future.done();

        // 400s indicate a bad request
        if (399 < status && status < 500) {
            throw new ConnectionException(String.format("Received status code (%d) from a bad request for app %s", status, getAppKey()));
        }

        if (status != 307) {
            throw new ConnectionException(String.format("Received unexpected status code (%d) from request for stream for app %s", status, getAppKey()));
        }

        return handleRedirect(statusAndHeaders, startingOffset);
    }

    private AsyncHttpClient.BoundRequestBuilder buildRequest(Collection<Cookie> cookies, Optional<String> startingOffset) {
        byte[] query = getQuery(startingOffset);

        AsyncHttpClient.BoundRequestBuilder request = client.preparePost(url)
                .addHeader(HttpHeaders.ACCEPT, ACCEPT_HEADER)
                .addHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(query.length));

        Map<String, String> authHeaders = getAuthHeaders(descriptor.getCreds());
        for (Map.Entry<String, String> entry : authHeaders.entrySet()) {
            request.addHeader(entry.getKey(), entry.getValue());
        }

        for (Cookie cookie : cookies) {
            request.addCookie(cookie);
        }

        request.setBody(query);

        return request;
    }

    private Connection handleRedirect(StatusAndHeaders statusAndHeaders, Optional<String> startingOffset) throws InterruptedException, ExecutionException {

        List<String> values = statusAndHeaders.getHeaders().get("Set-Cookie");
        if (values == null || values.isEmpty()) {
            throw new ConnectionException("Received redirect response with no 'Set-Cookie' header in response!");
        }

        String value = values.get(0);
        Cookie cookie = CookieDecoder.decode(value);

        if (cookie == null) {
            throw new ConnectionException("Received redirect response with unparsable 'Set-Cookie' value - " + value);
        }

        return connect(ImmutableList.of(cookie), startingOffset);
    }

    private Map<String, String> getAuthHeaders(Creds creds) {
        return ImmutableMap.of(
            HttpHeaders.AUTHORIZATION, "Bearer " + creds.getToken(),
            X_UA_APPKEY, creds.getAppKey()
        );
    }

    private byte[] getQuery(Optional<String> startOffset) {
        Map<String, Object> body = new HashMap<>();

        if (!startOffset.isPresent()) {
            body.put("start", "LATEST");
        }
        else if ("EARLIEST".equals(startOffset.get()) || "LATEST".equals(startOffset.get())) {
            body.put("start", startOffset.get());
        }
        else {
            body.put("resume_offset", startOffset.get());
        }

        if (descriptor.getSubset().isPresent()) {
            body.put("subset", descriptor.getSubset().get());
        }

        if (descriptor.getFilters().isPresent()) {
            body.put("filters", descriptor.getFilters().get());
        }

        String json = GSON.toJson(body);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    private String getAppKey() {
        return descriptor.getCreds().getAppKey();
    }

    private static final class Connection {

        private final ListenableFuture<Boolean> future;
        private final MobileEventStreamResponseHandler handler;

        private Connection(ListenableFuture<Boolean> future, MobileEventStreamResponseHandler handler) {
            this.future = future;
            this.handler = handler;
        }

        public void consume(final CountDownLatch doneLatch) {
            Runnable doneLatchCountDownRunnable = new Runnable() {
                @Override
                public void run() {
                    doneLatch.countDown();
                }
            };

            future.addListener(doneLatchCountDownRunnable, MoreExecutors.directExecutor());
            handler.consumeBody();
        }

        public Optional<Throwable> getConsumeError() {
            return handler.getError();
        }

        public void close() {
            try {
                handler.stop();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            future.done();
        }
    }
}
