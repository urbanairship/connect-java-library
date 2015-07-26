package com.urbanairship.connect.client;

import com.urbanairship.connect.client.filters.Filter;

import java.util.Objects;
import java.util.Optional;

public final class StreamDescriptor {

    private final Creds creds;
    private final Optional<Long> offset;
    private final Optional<Filter> filters;

    public StreamDescriptor(Creds creds, Optional<Long> offset, Optional<Filter> filters) {
        this.creds = creds;
        this.offset = offset;
        this.filters = filters;
    }

    public Creds getCreds() {
        return creds;
    }

    public Optional<Long> getOffset() {
        return offset;
    }

    public Optional<Filter> getFilters() {
        return filters;
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
                Objects.equals(offset, that.offset) &&
                Objects.equals(filters, that.filters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(creds, offset, filters);
    }
}
