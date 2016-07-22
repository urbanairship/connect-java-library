/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client;

import com.google.common.base.Optional;
import com.ning.http.client.AsyncHttpClient;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StreamConsumeTaskTest {

    private StreamConsumeTask task;

    @Mock private Consumer<String> consumer;

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
        StreamQueryDescriptor descriptor = descriptor();

        task = task(descriptor);

        final CountDownLatch readCalled = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                readCalled.countDown();
                release.await();
                return null;
            }
        })
        .doNothing()
        .when(stream).read(Matchers.<Optional<StartPosition>>any());

        readThread.submit(task);

        assertTrue(readCalled.await(10, TimeUnit.SECONDS));

        release.countDown();

        task.stop();

        verify(supplier, atLeastOnce()).get(eq(descriptor), Matchers.<AsyncHttpClient>any(), eq(consumer));
        verify(stream, atLeastOnce()).read(Optional.<StartPosition>absent());
    }

    @Test
    public void testRetriesOnException() throws Exception {
        StreamQueryDescriptor descriptor = descriptor();
        task = task(descriptor);

        final CountDownLatch done = new CountDownLatch(1);

        doThrow(new RuntimeException("fail once"))
        .doThrow(new RuntimeException("fail twice"))
        .doThrow(new RuntimeException("fail thrice"))
        .doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                done.countDown();
                return null;
            }
        })
        .when(stream).read(Matchers.<Optional<StartPosition>>any());

        readThread.submit(task);

        assertTrue(done.await(10, TimeUnit.SECONDS));

        task.stop();
    }

    @Test
    public void testConnectionException() throws Exception {
        task = task(descriptor());

        doThrow(new ConnectionException("boom")).when(stream).read(Matchers.<Optional<StartPosition>>any());

        Future<?> future = readThread.submit(task);

        Throwable failure = null;
        try {
            future.get(10, TimeUnit.SECONDS);
        }
        catch (ExecutionException e) {
            failure = e.getCause();
        }

        assertNotNull(failure);
        assertTrue(failure instanceof ConnectionException);
    }

    private StreamConsumeTask task(StreamQueryDescriptor descriptor) {
        return StreamConsumeTask.newBuilder()
                .setStreamQueryDescriptor(descriptor)
                .setStreamSupplier(supplier)
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
