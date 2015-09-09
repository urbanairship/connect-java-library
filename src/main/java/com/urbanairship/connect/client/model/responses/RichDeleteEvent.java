/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model.responses;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;

public class RichDeleteEvent implements EventBody{

    @SerializedName("push_id")
    private final String pushId;
    @SerializedName("group_id")
    private final Optional<String> groupId;
    @SerializedName("variant_id")
    private final Optional<Integer> variantId;

    private RichDeleteEvent() {
        this(null, Optional.<String>absent(), Optional.<Integer>absent());
    }

    public RichDeleteEvent(String pushId, Optional<String> groupId, Optional<Integer> variantId) {
        this.pushId = pushId;
        this.groupId = groupId;
        this.variantId = variantId;
    }

    public Optional<Integer> getVariantId() {
        return variantId;
    }

    public Optional<String> getGroupId() {
        return groupId;
    }

    public String getPushId() {
        return pushId;
    }

    public static RichDeleteEvent parseJSONfromBytes(byte[] bytes) {
        JsonObject jsonObject = GsonUtil.parseJSONfromBytes(bytes);
        return parseJSON(jsonObject.toString());
    }

    public static RichDeleteEvent parseJSON(String json) {
        return GsonUtil.getGson().fromJson(json, RichDeleteEvent.class);
    }

    public byte[] serializeToJSONBytes() {
        return GsonUtil.serializeToJSONBytes(this, RichDeleteEvent.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RichDeleteEvent)) return false;

        RichDeleteEvent sendEvent = (RichDeleteEvent) o;

        if (!groupId.equals(sendEvent.groupId)) return false;
        if (!pushId.equals(sendEvent.pushId)) return false;
        if (!variantId.equals(sendEvent.variantId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pushId.hashCode();
        result = 31 * result + groupId.hashCode();
        result = 31 * result + variantId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SendEvent{" +
            "pushId='" + pushId + '\'' +
            ", groupId=" + groupId +
            ", variantId=" + variantId +
            '}';
    }

    @Override
    public EventType getType() {
        return EventType.RICH_DELETE;
    }
}
