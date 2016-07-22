/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.gson.JsonObject;
import com.ning.http.client.AsyncHttpClient;
import com.urbanairship.connect.client.model.GsonUtil;
import com.urbanairship.connect.client.model.StartPosition;
import com.urbanairship.connect.java8.Consumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class StreamConsumeTaskTest {

    private StreamConsumeTask task;

    @Mock private Consumer<String> consumer;

    @Mock private AsyncHttpClient http;
    @Mock private StreamConnectionSupplier supplier;
    @Mock private StreamConnection stream;

    @Captor private ArgumentCaptor<Optional<StartPosition>> positionCaptor;

    private ExecutorService readThread;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(supplier.get(Matchers.<StreamQueryDescriptor>any(), Matchers.<AsyncHttpClient>any(), Matchers.<Consumer<String>>any()))
                .thenReturn(stream);

        readThread = Executors.newSingleThreadExecutor();
    }

    @After
    public void tearDown() throws Exception {
        if (task != null) task.stop();
        if (readThread != null) readThread.shutdownNow();
    }

    @Test
    public void testRun() throws Exception {
        final Future<Consumer<String>> hook = hookStream();

        StreamQueryDescriptor descriptor = descriptor();
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        task = task(descriptor, queue);

        final List<String> events = events(10);
        final CountDownLatch readDone = new CountDownLatch(1);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                consume(hook.get(), events);
                readDone.countDown();
                return null;
            }
        })
        .doNothing()
        .when(stream).read(Matchers.<Optional<StartPosition>>any());

        readThread.submit(task);

        assertTrue(readDone.await(10, TimeUnit.SECONDS));

        task.stop();

        assertEquals(events, ImmutableList.copyOf(queue));
    }

    @Test
    public void testRetries() throws Exception {
        final List<String> batch1 = events(2);
        final List<String> batch2 = events(3);
        final List<String> batch3 = events(1);

        final Future<Consumer<String>> hook = hookStream();

        final CountDownLatch iterationsDone = new CountDownLatch(1);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                consume(hook.get(), batch1);
                return null;
            }
        })
        .doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                consume(hook.get(), batch2);
                throw new RuntimeException("Boom!");
            }
        })
        .doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                consume(hook.get(), batch3);
                iterationsDone.countDown();
                return null;
            }
        })
        .doNothing()
        .when(stream).read(Matchers.<Optional<StartPosition>>any());

        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        task = task(descriptor(), queue);

        readThread.submit(task);

        assertTrue(iterationsDone.await(10, TimeUnit.SECONDS));

        assertEquals(
            ImmutableList.builder()
                .addAll(batch1)
                .addAll(batch2)
                .addAll(batch3)
                .build(),
            ImmutableList.copyOf(queue)
        );
    }

    private void consume(Consumer<String> consumer, List<String> events) {
        for (String event : events) {
            consumer.accept(event);
        }
    }

    //    @Test
//    public void testRun() throws Exception {
//        StreamQueryDescriptor descriptor = descriptor();
//
//        task = task(descriptor);
//
//        final CountDownLatch readCalled = new CountDownLatch(1);
//        final CountDownLatch release = new CountDownLatch(1);
//        doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocation) throws Throwable {
//                readCalled.countDown();
//                release.await();
//                return null;
//            }
//        })
//        .doNothing()
//        .when(stream).read(Matchers.<Optional<StartPosition>>any());
//
//        readThread.submit(task);
//
//        assertTrue(readCalled.await(10, TimeUnit.SECONDS));
//
//        release.countDown();
//
//        task.stop();
//
//        verify(supplier, atLeastOnce()).get(eq(descriptor), Matchers.<AsyncHttpClient>any(), eq(consumer));
//        verify(stream, atLeastOnce()).read(Optional.<StartPosition>absent());
//    }

//    @Test
//    public void testRetriesOnException() throws Exception {
//        StreamQueryDescriptor descriptor = descriptor();
//        task = task(descriptor, queue);
//
//        final CountDownLatch done = new CountDownLatch(1);
//
//        doThrow(new RuntimeException("fail once"))
//        .doThrow(new RuntimeException("fail twice"))
//        .doThrow(new RuntimeException("fail thrice"))
//        .doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocation) throws Throwable {
//                done.countDown();
//                return null;
//            }
//        })
//        .when(stream).read(Matchers.<Optional<StartPosition>>any());
//
//        readThread.submit(task);
//
//        assertTrue(done.await(10, TimeUnit.SECONDS));
//
//        task.stop();
//    }
//
//    @Test
//    public void testConnectionException() throws Exception {
//        task = task(descriptor(), queue);
//
//        doThrow(new ConnectionException("boom")).when(stream).read(Matchers.<Optional<StartPosition>>any());
//
//        Future<?> future = readThread.submit(task);
//
//        Throwable failure = null;
//        try {
//            future.get(10, TimeUnit.SECONDS);
//        }
//        catch (ExecutionException e) {
//            failure = e.getCause();
//        }
//
//        assertNotNull(failure);
//        assertTrue(failure instanceof ConnectionException);
//    }

    @SuppressWarnings("unchecked")
    private Future<Consumer<String>> hookStream() {
        class Hook extends AbstractFuture<Consumer<String>> {
            void signal(Consumer<String> consumer) {
                set(consumer);
            }
        }

        final Hook hook = new Hook();

        final AtomicBoolean first = new AtomicBoolean(true);
        when(supplier.get(Matchers.<StreamQueryDescriptor>any(), Matchers.<AsyncHttpClient>any(), Matchers.<Consumer<String>>any()))
            .thenAnswer(new Answer<StreamConnection>() {
                @Override
                public StreamConnection answer(InvocationOnMock invocation) throws Throwable {
                    if (first.compareAndSet(true, false)) {
                        Consumer<String> consumer = (Consumer<String>) invocation.getArguments()[2];
                        hook.signal(consumer);
                    }

                    return stream;
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

    private StreamConsumeTask task(StreamQueryDescriptor descriptor, BlockingQueue<String> queue) {
        return StreamConsumeTask.newBuilder()
                .setStreamQueryDescriptor(descriptor)
                .setStreamSupplier(supplier)
                .setTargetQueue(queue)
                .build();
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
