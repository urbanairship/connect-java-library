package com.urbanairship.connect.client.model.responses;


import com.google.gson.JsonObject;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;

import java.util.Optional;

public class PushBody implements EventBody {

    private final String pushId;
    private final Optional<String> groupId;
    private final boolean trimmed;
    private final String payload;

    private PushBody() {
        this(null, Optional.<String>empty(), false, null);
    }

    public PushBody(String pushId, Optional<String> groupId, boolean trimmed, String payload) {
        this.pushId = pushId;
        this.groupId = groupId;
        this.trimmed = trimmed;
        this.payload = payload;
    }

    public String getPushId() {
        return pushId;
    }

    public Optional<String> getGroupId() {
        return groupId;
    }

    public boolean isTrimmed() {
        return trimmed;
    }

    public String getPayload() {
        return payload;
    }


    public static PushBody parseJSONfromBytes(byte[] bytes) {
        JsonObject jsonObject = GsonUtil.parseJSONfromBytes(bytes);
        return parseJSON(jsonObject.toString());
    }

    public static PushBody parseJSON(String json) {
        return GsonUtil.getGson().fromJson(json, PushBody.class);
    }

    public byte[] serializeToJSONBytes() {
        return GsonUtil.serializeToJSONBytes(this, PushBody.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PushBody pushBody = (PushBody) o;

        if (trimmed != pushBody.trimmed) return false;
        if (!pushId.equals(pushBody.pushId)) return false;
        if (!groupId.equals(pushBody.groupId)) return false;
        return payload.equals(pushBody.payload);

    }

    @Override
    public int hashCode() {
        int result = pushId.hashCode();
        result = 31 * result + groupId.hashCode();
        result = 31 * result + (trimmed ? 1 : 0);
        result = 31 * result + payload.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PushBody{" +
            "pushId='" + pushId + '\'' +
            ", groupId=" + groupId +
            ", trimmed=" + trimmed +
            ", payload='" + payload + '\'' +
            '}';
    }

    @Override
    public EventType getType() {
        return EventType.PUSH_BODY;
    }
}