/*
Copyright 2022 Airship and Contributors
*/

package com.urbanairship.connect.client;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.urbanairship.connect.client.model.Creds;
import com.urbanairship.connect.client.model.GsonUtil;
import com.urbanairship.connect.client.model.StreamQueryDescriptor;
import com.urbanairship.connect.client.model.request.StartPosition;
import com.urbanairship.connect.java8.Consumer;
import org.apache.commons.lang3.RandomStringUtils;
import org.asynchttpclient.AsyncHttpClient;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StreamConsumeTaskTest {

    private StreamConsumeTask task;

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
        final AtomicReference<Consumer<String>> hook = hookStream();

        StreamQueryDescriptor descriptor = descriptor();
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        task = task(descriptor, queue);

        final List<TestEvent> events = events(10);
        final CountDownLatch readDone = new CountDownLatch(1);
        final CountDownLatch assertionDone = new CountDownLatch(1);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                consume(hook.get(), events);
                readDone.countDown();
                assertionDone.await();
                return null;
            }
        })
        .doNothing()
        .when(stream).read(Matchers.<Optional<StartPosition>>any());

        readThread.submit(task);

        try {
            assertTrue(readDone.await(10, TimeUnit.SECONDS));

            assertEquals(reduce(events), ImmutableList.copyOf(queue));

            verify(supplier).get(eq(descriptor), Matchers.<AsyncHttpClient>any(), Matchers.<Consumer<String>>any());
            verify(stream).read(Optional.<StartPosition>absent());
        }
        finally {
            assertionDone.countDown();
        }
    }

    @Test
    public void testRetries() throws Exception {
        final List<TestEvent> batch1 = events(2);
        final List<TestEvent> batch2 = events(3);
        final List<TestEvent> batch3 = events(1);

        final AtomicReference<Consumer<String>> hook = hookStream();

        final CountDownLatch iterationsDone = new CountDownLatch(1);
        final CountDownLatch assertionDone = new CountDownLatch(1);
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
                assertionDone.await();
                return null;
            }
        })
        .doNothing()
        .when(stream).read(Matchers.<Optional<StartPosition>>any());

        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        task = task(descriptor(), queue);

        readThread.submit(task);

        try {
            assertTrue(iterationsDone.await(10, TimeUnit.SECONDS));

            assertEquals(
                ImmutableList.builder()
                    .addAll(reduce(batch1))
                    .addAll(reduce(batch2))
                    .addAll(reduce(batch3))
                    .build(),
                ImmutableList.copyOf(queue)
            );

            verify(stream, atLeastOnce()).read(positionCaptor.capture());

            assertEquals(
                ImmutableList.builder()
                    .add(Optional.<Long>absent())
                    .add(Optional.of(StartPosition.offset(Iterables.getLast(batch1).offset)))
                    .add(Optional.of(StartPosition.offset(Iterables.getLast(batch2).offset)))
                    .build(),
                positionCaptor.getAllValues().subList(0, 3)
            );
        }
        finally {
            assertionDone.countDown();
        }
    }

    @Test
    public void testSpecifiedStartPosition() throws Exception {
        StartPosition position = StartPosition.offset(RandomStringUtils.randomAlphanumeric(32));
        task = StreamConsumeTask.newBuilder()
                .setStreamQueryDescriptor(descriptor())
                .setStreamConnectionSupplier(supplier)
                .setTargetQueue(new LinkedBlockingQueue<String>())
                .setStartingPosition(position)
                .build();

        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch verified = new CountDownLatch(1);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                latch.countDown();
                verified.await();
                return null;
            }
        }).when(stream).read(Matchers.<Optional<StartPosition>>any());

        readThread.submit(task);

        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));

            verify(stream).read(Optional.of(position));
        }
        finally {
            verified.countDown();
        }
    }

    @Test
    public void testConnectException() throws Exception {
        task = task(descriptor(), new LinkedBlockingQueue<String>());

        when(supplier.get(Matchers.<StreamQueryDescriptor>any(), Matchers.<AsyncHttpClient>any(), Matchers.<Consumer<String>>any()))
                .thenReturn(stream);

        doThrow(new ConnectionException("boom", 400)).when(stream).read(Matchers.<Optional<StartPosition>>any());

        Future<?> future = readThread.submit(task);

        try {
            future.get(10, TimeUnit.SECONDS);
            fail();
        }
        catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof ConnectionException);
        }
    }

    @Test
    public void testAlreadySeenEventAfterReconnectIsIgnored() throws Exception {
        final List<TestEvent> batch1 = events(2);
        final List<TestEvent> batch2 = events(3);

        final AtomicReference<Consumer<String>> hook = hookStream();

        final CountDownLatch iterationsDone = new CountDownLatch(1);
        final CountDownLatch assertionDone = new CountDownLatch(1);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                consume(hook.get(), batch1);
                throw new RuntimeException();
            }
        })
        .doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                consume(hook.get(), ImmutableList.of(Iterables.getLast(batch1)));
                consume(hook.get(), batch2);
                iterationsDone.countDown();
                assertionDone.await();
                return null;
            }
        })
        .doNothing()
        .when(stream).read(Matchers.<Optional<StartPosition>>any());

        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        task = task(descriptor(), queue);

        readThread.submit(task);

        try {
            iterationsDone.await();
            assertTrue(iterationsDone.await(10, TimeUnit.SECONDS));

            assertEquals(
                    ImmutableList.builder()
                            .addAll(reduce(batch1))
                            .addAll(reduce(batch2))
                            .build(),
                    ImmutableList.copyOf(queue)
            );
        }
        finally {
            assertionDone.countDown();
        }
    }

    @Test
    public void testTaskExistWhenConsumingBlockedOnFullQueue() throws Exception {
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(1);
        task = task(descriptor(), queue);

        final AtomicReference<Consumer<String>> hook = hookStream();

        final List<TestEvent> events = events(5);

        final CountDownLatch consumeDone = new CountDownLatch(1);
        final CountDownLatch ignoredDone = new CountDownLatch(1);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                consume(hook.get(), events.subList(0, 1));
                consumeDone.countDown();
                consume(hook.get(), events.subList(1, 5));
                ignoredDone.countDown();
                return null;
            }
        })
        .when(stream).read(Matchers.<Optional<StartPosition>>any());

        Future<?> future = readThread.submit(task);

        assertTrue(consumeDone.await(10, TimeUnit.SECONDS));

        task.stop();

        assertTrue(ignoredDone.await(10, TimeUnit.SECONDS));

        future.get(10, TimeUnit.SECONDS);

        assertEquals(1, queue.size());
        assertEquals(events.get(0).json, queue.remove());
    }

    @Test
    public void testStopPreventsFurtherStreamRead() throws Exception {
        task = task(descriptor(), new LinkedBlockingQueue<String>());

        final CountDownLatch streamRequested = new CountDownLatch(1);
        final CountDownLatch stopped = new CountDownLatch(1);
        when(supplier.get(Matchers.<StreamQueryDescriptor>any(), Matchers.<AsyncHttpClient>any(), Matchers.<Consumer<String>>any()))
                .thenAnswer(new Answer<StreamConnection>() {
                    @Override
                    public StreamConnection answer(InvocationOnMock invocation) throws Throwable {
                        streamRequested.countDown();
                        stopped.await();
                        return stream;
                    }
                });

        Future<?> future = readThread.submit(task);

        assertTrue(streamRequested.await(10, TimeUnit.SECONDS));

        task.stop();

        stopped.countDown();

        future.get(10, TimeUnit.SECONDS);

        verify(stream, never()).read(Matchers.<Optional<StartPosition>>any());
    }

    private void consume(Consumer<String> consumer, List<TestEvent> events) {
        for (TestEvent event : events) {
            consumer.accept(event.json);
        }
    }

    @SuppressWarnings("unchecked")
    private AtomicReference<Consumer<String>> hookStream() {
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

                    return stream;
                }
            });

        return hook;
    }

    private List<TestEvent> events(int count) {
        List<TestEvent> events = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            JsonObject o = new JsonObject();
            final String offset = Integer.toString(i);
            o.addProperty("offset", offset);

            String json = GsonUtil.getGson().toJson(o);
            events.add(new TestEvent(offset, json));
        }

        return events;
    }

    private List<String> reduce(List<TestEvent> events) {
        List<String> reduced = new ArrayList<>();
        for (TestEvent event : events) {
            reduced.add(event.json);
        }

        return reduced;
    }

    private StreamConsumeTask task(StreamQueryDescriptor descriptor, BlockingQueue<String> queue) {
        return StreamConsumeTask.newBuilder()
                .setStreamQueryDescriptor(descriptor)
                .setStreamConnectionSupplier(supplier)
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

    private static class TestEvent {

        private final String offset;
        private final String json;

        public TestEvent(String offset, String json) {
            this.offset = offset;
            this.json = json;
        }
    }
}
