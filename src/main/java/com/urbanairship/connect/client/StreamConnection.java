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
import com.urbanairship.connect.client.consume.ConnectionRetryStrategy;
import com.urbanairship.connect.client.consume.FullBodyConsumer;
import com.urbanairship.connect.client.consume.MobileEventStreamBodyConsumer;
import com.urbanairship.connect.client.consume.MobileEventStreamConnectFuture;
import com.urbanairship.connect.client.consume.MobileEventStreamResponseHandler;
import com.urbanairship.connect.client.consume.StatusAndHeaders;
import com.urbanairship.connect.client.model.Creds;
import com.urbanairship.connect.client.model.GsonUtil;
import com.urbanairship.connect.client.model.StreamQueryDescriptor;
import com.urbanairship.connect.client.model.request.StartPosition;
import com.urbanairship.connect.client.model.request.StreamRequestPayload;
import com.urbanairship.connect.java8.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.www.protocol.http.HttpURLConnection;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides the abstraction through which events are streamed from the Urban Airship Connect endpoint to a caller.
 *
 * Usage should follow the pattern:
 * <pre>
 *    try (StreamConnection conn = new StreamConnection(...)) {
 *        conn.read(...)
 *    }
 * </pre>
 *
 * Proper use of this class means that only a single thread will call {@link #read(Optional)} and a call will only be
 * made once. A call to {@link #close()} can be made by any other thread and at any time and resources will be appropriately
 * cleaned up and cause any call to {@link #read(Optional)} to exit.
 */
public class StreamConnection implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(StreamConnection.class);

    public static final String X_UA_APPKEY = "X-UA-Appkey";
    public static final String ACCEPT_HEADER = "application/vnd.urbanairship+x-ndjson; version=3;";

    private static final Gson GSON = GsonUtil.getGson();

    private final StreamQueryDescriptor descriptor;
    private final AsyncHttpClient client;
    private final ConnectionRetryStrategy connectionRetryStrategy;
    private final Consumer<String> eventConsumer;
    private final String url;

    private final AtomicBoolean gate = new AtomicBoolean(false);

    private volatile Connection connection = null;
    private volatile CountDownLatch bodyConsumeLatch = null;

    private volatile boolean closed = false;

    private final Object transitionLock = new Object();

    public StreamConnection(StreamQueryDescriptor descriptor,
                            AsyncHttpClient client,
                            ConnectionRetryStrategy connectionRetryStrategy,
                            Consumer<String> eventConsumer,
                            String url) {
        this.descriptor = descriptor;
        this.client = client;
        this.connectionRetryStrategy = connectionRetryStrategy;
        this.eventConsumer = eventConsumer;
        this.url = url;
    }

    /**
     * Opens up a connection to Urban Airship Connect and begins consuming data and passing it to the configured consumer
     * starting at the position specified by the startPosition parameter.
     *
     * @param startPosition optionally specifies the starting position to consume from.
     *
     * @throws ConnectionException thrown if a connection cannot be successfully made and indicates a problem with either
     * the request or unexpected behavior from the API.
     * @throws InterruptedException this method is blocking and this will be thrown if the underlying blocking calls are
     * interrupted.
     */
    public void read(Optional<StartPosition> startPosition) throws ConnectionException, InterruptedException {
        if (!gate.compareAndSet(false, true)) {
            throw new IllegalStateException("Stream is already consuming!");
        }

        boolean connected = false;
        boolean retry;
        int attempt = 0;
        Optional<? extends Exception> failure = Optional.absent();
        do {
            attempt++;

            // The sync is shared with the cleanup method and ensures we don't miss a "close" signal and potentially setup
            // resources after the close and thus don't have those resources cleaned up in the case of a race between a call
            // to cleanup and this method.
            synchronized (transitionLock) {
                if (closed) {
                    break;
                }
                failure = begin(startPosition, attempt);
                connected = !failure.isPresent();
            }

            retry = false;
            if (!connected && !closed) {
                retry = connectionRetryStrategy.shouldRetry(attempt);
                if (retry) {
                    Thread.sleep(connectionRetryStrategy.getPauseMillis(attempt));
                }
            }
        } while (!connected && retry);

        if (!connected && !closed) {
            Exception lastFailure = failure.get();
            String message = String.format("Failed to establish connection to event stream after %d attempts", attempt);

            if (lastFailure instanceof ConnectionException) {
                ConnectionException lastConnectionException = (ConnectionException) lastFailure;
                throw new ConnectionException(message, lastConnectionException.getErrorCode(), lastConnectionException);
            }

            throw new RuntimeException(message, lastFailure);
        }

        if (connected) {
            consume();
        }
    }

    private Optional<? extends Exception> begin(Optional<StartPosition> startPosition, int attempt) throws InterruptedException {
        try {
            connection = connect(Collections.<Cookie>emptyList(), startPosition);
        }
        catch (InterruptedException e) {
            throw e;
        }
        catch (ConnectionException e) {
            return Optional.of(e);
        }
        catch (Exception e) {
            return Optional.of(e);
        }

        bodyConsumeLatch = new CountDownLatch(1);
        connection.consume(bodyConsumeLatch, eventConsumer);

        return Optional.absent();
    }

    private void consume() throws InterruptedException {
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
        // The sync ensures consistency in read of "closed" between the cleanup a potential call to read (the method).
        // We need to ensure that in a race between a call to close and a call to read, it's not possible for the close
        // to miss the setup of the latch and connection.
        synchronized (transitionLock) {
            if (closed) {
                return;
            }

            closed = true;
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

    private Connection connect(Collection<Cookie> cookies, Optional<StartPosition> startPosition) throws InterruptedException, ExecutionException, ConnectionException {

        AsyncHttpClient.BoundRequestBuilder request = buildRequest(cookies, startPosition);

        MobileEventStreamConnectFuture connectFuture = new MobileEventStreamConnectFuture();
        MobileEventStreamResponseHandler responseHandler = new MobileEventStreamResponseHandler(connectFuture);

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

        if (status != 307) {
            throw buildErrorException(responseHandler, future, status);
        }

        return handleRedirect(statusAndHeaders, startPosition);
    }

    private ConnectionException buildErrorException(MobileEventStreamResponseHandler responseHandler,
                                                 ListenableFuture<Boolean> future,
                                                 int status)
            throws InterruptedException, ExecutionException {

        // The response indicates an error of some sort. Read the response body so we can detail any information
        // about the error from the server out through an exception message
        FullBodyConsumer bodyReader = new FullBodyConsumer();
        try {
            responseHandler.consumeBody(bodyReader);
            future.get();
        }
        finally {
            responseHandler.stop();
            future.done();
        }

        String body = bodyReader.get();

        // 400s indicate a bad request
        if (399 < status && status < 500) {
            return new ConnectionException(String.format("Received status code (%d) from a bad request for app %s. Response body: %s", status, getAppKey(), body), status);
        }

        return new ConnectionException(String.format("Received unexpected status code (%d) from request for stream for app %s. Response body: %s", status, getAppKey(), body), status);
    }

    private AsyncHttpClient.BoundRequestBuilder buildRequest(Collection<Cookie> cookies, Optional<StartPosition> startPosition) {
        byte[] query = getQuery(startPosition);

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

    private Connection handleRedirect(StatusAndHeaders statusAndHeaders, Optional<StartPosition> startPosition) throws InterruptedException, ExecutionException, ConnectionException {

        List<String> values = statusAndHeaders.getHeaders().get("Set-Cookie");
        if (values == null || values.isEmpty()) {
            throw new ConnectionException("Received redirect response with no 'Set-Cookie' header in response!", statusAndHeaders.getStatusCode());
        }

        String value = values.get(0);
        Cookie cookie = CookieDecoder.decode(value);

        if (cookie == null) {
            throw new ConnectionException("Received redirect response with unparsable 'Set-Cookie' value - " + value, statusAndHeaders.getStatusCode());
        }

        try {
            return connect(ImmutableList.of(cookie), startPosition);
        } catch (Exception e) {
            throw e;
        }
    }

    private Map<String, String> getAuthHeaders(Creds creds) {
        return ImmutableMap.of(
            HttpHeaders.AUTHORIZATION, "Bearer " + creds.getToken(),
            X_UA_APPKEY, creds.getAppKey()
        );
    }

    private byte[] getQuery(Optional<StartPosition> position) {
        StreamRequestPayload payload =
                new StreamRequestPayload(descriptor.getFilters(), descriptor.getSubset(), position, descriptor.offsetUpdatesEnabled());

        String json = GSON.toJson(payload);

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

        public void consume(final CountDownLatch doneLatch, Consumer<String> eventConsumer) {
            Runnable doneLatchCountDownRunnable = new Runnable() {
                @Override
                public void run() {
                    doneLatch.countDown();
                }
            };

            future.addListener(doneLatchCountDownRunnable, MoreExecutors.directExecutor());

            MobileEventStreamBodyConsumer bodyConsumer = new MobileEventStreamBodyConsumer(eventConsumer);
            handler.consumeBody(bodyConsumer);
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
