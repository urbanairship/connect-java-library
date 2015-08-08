package com.urbanairship.connect.client;

import com.google.gson.Gson;
import com.urbanairship.connect.client.model.GsonUtil;
import com.urbanairship.connect.client.model.responses.Event;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Class that consumes and parses the API response while tracking the stream offset.
 */
public class RawEventReceiver implements Consumer<String>, Supplier<Long> {

    private static final Logger log = LogManager.getLogger(RawEventReceiver.class);

    private final Gson gson = GsonUtil.getGson();
    private long lastOffset = 0L;
    private final Consumer<Event> consumer;

    /**
     * Default constructor
     *
     * @param consumer Consumer<>Event</> Implemented by a library user to consume response POJOs.
     */
    public RawEventReceiver(Consumer<Event> consumer) {
        this.consumer = consumer;
    }

    /**
     * Accepts an API response entry and parses it into an {@link com.urbanairship.connect.client.model.responses.Event}.
     * The offset is then retrieved from the parsed response, which then gets passed into the Event consumer.
     *
     * @param event String a single event from the API response.
     */
    @Override
    public void accept(String event) {
        Event eventObj = gson.fromJson(event, Event.class);
        log.debug("Parsing event " + eventObj.getIdentifier());

        if (lastOffset != eventObj.getOffset()) {
            // assuming duplicates arrive sequentially, drop the event if it's a duplicate
            lastOffset = eventObj.getOffset();
            consumer.accept(eventObj);
        }
    }

    /**
     * Gets the last stream offset.
     *
     * @return offset
     */
    @Override
    public Long get() {
        return lastOffset;
    }

}
