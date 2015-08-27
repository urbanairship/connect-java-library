package com.urbanairship.connect.client;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.urbanairship.connect.client.offsets.OffsetManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * A class of util methods for {@link MobileEventConsumerService}.
 */
public class StreamUtils {

    private static final Logger log = LogManager.getLogger(StreamUtils.class);

    public StreamUtils() {}

    /**
     * Unpacks the original {@link StreamQueryDescriptor} passed into the
     * {@link MobileEventConsumerService} and creates a new one with an updated offset.
     *
     * @param baseStreamQueryDescriptor The base StreamDescriptor.
     * @param offsetManager The OffsetManager.
     * @return StreamDescriptor with updated offset.
     */
    public static StreamQueryDescriptor buildNewDescriptor(StreamQueryDescriptor baseStreamQueryDescriptor, OffsetManager offsetManager) {
        Creds creds = baseStreamQueryDescriptor.getCreds();

        StreamQueryDescriptor.Builder builder = StreamQueryDescriptor.newBuilder();
        builder.setCreds(creds);
        if (baseStreamQueryDescriptor.getFilters().isPresent()) {
            builder.addFilters(baseStreamQueryDescriptor.getFilters().get());
        }

        if (baseStreamQueryDescriptor.getSubset().isPresent()) {
            builder.setSubset(baseStreamQueryDescriptor.getSubset().get());
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

    public static boolean connectWithRetries(MobileEventStream stream, ConnectClientConfiguration config, String appKey) throws InterruptedException {

        int baseConnectionAttempts = 0;
        long backoff = config.mesReconnectBackoffTime;

        for (int connectionAttempts = baseConnectionAttempts; connectionAttempts < config.maxConnectionAttempts; connectionAttempts++) {

            try {
                // have the thread sleep before trying to reconnect
                if (connectionAttempts > baseConnectionAttempts) {
                    Thread.sleep(backoff);
                }

                // update the backoff value for each connection attempt
                backoff += backoff * connectionAttempts;
                if (backoff > config.maxConnectionAttempts * config.mesReconnectBackoffTime) {
                    backoff = config.maxConnectionAttempts * config.mesReconnectBackoffTime;
                }

                stream.connect(config.appStreamConnectTimeout, TimeUnit.MILLISECONDS);
                return true;
            } catch (InterruptedException e) {
                throw e;
            } catch (Throwable throwable) {
                log.error("Error encountered while connecting to stream for app " + appKey, throwable);
            }
        }

        // if the reconnection retry limit is reached without connection, exit.
        log.error("Handler failed to reconnect after " + config.maxConnectionAttempts + " attempts, exiting.");
        return false;
    }
}
