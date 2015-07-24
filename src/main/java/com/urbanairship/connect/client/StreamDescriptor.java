package com.urbanairship.connect.client;

import java.util.Objects;
import java.util.Optional;

public final class StreamDescriptor {

    private final Creds creds;
    private final Optional<Long> offset;

    public StreamDescriptor(Creds creds, Optional<Long> offset) {
        this.creds = creds;
        this.offset = offset;
    }

    public Creds getCreds() {
        return creds;
    }

    public Optional<Long> getOffset() {
        return offset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StreamDescriptor that = (StreamDescriptor) o;
        return Objects.equals(creds, that.creds) &&
                Objects.equals(offset, that.offset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(creds, offset);
    }
}
