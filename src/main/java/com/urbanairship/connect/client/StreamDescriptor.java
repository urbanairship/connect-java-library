package com.urbanairship.connect.client;

import com.urbanairship.connect.client.model.Subset;
import com.urbanairship.connect.client.model.filters.Filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Object containing app credentials and API request details for the {@link com.urbanairship.connect.client.MobileEventStream}.
 */
public final class StreamDescriptor {

    private final Creds creds;
    private final Optional<String> offset;
    private final Optional<Set<Filter>> filters;
    private final Optional<Subset> subset;

    /**
     * StreamDescriptor builder
     * @return Builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    private StreamDescriptor(Creds creds, Optional<String> offset, Optional<Set<Filter>> filters, Optional<Subset> subset) {
        this.creds = creds;
        this.offset = offset;
        this.filters = filters;
        this.subset = subset;
    }

    /**
     * Get the app creds.
     *
     * @return the app creds.
     */
    public Creds getCreds() {
        return creds;
    }

    /**
     * Get the stream offset.
     *
     * @return offset
     */
    public Optional<String> getOffset() {
        return offset;
    }

    /**
     * Get the request filters.
     *
     * @return set of filters.
     */
    public Optional<Set<Filter>> getFilters() {
        return filters;
    }

    /**
     * Get the request subset.
     *
     * @return subset.
     */
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
        private String offset = null;
        private Set<Filter> filters = null;
        private Subset subset = null;

        private Builder() {}

        /**
         * Sets the app creds.
         *
         * @param value Creds
         * @return Builder
         */
        public Builder setCreds(Creds value) {
            this.creds = value;
            return this;
        }

        /**
         * Set the stream offset.
         *
         * @param value String
         * @return Builder
         */
        public Builder setOffset(String value) {
            this.offset = value;
            return this;
        }

        /**
         * Add a single filter.
         *
         * @param value Filter
         * @return Builder
         */
        public Builder addFilter(Filter value) {
            return addFilters(value);
        }

        /**
         * Add multiple filters.
         *
         * @param value Filter...
         * @return Builder
         */
        public Builder addFilters(Filter... value) {
            return addFilters(new HashSet<>(Arrays.asList(value)));
        }

        /**
         * Add a Collection of filters.
         *
         * @param value Collection<>Filters</>
         * @return Builder
         */
        public Builder addFilters(Collection<Filter> value) {
            if (this.filters == null) {
                this.filters = new HashSet<>();
            }

            this.filters.addAll(value);
            return this;
        }

        /**
         * Set the subset.
         *
         * @param value Subset
         * @return Builder
         */
        public Builder setSubset(Subset value) {
            this.subset = value;
            return this;
        }

        /**
         * Builder a StreamDescriptor object.
         * @return StreamDescriptor
         */
        public StreamDescriptor build() {
            return new StreamDescriptor(creds, Optional.ofNullable(offset), Optional.ofNullable(filters), Optional.ofNullable(subset));
        }
    }
}
