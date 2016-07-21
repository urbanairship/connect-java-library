/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.urbanairship.connect.client.model.GsonUtil;
import com.urbanairship.connect.java8.Consumer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Class that consumes and parses the API response while tracking the stream offset.
 */
public final class RawEventReceiver implements Consumer<String> {

    private static final Logger log = LogManager.getLogger(RawEventReceiver.class);

    public static final String OFFSET_KEY = "offset";

    private static final Gson gson = GsonUtil.getGson();

    private final AtomicReference<String> lastOffset = new AtomicReference<>("0");

    private final Consumer<String> consumer;

    /**
     * Default constructor
     *
     * @param consumer {@code Consumer<String>} Implemented by a library user to consume events.
     */
    public RawEventReceiver(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    /**
     * Accepts an API response entry and passes it on to the the wrapped consumer. The offset of the event is parsed
     * and tracked on each receipt and internal state is updated to provide that offset to the outside world via
     * {@link #getLastOffset()}.
     *
     * @param event String a single event from the API response.
     */
    @Override
    public void accept(String event) {
        try {
            JsonObject obj = gson.fromJson(event, JsonObject.class);
            String offset = obj.get(OFFSET_KEY).getAsString();

            lastOffset.set(offset);
        }
        catch (Exception e) {
            log.error("Error extracting offset from event " + event, e);
            return;
        }

        consumer.accept(event);
    }

    /**
     * Gets the last stream offset.
     *
     * @return offset
     */
    public String getLastOffset() {
        return lastOffset.get();
    }

}
