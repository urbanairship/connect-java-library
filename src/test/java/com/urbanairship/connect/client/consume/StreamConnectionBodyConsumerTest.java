/*
Copyright 2015-2022 Airship and Contributors
*/

package com.urbanairship.connect.client.consume;

import com.google.common.base.Joiner;
import com.urbanairship.connect.java8.Consumer;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verifyZeroInteractions;

public class StreamConnectionBodyConsumerTest {

    @Mock private Consumer<String> handler;

    private MobileEventStreamBodyConsumer consumer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        consumer = new MobileEventStreamBodyConsumer(handler);
    }

    @Test
    public void testConsume() throws Exception {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add(RandomStringUtils.randomAlphanumeric(1 + RandomUtils.nextInt(0, 15)));
        }

        final List<String> received = new ArrayList<>();
        Answer handlerAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                String line = (String) invocation.getArguments()[0];
                received.add(line);
                return null;
            }
        };

        doAnswer(handlerAnswer).when(handler).accept(anyString());

        String everything = Joiner.on("\n").join(strings) + "\n";
        ByteBuffer buffer = ByteBuffer.wrap(everything.getBytes(UTF_8));

        while (buffer.remaining() > 0) {
            int r = 1 + RandomUtils.nextInt(0, 7);
            int length = Math.min(r, buffer.remaining());

            byte[] chunk = new byte[length];
            buffer.get(chunk);

            consumer.accept(chunk);
        }

        assertEquals(strings, received);
    }

    @Test
    public void testEmptyBytes() throws Exception {
        // Just shouldn't blow up
        consumer.accept(new byte[0]);
    }

    @Test
    public void testEmptyLines() throws Exception {
        byte[] bytes = "\n\n".getBytes(UTF_8);

        consumer.accept(bytes);

        verifyZeroInteractions(handler);
    }
}