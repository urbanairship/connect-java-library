/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.offsets;

import com.google.common.base.Optional;

import java.util.concurrent.atomic.AtomicReference;

public class InMemOffsetManager implements OffsetManager {

    private final AtomicReference<String> offset = new AtomicReference<>(null);

    public InMemOffsetManager() {}

    @Override
    public Optional<String> getLastOffset() {
        return Optional.fromNullable(offset.get());
    }

    @Override
    public void update(String offset) {
        this.offset.set(offset);
    }
}
