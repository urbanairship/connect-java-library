/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model.responses;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;

public class CustomEvent implements EventBody {
    private final String name;
    private final Optional<Double> value;
    @SerializedName("interaction_id")
    private final Optional<String> interactionId;
    @SerializedName("interaction_type")
    private final String interactionType;
    @SerializedName("last_delivered")
    private final Optional<AssociatedPush> lastDelivered;
    @SerializedName("triggering_push")
    private final Optional<AssociatedPush> triggeringPush;
    @SerializedName("sessionId")
    private final Optional<String> sessionId;

    private CustomEvent() {
        this(null, Optional.<Double>absent(), Optional.<String>absent(), null, Optional.<AssociatedPush>absent(),
                Optional.<AssociatedPush>absent(), Optional.<String>absent());
    }

    public CustomEvent(String name,
                       Optional<Double> value,
                       Optional<String> interactionId,
                       String interactionType,
                       Optional<AssociatedPush> lastDelivered,
                       Optional<AssociatedPush> triggeringPush, Optional<String> sessionId) {
        this.name = name;
        this.value = value;
        this.interactionId = interactionId;
        this.interactionType = interactionType;
        this.lastDelivered = lastDelivered;
        this.triggeringPush = triggeringPush;
        this.sessionId = sessionId;
    }

    public Optional<String> getSessionId() {
        return sessionId;
    }

    public String getName() {
        return name;
    }

    public Optional<Double> getValue() {
        return value;
    }

    public Optional<String> getInteractionId() {
        return interactionId;
    }

    public String getInteractionType() {
        return interactionType;
    }

    public Optional<AssociatedPush> getLastDelivered() {
        return lastDelivered;
    }

    public Optional<AssociatedPush> getTriggeringPush() {
        return triggeringPush;
    }

    public static CustomEvent parseJSONfromBytes(byte[] bytes) {
        JsonObject jsonObject = GsonUtil.parseJSONfromBytes(bytes);
        return parseJSON(jsonObject.toString());
    }

    public static CustomEvent parseJSON(String json) {
        return GsonUtil.getGson().fromJson(json, CustomEvent.class);
    }

    public byte[] serializeToJSONBytes() {
        return GsonUtil.serializeToJSONBytes(this, CustomEvent.class);
    }

    @Override
    public String toString() {
        return "CustomEvent{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", interactionId=" + interactionId +
                ", interactionType='" + interactionType + '\'' +
                ", lastDelivered=" + lastDelivered +
                ", triggeringPush=" + triggeringPush +
                ", sessionId=" + sessionId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final CustomEvent that = (CustomEvent) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getValue() != null ? !getValue().equals(that.getValue()) : that.getValue() != null) return false;
        if (getInteractionId() != null ? !getInteractionId().equals(that.getInteractionId()) : that.getInteractionId() != null)
            return false;
        if (getInteractionType() != null ? !getInteractionType().equals(that.getInteractionType()) : that.getInteractionType() != null)
            return false;
        if (getLastDelivered() != null ? !getLastDelivered().equals(that.getLastDelivered()) : that.getLastDelivered() != null)
            return false;
        if (getTriggeringPush() != null ? !getTriggeringPush().equals(that.getTriggeringPush()) : that.getTriggeringPush() != null)
            return false;
        return getSessionId() != null ? getSessionId().equals(that.getSessionId()) : that.getSessionId() == null;

    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
        result = 31 * result + (getInteractionId() != null ? getInteractionId().hashCode() : 0);
        result = 31 * result + (getInteractionType() != null ? getInteractionType().hashCode() : 0);
        result = 31 * result + (getLastDelivered() != null ? getLastDelivered().hashCode() : 0);
        result = 31 * result + (getTriggeringPush() != null ? getTriggeringPush().hashCode() : 0);
        result = 31 * result + (getSessionId() != null ? getSessionId().hashCode() : 0);
        return result;
    }

    @Override
    public EventType getType() {
        return EventType.CUSTOM;
    }
}
