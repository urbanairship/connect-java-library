package com.urbanairship.connect.client;

import com.urbanairship.connect.client.model.Subset;
import com.urbanairship.connect.client.model.filters.Filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class StreamDescriptor {

    private final Creds creds;
    private final Optional<Long> offset;
    private final Optional<Set<Filter>> filters;
    private final Optional<Subset> subset;

    public static Builder newBuilder() {
        return new Builder();
    }

    private StreamDescriptor(Creds creds, Optional<Long> offset, Optional<Set<Filter>> filters, Optional<Subset> subset) {
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

    public Optional<Set<Filter>> getFilters() {
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
        private Set<Filter> filters = null;
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

        public Builder addFilter(Filter value) {
            return addFilters(value);
        }

        public Builder addFilters(Filter... value) {
            return addFilters(new HashSet<>(Arrays.asList(value)));
        }

        public Builder addFilters(Collection<Filter> value) {
            if (this.filters == null) {
                this.filters = new HashSet<>();
            }

            this.filters.addAll(value);
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
