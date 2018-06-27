/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

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
        AsyncHttpClientConfig clientConfig = new AsyncHttpClientConfig.Builder()
            .setConnectTimeout(HTTP_CONNECT_TIMEOUT)
            .setReadTimeout(HTTP_READ_TIMEOUT)
            .setUserAgent("UA Java Connect Client")
            .setRequestTimeout(-1)
            .setAllowPoolingConnections(false)
            .setAllowPoolingSslConnections(false)
            .setMaxRequestRetry(0)
            .build();

        return new AsyncHttpClient(clientConfig);
    }
}
