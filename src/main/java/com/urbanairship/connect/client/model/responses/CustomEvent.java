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
    private final Optional<Integer> value;
    @SerializedName("transactional_id")
    private final Optional<String> transactionalId;
    @SerializedName("customer_id")
    private final Optional<String> customerId;
    @SerializedName("interaction_id")
    private final String interactionId;
    @SerializedName("interaction_type")
    private final String interactionType;
    @SerializedName("last_delivered")
    private final Optional<AssociatedPush> lastDelivered;
    @SerializedName("triggering_push")
    private final Optional<AssociatedPush> triggeringPush;

    private CustomEvent() {
        this(null, Optional.<Integer>absent(), Optional.<String>absent(), Optional.<String>absent(), null, null, Optional.<AssociatedPush>absent(), Optional.<AssociatedPush>absent());
    }

    public CustomEvent(String name,
                       Optional<Integer> value,
                       Optional<String> transactionalId,
                       Optional<String> customerId,
                       String interactionId,
                       String interactionType,
                       Optional<AssociatedPush> lastDelivered,
                       Optional<AssociatedPush> triggeringPush) {
        this.name = name;
        this.value = value;
        this.transactionalId = transactionalId;
        this.customerId = customerId;
        this.interactionId = interactionId;
        this.interactionType = interactionType;
        this.lastDelivered = lastDelivered;
        this.triggeringPush = triggeringPush;
    }

    public String getName() {
        return name;
    }

    public Optional<Integer> getValue() {
        return value;
    }

    public Optional<String> getTransactionalId() {
        return transactionalId;
    }

    public Optional<String> getCustomerId() {
        return customerId;
    }

    public String getInteractionId() {
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
        if (!(o instanceof CustomEvent)) return false;

        CustomEvent that = (CustomEvent) o;

        if (!customerId.equals(that.customerId)) return false;
        if (!interactionId.equals(that.interactionId)) return false;
        if (!interactionType.equals(that.interactionType)) return false;
        if (!lastDelivered.equals(that.lastDelivered)) return false;
        if (!name.equals(that.name)) return false;
        if (!transactionalId.equals(that.transactionalId)) return false;
        if (!triggeringPush.equals(that.triggeringPush)) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + transactionalId.hashCode();
        result = 31 * result + customerId.hashCode();
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
            ", transactionalId=" + transactionalId +
            ", customerId=" + customerId +
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
