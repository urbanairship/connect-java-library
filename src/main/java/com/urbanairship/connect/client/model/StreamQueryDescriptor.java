/*
Copyright 2022 Airship and Contributors
*/

package com.urbanairship.connect.client.model;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.urbanairship.connect.client.Constants;
import com.urbanairship.connect.client.StreamConnection;
import com.urbanairship.connect.client.model.request.Subset;
import com.urbanairship.connect.client.model.request.filters.Filter;
import org.apache.commons.lang3.StringUtils;

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
    private final Optional<Boolean> offsetUpdatesEnabled;
    private final String endpointUrl;

    /**
     * StreamDescriptor builder
     * @return Builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    private StreamQueryDescriptor(Creds creds, Set<Filter> filters, Optional<Subset> subset, Optional<Boolean> offsetUpdatesEnabled, String endpointUrl) {
        this.creds = creds;
        this.filters = filters;
        this.subset = subset;
        this.offsetUpdatesEnabled = offsetUpdatesEnabled;
        this.endpointUrl = endpointUrl;
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

    /**
     * Get offset update enabled status
     *
     * @return Absent if no override is specified otherwise true if offset update events are enabled, false otherwise
     */
    public Optional<Boolean> offsetUpdatesEnabled() {
        return offsetUpdatesEnabled;
    }

    /**
     * Get Configured endpoint URL
     *
     * @return endpoint URL
     */
    public String getEndpointUrl() {
        return endpointUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreamQueryDescriptor that = (StreamQueryDescriptor) o;
        return Objects.equals(creds, that.creds) &&
                Objects.equals(filters, that.filters) &&
                Objects.equals(subset, that.subset) &&
                Objects.equals(offsetUpdatesEnabled, that.offsetUpdatesEnabled) &&
                Objects.equals(endpointUrl, that.endpointUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(creds, filters, subset, offsetUpdatesEnabled, endpointUrl);
    }

    public static final class Builder {

        private final Set<Filter> filters = new HashSet<>();

        private Creds creds = null;
        private Subset subset = null;
        private Optional<Boolean> offsetUpdatesEnabled = Optional.absent();
        private String endpointUrl = Constants.API_URL;

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
         * Enable offset updates
         *
         * @return Builder
         */
        public Builder enableOffsetUpdates() {
            this.offsetUpdatesEnabled = Optional.of(true);
            return this;
        }

        /**
         * Disable offset updates
         *
         * @return Builder
         */
        public Builder disableOffsetUpdates() {
            this.offsetUpdatesEnabled = Optional.of(false);
            return this;
        }

        public Builder setEndpointUrl(String url) {
            this.endpointUrl = url;
            return this;
        }

        /**
         * Builder a StreamDescriptor object.
         * @return StreamDescriptor
         */
        public StreamQueryDescriptor build() {
            Preconditions.checkNotNull(creds, "Creds object must be provided");
            Preconditions.checkArgument(StringUtils.isNotBlank(endpointUrl), "Endpoint URL must not be null");

            return new StreamQueryDescriptor(
                    creds,
                    ImmutableSet.copyOf(filters),
                    Optional.fromNullable(subset),
                    offsetUpdatesEnabled,
                    endpointUrl);
        }
    }
}
