package com.urbanairship.connect.client;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

public final class Creds {

    private final String appKey;
    private final String secret;

    public static Builder newBuilder() {
        return new Builder();
    }

    private Creds(String appKey, String secret) {
        this.appKey = appKey;
        this.secret = secret;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getSecret() {
        return secret;
    }

    public static final class Builder {

        private String appKey;
        private String secret;

        private Builder() { }

        public Builder setAppKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        public Builder setSecret(String secret) {
            this.secret = secret;
            return this;
        }

        public Creds build() {
            Preconditions.checkArgument(StringUtils.isNotBlank(appKey));
            Preconditions.checkArgument(StringUtils.isNotBlank(secret));

            return new Creds(appKey, secret);
        }
    }
}
