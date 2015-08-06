package com.urbanairship.connect.client.model.responses;

import com.google.gson.annotations.SerializedName;

import java.util.Optional;

public class PushIds {

    @SerializedName("push_id")
    private String pushId;
    @SerializedName("group_id")
    private Optional<String> groupId;

    private PushIds() {
        this(null, Optional.empty());
    }

    public PushIds(String pushId, Optional<String> groupId) {
        this.pushId = pushId;
        this.groupId = groupId;
    }

    public String getPushId() {
        return pushId;
    }

    public Optional<String> getGroupId() {
        return groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PushIds)) return false;

        PushIds pushIds = (PushIds) o;

        if (!groupId.equals(pushIds.groupId)) return false;
        if (!pushId.equals(pushIds.pushId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pushId.hashCode();
        result = 31 * result + groupId.hashCode();
        return result;
    }
}
