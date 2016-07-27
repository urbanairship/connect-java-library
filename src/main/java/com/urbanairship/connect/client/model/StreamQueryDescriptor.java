/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.urbanairship.connect.client.StreamConnection;
import com.urbanairship.connect.client.model.request.Subset;
import com.urbanairship.connect.client.model.request.filters.Filter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Object containing app credentials and API request details for the {@link StreamConnection}.
 */
public final class StreamQueryDescriptor {

    private final Creds creds;
    private final Set<Filter> filters;
    private final Optional<Subset> subset;

    /**
     * StreamDescriptor builder
     * @return Builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    private StreamQueryDescriptor(Creds creds, Set<Filter> filters, Optional<Subset> subset) {
        this.creds = creds;
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
     * Get the request filters.
     *
     * @return set of filters.
     */
    public Set<Filter> getFilters() {
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
        StreamQueryDescriptor that = (StreamQueryDescriptor) o;
        return Objects.equals(creds, that.creds) &&
                Objects.equals(filters, that.filters) &&
                Objects.equals(subset, that.subset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(creds, filters, subset);
    }

    public static final class Builder {

        private final Set<Filter> filters = new HashSet<>();

        private Creds creds = null;
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
         * Add multiple filters.
         *
         * @param filters Filter...
         * @return Builder
         */
        public Builder addFilters(Filter... filters) {
            Collections.addAll(this.filters, filters);
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
        public StreamQueryDescriptor build() {
            Preconditions.checkNotNull(creds, "Creds object must be provided");

            return new StreamQueryDescriptor(creds, ImmutableSet.copyOf(filters), Optional.fromNullable(subset));
        }
    }
}
