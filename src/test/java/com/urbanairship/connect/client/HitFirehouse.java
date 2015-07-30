package com.urbanairship.connect.client;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class HitFirehouse {

    public static void main(String[] args) throws Exception {
        AsyncHttpClientConfig clientConfig = new AsyncHttpClientConfig.Builder()
                .setConnectTimeout((int) TimeUnit.SECONDS.toMillis(10L))
                .setReadTimeout((int) TimeUnit.SECONDS.toMillis(10L))
                .setUserAgent("Wildwood Ingress Client")
                .setRequestTimeout(-1)
                .setAllowPoolingConnections(false)
                .setAllowPoolingSslConnections(false)
                .build();

        AsyncHttpClient client = new AsyncHttpClient(clientConfig);

        StreamDescriptor descriptor = StreamDescriptor.newBuilder()
            .setCreds(Creds.newBuilder()
                .setAppKey("wat")
                .setSecret("wat")
                .build())
                .build();


        Consumer<String> consumer = s -> {};//System.out.println("Event: " + s);

        try (MobileEventStream stream = new MobileEventStream(descriptor, client, consumer, "http://localhost:5555/api/events/")) {
            stream.connect(2, TimeUnit.SECONDS);
            stream.consume(5, TimeUnit.MINUTES);
        }
        finally {
            client.close();
        }
    }
}
