/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model.responses;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;

public class OpenEvent implements EventBody {

    @SerializedName("last_delivered")
    private final Optional<AssociatedPush> lastDelivered;
    @SerializedName("triggering_push")
    private final Optional<AssociatedPush> triggeringPush;
    @SerializedName("session_id")
    private final Optional<String> sessionId;

    private OpenEvent() {
        this(Optional.<AssociatedPush>absent(), Optional.<AssociatedPush>absent(), Optional.<String>absent());
    }

    public OpenEvent(Optional<AssociatedPush> lastDelivered, Optional<AssociatedPush> triggeringPush, Optional<String> sessionId) {
        this.lastDelivered = lastDelivered;
        this.triggeringPush = triggeringPush;
        this.sessionId = sessionId;
    }

    public Optional<AssociatedPush> getLastDelivered() {
        return lastDelivered;
    }

    public Optional<AssociatedPush> getTriggeringPush() {
        return triggeringPush;
    }

    public Optional<String> getSessionId() {
        return sessionId;
    }

    public static OpenEvent parseJSONfromBytes(byte[] bytes) {
        JsonObject jsonObject = GsonUtil.parseJSONfromBytes(bytes);
        return parseJSON(jsonObject.toString());
    }

    public static OpenEvent parseJSON(String json) {
        return GsonUtil.getGson().fromJson(json, OpenEvent.class);
    }

    public byte[] serializeToJSONBytes() {
        return GsonUtil.serializeToJSONBytes(this, OpenEvent.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OpenEvent)) return false;

        OpenEvent openEvent = (OpenEvent) o;

        if (!lastDelivered.equals(openEvent.lastDelivered)) return false;
        if (!sessionId.equals(openEvent.sessionId)) return false;
        if (!triggeringPush.equals(openEvent.triggeringPush)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = lastDelivered.hashCode();
        result = 31 * result + triggeringPush.hashCode();
        result = 31 * result + sessionId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "OpenEvent{" +
            "lastDelivered=" + lastDelivered +
            ", triggeringPush=" + triggeringPush +
            ", sessionId=" + sessionId +
            '}';
    }

    @Override
    public EventType getType() {
        return EventType.OPEN;
    }
}
