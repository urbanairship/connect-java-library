/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client;

import com.google.common.base.Optional;
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
import com.urbanairship.connect.client.model.responses.AssociatedPush;
import com.urbanairship.connect.client.model.responses.CustomEvent;
import com.urbanairship.connect.client.model.responses.DeviceInfo;
import com.urbanairship.connect.client.model.responses.Event;
import com.urbanairship.connect.client.offsets.InMemOffsetManager;
import com.urbanairship.connect.client.offsets.OffsetManager;
import com.urbanairship.connect.java8.Consumer;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang3.RandomUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextInt;
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

public class MobileEventConsumerServiceTest {

    private static final int PORT = 10000 + RandomUtils.nextInt(0, 50000);
    private static final String PATH = "/test";

    private static final JsonParser parser = new JsonParser();

    private static HttpServer server;
    private static HttpHandler serverHandler;
    private static Configuration config;
    private static MobileEventConsumerService mobileEventConsumerService;

    private AsyncHttpClient http;

    @Mock
    private Consumer<Event> consumer;
    @Mock
    private StreamSupplier supplier;
    @Mock
    private FatalExceptionHandler fatalExceptionHandler;

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
        mobileEventConsumerService = null;

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
        final List<Event> events = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            events.add(createEvent((long) i));
        }

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

                for (Event e : events) {
                    String eventJson = GsonUtil.getGson().toJson(e);
                    exchange.getResponseBody().write(eventJson.getBytes(UTF_8));
                    exchange.getResponseBody().write("\n".getBytes(UTF_8));
                }
                exchange.close();

                return null;
            }
        };
        doAnswer(httpAnswer).when(serverHandler).handle(Matchers.<HttpExchange>any());

        final List<Event> received = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(events.size());
        Answer consumerAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                latch.countDown();
                Event consumedEvent = (Event) invocation.getArguments()[0];
                received.add(consumedEvent);
                return null;
            }
        };
        doAnswer(consumerAnswer).when(consumer).accept(any(Event.class));

        OffsetManager offsetManager = new InMemOffsetManager();
        mobileEventConsumerService = createMobileEventConsumerService(offsetManager).build();
        ExecutorService thread = Executors.newSingleThreadExecutor();
        Callable<Boolean> task = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mobileEventConsumerService.run();
                return Boolean.TRUE;
            }
        };
        try {
            Future<Boolean> future = thread.submit(task);

            // Wait till we get something from the server
            latch.await(1, TimeUnit.MINUTES);

            // Now kill the mobileEventConsumerService connection
            mobileEventConsumerService.triggerShutdown();

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

        assertFalse(mobileEventConsumerService.getDoConsume().get());
    }

    @Test
    public void testStreamHandlerReconnects() throws Exception {
        final List<Event> events = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            events.add(createEvent((long) i));
        }

        final AtomicReference<String> body = new AtomicReference<>();

        Answer firstHttpAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];

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
            }
        };

        Answer secondHttpAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];
                exchange.sendResponseHeaders(500, 0L);
                exchange.close();
                return null;
            }
        };

        Answer thirdHttpAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];

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
            }
        };

        doAnswer(firstHttpAnswer)
        .doAnswer(secondHttpAnswer)
        .doAnswer(thirdHttpAnswer).when(serverHandler).handle(Matchers.<HttpExchange>any());

        final List<Event> received = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(events.size());
        Answer consumerAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                latch.countDown();
                Event consumedEvent = (Event) invocation.getArguments()[0];
                received.add(consumedEvent);
                return null;
            }
        };

        doAnswer(consumerAnswer).when(consumer).accept(any(Event.class));

        OffsetManager offsetManager = new InMemOffsetManager();
        mobileEventConsumerService = createMobileEventConsumerService(offsetManager).build();
        ExecutorService thread = Executors.newSingleThreadExecutor();
        Callable<Boolean> task = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mobileEventConsumerService.run();
                return Boolean.TRUE;
            }
        };
        try {
            Future<Boolean> future = thread.submit(task);

            // Wait till we get something from the server
            latch.await(10, TimeUnit.MINUTES);

            // Now kill the mobileEventConsumerService connection
            mobileEventConsumerService.triggerShutdown();

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
        Answer httpAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                HttpExchange exchange = (HttpExchange) invocation.getArguments()[0];
                exchange.sendResponseHeaders(500, 0L);
                exchange.close();
                return null;
            }
        };

        doAnswer(httpAnswer).when(serverHandler).handle(Matchers.<HttpExchange>any());

        final MobileEventStream stream = mock(MobileEventStream.class);
        Answer supplierAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return stream;
            }
        };

        doAnswer(supplierAnswer).when(supplier).get(any(StreamQueryDescriptor.class), any(AsyncHttpClient.class), Matchers.<Consumer<String>>any(), any(String.class), any(FatalExceptionHandler.class));
        doThrow(new RuntimeException()).when(stream).connect(anyLong(), any(TimeUnit.class));

        OffsetManager offsetManager = new InMemOffsetManager();
        mobileEventConsumerService = createMobileEventConsumerService(offsetManager).setSupplier(supplier).build();
        mobileEventConsumerService.run();

        verify(stream, never()).consume(anyLong(), any(TimeUnit.class));
        verify(fatalExceptionHandler).handle(any(RuntimeException.class));
    }
    @Test
    public void testInterrupted() throws Exception {
        final MobileEventStream stream = mock(MobileEventStream.class);
        Answer supplierAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return stream;
            }
        };

        doAnswer(supplierAnswer).when(supplier).get(any(StreamQueryDescriptor.class), any(AsyncHttpClient.class), Matchers.<Consumer<String>>any(), any(String.class), any(FatalExceptionHandler.class));
        doThrow(new InterruptedException()).when(stream).connect(anyLong(), any(TimeUnit.class));

        mobileEventConsumerService = createMobileEventConsumerService(new InMemOffsetManager()).setSupplier(supplier).build();
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean interrupted = new AtomicBoolean(false);
        ExecutorService thread = Executors.newSingleThreadExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mobileEventConsumerService.run();
                interrupted.set(Thread.interrupted());
                latch.countDown();
            }
        };

        try {
            thread.execute(runnable);
        } finally {
            thread.shutdown();
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertTrue(interrupted.get());
    }

    private StreamQueryDescriptor descriptor(Optional<Long> offset) {
        StreamQueryDescriptor.Builder builder = StreamQueryDescriptor.newBuilder()
            .setCreds(Creds.newBuilder()
                .setAppKey(randomAlphabetic(22))
                .setToken(randomAlphabetic(5))
                .build());
        if (offset.isPresent()) {
            builder.setOffset(String.valueOf(offset.get()));
        }
        return builder.build();
    }

    private MobileEventConsumerService.Builder createMobileEventConsumerService(OffsetManager offsetManager) {
        return MobileEventConsumerService.newBuilder()
            .setClient(http)
            .setBaseStreamQueryDescriptor(descriptor(Optional.<Long>absent()))
            .setConfig(config)
            .setConsumer(consumer)
            .setOffsetManager(offsetManager)
            .setFatalExceptionHandler(fatalExceptionHandler);
    }

    private Event createEvent(Long offset) {
        String name = randomAlphabetic(20);
        Optional<Double> value = Optional.of(2.00);
        String interactionType = "Landing Page";
        String interactionId = UUID.randomUUID().toString();
        Optional<String> customerId = Optional.of(randomAlphabetic(10));
        Optional<String> transactionId = Optional.of(randomAlphabetic(10));
        String lastDeliveredPushId = UUID.randomUUID().toString();
        Optional<String> lastDeliveredGroupId = Optional.of(UUID.randomUUID().toString());
        AssociatedPush lastDelivered = new AssociatedPush(lastDeliveredPushId, lastDeliveredGroupId, Optional.<Integer>absent(), Optional.<DateTime>absent());
        String triggeringPushPushId = UUID.randomUUID().toString();
        Optional<String> triggeringPushGroupId = Optional.of(UUID.randomUUID().toString());
        AssociatedPush triggeringPush = new AssociatedPush(triggeringPushPushId, triggeringPushGroupId, Optional.<Integer>absent(), Optional.<DateTime>absent());

        CustomEvent customEvent = new CustomEvent(name, value, transactionId, customerId, interactionId, interactionType, Optional.of(lastDelivered), Optional.of(triggeringPush));

        DeviceInfo deviceInfo = DeviceInfo.newBuilder()
            .setChanneId(UUID.randomUUID().toString())
            .setPlatform(DeviceFilterType.IOS)
            .build();

        return Event.newBuilder()
            .setEventType(EventType.CUSTOM)
            .setEventBody(customEvent)
            .setDeviceInfo(deviceInfo)
            .setOffset(String.valueOf(offset))
            .setOccurred(DateTime.now().withZone(DateTimeZone.UTC))
            .setProcessed(DateTime.now().plusSeconds(nextInt(1, 10)).withZone(DateTimeZone.UTC))
            .setIdentifier(UUID.randomUUID().toString())
            .build();
    }

}
