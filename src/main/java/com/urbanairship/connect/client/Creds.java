/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import java.util.Objects;

/**
 * Creds contains app auth info.
 */
public final class Creds {

    private final String appKey;
    private final String token;

    /**
     * Creds Builder
     * @return Builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    private Creds(String appKey, String token) {
        this.appKey = appKey;
        this.token = token;
    }

    /**
     * Get the appkey.
     *
     * @return String
     */
    public String getAppKey() {
        return appKey;
    }

    /**
     * Get the app token.
     * @return String
     */
    public String getToken() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Creds)) return false;

        Creds creds = (Creds) o;

        return Objects.equals(appKey, creds.appKey) &&
            Objects.equals(token, creds.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appKey, token);
    }

    public static final class Builder {

        private String appKey;
        private String token;

        private Builder() { }

        /**
         * Set the appkey.
         *
         * @param appKey String
         * @return Builder
         */
        public Builder setAppKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        /**
         * Set the app token.
         *
         * @param token String
         * @return Builder
         */
        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        /**
         * Build the Creds object.
         *
         * Will fail if either of the two
         * preconditions are not met.
         * <pre>
         * 1. App key must be set.
         * 2. App token must be set.
         * </pre>
         *
         * @return Creds
         */
        public Creds build() {
            Preconditions.checkArgument(StringUtils.isNotBlank(appKey), "App key is required");
            Preconditions.checkArgument(StringUtils.isNotBlank(token), "Non-blank token value is required");

            return new Creds(appKey, token);
        }
    }
}
