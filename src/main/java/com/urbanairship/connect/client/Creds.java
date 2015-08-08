package com.urbanairship.connect.client;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

/**
 * Creds contains app auth info.
 */
public final class Creds {

    private final String appKey;
    private final String secret;

    /**
     * Creds Builder
     * @return Builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    private Creds(String appKey, String secret) {
        this.appKey = appKey;
        this.secret = secret;
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
     * Get the app secret.
     * @return String
     */
    public String getSecret() {
        return secret;
    }

    public static final class Builder {

        private String appKey;
        private String secret;

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
         * Set the app secret.
         *
         * @param secret String
         * @return Builder
         */
        public Builder setSecret(String secret) {
            this.secret = secret;
            return this;
        }

        /**
         * Build the Creds object.
         *
         * Will fail if either of the two
         * preconditions are not met.
         * <pre>
         * 1. App key must be set.
         * 2. App secret must be set.
         * </pre>
         *
         * @return Creds
         */
        public Creds build() {
            Preconditions.checkArgument(StringUtils.isNotBlank(appKey));
            Preconditions.checkArgument(StringUtils.isNotBlank(secret));

            return new Creds(appKey, secret);
        }
    }
}
