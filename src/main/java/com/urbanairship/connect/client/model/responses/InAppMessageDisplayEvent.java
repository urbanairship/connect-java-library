/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model.responses;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;

public class InAppMessageDisplayEvent implements EventBody {

    @SerializedName("push_id")
    private final String pushId;

    @SerializedName("group_id")
    private final Optional<String> groupId;

    @SerializedName("variant_id")
    private final Optional<Integer> variantId;

    @SerializedName("triggering_push")
    private final Optional<AssociatedPush> triggeringPush;

    @SerializedName("session_id")
    private final Optional<String> sessionId;

    private InAppMessageDisplayEvent() {
        this(null, Optional.<String>absent(), Optional.<Integer>absent(), Optional.<AssociatedPush>absent(), Optional.<String>absent());
    }

    public InAppMessageDisplayEvent(String pushId, Optional<String> groupId, Optional<Integer> variantId,
                                    Optional<AssociatedPush> triggeringPush, Optional<String> sessionId) {
        this.pushId = pushId;
        this.groupId = groupId;
        this.variantId = variantId;
        this.triggeringPush = triggeringPush;
        this.sessionId = sessionId;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String pushId;
        private Optional<String> groupId = Optional.absent();
        private Optional<Integer> variantId = Optional.absent();
        private Optional<AssociatedPush> triggeringPush = Optional.absent();
        private Optional<String> sessionId = Optional.absent();

        public Builder setPushId(String pushId) {
            this.pushId = pushId;
            return this;
        }

        public Builder setGroupId(String groupId) {
            this.groupId = Optional.of(groupId);
            return this;
        }

        public Builder setVariantId(int variantId) {
            this.variantId = Optional.of(variantId);
            return this;
        }

        public Builder setTriggeringPush(AssociatedPush triggeringPush) {
            this.triggeringPush = Optional.of(triggeringPush);
            return this;
        }

        public Builder setSessionId(String sessionId) {
            this.sessionId = Optional.of(sessionId);
            return this;
        }

        public InAppMessageDisplayEvent build() {
            Preconditions.checkNotNull(pushId);
            return new InAppMessageDisplayEvent(pushId, groupId, variantId, triggeringPush, sessionId);
        }
    }

    public Optional<String> getSessionId() {
        return sessionId;
    }

    public String getPushId() {
        return pushId;
    }

    public Optional<String> getGroupId() {
        return groupId;
    }

    public Optional<Integer> getVariantId() {
        return variantId;
    }

    public Optional<AssociatedPush> getTriggeringPush() {
        return triggeringPush;
    }

    @Override
    public EventType getType() {
        return EventType.IN_APP_MESSAGE_DISPLAY;
    }

    public static InAppMessageDisplayEvent parseJSONfromBytes(byte[] bytes) {
        JsonObject jsonObject = GsonUtil.parseJSONfromBytes(bytes);
        return parseJSON(jsonObject.toString());
    }

    public static InAppMessageDisplayEvent parseJSON(String json) {
        return GsonUtil.getGson().fromJson(json, InAppMessageDisplayEvent.class);
    }

    public byte[] serializeToJSONBytes() {
        return GsonUtil.serializeToJSONBytes(this, InAppMessageDisplayEvent.class);
    }

    @Override
    public String toString() {
        return "InAppMessageDisplayEvent{" +
                "pushId='" + pushId + '\'' +
                ", groupId=" + groupId +
                ", variantId=" + variantId +
                ", triggeringPush=" + triggeringPush +
                ", sessionId=" + sessionId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final InAppMessageDisplayEvent that = (InAppMessageDisplayEvent) o;

        if (getPushId() != null ? !getPushId().equals(that.getPushId()) : that.getPushId() != null) return false;
        if (getGroupId() != null ? !getGroupId().equals(that.getGroupId()) : that.getGroupId() != null) return false;
        if (getVariantId() != null ? !getVariantId().equals(that.getVariantId()) : that.getVariantId() != null)
            return false;
        if (getTriggeringPush() != null ? !getTriggeringPush().equals(that.getTriggeringPush()) : that.getTriggeringPush() != null)
            return false;
        return getSessionId() != null ? getSessionId().equals(that.getSessionId()) : that.getSessionId() == null;

    }

    @Override
    public int hashCode() {
        int result = getPushId() != null ? getPushId().hashCode() : 0;
        result = 31 * result + (getGroupId() != null ? getGroupId().hashCode() : 0);
        result = 31 * result + (getVariantId() != null ? getVariantId().hashCode() : 0);
        result = 31 * result + (getTriggeringPush() != null ? getTriggeringPush().hashCode() : 0);
        result = 31 * result + (getSessionId() != null ? getSessionId().hashCode() : 0);
        return result;
    }
}
