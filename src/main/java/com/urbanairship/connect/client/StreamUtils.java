package com.urbanairship.connect.client;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.urbanairship.connect.client.offsets.OffsetManager;

/**
 * A class of util methods for {@link com.urbanairship.connect.client.StreamHandler}.
 */
public class StreamUtils {

    public StreamUtils() {}

    /**
     * Unpacks the original {@link com.urbanairship.connect.client.StreamDescriptor} passed into the
     * {@link com.urbanairship.connect.client.StreamHandler} and creates a new one with an updated offset.
     *
     * @param baseStreamDescriptor The base StreamDescriptor.
     * @param offsetManager The OffsetManager.
     * @return StreamDescriptor with updated offset.
     */
    public static StreamDescriptor buildNewDescriptor(StreamDescriptor baseStreamDescriptor, OffsetManager offsetManager) {
        Creds creds = baseStreamDescriptor.getCreds();

        StreamDescriptor.Builder builder = StreamDescriptor.newBuilder();
        builder.setCreds(creds);
        if (baseStreamDescriptor.getFilters().isPresent()) {
            builder.addFilters(baseStreamDescriptor.getFilters().get());
        }

        if (baseStreamDescriptor.getSubset().isPresent()) {
            builder.setSubset(baseStreamDescriptor.getSubset().get());
        }

        if (offsetManager.getLastOffset().isPresent()) {
            builder.setOffset(offsetManager.getLastOffset().get());
        }

        return builder.build();
    }

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
                // We'll handle this ourselves and looks like -1 is how you turn if off?
            .setRequestTimeout(-1)
            .setAllowPoolingConnections(false)
            .setAllowPoolingSslConnections(false)
            .build();

        return new AsyncHttpClient(clientConfig);
    }
}
