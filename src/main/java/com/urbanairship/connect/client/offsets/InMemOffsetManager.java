package com.urbanairship.connect.client.offsets;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class InMemOffsetManager implements OffsetManager {

    private final AtomicReference<String> offset = new AtomicReference<>(null);

    public InMemOffsetManager() {}

    @Override
    public Optional<String> getLastOffset() {
        return Optional.ofNullable(offset.get());
    }

    @Override
    public void update(String offset) {
        this.offset.set(offset);
    }
}
