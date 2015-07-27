package com.urbanairship.connect.client;

import com.urbanairship.connect.client.filters.Filter;

import java.util.Objects;
import java.util.Optional;

public final class StreamDescriptor {

    private final Creds creds;
    private final Optional<Long> offset;
    private final Optional<Filter> filters;
    private final Optional<Subset> subset;

    public static Builder newBuilder() {
        return new Builder();
    }

    private StreamDescriptor(Creds creds, Optional<Long> offset, Optional<Filter> filters, Optional<Subset> subset) {
        this.creds = creds;
        this.offset = offset;
        this.filters = filters;
        this.subset = subset;
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

    public Optional<Subset> getSubset() {
        return subset;
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
                Objects.equals(filters, that.filters) &&
                Objects.equals(subset, that.subset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(creds, offset, filters, subset);
    }

    public static final class Builder {
        private Creds creds;
        private Long offset = null;
        private Filter filters = null;
        private Subset subset = null;

        private Builder() {}

        public Builder setCreds(Creds value) {
            this.creds = value;
            return this;
        }

        public Builder setOffset(Long value) {
            this.offset = value;
            return this;
        }

        public Builder setFilters(Filter value) {
            this.filters = value;
            return this;
        }

        public Builder setSubset(Subset value) {
            this.subset = value;
            return this;
        }

        public StreamDescriptor build() {
            return new StreamDescriptor(creds, Optional.ofNullable(offset), Optional.ofNullable(filters), Optional.ofNullable(subset));
        }
    }
}
