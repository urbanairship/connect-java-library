/*
Copyright 2015-2022 Airship and Contributors
*/

package com.urbanairship.connect.client.consume;

import com.google.common.base.Optional;
import com.urbanairship.connect.java8.Consumer;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.commons.lang3.RandomStringUtils;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.HttpURLConnection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StreamConnectionResponseHandlerTest {

    private MobileEventStreamResponseHandler handler;

    @Mock private Consumer<byte[]> receiver;
    @Mock private ConnectCallback connectCallback;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        handler = new MobileEventStreamResponseHandler(connectCallback);
    }

    @Test
    public void testExceptionBeforeConnected() throws Exception {
        RuntimeException exception = new RuntimeException("boom");

        handler.onThrowable(exception);

        verify(connectCallback).error(exception);
    }

    @Test
    public void testConnectFlow() throws Exception {
        int code = HttpURLConnection.HTTP_OK;
        String message = RandomStringUtils.randomAlphabetic(20);
        HttpResponseStatus status = mock(HttpResponseStatus.class);
        when(status.getStatusCode()).thenReturn(code);
        when(status.getStatusText()).thenReturn(message);

        AsyncHandler.State state = handler.onStatusReceived(status);
        assertEquals(AsyncHandler.State.CONTINUE, state);

        HttpHeaders headers = mock(HttpHeaders.class);
        List<Map.Entry<String, String>> entryList = new ArrayList<>();
        entryList.add(new AbstractMap.SimpleEntry<String, String>(RandomStringUtils.randomAlphanumeric(5), RandomStringUtils.randomAlphabetic(10)));
        entryList.add(new AbstractMap.SimpleEntry<String, String>(RandomStringUtils.randomAlphanumeric(5), RandomStringUtils.randomAlphabetic(10)));

        Map<String, String> entries = new HashMap<>();
        for (Map.Entry<String, String> entry : entryList) {
            entries.put(entry.getKey(), entry.getValue());
        }

        when(headers.entries()).thenReturn(entryList);
        state = handler.onHeadersReceived(headers);
        assertEquals(AsyncHandler.State.CONTINUE, state);

        ArgumentCaptor<StatusAndHeaders> captor = ArgumentCaptor.forClass(StatusAndHeaders.class);
        verify(connectCallback).connected(captor.capture());

        StatusAndHeaders received = captor.getValue();

        assertEquals(code, received.getStatusCode());
        assertEquals(message, received.getStatusMessage());
        assertEquals(entries, received.getHeaders());
    }

    @Test
    public void testBodyConsume() throws Exception {
        final HttpResponseBodyPart bodyPart = mock(HttpResponseBodyPart.class);
        byte[] bytes = RandomStringUtils.randomAlphabetic(5).getBytes();
        when(bodyPart.getBodyPartBytes()).thenReturn(bytes);

        ExecutorService thread = Executors.newSingleThreadExecutor();
        Callable<AsyncHandler.State> callable = new Callable<AsyncHandler.State>() {
            @Override
            public AsyncHandler.State call() throws Exception {
                return handler.onBodyPartReceived(bodyPart);
            }
        };

        try {
            Future<AsyncHandler.State> future = thread.submit(callable);

            boolean timedOut = false;
            try {
                future.get(1, TimeUnit.SECONDS);
            }
            catch (TimeoutException e) {
                timedOut = true;
            }

            assertTrue(timedOut);

            handler.consumeBody(receiver);

            AsyncHandler.State result = future.get(1, TimeUnit.SECONDS);

            assertEquals(AsyncHandler.State.CONTINUE, result);
        }
        finally {
            thread.shutdownNow();
        }
    }

    @Test
    public void testBodyConsumeAfterClose() throws Exception {
        handler.consumeBody(receiver);
        handler.stop();

        HttpResponseBodyPart bodyPart = mock(HttpResponseBodyPart.class);
        AsyncHandler.State result = handler.onBodyPartReceived(bodyPart);

        assertEquals(AsyncHandler.State.ABORT, result);
    }

    @Test
    public void testExceptionAfterConnect() throws Exception {
        int code = HttpURLConnection.HTTP_OK;
        String message = RandomStringUtils.randomAlphabetic(20);
        HttpResponseStatus status = mock(HttpResponseStatus.class);
        when(status.getStatusCode()).thenReturn(code);
        when(status.getStatusText()).thenReturn(message);

        HttpHeaders headers = mock(HttpHeaders.class);
        List<Map.Entry<String, String>> entries = new ArrayList<>();
        when(headers.entries()).thenReturn(entries);

        handler.onStatusReceived(status);
        handler.onHeadersReceived(headers);

        Throwable exception = new RuntimeException("boom");
        handler.onThrowable(exception);

        Optional<Throwable> error = handler.getError();

        assertTrue(error.isPresent());
        assertEquals(exception, error.get());
    }

    @Test
    public void testBodyReceivedBeforeBodyConsumeLatchReleasedHandlerClosesProperly() throws Exception {
        int code = HttpURLConnection.HTTP_PAYMENT_REQUIRED;
        String message = RandomStringUtils.randomAlphabetic(20);
        HttpResponseStatus status = mock(HttpResponseStatus.class);
        when(status.getStatusCode()).thenReturn(code);
        when(status.getStatusText()).thenReturn(message);

        HttpHeaders headers = mock(HttpHeaders.class);
        List<Map.Entry<String, String>> entries = new ArrayList<>();
        when(headers.entries()).thenReturn(entries);

        handler.onStatusReceived(status);
        handler.onHeadersReceived(headers);

        final CountDownLatch bodyReceivedThreadStarted = new CountDownLatch(1);
        final CountDownLatch bodyReceivedThreadExit = new CountDownLatch(1);
        final HttpResponseBodyPart part = mock(HttpResponseBodyPart.class);

        ExecutorService bodyReceivedThread = Executors.newSingleThreadExecutor();
        Runnable bodyReceivedRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    bodyReceivedThreadStarted.countDown();
                    handler.onBodyPartReceived(part);
                    bodyReceivedThreadExit.countDown();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        try {
            bodyReceivedThread.submit(bodyReceivedRunnable);

            assertTrue(bodyReceivedThreadStarted.await(10, TimeUnit.SECONDS));

            // Wait just an exta little bit to try to guarantee the body received call gets made...
            Thread.sleep(2000L);

            handler.stop();

            assertTrue(bodyReceivedThreadExit.await(10, TimeUnit.SECONDS));
        }
        finally {
            bodyReceivedThread.shutdownNow();
        }
    }
}
