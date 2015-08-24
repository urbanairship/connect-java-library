package com.urbanairship.connect.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.HttpHeaders;
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
import com.urbanairship.connect.client.model.responses.CustomEvent;
import com.urbanairship.connect.client.model.responses.DeviceInfo;
import com.urbanairship.connect.client.model.responses.Event;
import com.urbanairship.connect.client.model.responses.PushIds;
import com.urbanairship.connect.client.offsets.InMemOffsetManager;
import com.urbanairship.connect.client.offsets.OffsetManager;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang3.RandomUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class StreamHandlerTest {

    private static final int PORT = 10000 + RandomUtils.nextInt(0, 50000);
    private static final String PATH = "/test";

    private static final JsonParser parser = new JsonParser();

    private static HttpServer server;
    private static HttpHandler serverHandler;
    private static Configuration config;
    private static StreamHandler handler;

    private AsyncHttpClient http;

    @Mock
    private Consumer<Event> consumer;
    @Mock
    private StreamSupplier supplier;

    @BeforeClass
    public static void serverStart() throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        serverHandler = mock(HttpHandler.class);
        server.createContext(PATH, serverHandler);
        server.start();

        String serverUrl = String.format("http://localhost:%d%s", PORT, PATH);
        config = new MapConfiguration(ImmutableMap.of(
            ConnectClientConfiguration.MES_URL_PROP, serverUrl,
            ConnectClientConfiguration.MAX_CONNECTION_ATTEMPTS_PROP, 3
        ));
    }

    @AfterClass
    public static void serverStop() throws Exception {
        server.stop(0);
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.reset(serverHandler);
        handler = null;

        AsyncHttpClientConfig clientConfig = new AsyncHttpClientConfig.Builder()
            .setUserAgent("Connect Client")
            .setRequestTimeout(-1)
            .setAllowPoolingConnections(false)
            .setAllowPoolingSslConnections(false)
            .build();

        http = new AsyncHttpClient(clientConfig);
    }

    @Test
    public void testStreamHandler() throws Exception {
        List<Event> events = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            events.add(createEvent((long) i));
        }

        AtomicReference<String> body = new AtomicReference<>();
        doAnswer(invocationOnMock -> {
            HttpExchange exchange = (HttpExchange) invocationOnMock.getArguments()[0];

            int length = Integer.parseInt(exchange.getRequestHeaders().getFirst(HttpHeaders.CONTENT_LENGTH));
            byte[] bytes = new byte[length];
            exchange.getRequestBody().read(bytes);
            body.set(new String(bytes, UTF_8));

            exchange.sendResponseHeaders(200, 0L);

            for (Event e : events) {
                String eventJson = GsonUtil.getGson().toJson(e);
                exchange.getResponseBody().write(eventJson.getBytes(UTF_8));
                exchange.getResponseBody().write("\n".getBytes(UTF_8));
            }
            exchange.close();

            return null;
        }).doAnswer(invocationOnMock -> {
            HttpExchange exchange = (HttpExchange) invocationOnMock.getArguments()[0];

            exchange.sendResponseHeaders(200, 0L);
            exchange.close();

            return null;
        }).when(serverHandler).handle(Matchers.<HttpExchange>any());

        List<Event> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(events.size());
        doAnswer(invocationOnMock -> {
            latch.countDown();
            Event consumedEvent = (Event) invocationOnMock.getArguments()[0];
            received.add(consumedEvent);
            return null;
        }).when(consumer).accept(any(Event.class));

        OffsetManager offsetManager = new InMemOffsetManager();
        handler = createHandler(offsetManager).build();
        ExecutorService thread = Executors.newSingleThreadExecutor();
        try {
            Future<Boolean> future = thread.submit(() -> {
                handler.run();
                return Boolean.TRUE;
            });

            // Wait till we get something from the server
            latch.await(1, TimeUnit.MINUTES);

            // Now kill the handler connection
            handler.triggerShutdown();

            // Consume future should return without issue now
            try {
                future.get(1, TimeUnit.SECONDS);
            } catch (TimeoutException te) {
                fail();
            }
        }
        finally {
            thread.shutdownNow();
        }

        assertEquals(events.size(), received.size());
        assertEquals(events, received);

        JsonObject bodyObj = parser.parse(body.get()).getAsJsonObject();
        assertEquals("LATEST", bodyObj.get("start").getAsString());
        assertEquals(String.valueOf(events.get(events.size() - 1).getOffset()), offsetManager.getLastOffset().get());

        assertEquals(http, handler.getAsyncClient());
        assertEquals(offsetManager, handler.getOffsetManager());
        assertFalse(handler.getDoConsume().get());
        assertEquals(3, handler.getConfig().maxConnectionAttempts);
        String serverUrl = String.format("http://localhost:%d%s", PORT, PATH);
        assertEquals(serverUrl, handler.getConfig().mesUrl);
    }

    @Test
    public void testStreamHandlerReconnects() throws Exception {
        List<Event> events = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            events.add(createEvent((long) i));
        }

        AtomicReference<String> body = new AtomicReference<>();

        doAnswer(invocationOnMock -> {
            HttpExchange exchange = (HttpExchange) invocationOnMock.getArguments()[0];

            int length = Integer.parseInt(exchange.getRequestHeaders().getFirst(HttpHeaders.CONTENT_LENGTH));
            byte[] bytes = new byte[length];
            exchange.getRequestBody().read(bytes);
            body.set(new String(bytes, UTF_8));

            exchange.sendResponseHeaders(200, 0L);

            for (Event e : events.subList(0, 4)) {
                String eventJson = GsonUtil.getGson().toJson(e);
                exchange.getResponseBody().write(eventJson.getBytes(UTF_8));
                exchange.getResponseBody().write("\n".getBytes(UTF_8));
            }
            exchange.close();

            return null;
        })
        .doAnswer(invocationOnMock -> {
            HttpExchange exchange = (HttpExchange) invocationOnMock.getArguments()[0];
            exchange.sendResponseHeaders(500, 0L);
            exchange.close();
            return null;
        })
        .doAnswer(invocationOnMock -> {
            HttpExchange exchange = (HttpExchange) invocationOnMock.getArguments()[0];

            int length = Integer.parseInt(exchange.getRequestHeaders().getFirst(HttpHeaders.CONTENT_LENGTH));
            byte[] bytes = new byte[length];
            exchange.getRequestBody().read(bytes);
            body.set(new String(bytes, UTF_8));

            exchange.sendResponseHeaders(200, 0L);

            for (Event e : events.subList(4, events.size())) {
                String eventJson = GsonUtil.getGson().toJson(e);
                exchange.getResponseBody().write(eventJson.getBytes(UTF_8));
                exchange.getResponseBody().write("\n".getBytes(UTF_8));
            }
            exchange.close();

            return null;
        }).when(serverHandler).handle(Matchers.<HttpExchange>any());

        List<Event> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(events.size());
        doAnswer(invocationOnMock -> {
            latch.countDown();
            Event consumedEvent = (Event) invocationOnMock.getArguments()[0];
            received.add(consumedEvent);
            return null;
        }).when(consumer).accept(any(Event.class));

        OffsetManager offsetManager = new InMemOffsetManager();
        handler = createHandler(offsetManager).build();
        ExecutorService thread = Executors.newSingleThreadExecutor();
        try {
            Future<Boolean> future = thread.submit(() -> {
                handler.run();
                return Boolean.TRUE;
            });

            // Wait till we get something from the server
            latch.await(10, TimeUnit.MINUTES);

            // Now kill the handler connection
            handler.triggerShutdown();

            // Consume future should return without issue now
            try {
                future.get(1, TimeUnit.SECONDS);
            } catch (TimeoutException te) {
                fail();
            }
        }
        finally {
            thread.shutdownNow();
        }

        assertEquals(events.size(), received.size());
        assertEquals(events, received);

        JsonObject bodyObj = parser.parse(body.get()).getAsJsonObject();
        assertEquals("4", bodyObj.get("resume_offset").getAsString());
        assertEquals(String.valueOf(events.get(events.size() - 1).getOffset()), offsetManager.getLastOffset().get());
    }

    @Test
    public void testMaxRetries() throws Exception {
        doAnswer(invocationOnMock -> {
            HttpExchange exchange = (HttpExchange) invocationOnMock.getArguments()[0];
            exchange.sendResponseHeaders(500, 0L);
            exchange.close();

            return null;
        }).when(serverHandler).handle(Matchers.<HttpExchange>any());

        MobileEventStream stream = mock(MobileEventStream.class);
        doAnswer(invocationMock -> stream).when(supplier).get(any(StreamDescriptor.class), any(AsyncHttpClient.class), Matchers.<Consumer<String>>any(), any(String.class));
        doThrow(new RuntimeException()).when(stream).connect(anyLong(), any(TimeUnit.class));

        OffsetManager offsetManager = new InMemOffsetManager();
        handler = createHandler(offsetManager).setSupplier(supplier).build();
        handler.run();

        verify(stream, never()).consume(anyLong(), any(TimeUnit.class));
    }
    @Test
    public void testInterrupted() throws Exception {
        MobileEventStream stream = mock(MobileEventStream.class);
        doAnswer(invocationMock -> stream).when(supplier).get(any(StreamDescriptor.class), any(AsyncHttpClient.class), Matchers.<Consumer<String>>any(), any(String.class));
        doThrow(new InterruptedException()).when(stream).connect(anyLong(), any(TimeUnit.class));

        handler = createHandler(new InMemOffsetManager()).setSupplier(supplier).build();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean interrupted = new AtomicBoolean(false);
        ExecutorService thread = Executors.newSingleThreadExecutor();
        try {
            thread.execute(() -> {
                handler.run();
                interrupted.set(Thread.interrupted());
                latch.countDown();
            });
        } finally {
            thread.shutdown();
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertTrue(interrupted.get());
    }

    private StreamDescriptor descriptor(Optional<Long> offset) {
        StreamDescriptor.Builder builder = StreamDescriptor.newBuilder()
            .setCreds( Creds.newBuilder()
                .setAppKey(randomAlphabetic(22))
                .setSecret(randomAlphabetic(5))
                .build());
        if (offset.isPresent()) {
            builder.setOffset(String.valueOf(offset.get()));
        }
        return builder.build();
    }

    private StreamHandler.Builder createHandler(OffsetManager offsetManager) {
        return StreamHandler.newBuilder()
            .setClient(http)
            .setBaseStreamDescriptor(descriptor(Optional.empty()))
            .setConfig(config)
            .setConsumer(consumer)
            .setOffsetManager(offsetManager);
    }

    private Event createEvent(Long offset) {
        String name = randomAlphabetic(20);
        Optional<Integer> value = Optional.of(2);
        String interactionType = "Landing Page";
        String interactionId = UUID.randomUUID().toString();
        Optional<String> customerId = Optional.of(randomAlphabetic(10));
        Optional<String> transactionId = Optional.of(randomAlphabetic(10));
        String lastDeliveredPushId = UUID.randomUUID().toString();
        Optional<String> lastDeliveredGroupId = Optional.of(UUID.randomUUID().toString());
        PushIds lastDelivered = new PushIds(lastDeliveredPushId, lastDeliveredGroupId);
        String triggeringPushPushId = UUID.randomUUID().toString();
        Optional<String> triggeringPushGroupId = Optional.of(UUID.randomUUID().toString());
        PushIds triggeringPush = new PushIds(triggeringPushPushId, triggeringPushGroupId);

        CustomEvent customEvent = new CustomEvent(name, value, transactionId, customerId, interactionId, interactionType, lastDelivered, triggeringPush);

        DeviceInfo deviceInfo = DeviceInfo.newBuilder()
            .setChanneId(UUID.randomUUID().toString())
            .setPlatform(DeviceFilterType.IOS)
            .build();

        return Event.newBuilder()
            .setAppKey(randomAlphabetic(22))
            .setEventType(EventType.CUSTOM)
            .setEventBody(customEvent)
            .setDeviceInfo(deviceInfo)
            .setOffset(String.valueOf(offset))
            .setOccurred(Instant.now())
            .setProcessed(Instant.now().plusSeconds(nextLong(1, 10)))
            .setIdentifier(UUID.randomUUID().toString())
            .build();
    }

}
