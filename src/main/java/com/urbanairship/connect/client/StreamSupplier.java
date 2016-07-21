/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client;

import com.ning.http.client.AsyncHttpClient;
import com.urbanairship.connect.java8.Consumer;

/**
 * Interface for supplying the {@link MobileEventConsumeTask} with
 * a {@link com.urbanairship.connect.client.MobileEventStream} instance.
 */
public interface StreamSupplier {

    /**
     * Get the {@link com.urbanairship.connect.client.MobileEventStream} instance.
     *
     * @param descriptor StreamDescriptor containing the app credentials and request info.
     * @param client AsyncHttpClient
     * @param eventConsumer {@code Consumer<String>} of the API response events.
     * @param url The API URL as a String.
     * @return MobileEventStream instance.
     */
    MobileEventStream get(StreamQueryDescriptor descriptor,
                          AsyncHttpClient client,
                          Consumer<String> eventConsumer,
                          String url);
}
