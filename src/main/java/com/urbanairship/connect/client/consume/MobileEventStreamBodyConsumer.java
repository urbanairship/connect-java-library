/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.consume;

import com.urbanairship.connect.java8.Consumer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Receives raw bytes from a stream and handles grouping them together into full lines and thus full events.
 */
public final class MobileEventStreamBodyConsumer implements Consumer<byte[]> {

    private static final byte[] EMPTY = new byte[0];

    private final Consumer<String> eventHandler;

    private byte[] remaining = EMPTY;

    public MobileEventStreamBodyConsumer(Consumer<String> eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public void accept(byte[] bodyContent) {
        byte[] available;
        // If we have a chunk left over from previous call(s), prepend it to the latest chunk
        if (remaining.length > 0) {
            int totalSize = remaining.length + bodyContent.length;
            available = new byte[totalSize];
            System.arraycopy(remaining, 0, available, 0, remaining.length);
            System.arraycopy(bodyContent, 0, available, remaining.length, bodyContent.length);
        }
        else {
            available = bodyContent;
        }

        // Search for occurrences of new lines in the bytes
        List<Integer> newLineIndexes = new ArrayList<>();
        for (int i = 0; i < available.length; i++) {
            if ('\n' == available[i]) {
                newLineIndexes.add(i);
            }
        }

        // Pass out the lines we've discovered in the bytes we have
        int rollingIdx = 0;
        for (Integer idx : newLineIndexes) {
            int lineBytesLength  = (idx - rollingIdx);
            if (lineBytesLength > 0) {
                String line = new String(available, rollingIdx, lineBytesLength, StandardCharsets.UTF_8);
                eventHandler.accept(line);
            }

            rollingIdx += lineBytesLength + 1;
        }

        // Capture any remaining bytes that were not handed out as part of a full line
        byte[] newRemaining = EMPTY;
        if (rollingIdx < available.length) {
            int remainingLength = (available.length - rollingIdx);
            newRemaining = new byte[remainingLength];
            System.arraycopy(available, rollingIdx, newRemaining, 0, remainingLength);
        }

        remaining = newRemaining;
    }
}
