package com.urbanairship.connect.client.model.responses;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;

public class ControlEvent implements EventBody {
    @SerializedName("push_id")
    private final String pushId;
    @SerializedName("group_id")
    private final Optional<String> groupId;

    @Override
    public EventType getType() {
        return EventType.CONTROL;
    }

    public ControlEvent(String pushId, Optional<String> groupId) {
        this.pushId = pushId;
        this.groupId = groupId;
    }

    public Optional<String> getGroupId() {
        return groupId;
    }

    public String getPushId() {
        return pushId;
    }

    public static ControlEvent parseJSONfromBytes(byte[] bytes) {
        JsonObject jsonObject = GsonUtil.parseJSONfromBytes(bytes);
        return parseJSON(jsonObject.toString());
    }

    public static ControlEvent parseJSON(String s) {
        return GsonUtil.getGson().fromJson(s, ControlEvent.class);
    }

    public byte[] serializeToJSONBytes() {
        return GsonUtil.serializeToJSONBytes(this, ControlEvent.class);
    }

    @Override
    public String toString() {
        return "ControlEvent{" +
                "pushId='" + pushId + '\'' +
                ", groupId=" + groupId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ControlEvent that = (ControlEvent) o;

        if (!pushId.equals(that.pushId)) return false;
        return groupId.equals(that.groupId);

    }

    @Override
    public int hashCode() {
        int result = pushId.hashCode();
        result = 31 * result + groupId.hashCode();
        return result;
    }
}
