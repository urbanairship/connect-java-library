package com.urbanairship.connect.client;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.proxy.ProxyServer;

public class AirshipRequestClient implements RequestClient {
    private final AsyncHttpClient httpClient;

    private AirshipRequestClient(Builder builder) {
        httpClient = new DefaultAsyncHttpClient(builder.asyncConfigBuilder.build());
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public AsyncHttpClient getRequestClient() {
        return httpClient;
    }

    public static class Builder {
        DefaultAsyncHttpClientConfig.Builder asyncConfigBuilder = HttpClientUtil.defaultHttpClientConfigBuilder();

        public Builder setProxyServer(ProxyServer proxyServer) {
            asyncConfigBuilder.setProxyServer(proxyServer);
            return this;
        }

        public AirshipRequestClient build() {
            return new AirshipRequestClient(this);
        }
    }
}
