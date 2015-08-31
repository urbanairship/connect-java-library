package com.urbanairship.connect.client.model.responses;

import com.urbanairship.connect.client.model.EventType;

/**
 * First open events do not have a message body.
 * This empty class serves as a marker for the presence of an first open event
 * The builder part of the class is just for consistency
 */
public class FirstOpenEvent implements EventBody {

    public static class Builder {
        public FirstOpenEvent build() {
            return new FirstOpenEvent();
        }
    }

    public FirstOpenEvent() {};

    public static Builder newBuilder() {
        return new Builder();
    }

    // Override equality to return true if objects are of the same class
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return !(o == null || getClass() != o.getClass());
    }

    @Override
    public String toString() {
        return "FirstOpenEvent{}";
    }

    @Override
    public EventType getType() {
        return EventType.FIRST_OPEN;
    }
}