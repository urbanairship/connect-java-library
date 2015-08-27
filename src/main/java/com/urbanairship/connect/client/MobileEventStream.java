package com.urbanairship.connect.client;

import com.google.common.base.Preconditions;
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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sun.net.www.protocol.http.HttpURLConnection;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Provides the abstraction through which events are stream from the mobile event stream endpoint to a caller.
 *
 * Usage should follow the pattern:
 * <pre>
 *    try (MobileEventStream stream = new MobileEventStream(...)) {
 *        stream.connect(...);
 *        stream.consume(...);
 *    }
 * </pre>
 */
public class MobileEventStream implements AutoCloseable {

    private static final Logger log = LogManager.getLogger(MobileEventStream.class);

    public static final String X_UA_APPKEY = "X-UA-Appkey";
    private static final String ACCEPT_HEADER = "application/vnd.urbanairship+x-ndjson; version=3;";

    private static final Gson GSON = GsonUtil.getGson();

    private final StreamQueryDescriptor descriptor;
    private final AsyncHttpClient client;
    private final Consumer<String> eventConsumer;
    private final String url;
    private final FatalExceptionHandler fatalExceptionHandler;

    private final Object stateLock = new Object();

    private volatile Connection connection = null;
    private volatile CountDownLatch bodyConsumeLatch = null;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public MobileEventStream(StreamQueryDescriptor descriptor,
                             AsyncHttpClient client,
                             Consumer<String> eventConsumer,
                             String url,
                             FatalExceptionHandler fatalExceptionHandler) {
        this.descriptor = descriptor;
        this.client = client;
        this.eventConsumer = eventConsumer;
        this.url = url;
        this.fatalExceptionHandler = fatalExceptionHandler;
    }

    public void connect(long maxConnectWaitTime, TimeUnit unit) throws InterruptedException {
        synchronized (stateLock) {
            Preconditions.checkState(connection == null);

            try {
                connection = connect(maxConnectWaitTime, unit, Collections.<Cookie>emptyList());
            }
            catch (ExecutionException e) {
                throw new RuntimeException("Failure attempting to connect to mobile event stream for app " + getAppKey(), e);
            }
            catch (TimeoutException e) {
                throw new RuntimeException("Timed out waiting to establish connection to mobile event stream for app " + getAppKey());
            }
        }
    }

    public void consume(long maxConsumeTime, TimeUnit unit) throws InterruptedException {
        synchronized (stateLock) {
            Preconditions.checkState(connection != null && !closed.get());

            bodyConsumeLatch = new CountDownLatch(1);
            connection.future.addListener(bodyConsumeLatch::countDown, MoreExecutors.directExecutor());
        }

        try {
            connection.handler.consumeBody();
            if (!bodyConsumeLatch.await(maxConsumeTime, unit)) {
                log.debug("Hit max consume time for stream for app " + getAppKey());
            }

            Optional<Throwable> error = connection.handler.getError();
            if (error.isPresent()) {
                throw new RuntimeException("Error occurred consuming stream for app " + getAppKey(), error.get());
            }
        }
        finally {
            cleanup();
        }
    }

    private void cleanup() {
        synchronized (stateLock) {
            if (!closed.compareAndSet(false, true)) {
                return;
            }

            if (bodyConsumeLatch != null) {
                bodyConsumeLatch.countDown();
            }

            if (connection != null) {
                try {
                    connection.handler.stop();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                connection.future.done();
            }
        }
    }

    @Override
    public void close() throws Exception {
        cleanup();
    }

    private Connection connect(long maxConnectTime, TimeUnit unit, Collection<Cookie> cookies)
            throws InterruptedException, ExecutionException, TimeoutException {

        AsyncHttpClient.BoundRequestBuilder request = buildRequest(cookies);

        MobileEventStreamConnectFuture connectFuture = new MobileEventStreamConnectFuture();
        Consumer<byte[]> consumer = new MobileEventStreamBodyConsumer(eventConsumer);
        MobileEventStreamResponseHandler responseHandler = new MobileEventStreamResponseHandler(consumer, connectFuture);

        ListenableFuture<Boolean> future = request.execute(responseHandler);

        StatusAndHeaders statusAndHeaders = connectFuture.get(maxConnectTime, unit);

        int status = statusAndHeaders.getStatusCode();
        if (status == HttpURLConnection.HTTP_OK) {
            return new Connection(future, responseHandler);
        }

        // At this point, we know we don't want to consume anything else on the response - even if there was something there
        responseHandler.stop();
        future.done();

        // 400s indicate a bad request, don't want to cause unnecessary connection retries in the MobileEventConsumerService
        if (399 < status || status < 500) {
            fatalExceptionHandler.handle(new RuntimeException(String.format("Received status code (%d) from a bad request for app %s", status, getAppKey())));
        }

        if (status != 307) {
            throw new RuntimeException(String.format("Received unexpected status code (%d) from request for stream for app %s", status, getAppKey()));
        }

        // TODO: should probably handle possibility of infinite recursion with this path...
        return handleRedirect(maxConnectTime, unit, statusAndHeaders);
    }

    private AsyncHttpClient.BoundRequestBuilder buildRequest(Collection<Cookie> cookies) {
        byte[] query = getQuery();

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

    private Connection handleRedirect(long maxConnectTime, TimeUnit unit, StatusAndHeaders statusAndHeaders)
            throws InterruptedException, ExecutionException, TimeoutException {

        List<String> values = statusAndHeaders.getHeaders().get("Set-Cookie");
        if (values == null || values.isEmpty()) {
            throw new RuntimeException("Received redirect response with no 'Set-Cookie' header in response!");
        }

        String value = values.get(0);
        Cookie cookie = CookieDecoder.decode(value);

        if (cookie == null) {
            throw new RuntimeException("Received redirect response with unparsable 'Set-Cookie' value - " + value);
        }

        return connect(maxConnectTime, unit, ImmutableList.of(cookie));
    }

    private Map<String, String> getAuthHeaders(Creds creds) {
        return ImmutableMap.of(
            HttpHeaders.AUTHORIZATION, "Bearer " + creds.getToken(),
            X_UA_APPKEY, creds.getAppKey()
        );
    }

    private byte[] getQuery() {
        Map<String, Object> body = new HashMap<>();

        if (descriptor.getOffset().isPresent()) {
            body.put("resume_offset", descriptor.getOffset().get());
        }
        else {
            body.put("start", "LATEST");
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

        public Connection(ListenableFuture<Boolean> future, MobileEventStreamResponseHandler handler) {
            this.future = future;
            this.handler = handler;
        }
    }
}
