package com.urbanairship.connect.client.offsets;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class InMemOffsetManager implements OffsetManager {

    private final AtomicReference<Long> offset = new AtomicReference<>(null);

    public InMemOffsetManager() {}

    @Override
    public Optional<Long> getLastOffset() {
        return Optional.ofNullable(offset.get());
    }

    @Override
    public void update(Long offset) {
        this.offset.set(offset);
    }
}
