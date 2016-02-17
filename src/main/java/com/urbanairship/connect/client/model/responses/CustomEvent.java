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

    private CustomEvent() {
        this(null, Optional.<Double>absent(), Optional.<String>absent(), null, Optional.<AssociatedPush>absent(), Optional.<AssociatedPush>absent());
    }

    public CustomEvent(String name,
                       Optional<Double> value,
                       Optional<String> interactionId,
                       String interactionType,
                       Optional<AssociatedPush> lastDelivered,
                       Optional<AssociatedPush> triggeringPush) {
        this.name = name;
        this.value = value;
        this.interactionId = interactionId;
        this.interactionType = interactionType;
        this.lastDelivered = lastDelivered;
        this.triggeringPush = triggeringPush;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final CustomEvent that = (CustomEvent) o;

        if (!name.equals(that.name)) return false;
        if (!value.equals(that.value)) return false;
        if (!interactionId.equals(that.interactionId)) return false;
        if (!interactionType.equals(that.interactionType)) return false;
        if (!lastDelivered.equals(that.lastDelivered)) return false;
        return triggeringPush.equals(that.triggeringPush);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + interactionId.hashCode();
        result = 31 * result + interactionType.hashCode();
        result = 31 * result + lastDelivered.hashCode();
        result = 31 * result + triggeringPush.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CustomEvent{" +
            "name='" + name + '\'' +
            ", value=" + value +
            ", interactionId='" + interactionId + '\'' +
            ", interactionType='" + interactionType + '\'' +
            ", lastDelivered=" + lastDelivered +
            ", triggeringPush=" + triggeringPush +
            '}';
    }

    @Override
    public EventType getType() {
        return EventType.CUSTOM;
    }
}
