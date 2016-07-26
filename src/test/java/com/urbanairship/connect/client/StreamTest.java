package com.urbanairship.connect.client;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;
import com.ning.http.client.AsyncHttpClient;
import com.urbanairship.connect.client.model.GsonUtil;
import com.urbanairship.connect.client.model.StartPosition;
import com.urbanairship.connect.java8.Consumer;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StreamTest {

    @Mock private StreamConnectionSupplier connSupplier;
    @Mock private StreamConnection conn;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testStream() throws Exception {
        final AtomicReference<Consumer<String>> consumer = hookStream(connSupplier, conn);
        final List<String> events = events(20);
        final CountDownLatch stop = new CountDownLatch(1);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                for (String event : events) {
                    consumer.get().accept(event);
                }

                stop.await();
                return null;
            }
        })
        .when(conn).read(Matchers.<Optional<StartPosition>>any());

        List<String> received = new ArrayList<>();
        try (Stream stream = new Stream(descriptor(), Optional.<StartPosition>absent(), Optional.of(connSupplier))){
            while (stream.hasNext()) {
                received.add(stream.next());
                if (received.size() == 20) {
                    break;
                }
            }
        }
        finally {
            stop.countDown();
        }

        assertEquals(events, received);

        verify(conn, atLeastOnce()).close();
    }

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testExceptionBubblesOut() throws Exception {
        when(connSupplier.get(Matchers.<StreamQueryDescriptor>any(), Matchers.<AsyncHttpClient>any(), Matchers.<Consumer<String>>any()))
                .thenReturn(conn);

        doThrow(new ConnectionException("boom")).when(conn).read(Matchers.<Optional<StartPosition>>any());

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(Is.isA(ConnectionException.class));

        try (Stream stream = new Stream(descriptor(), Optional.<StartPosition>absent(), Optional.of(connSupplier))){
            while (stream.hasNext()) {
                stream.next();
            }
        }
    }

    @Test
    public void testStartPositionPropogated() throws Exception {
        final AtomicReference<Consumer<String>> consumer = hookStream(connSupplier, conn);
        final List<String> events = events(1);
        final CountDownLatch stop = new CountDownLatch(1);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                for (String event : events) {
                    consumer.get().accept(event);
                }

                stop.await();
                return null;
            }
        })
        .when(conn).read(Matchers.<Optional<StartPosition>>any());

        StartPosition pos = StartPosition.relative(StartPosition.RelativePosition.EARLIEST);
        List<String> received = new ArrayList<>();
        try (Stream stream = new Stream(descriptor(), Optional.of(pos), Optional.of(connSupplier))){
            if (stream.hasNext()) {
                received.add(stream.next());
            }
        }
        finally {
            stop.countDown();
        }

        assertEquals(events, received);

        verify(conn).read(Optional.of(pos));
    }

    @SuppressWarnings("unchecked")
    private AtomicReference<Consumer<String>> hookStream(StreamConnectionSupplier supplier,
                                                         final StreamConnection conn) {

        final AtomicReference<Consumer<String>> hook = new AtomicReference<>();

        final AtomicBoolean first = new AtomicBoolean(true);
        when(supplier.get(Matchers.<StreamQueryDescriptor>any(), Matchers.<AsyncHttpClient>any(), Matchers.<Consumer<String>>any()))
            .thenAnswer(new Answer<StreamConnection>() {
                @Override
                public StreamConnection answer(InvocationOnMock invocation) throws Throwable {
                    if (first.compareAndSet(true, false)) {
                        Consumer<String> consumer = (Consumer<String>) invocation.getArguments()[2];
                        hook.set(consumer);
                    }

                    return conn;
                }
            });

        return hook;
    }

    private List<String> events(int count) {
        List<String> events = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            JsonObject o = new JsonObject();
            o.addProperty("offset", i);

            String json = GsonUtil.getGson().toJson(o);
            events.add(json);
        }

        return events;
    }

    private StreamQueryDescriptor descriptor() {
        StreamQueryDescriptor.Builder builder = StreamQueryDescriptor.newBuilder()
                .setCreds( Creds.newBuilder()
                        .setAppKey(randomAlphabetic(22))
                        .setToken(randomAlphabetic(5))
                        .build());

        return builder.build();
    }
}