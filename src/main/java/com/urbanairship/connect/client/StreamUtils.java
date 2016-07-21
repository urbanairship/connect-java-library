/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

/**
 * A class of util methods for {@link MobileEventConsumerService}.
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

//    public static boolean connectWithRetries(MobileEventStream stream, ConnectClientConfiguration config, String appKey) throws InterruptedException {
//
//        int baseConnectionAttempts = 0;
//        long backoff = config.mesReconnectBackoffTime;
//
//        for (int connectionAttempts = baseConnectionAttempts; connectionAttempts < config.maxConnectionAttempts; connectionAttempts++) {
//
//            try {
//                // have the thread sleep before trying to reconnect
//                if (connectionAttempts > baseConnectionAttempts) {
//                    Thread.sleep(backoff);
//                }
//
//                // update the backoff value for each connection attempt
//                backoff += backoff * connectionAttempts;
//                if (backoff > config.maxConnectionAttempts * config.mesReconnectBackoffTime) {
//                    backoff = config.maxConnectionAttempts * config.mesReconnectBackoffTime;
//                }
//
//                return stream.connect(config.appStreamConnectTimeout, TimeUnit.MILLISECONDS);
//            } catch (InterruptedException e) {
//                throw e;
//            } catch (Throwable throwable) {
//                log.error("Error encountered while connecting to stream for app " + appKey, throwable);
//            }
//        }
//
//        // if the reconnection retry limit is reached without connection, exit.
//        log.error("Handler failed to reconnect after " + config.maxConnectionAttempts + " attempts, exiting.");
//        return false;
//    }
}
