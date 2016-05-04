/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model.responses;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;
import org.joda.time.DateTime;


public class InAppMessageExpirationEvent implements EventBody {


    public static final String REPLACED = "REPLACED";
    public static final String EXPIRED = "EXPIRED";
    public static final String ALREADY_DISPLAYED = "ALREADY_DISPLAYED";

    public static final ImmutableSet<String> VALID_TYPES = ImmutableSet.of(REPLACED, EXPIRED, ALREADY_DISPLAYED);

    @SerializedName("push_id")
    private final String pushId;

    @SerializedName("group_id")
    private final Optional<String> groupId;

    @SerializedName("variant_id")
    private final Optional<Integer> variantId;

    @SerializedName("time_sent")
    private final Optional<DateTime> timeSent;

    @SerializedName("triggering_push")
    private final Optional<AssociatedPush> triggeringPush;

    private final String type;

    @SerializedName("time_expired")
    private final Optional<DateTime> timeExpired;

    @SerializedName("replacing_push")
    private final Optional<AssociatedPush> replacingPush;

    @SerializedName("session_id")
    private final Optional<String> sessionId;

    private InAppMessageExpirationEvent() {
        this(null, Optional.<String>absent(), Optional.<Integer>absent(), Optional.<DateTime>absent(), Optional.<AssociatedPush>absent(), null, null,
                Optional.<AssociatedPush>absent(), Optional.<String>absent());
    }

    public static InAppMessageExpirationEvent parseJSONfromBytes(byte[] bytes) {
        JsonObject jsonObject = GsonUtil.parseJSONfromBytes(bytes);
        return parseJSON(jsonObject.toString());
    }

    public static InAppMessageExpirationEvent parseJSON(String json) {
        return GsonUtil.getGson().fromJson(json, InAppMessageExpirationEvent.class);
    }

    public byte[] serializeToJSONBytes() {
        return GsonUtil.serializeToJSONBytes(this, InAppMessageExpirationEvent.class);
    }

    public static class Builder {
        private String pushId;
        private Optional<String> groupId = Optional.absent();
        private Optional<Integer> variantId = Optional.absent();
        private Optional<DateTime> timeSent = Optional.absent();
        private Optional<AssociatedPush> triggeringPush = Optional.absent();
        private String type;
        private Optional<DateTime> timeExpired = Optional.absent();
        private Optional<AssociatedPush> replacingPush = Optional.absent();
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

        public Builder setTimeSent(DateTime timeSent) {
            this.timeSent = Optional.of(timeSent);
            return this;
        }

        public Builder setTriggeringPush(AssociatedPush triggeringPush) {
            this.triggeringPush = Optional.of(triggeringPush);
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setTimeExpired(DateTime timeExpired) {
            this.timeExpired = Optional.of(timeExpired);
            return this;
        }

        public Builder setReplacingPush(AssociatedPush replacingPush) {
            this.replacingPush = Optional.of(replacingPush);
            return this;
        }

        public Builder setSessionId(String sessionId) {
            this.sessionId = Optional.of(sessionId);
            return this;
        }

        public InAppMessageExpirationEvent build() {
            Preconditions.checkNotNull(pushId);
            Preconditions.checkNotNull(type);
            Preconditions.checkState(VALID_TYPES.contains(type));
            return new InAppMessageExpirationEvent(pushId, groupId, variantId, timeSent, triggeringPush, type, timeExpired, replacingPush, sessionId);
        }
    }

    public InAppMessageExpirationEvent(String pushId, Optional<String> groupId, Optional<Integer> variantId, Optional<DateTime> timeSent, Optional<AssociatedPush> triggeringPush, String type, Optional<DateTime> timeExpired, Optional<AssociatedPush> replacingPush, Optional<String> sessionId) {
        this.pushId = pushId;
        this.groupId = groupId;
        this.variantId = variantId;
        this.timeSent = timeSent;
        this.triggeringPush = triggeringPush;
        this.type = type;
        this.timeExpired = timeExpired;
        this.replacingPush = replacingPush;
        this.sessionId = sessionId;
    }

    @Override
    public EventType getType() {
        return EventType.IN_APP_MESSAGE_EXPIRATION;
    }

    public String getExpirationType() {
        return type;
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

    public Optional<DateTime> getTimeSent() {
        return timeSent;
    }

    public Optional<AssociatedPush> getTriggeringPush() {
        return triggeringPush;
    }

    public Optional<DateTime> getTimeExpired() {
        return timeExpired;
    }

    public Optional<AssociatedPush> getReplacingPush() {
        return replacingPush;
    }

    public Optional<String> getSessionId() {
        return sessionId;
    }


    @Override
    public String toString() {
        return "InAppMessageExpirationEvent{" +
                "pushId='" + pushId + '\'' +
                ", groupId=" + groupId +
                ", variantId=" + variantId +
                ", timeSent=" + timeSent +
                ", triggeringPush=" + triggeringPush +
                ", type='" + type + '\'' +
                ", timeExpired=" + timeExpired +
                ", replacingPush=" + replacingPush +
                ", sessionId=" + sessionId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final InAppMessageExpirationEvent that = (InAppMessageExpirationEvent) o;

        if (getPushId() != null ? !getPushId().equals(that.getPushId()) : that.getPushId() != null) return false;
        if (getGroupId() != null ? !getGroupId().equals(that.getGroupId()) : that.getGroupId() != null) return false;
        if (getVariantId() != null ? !getVariantId().equals(that.getVariantId()) : that.getVariantId() != null)
            return false;
        if (getTimeSent() != null ? !getTimeSent().equals(that.getTimeSent()) : that.getTimeSent() != null)
            return false;
        if (getTriggeringPush() != null ? !getTriggeringPush().equals(that.getTriggeringPush()) : that.getTriggeringPush() != null)
            return false;
        if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null) return false;
        if (getTimeExpired() != null ? !getTimeExpired().equals(that.getTimeExpired()) : that.getTimeExpired() != null)
            return false;
        if (getReplacingPush() != null ? !getReplacingPush().equals(that.getReplacingPush()) : that.getReplacingPush() != null)
            return false;
        return getSessionId() != null ? getSessionId().equals(that.getSessionId()) : that.getSessionId() == null;

    }

    @Override
    public int hashCode() {
        int result = getPushId() != null ? getPushId().hashCode() : 0;
        result = 31 * result + (getGroupId() != null ? getGroupId().hashCode() : 0);
        result = 31 * result + (getVariantId() != null ? getVariantId().hashCode() : 0);
        result = 31 * result + (getTimeSent() != null ? getTimeSent().hashCode() : 0);
        result = 31 * result + (getTriggeringPush() != null ? getTriggeringPush().hashCode() : 0);
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        result = 31 * result + (getTimeExpired() != null ? getTimeExpired().hashCode() : 0);
        result = 31 * result + (getReplacingPush() != null ? getReplacingPush().hashCode() : 0);
        result = 31 * result + (getSessionId() != null ? getSessionId().hashCode() : 0);
        return result;
    }
}
