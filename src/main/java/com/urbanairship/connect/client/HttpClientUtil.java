/*
Copyright 2015-2022 Airship and Contributors
*/

package com.urbanairship.connect.client;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

import java.util.concurrent.TimeUnit;

public final class HttpClientUtil {

    public static final int HTTP_CONNECT_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10L);
    public static final int HTTP_READ_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(5L);

    private HttpClientUtil() {}

    /**
     * Builds an AsyncHttpClient with default settings
     * The setMaxRequestRetry will be handled by the StreamConsumeTask.
     * Allowing the Aysnc client to handle the re-request will result in the same start offset being requested,
     * even if the connection has been processing for hours.
     * @return An AsyncHttpClient instance.
     */
    public static AsyncHttpClient defaultHttpClient() {
        AsyncHttpClientConfig clientConfig = new DefaultAsyncHttpClientConfig.Builder()
            .setConnectTimeout(HTTP_CONNECT_TIMEOUT)
            .setReadTimeout(HTTP_READ_TIMEOUT)
            .setUserAgent("UA Java Connect Client")
            .setRequestTimeout(-1)
            .setMaxRequestRetry(0)
            .build();

        return new DefaultAsyncHttpClient(clientConfig);
    }

    public static DefaultAsyncHttpClientConfig.Builder defaultHttpClientConfigBuilder() {
        DefaultAsyncHttpClientConfig.Builder clientConfigBuilder = new DefaultAsyncHttpClientConfig.Builder()
                .setConnectTimeout(HttpClientUtil.HTTP_CONNECT_TIMEOUT)
                .setReadTimeout(HttpClientUtil.HTTP_READ_TIMEOUT)
                .setUserAgent("UA Java Connect Client")
                .setRequestTimeout(-1)
                .setMaxRequestRetry(0);

        return clientConfigBuilder;
    }
}
