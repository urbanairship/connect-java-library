/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

/**
 * A class of util methods for {@link MobileEventConsumeTask}.
 */
public final class StreamUtils {

    private StreamUtils() {}

    /**
     * Builds an AsyncHttpClient.
     *
     * @param config ConnectClientConfiguration with timeout settings.
     * @return An AsyncHttpClient instance.
     */
    public static AsyncHttpClient buildHttpClient(ConnectClientConfiguration config) {
        AsyncHttpClientConfig clientConfig = new AsyncHttpClientConfig.Builder()
            .setConnectTimeout(config.mesHttpConnectTimeout)
            .setReadTimeout(config.mesHttpReadTimeout)
            .setUserAgent("Connect Client")
            .setRequestTimeout(-1)
            .setAllowPoolingConnections(false)
            .setAllowPoolingSslConnections(false)
            .build();

        return new AsyncHttpClient(clientConfig);
    }
}
