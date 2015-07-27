package com.urbanairship.connect.client;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HttpHeaders;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.urbanairship.connect.client.consume.StatusAndHeaders;
import com.urbanairship.connect.client.consume.MobileEventStreamBodyConsumer;
import com.urbanairship.connect.client.consume.MobileEventStreamConnectFuture;
import com.urbanairship.connect.client.consume.MobileEventStreamResponseHandler;
import com.urbanairship.connect.client.filters.DeviceFilter;
import com.urbanairship.connect.client.filters.DeviceFilterSerializer;
import com.urbanairship.connect.client.filters.OptionalSerializer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sun.net.www.protocol.http.HttpURLConnection;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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

    private static final String ACCEPT_HEADER = "application/vnd.urbanairship+x-ndjson; version=3;";

    private static final Gson GSON = new Gson();

    private final StreamDescriptor descriptor;
    private final AsyncHttpClient client;
    private final Consumer<String> eventConsumer;
    private final String url;

    private final Object stateLock = new Object();

    private volatile Connection connection = null;
    private volatile CountDownLatch bodyConsumeLatch = null;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public MobileEventStream(StreamDescriptor descriptor,
                             AsyncHttpClient client,
                             Consumer<String> eventConsumer,
                             String url) {
        this.descriptor = descriptor;
        this.client = client;
        this.eventConsumer = eventConsumer;
        this.url = url;
    }

    public void connect(long maxConnectWaitTime, TimeUnit unit) throws InterruptedException {
        synchronized (stateLock) {
            Preconditions.checkState(connection == null);

            try {
                connection = connect(maxConnectWaitTime, unit, Collections.emptyMap());
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

    private Connection connect(long maxConnectTime, TimeUnit unit, Map<String, String> extraHeaders)
            throws InterruptedException, ExecutionException, TimeoutException {

        AsyncHttpClient.BoundRequestBuilder request = buildRequest(extraHeaders);

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

        if (status != 307) {
            throw new RuntimeException(String.format("Received unexpected status code (%d) from request for stream for app %s", status, getAppKey()));
        }

        // TODO: should probably handle possibility of infinite recursion with this path...
        return handleRedirect(maxConnectTime, unit, statusAndHeaders);
    }

    private AsyncHttpClient.BoundRequestBuilder buildRequest(Map<String, String> extraHeaders) {
        byte[] query = getQuery();

        AsyncHttpClient.BoundRequestBuilder request = client.preparePost(url)
                .addHeader(HttpHeaders.ACCEPT, ACCEPT_HEADER)
                .addHeader(HttpHeaders.AUTHORIZATION, getAuth())
                .addHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(query.length));

        for (Map.Entry<String, String> entry : extraHeaders.entrySet()) {
            request.addHeader(entry.getKey(), entry.getValue());
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

        String streamLeaderHost = values.get(0);
        return connect(maxConnectTime, unit, ImmutableMap.of("Cookie", streamLeaderHost));
    }

    private String getAuth() {
        String auth = descriptor.getCreds().getAppKey() + ":" + descriptor.getCreds().getSecret();
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] getQuery() {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(DeviceFilter.class, new DeviceFilterSerializer())
            .registerTypeAdapter(Optional.class, new OptionalSerializer())
            .create();

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

        String json = gson.toJson(body);
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
