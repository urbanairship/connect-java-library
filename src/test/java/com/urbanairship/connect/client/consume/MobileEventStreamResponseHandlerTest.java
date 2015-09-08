/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.consume;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.urbanairship.connect.java8.Consumer;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
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

public class MobileEventStreamResponseHandlerTest {

    private MobileEventStreamResponseHandler handler;

    @Mock private Consumer<byte[]> receiver;
    @Mock private ConnectCallback connectCallback;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        handler = new MobileEventStreamResponseHandler(receiver, connectCallback);
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

        AsyncHandler.STATE state = handler.onStatusReceived(status);
        assertEquals(AsyncHandler.STATE.CONTINUE, state);

        HttpResponseHeaders headers = mock(HttpResponseHeaders.class);
        Map<String, Collection<String>> headerMap = ImmutableMap.of(
                RandomStringUtils.randomAlphanumeric(5), ImmutableList.of(RandomStringUtils.randomAlphabetic(10)),
                RandomStringUtils.randomAlphanumeric(5), (Collection<String>) ImmutableList.of(RandomStringUtils.randomAlphabetic(10), RandomStringUtils.randomAlphabetic(5))
        );
        when(headers.getHeaders()).thenReturn(new FluentCaseInsensitiveStringsMap(headerMap));
        state = handler.onHeadersReceived(headers);
        assertEquals(AsyncHandler.STATE.CONTINUE, state);

        ArgumentCaptor<StatusAndHeaders> captor = ArgumentCaptor.forClass(StatusAndHeaders.class);
        verify(connectCallback).connected(captor.capture());

        StatusAndHeaders received = captor.getValue();

        assertEquals(code, received.getStatusCode());
        assertEquals(message, received.getStatusMessage());
        assertEquals(headerMap, received.getHeaders());
    }

    @Test
    public void testBodyConsume() throws Exception {
        final HttpResponseBodyPart bodyPart = mock(HttpResponseBodyPart.class);
        byte[] bytes = RandomStringUtils.randomAlphabetic(5).getBytes();
        when(bodyPart.getBodyPartBytes()).thenReturn(bytes);

        ExecutorService thread = Executors.newSingleThreadExecutor();
        Callable<AsyncHandler.STATE> callable = new Callable<AsyncHandler.STATE>() {
            @Override
            public AsyncHandler.STATE call() throws Exception {
                return handler.onBodyPartReceived(bodyPart);
            }
        };

        try {
            Future<AsyncHandler.STATE> future = thread.submit(callable);

            boolean timedOut = false;
            try {
                future.get(1, TimeUnit.SECONDS);
            }
            catch (TimeoutException e) {
                timedOut = true;
            }

            assertTrue(timedOut);

            handler.consumeBody();

            AsyncHandler.STATE result = future.get(1, TimeUnit.SECONDS);

            assertEquals(AsyncHandler.STATE.CONTINUE, result);
        }
        finally {
            thread.shutdownNow();
        }
    }

    @Test
    public void testBodyConsumeAfterClose() throws Exception {
        handler.consumeBody();
        handler.stop();

        HttpResponseBodyPart bodyPart = mock(HttpResponseBodyPart.class);
        AsyncHandler.STATE result = handler.onBodyPartReceived(bodyPart);

        assertEquals(AsyncHandler.STATE.ABORT, result);
    }

    @Test
    public void testExceptionAfterConnect() throws Exception {
        int code = HttpURLConnection.HTTP_OK;
        String message = RandomStringUtils.randomAlphabetic(20);
        HttpResponseStatus status = mock(HttpResponseStatus.class);
        when(status.getStatusCode()).thenReturn(code);
        when(status.getStatusText()).thenReturn(message);

        HttpResponseHeaders headers = mock(HttpResponseHeaders.class);
        when(headers.getHeaders()).thenReturn(new FluentCaseInsensitiveStringsMap(Collections.<String, Collection<String>>emptyMap()));

        handler.onStatusReceived(status);
        handler.onHeadersReceived(headers);

        Throwable exception = new RuntimeException("boom");
        handler.onThrowable(exception);

        Optional<Throwable> error = handler.getError();

        assertTrue(error.isPresent());
        assertEquals(exception, error.get());
    }
}