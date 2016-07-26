/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client;

import com.google.common.base.Optional;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.urbanairship.connect.client.model.DeviceFilterType;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;
import com.urbanairship.connect.client.model.StartPosition;
import com.urbanairship.connect.client.model.Subset;
import com.urbanairship.connect.client.model.filters.DeviceFilter;
import com.urbanairship.connect.client.model.filters.Filter;
import com.urbanairship.connect.client.model.filters.NotificationFilter;
import com.urbanairship.connect.java8.Consumer;
import org.apache.commons.lang3.RandomUtils;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class StreamConnectionTest {

    private static final int PORT = 10000 + RandomUtils.nextInt(0, 50000);
    private static final String PATH = "/test";

    private static final JsonParser parser = new JsonParser();

    private static HttpServer server;
    private static HttpHandler serverHandler;
    private static String url;

    private AsyncHttpClient http;

    @Mock private Consumer<String> consumer;

    private StreamConnection stream;

    @BeforeClass
    public static void serverStart() throws Exception {
        String serverUrl = String.format("http://localhost:%d%s", PORT, PATH);
        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);

        serverHandler = mock(HttpHandler.class);
        server.createContext(PATH, serverHandler);

        server.start();

        url = serverUrl;
    }

    @AfterClass
    public static void serverStop() throws Exception {
        server.stop(0);
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.reset(serverHandler);
        stream = null;

        AsyncHttpClientConfig clientConfig = new AsyncHttpClientConfig.Builder()
                .setUserAgent("Connect Client")
                .setRequestTimeout(-1)
                .setAllowPoolingConnections(false)
                .setAllowPoolingSslConnections(false)
                .build();

        http = new AsyncHttpClient(clientConfig);
    }

    @After
    public void tearDown() throws Exception {
        if (stream != null) stream.close();
        http.close();
    }

    @Test
    public void testStream() throws Exception {
        StreamQueryDescriptor descriptor = descriptor();

        final String line = randomAlphabetic(15);

        final AtomicReference<String> body = new AtomicReference<>();
        Answer httpAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];

                int length = Integer.parseInt(exchange.getRequestHeaders().getFirst(HttpHeaders.CONTENT_LENGTH));
                byte[] bytes = new byte[length];
                exchange.getRequestBody().read(bytes);
                body.set(new String(bytes, UTF_8));

                exchange.sendResponseHeaders(200, 0L);

                exchange.getResponseBody().write(line.getBytes(UTF_8));
                exchange.getResponseBody().write("\n".getBytes(UTF_8));
                exchange.close();

                return null;
            }
        };

        doAnswer(httpAnswer).when(serverHandler).handle(Matchers.<HttpExchange>any());

        final List<String> received = new ArrayList<>();
        Answer consumerAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String l = (String) invocation.getArguments()[0];
                received.add(l);
                return null;
            }
        };

        doAnswer(consumerAnswer).when(consumer).accept(anyString());

        stream = new StreamConnection(descriptor, http, consumer, url);
        read(stream, Optional.<StartPosition>absent(), 10);

        assertEquals(1, received.size());
        assertEquals(line, received.get(0));

        JsonObject bodyObj = parser.parse(body.get()).getAsJsonObject();
        assertFalse(bodyObj.has("start"));
        assertFalse(bodyObj.has("resume_offset"));
    }

    @Test
    public void testAuth() throws Exception {
        final AtomicReference<String> authorization = new AtomicReference<>();
        final AtomicReference<String> appKeyHeader = new AtomicReference<>();
        Answer httpAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];
                authorization.set(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
                appKeyHeader.set(exchange.getRequestHeaders().getFirst("X-UA-Appkey"));
                exchange.sendResponseHeaders(200, 0L);

                return null;
            }
        };

        doAnswer(httpAnswer).when(serverHandler).handle(Matchers.<HttpExchange>any());

        StreamQueryDescriptor descriptor = descriptor();
        stream = new StreamConnection(descriptor, http, consumer, url);
        read(stream, Optional.<StartPosition>absent(), 10);

        assertTrue(authorization.get().toLowerCase().startsWith("bearer"));
        String token = authorization.get().substring("bearer ".length());

        assertEquals(descriptor.getCreds().getAppKey(), appKeyHeader.get());
        assertEquals(descriptor.getCreds().getToken(), token);
    }

    @Test
    public void testRequestBodyWithOffset() throws Exception {
        final AtomicReference<String> body = new AtomicReference<>();
        Answer httpAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];

                int length = Integer.parseInt(exchange.getRequestHeaders().getFirst(HttpHeaders.CONTENT_LENGTH));
                byte[] bytes = new byte[length];
                exchange.getRequestBody().read(bytes);
                body.set(new String(bytes, UTF_8));

                exchange.sendResponseHeaders(200, 0L);
                return null;
            }
        };

        doAnswer(httpAnswer).when(serverHandler).handle(Matchers.<HttpExchange>any());

        long offset = RandomUtils.nextInt(0, 100000);
        StreamQueryDescriptor descriptor = descriptor();

        stream = new StreamConnection(descriptor, http, consumer, url);
        read(stream, Optional.of(StartPosition.offset(offset)), 10);

        JsonObject bodyObj = parser.parse(body.get()).getAsJsonObject();
        assertEquals(offset, bodyObj.get("resume_offset").getAsLong());
    }

    @Test
    public void testRequestWithRelativeOffsetLatest() throws Exception {
        final AtomicReference<String> body = new AtomicReference<>();
        Answer httpAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];

                int length = Integer.parseInt(exchange.getRequestHeaders().getFirst(HttpHeaders.CONTENT_LENGTH));
                byte[] bytes = new byte[length];
                exchange.getRequestBody().read(bytes);
                body.set(new String(bytes, UTF_8));

                exchange.sendResponseHeaders(200, 0L);
                return null;
            }
        };

        doAnswer(httpAnswer).when(serverHandler).handle(Matchers.<HttpExchange>any());

        StreamQueryDescriptor descriptor = descriptor();

        stream = new StreamConnection(descriptor, http, consumer, url);
        read(stream, Optional.of(StartPosition.relative(StartPosition.RelativePosition.LATEST)), 10);

        JsonObject bodyObj = parser.parse(body.get()).getAsJsonObject();
        assertEquals("LATEST", bodyObj.get("start").getAsString());
    }

    @Test
    public void testRequestWithRelativeOffsetEarliest() throws Exception {
        final AtomicReference<String> body = new AtomicReference<>();
        Answer httpAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];

                int length = Integer.parseInt(exchange.getRequestHeaders().getFirst(HttpHeaders.CONTENT_LENGTH));
                byte[] bytes = new byte[length];
                exchange.getRequestBody().read(bytes);
                body.set(new String(bytes, UTF_8));

                exchange.sendResponseHeaders(200, 0L);
                return null;
            }
        };

        doAnswer(httpAnswer).when(serverHandler).handle(Matchers.<HttpExchange>any());

        StreamQueryDescriptor descriptor = descriptor();

        stream = new StreamConnection(descriptor, http, consumer, url);
        read(stream, Optional.of(StartPosition.relative(StartPosition.RelativePosition.EARLIEST)), 10);

        JsonObject bodyObj = parser.parse(body.get()).getAsJsonObject();
        assertEquals("EARLIEST", bodyObj.get("start").getAsString());
    }

    @Test
    public void testRequestBodyWithFilter() throws Exception {
        final AtomicReference<String> body = new AtomicReference<>();
        Answer httpAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];

                int length = Integer.parseInt(exchange.getRequestHeaders().getFirst(HttpHeaders.CONTENT_LENGTH));
                byte[] bytes = new byte[length];
                exchange.getRequestBody().read(bytes);
                body.set(new String(bytes, UTF_8));

                exchange.sendResponseHeaders(200, 0L);

                return null;
            }
        };

        doAnswer(httpAnswer).when(serverHandler).handle(Matchers.<HttpExchange>any());

        DeviceFilter device1 = new DeviceFilter(DeviceFilterType.ANDROID, "c8044c8a-d5fa-4e58-91d4-54d0f70b7409");
        DeviceFilter device2 = new DeviceFilter(DeviceFilterType.IOS, "3d970087-600e-4bb6-8474-5857d438faaa");
        DeviceFilter device3 = new DeviceFilter(DeviceFilterType.NAMED_USER, "cool user");
        NotificationFilter notification = NotificationFilter.createGroupIdFilter("a30abf06-7878-4096-9535-b50ac0ad6e8e");

        Filter filter1 = Filter.newBuilder()
            .setLatency(20000000)
            .addDevices(device1, device2, device3)
            .addDeviceTypes(DeviceFilterType.ANDROID, DeviceFilterType.AMAZON)
            .addNotification(notification)
            .addType(EventType.OPEN)
            .build();

        Filter filter2 = Filter.newBuilder()
            .setLatency(400)
            .addDeviceTypes(DeviceFilterType.IOS)
            .addType(EventType.TAG_CHANGE)
            .build();

        StreamQueryDescriptor descriptor = filterDescriptor(filter1, filter2);

        stream = new StreamConnection(descriptor, http, consumer, url);
        read(stream, Optional.<StartPosition>absent(), 10);

        Gson gson = GsonUtil.getGson();

        JsonObject bodyObj = parser.parse(body.get()).getAsJsonObject();
        assertEquals(gson.toJson(new HashSet<>(Arrays.asList(filter1, filter2))), gson.toJson(bodyObj.get("filters")));
    }

    @Test
    public void testRequestBodyWithSubset() throws Exception {
        final AtomicReference<String> body = new AtomicReference<>();
        Answer httpAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];

                int length = Integer.parseInt(exchange.getRequestHeaders().getFirst(HttpHeaders.CONTENT_LENGTH));
                byte[] bytes = new byte[length];
                exchange.getRequestBody().read(bytes);
                body.set(new String(bytes, UTF_8));

                exchange.sendResponseHeaders(200, 0L);

                return null;
            }
        };
        doAnswer(httpAnswer).when(serverHandler).handle(Matchers.<HttpExchange>any());

        Subset subset = Subset.createPartitionSubset(10, 0);
        StreamQueryDescriptor descriptor = subsetDescriptor(subset);

        stream = new StreamConnection(descriptor, http, consumer, url);
        read(stream, Optional.<StartPosition>absent(), 10);

        Gson gson = GsonUtil.getGson();

        JsonObject bodyObj = parser.parse(body.get()).getAsJsonObject();
        assertEquals(gson.toJson(subset), gson.toJson(bodyObj.get("subset")));
    }

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testConnectionRefused() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(Is.isA(ExecutionException.class));

        stream = new StreamConnection(descriptor(), http, consumer, String.format("https://localhost:%d%s", PORT, PATH));

        stream.read(Optional.<StartPosition>absent());
    }

    @Test
    public void testConnectionFail() throws Exception {
        Answer httpAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];
                exchange.sendResponseHeaders(500, 0L);
                return null;
            }
        };

        doAnswer(httpAnswer).when(serverHandler).handle(Matchers.<HttpExchange>any());

        expectedException.expect(ConnectionException.class);

        stream = new StreamConnection(descriptor(), http, consumer, url);

        read(stream, Optional.<StartPosition>absent(), 10);
    }

    @Test
    public void testBadRequest() throws Exception {
        Answer httpAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];
                exchange.sendResponseHeaders(400, 0L);
                return null;
            }
        };

        doAnswer(httpAnswer).when(serverHandler).handle(Matchers.<HttpExchange>any());

        expectedException.expect(ConnectionException.class);

        stream = new StreamConnection(descriptor(), http, consumer, url);

        read(stream, Optional.<StartPosition>absent(), 10);
    }

    @Test
    public void testConnectRedirect() throws Exception {
        final String leaderHost = "SRV=" + randomAlphanumeric(15);

        final AtomicReference<String> receivedLeaderHost = new AtomicReference<>();

        Answer firstHttpAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];
                exchange.getResponseHeaders().add("Set-Cookie", leaderHost);
                exchange.sendResponseHeaders(307, 0L);
                exchange.close();
                return null;
            }
        };
        Answer secondHttpAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];
                String value = exchange.getRequestHeaders().getFirst("Cookie");
                receivedLeaderHost.set(value);

                exchange.sendResponseHeaders(200, 0L);
                exchange.close();
                return null;
            }
        };
        doAnswer(firstHttpAnswer)
        .doAnswer(secondHttpAnswer)
        .when(serverHandler).handle(Matchers.<HttpExchange>any());

        stream = new StreamConnection(descriptor(), http, consumer, url);

        read(stream, Optional.<StartPosition>absent(), 10);

        assertEquals(leaderHost, receivedLeaderHost.get());
    }

    @Test
    public void testExceptionDuringConsume() throws Exception {
        Answer httpAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];
                exchange.sendResponseHeaders(200, 0L);
                exchange.getResponseBody().write(randomAlphabetic(10).getBytes(UTF_8));
                exchange.getResponseBody().write("\n".getBytes(UTF_8));
                exchange.close();
                return null;
            }
        };

        doAnswer(httpAnswer).when(serverHandler).handle(Matchers.<HttpExchange>any());

        doThrow(new RuntimeException("boom")).when(consumer).accept(anyString());

        stream = new StreamConnection(descriptor(), http, consumer, url);

        expectedException.expect(RuntimeException.class);

        read(stream, Optional.<StartPosition>absent(), 10);
    }

    @Test
    public void testCloseKillsConsume() throws Exception {
        Answer httpAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];
                exchange.sendResponseHeaders(200, 0L);

                // hoping this is enough to force flushing data down the wire
                exchange.getResponseBody().write(randomAlphabetic(10000).getBytes(UTF_8));
                exchange.getResponseBody().write("\n".getBytes(UTF_8));

                return null;
            }
        };

        doAnswer(httpAnswer).when(serverHandler).handle(Matchers.<HttpExchange>any());

        final CountDownLatch latch = new CountDownLatch(1);
        Answer consumerAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                latch.countDown();
                return null;
            }
        };

        doAnswer(consumerAnswer).when(consumer).accept(anyString());

        stream = new StreamConnection(descriptor(), http, consumer, url);

        ExecutorService thread = Executors.newSingleThreadExecutor();
        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                stream.read(Optional.<StartPosition>absent());
                return Boolean.TRUE;
            }
        };
        try {
            Future<Boolean> future = thread.submit(callable);

            // Wait till we get something from the server
            latch.await(1, TimeUnit.MINUTES);

            // Now kill the stream
            stream.close();

            // Consume future should return without issue now
            try {
                future.get(1, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                fail();
            }
        }
        finally {
            thread.shutdownNow();
        }
    }

    @Test
    public void testCloseBeforeRead() throws Exception {
        stream = new StreamConnection(descriptor(), http, consumer, url);

        stream.close();

        final CountDownLatch latch = new CountDownLatch(1);
        ExecutorService thread = Executors.newSingleThreadExecutor();
        try {
            thread.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        stream.read(Optional.<StartPosition>absent());
                        latch.countDown();
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });

            assertTrue(latch.await(1, TimeUnit.SECONDS));
        }
        finally {
            thread.shutdownNow();
        }
    }

    private void read(final StreamConnection mes,
                      final Optional<StartPosition> position,
                      final int consumeSeconds) throws Exception {

        ExecutorService thread = Executors.newFixedThreadPool(2);
        Future<?> future = thread.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    mes.read(position);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        thread.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(consumeSeconds);
                    mes.close();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            future.get(consumeSeconds * 2, TimeUnit.SECONDS);
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            else {
                e.printStackTrace();
                fail();
            }
        }
        finally {
            thread.shutdownNow();
        }
    }

    private StreamQueryDescriptor descriptor() {
        StreamQueryDescriptor.Builder builder = StreamQueryDescriptor.newBuilder()
            .setCreds( Creds.newBuilder()
                .setAppKey(randomAlphabetic(22))
                .setToken(randomAlphabetic(5))
                .build());

        return builder.build();
    }

    private StreamQueryDescriptor filterDescriptor(Filter... filter) {
        return StreamQueryDescriptor.newBuilder()
            .setCreds( Creds.newBuilder()
                .setAppKey(randomAlphabetic(22))
                .setToken(randomAlphabetic(5))
                .build())
            .addFilters(filter)
            .build();
    }

    private StreamQueryDescriptor subsetDescriptor(Subset subset) {
        return StreamQueryDescriptor.newBuilder()
            .setCreds( Creds.newBuilder()
                .setAppKey(randomAlphabetic(22))
                .setToken(randomAlphabetic(5))
                .build())
            .setSubset(subset)
            .build();
    }
}