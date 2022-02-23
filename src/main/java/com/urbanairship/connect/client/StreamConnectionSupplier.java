/*
Copyright 2015-2022 Airship and Contributors
*/

package com.urbanairship.connect.client;

import com.urbanairship.connect.client.model.StreamQueryDescriptor;
import com.urbanairship.connect.java8.Consumer;
import org.asynchttpclient.AsyncHttpClient;

/**
 * Interface for supplying the {@link StreamConsumeTask} with a {@link StreamConnection} instance.
 */
interface StreamConnectionSupplier {

    /**
     * Get a {@link StreamConnection} instance.
     *
     * @param descriptor StreamQueryDescriptor containing the app credentials and request info.
     * @param client AsyncHttpClient
     * @param eventConsumer {@code Consumer<String>} of the API response events.
     * @return StreamConnection instance.
     */
    StreamConnection get(StreamQueryDescriptor descriptor,
                         AsyncHttpClient client,
                         Consumer<String> eventConsumer);
}
