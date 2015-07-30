package com.urbanairship.connect.client.model.filters;

import com.google.gson.annotations.SerializedName;

import java.util.Optional;

public class NotificationFilter {

    @SerializedName("push_id")
    private Optional<String> pushId;
    @SerializedName("group_id")
    private Optional<String> groupId;

    public static NotificationFilter createPushIdFilter(String id) {
        return new NotificationFilter(Optional.ofNullable(id), Optional.ofNullable(null));
    }

    public static NotificationFilter createGroupIdFilter(String id) {
        return new NotificationFilter(Optional.<String>empty(), Optional.ofNullable(id));
    }

    private NotificationFilter(Optional<String> pushId, Optional<String> groupId) {
        this.pushId = pushId;
        this.groupId = groupId;
    }

    public Optional<String> getPushId() {
        return pushId;
    }

    public Optional<String> getGroupId() {
        return groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationFilter)) return false;

        NotificationFilter that = (NotificationFilter) o;

        if (!groupId.equals(that.groupId)) return false;
        if (!pushId.equals(that.pushId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pushId.hashCode();
        result = 31 * result + groupId.hashCode();
        return result;
    }
}
