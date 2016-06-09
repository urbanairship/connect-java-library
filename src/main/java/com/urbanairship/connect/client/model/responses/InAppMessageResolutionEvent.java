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

public class InAppMessageResolutionEvent implements EventBody {

    public static final String BUTTON_CLICK = "BUTTON_CLICK";
    public static final String MESSAGE_CLICK = "MESSAGE_CLICK";
    public static final String USER_DISMISSED = "USER_DISMISSED";
    public static final String TIMED_OUT = "TIMED_OUT";

    public static final ImmutableSet<String> VALID_TYPES = ImmutableSet.of(BUTTON_CLICK, MESSAGE_CLICK, USER_DISMISSED, TIMED_OUT);

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

    @SerializedName("button_id")
    private final Optional<String> buttonId;

    @SerializedName("button_group")
    private final Optional<String> buttonGroup;

    @SerializedName("button_description")
    private final Optional<String> buttonDescription;

    @SerializedName("session_id")
    private final Optional<String> sessionId;

    private final Optional<Long> duration;

    public InAppMessageResolutionEvent(String pushId, Optional<String> groupId, Optional<Integer> variantId, Optional<DateTime> timeSent, Optional<AssociatedPush> triggeringPush, String type, Optional<String> buttonId, Optional<String> buttonGroup, Optional<String> buttonDescription, Optional<String> sessionId, Optional<Long> duration) {
        this.pushId = pushId;
        this.groupId = groupId;
        this.variantId = variantId;
        this.timeSent = timeSent;
        this.triggeringPush = triggeringPush;
        this.type = type;
        this.buttonId = buttonId;
        this.buttonGroup = buttonGroup;
        this.buttonDescription = buttonDescription;
        this.sessionId = sessionId;
        this.duration = duration;
    }

    private InAppMessageResolutionEvent() {
        this(null, Optional.<String>absent(), Optional.<Integer>absent(), Optional.<DateTime>absent(), Optional.<AssociatedPush>absent(), null, Optional.<String>absent(), Optional.<String>absent(), Optional.<String>absent(), Optional.<String>absent(), Optional.<Long>absent());
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static InAppMessageResolutionEvent parseJSONfromBytes(byte[] bytes) {
        JsonObject jsonObject = GsonUtil.parseJSONfromBytes(bytes);
        return parseJSON(jsonObject.toString());
    }

    public static InAppMessageResolutionEvent parseJSON(String json) {
        return GsonUtil.getGson().fromJson(json, InAppMessageResolutionEvent.class);
    }

    public byte[] serializeToJSONBytes() {
        return GsonUtil.serializeToJSONBytes(this, InAppMessageResolutionEvent.class);
    }

    public static class Builder {
        private String pushId;
        private Optional<String> groupId = Optional.absent();
        private Optional<Integer> variantId = Optional.absent();
        private Optional<DateTime> timeSent = Optional.absent();
        private Optional<AssociatedPush> triggeringPush = Optional.absent();
        private String type;
        private Optional<Long> duration = Optional.absent();
        private Optional<String> buttonId = Optional.absent();
        private Optional<String> buttonGroup = Optional.absent();
        private Optional<String> buttonDescription = Optional.absent();
        private Optional<String> sessionId = Optional.absent();

        public Builder setPushId(String pushId) {
            this.pushId = pushId;
            return this;
        }

        public Builder setGroupId(String groupId) {
            this.groupId = Optional.of(groupId);
            return this;
        }

        public Builder setVariantId(Integer variantId) {
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

        public Builder setDuration(long duration) {
            this.duration = Optional.of(duration);
            return this;
        }

        public Builder setSessionId(String sessionId) {
            this.sessionId = Optional.of(sessionId);
            return this;
        }

        public InAppMessageResolutionEvent build() {
            Preconditions.checkNotNull(pushId, "Must give a a pushId");
            Preconditions.checkNotNull(type, "Must give a type");
            Preconditions.checkNotNull(duration, "Must give a duration");
            Preconditions.checkState(VALID_TYPES.contains(type));
            return new InAppMessageResolutionEvent(pushId, groupId, variantId, timeSent, triggeringPush, type, buttonId,
                    buttonGroup, buttonDescription, sessionId, duration);
        }
    }

    @Override
    public String toString() {
        return "InAppMessageResolutionEvent{" +
                "pushId='" + pushId + '\'' +
                ", groupId=" + groupId +
                ", variantId=" + variantId +
                ", timeSent=" + timeSent +
                ", triggeringPush=" + triggeringPush +
                ", type='" + type + '\'' +
                ", buttonId=" + buttonId +
                ", buttonGroup=" + buttonGroup +
                ", buttonDescription=" + buttonDescription +
                ", sessionId=" + sessionId +
                ", duration=" + duration +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final InAppMessageResolutionEvent that = (InAppMessageResolutionEvent) o;

        if (getPushId() != null ? !getPushId().equals(that.getPushId()) : that.getPushId() != null) return false;
        if (getGroupId() != null ? !getGroupId().equals(that.getGroupId()) : that.getGroupId() != null) return false;
        if (getVariantId() != null ? !getVariantId().equals(that.getVariantId()) : that.getVariantId() != null)
            return false;
        if (getTimeSent() != null ? !getTimeSent().equals(that.getTimeSent()) : that.getTimeSent() != null)
            return false;
        if (getTriggeringPush() != null ? !getTriggeringPush().equals(that.getTriggeringPush()) : that.getTriggeringPush() != null)
            return false;
        if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null) return false;
        if (getButtonId() != null ? !getButtonId().equals(that.getButtonId()) : that.getButtonId() != null)
            return false;
        if (getButtonGroup() != null ? !getButtonGroup().equals(that.getButtonGroup()) : that.getButtonGroup() != null)
            return false;
        if (getButtonDescription() != null ? !getButtonDescription().equals(that.getButtonDescription()) : that.getButtonDescription() != null)
            return false;
        if (getSessionId() != null ? !getSessionId().equals(that.getSessionId()) : that.getSessionId() != null)
            return false;
        return getDuration() != null ? getDuration().equals(that.getDuration()) : that.getDuration() == null;

    }

    @Override
    public int hashCode() {
        int result = getPushId() != null ? getPushId().hashCode() : 0;
        result = 31 * result + (getGroupId() != null ? getGroupId().hashCode() : 0);
        result = 31 * result + (getVariantId() != null ? getVariantId().hashCode() : 0);
        result = 31 * result + (getTimeSent() != null ? getTimeSent().hashCode() : 0);
        result = 31 * result + (getTriggeringPush() != null ? getTriggeringPush().hashCode() : 0);
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        result = 31 * result + (getButtonId() != null ? getButtonId().hashCode() : 0);
        result = 31 * result + (getButtonGroup() != null ? getButtonGroup().hashCode() : 0);
        result = 31 * result + (getButtonDescription() != null ? getButtonDescription().hashCode() : 0);
        result = 31 * result + (getSessionId() != null ? getSessionId().hashCode() : 0);
        result = 31 * result + (getDuration() != null ? getDuration().hashCode() : 0);
        return result;
    }

    public Optional<String> getButtonId() {
        return buttonId;
    }

    public Optional<String> getButtonGroup() {
        return buttonGroup;
    }

    public Optional<String> getButtonDescription() {
        return buttonDescription;
    }

    public String getResolutionType() {
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

    public Optional<Long> getDuration() {
        return duration;
    }

    public Optional<String> getSessionId() {
        return sessionId;
    }

    @Override
    public EventType getType() {
        return EventType.IN_APP_MESSAGE_RESOLUTION;
    }
}
