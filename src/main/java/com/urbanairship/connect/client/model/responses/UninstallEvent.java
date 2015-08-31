package com.urbanairship.connect.client.model.responses;

import com.urbanairship.connect.client.model.EventType;

/**
 * Uninstall events do not have a message body.
 * This empty class serves as a marker for the presence of an uninstall event
 * The builder part of the class is just for consistency
 */
public class UninstallEvent implements EventBody {

    public static class Builder {
        public UninstallEvent build() {
            return new UninstallEvent();
        }
    }

    public UninstallEvent() {}

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
        return "UninstallEvent{}";
    }

    @Override
    public EventType getType() {
        return EventType.UNINSTALL;
    }
}

