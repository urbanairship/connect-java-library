package com.urbanairship.connect.client.consume;

import com.google.common.base.Supplier;
import com.urbanairship.connect.java8.Consumer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class FullBodyConsumer implements Consumer<byte[]>, Supplier<String> {

    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    @Override
    public void accept(byte[] data) {
        try {
            bytes.write(data);
        }
        catch (IOException e) {
            throw new RuntimeException("Error writing bytes to memory", e);
        }
    }

    @Override
    public String get() {
        return bytes.size() == 0
                ? ""
                : new String(bytes.toByteArray(), StandardCharsets.UTF_8);

    }
}
