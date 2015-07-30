package com.urbanairship.connect.client;

import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.urbanairship.connect.client.model.DeviceIdType;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.Subset;
import com.urbanairship.connect.client.model.filters.DeviceFilter;
import com.urbanairship.connect.client.model.filters.DeviceFilterSerializer;
import com.urbanairship.connect.client.model.filters.Filter;
import com.urbanairship.connect.client.model.filters.NotificationFilter;
import com.urbanairship.connect.client.model.filters.OptionalSerializer;
import org.apache.commons.lang3.RandomUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class MobileEventStreamTest {

    private static final int PORT = 10000 + RandomUtils.nextInt(0, 50000);
    private static final String PATH = "/test";

    private static final JsonParser parser = new JsonParser();

    private static HttpServer server;
    private static HttpHandler serverHandler;
    private static String url;

    private AsyncHttpClient http;

    @Mock private Consumer<String> consumer;

    private MobileEventStream stream;

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
                .setUserAgent("Wildwood Ingress Client")
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
        StreamDescriptor descriptor = descriptor(Optional.<Long>empty());

        String line = randomAlphabetic(15);

        AtomicReference<String> body = new AtomicReference<>();
        doAnswer(invocationOnMock -> {
            HttpExchange exchange = (HttpExchange) invocationOnMock.getArguments()[0];

            int length = Integer.parseInt(exchange.getRequestHeaders().getFirst(HttpHeaders.CONTENT_LENGTH));
            byte[] bytes = new byte[length];
            exchange.getRequestBody().read(bytes);
            body.set(new String(bytes, UTF_8));

            exchange.sendResponseHeaders(200, 0L);

            exchange.getResponseBody().write(line.getBytes(UTF_8));
            exchange.getResponseBody().write("\n".getBytes(UTF_8));
            exchange.close();

            return null;
        }).when(serverHandler).handle(Matchers.<HttpExchange>any());

        List<String> received = new ArrayList<>();
        doAnswer(invocationOnMock -> {
            String l = (String) invocationOnMock.getArguments()[0];
            received.add(l);
            return null;
        }).when(consumer).accept(anyString());

        stream = new MobileEventStream(descriptor, http, consumer, url);

        stream.connect(10, TimeUnit.SECONDS);
        stream.consume(10, TimeUnit.SECONDS);

        assertEquals(1, received.size());
        assertEquals(line, received.get(0));

        JsonObject bodyObj = parser.parse(body.get()).getAsJsonObject();
        assertEquals("LATEST", bodyObj.get("start").getAsString());
    }

    @Test
    public void testAuth() throws Exception {
        AtomicReference<String> auth = new AtomicReference<>();
        doAnswer(invocationOnMock -> {
            HttpExchange exchange = (HttpExchange) invocationOnMock.getArguments()[0];
            String value = exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            auth.set(value);
            exchange.sendResponseHeaders(200, 0L);
            return null;
        }).when(serverHandler).handle(Matchers.<HttpExchange>any());

        StreamDescriptor descriptor = descriptor(Optional.<Long>empty());
        stream = new MobileEventStream(descriptor, http, consumer, url);
        stream.connect(10, TimeUnit.SECONDS);

        assertTrue(auth.get().toLowerCase().startsWith("basic"));
        byte[] decodeBytes = Base64.getDecoder().decode(auth.get().substring("basic ".length()));
        String decoded = new String(decodeBytes, UTF_8);
        String[] pieces = decoded.split(":");

        assertEquals(descriptor.getCreds().getAppKey(), pieces[0]);
        assertEquals(descriptor.getCreds().getSecret(), pieces[1]);
    }

    @Test
    public void testRequestBodyWithOffset() throws Exception {
        AtomicReference<String> body = new AtomicReference<>();
        doAnswer(invocationOnMock -> {
            HttpExchange exchange = (HttpExchange) invocationOnMock.getArguments()[0];

            int length = Integer.parseInt(exchange.getRequestHeaders().getFirst(HttpHeaders.CONTENT_LENGTH));
            byte[] bytes = new byte[length];
            exchange.getRequestBody().read(bytes);
            body.set(new String(bytes, UTF_8));

            exchange.sendResponseHeaders(200, 0L);
            return null;
        }).when(serverHandler).handle(Matchers.<HttpExchange>any());

        long offset = RandomUtils.nextInt(0, 100000);
        StreamDescriptor descriptor = descriptor(Optional.of(offset));

        stream = new MobileEventStream(descriptor, http, consumer, url);
        stream.connect(10, TimeUnit.SECONDS);

        JsonObject bodyObj = parser.parse(body.get()).getAsJsonObject();
        assertEquals(offset, bodyObj.get("resume_offset").getAsLong());
    }

    @Test
    public void testRequestBodyWithFilter() throws Exception {
        AtomicReference<String> body = new AtomicReference<>();
        doAnswer(invocationOnMock -> {
            HttpExchange exchange = (HttpExchange) invocationOnMock.getArguments()[0];

            int length = Integer.parseInt(exchange.getRequestHeaders().getFirst(HttpHeaders.CONTENT_LENGTH));
            byte[] bytes = new byte[length];
            exchange.getRequestBody().read(bytes);
            body.set(new String(bytes, UTF_8));

            exchange.sendResponseHeaders(200, 0L);
            return null;
        }).when(serverHandler).handle(Matchers.<HttpExchange>any());

        DeviceFilter device1 = new DeviceFilter(DeviceIdType.ANDROID, "c8044c8a-d5fa-4e58-91d4-54d0f70b7409");
        DeviceFilter device2 = new DeviceFilter(DeviceIdType.IOS, "3d970087-600e-4bb6-8474-5857d438faaa");
        DeviceFilter device3 = new DeviceFilter(DeviceIdType.NAMED_USER, "cool user");
        NotificationFilter notification = NotificationFilter.createGroupIdFilter("a30abf06-7878-4096-9535-b50ac0ad6e8e");

        Filter filter1 = Filter.newBuilder()
            .setLatency(20000000)
            .addDevices(device1, device2, device3)
            .addDeviceTypes(DeviceIdType.ANDROID, DeviceIdType.AMAZON)
            .addNotification(notification)
            .addType(EventType.OPEN)
            .build();

        Filter filter2 = Filter.newBuilder()
            .setLatency(400)
            .addDeviceTypes(DeviceIdType.IOS)
            .addType(EventType.TAG_CHANGE)
            .build();

        StreamDescriptor descriptor = filterDescriptor(filter1, filter2);

        stream = new MobileEventStream(descriptor, http, consumer, url);
        stream.connect(10, TimeUnit.SECONDS);

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(DeviceFilter.class, new DeviceFilterSerializer())
            .registerTypeAdapter(Optional.class, new OptionalSerializer())
            .create();

        JsonObject bodyObj = parser.parse(body.get()).getAsJsonObject();
        assertEquals(gson.toJson(new HashSet<>(Arrays.asList(filter1, filter2))), gson.toJson(bodyObj.get("filters")));
    }

    @Test
    public void testRequestBodyWithSubset() throws Exception {
        AtomicReference<String> body = new AtomicReference<>();
        doAnswer(invocationOnMock -> {
            HttpExchange exchange = (HttpExchange) invocationOnMock.getArguments()[0];

            int length = Integer.parseInt(exchange.getRequestHeaders().getFirst(HttpHeaders.CONTENT_LENGTH));
            byte[] bytes = new byte[length];
            exchange.getRequestBody().read(bytes);
            body.set(new String(bytes, UTF_8));

            exchange.sendResponseHeaders(200, 0L);
            return null;
        }).when(serverHandler).handle(Matchers.<HttpExchange>any());

        Subset subset = Subset.createPartitionSubset(10, 0);
        StreamDescriptor descriptor = subsetDescriptor(subset);

        stream = new MobileEventStream(descriptor, http, consumer, url);
        stream.connect(10, TimeUnit.SECONDS);

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(DeviceFilter.class, new DeviceFilterSerializer())
            .registerTypeAdapter(Optional.class, new OptionalSerializer())
            .create();

        JsonObject bodyObj = parser.parse(body.get()).getAsJsonObject();
        assertEquals(gson.toJson(subset), gson.toJson(bodyObj.get("subset")));
    }

    @Test
    public void testConnectionFail() throws Exception {
        doAnswer(invocationOnMock -> {
            HttpExchange exchange = (HttpExchange) invocationOnMock.getArguments()[0];
            exchange.sendResponseHeaders(500, 0L);
            return null;
        }).when(serverHandler).handle(Matchers.<HttpExchange>any());

        stream = new MobileEventStream(descriptor(Optional.<Long>empty()), http, consumer, url);

        boolean failed = false;
        try {
            stream.connect(10, TimeUnit.SECONDS);
        }
        catch (RuntimeException e) {
            failed = true;
        }

        assertTrue(failed);
    }

    @Test
    public void testConnectRedirect() throws Exception {
        String leaderHost = randomAlphanumeric(15);

        AtomicReference<String> receivedLeaderHost = new AtomicReference<>();

        doAnswer(invocationOnMock -> {
            HttpExchange exchange = (HttpExchange) invocationOnMock.getArguments()[0];
            exchange.getResponseHeaders().add("Set-Cookie", leaderHost);
            exchange.sendResponseHeaders(307, 0L);
            exchange.close();
            return null;
        })
        .doAnswer(invocationOnMock -> {
            HttpExchange exchange = (HttpExchange) invocationOnMock.getArguments()[0];
            String value = exchange.getRequestHeaders().getFirst("Cookie");
            receivedLeaderHost.set(value);

            exchange.sendResponseHeaders(200, 0L);
            exchange.close();
            return null;
        })
        .when(serverHandler).handle(Matchers.<HttpExchange>any());

        stream = new MobileEventStream(descriptor(Optional.<Long>empty()), http, consumer, url);

        stream.connect(10, TimeUnit.SECONDS);

        assertEquals(leaderHost, receivedLeaderHost.get());
    }

    @Test
    public void testExceptionDuringConsume() throws Exception {
        doAnswer(invocationOnMock -> {
            HttpExchange exchange = (HttpExchange) invocationOnMock.getArguments()[0];
            exchange.sendResponseHeaders(200, 0L);
            exchange.getResponseBody().write(randomAlphabetic(10).getBytes(UTF_8));
            exchange.getResponseBody().write("\n".getBytes(UTF_8));
            exchange.close();
            return null;
        }).when(serverHandler).handle(Matchers.<HttpExchange>any());

        doThrow(new RuntimeException("boom")).when(consumer).accept(anyString());

        stream = new MobileEventStream(descriptor(Optional.<Long>empty()), http, consumer, url);

        stream.connect(10, TimeUnit.SECONDS);

        boolean excepted = false;
        try {
            stream.consume(10, TimeUnit.SECONDS);
        }
        catch (Exception e) {
            excepted = true;
        }

        assertTrue(excepted);
    }

    @Test
    public void testCloseKillsConsume() throws Exception {
        doAnswer(invocationOnMock -> {
            HttpExchange exchange = (HttpExchange) invocationOnMock.getArguments()[0];
            exchange.sendResponseHeaders(200, 0L);

            // hoping this is enough to force flushing data down the wire
            exchange.getResponseBody().write(randomAlphabetic(10000).getBytes(UTF_8));
            exchange.getResponseBody().write("\n".getBytes(UTF_8));

            return null;
        }).when(serverHandler).handle(Matchers.<HttpExchange>any());

        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocationOnMock -> {
            latch.countDown();
            return null;
        }).when(consumer).accept(anyString());

        stream = new MobileEventStream(descriptor(Optional.<Long>empty()), http, consumer, url);

        stream.connect(10, TimeUnit.SECONDS);

        ExecutorService thread = Executors.newSingleThreadExecutor();
        try {
            Future<Boolean> future = thread.submit(() -> {
                stream.consume(10, TimeUnit.MINUTES);
                return Boolean.TRUE;
            });

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
    public void testConsumeTimesOut() throws Exception {
        doAnswer(invocationOnMock -> {
            HttpExchange exchange = (HttpExchange) invocationOnMock.getArguments()[0];
            exchange.sendResponseHeaders(200, 0L);
            return null;
        }).when(serverHandler).handle(Matchers.<HttpExchange>any());

        // No body is coming, but consume should exit after 1 second without failure
        long consumeTime;
        try {
            stream = new MobileEventStream(descriptor(Optional.<Long>empty()), http, consumer, url);
            stream.connect(10, TimeUnit.SECONDS);

            long start = System.currentTimeMillis();
            stream.consume(1, TimeUnit.SECONDS);
            consumeTime = System.currentTimeMillis() - start;
        }
        catch (Exception e) {
            fail();
            return;
        }

        // Allow a little leeway
        assertTrue(consumeTime < 1500L);
    }

    private StreamDescriptor descriptor(Optional<Long> offset) {
        StreamDescriptor.Builder builder = StreamDescriptor.newBuilder()
            .setCreds( Creds.newBuilder()
                .setAppKey(randomAlphabetic(22))
                .setSecret(randomAlphabetic(5))
                .build());
        if (offset.isPresent()) {
            builder.setOffset(offset.get());
        }
        return builder.build();
    }

    private StreamDescriptor filterDescriptor(Filter... filter) {
        return StreamDescriptor.newBuilder()
            .setCreds( Creds.newBuilder()
                .setAppKey(randomAlphabetic(22))
                .setSecret(randomAlphabetic(5))
                .build())
            .addFilters(filter)
            .build();
    }

    private StreamDescriptor subsetDescriptor(Subset subset) {
        return StreamDescriptor.newBuilder()
            .setCreds( Creds.newBuilder()
                .setAppKey(randomAlphabetic(22))
                .setSecret(randomAlphabetic(5))
                .build())
            .setSubset(subset)
            .build();
    }
}