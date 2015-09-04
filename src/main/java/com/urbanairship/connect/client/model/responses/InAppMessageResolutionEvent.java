package com.urbanairship.connect.client.model.responses;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;

import java.time.Instant;
import java.util.Optional;

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
    private final Optional<Instant> timeSent;

    @SerializedName("triggering_push")
    private final Optional<AssociatedPush> triggeringPush;

    private final String type;

    @SerializedName("button_id")
    private final Optional<String> buttonId;

    @SerializedName("button_group")
    private final Optional<String> buttonGroup;

    @SerializedName("button_description")
    private final Optional<String> buttonDescription;

    private final long duration;


    public InAppMessageResolutionEvent(String pushId, Optional<String> groupId, Optional<Integer> variantId, Optional<Instant> timeSent, Optional<AssociatedPush> triggeringPush, String type, Optional<String> buttonId, Optional<String> buttonGroup, Optional<String> buttonDescription, long duration) {
        this.pushId = pushId;
        this.groupId = groupId;
        this.variantId = variantId;
        this.timeSent = timeSent;
        this.triggeringPush = triggeringPush;
        this.type = type;
        this.buttonId = buttonId;
        this.buttonGroup = buttonGroup;
        this.buttonDescription = buttonDescription;
        this.duration = duration;
    }

    private InAppMessageResolutionEvent() {
        this(null, Optional.<String>empty(), Optional.<Integer>empty(), Optional.<Instant>empty(), Optional.<AssociatedPush>empty(), null, Optional.<String>empty(), Optional.<String>empty(), Optional.<String>empty(), 0);
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
        private Optional<String> groupId = Optional.empty();
        private Optional<Integer> variantId = Optional.empty();
        private Optional<Instant> timeSent = Optional.empty();
        private Optional<AssociatedPush> triggeringPush = Optional.empty();
        private String type;
        private long duration;
        private Optional<String> buttonId = Optional.empty();
        private Optional<String> buttonGroup = Optional.empty();
        private Optional<String> buttonDescription = Optional.empty();

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

        public Builder setTimeSent(Instant timeSent) {
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
            this.duration = duration;
            return this;
        }

        public InAppMessageResolutionEvent build() {
            Preconditions.checkNotNull(pushId, "Must give a a pushId");
            Preconditions.checkNotNull(type, "Must give a type");
            Preconditions.checkNotNull(duration, "Must give a duration");
            Preconditions.checkState(VALID_TYPES.contains(type));
            return new InAppMessageResolutionEvent(pushId, groupId, variantId, timeSent, triggeringPush, type, buttonId, buttonGroup, buttonDescription, duration);
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
            ", duration=" + duration +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InAppMessageResolutionEvent)) return false;

        InAppMessageResolutionEvent that = (InAppMessageResolutionEvent) o;

        if (duration != that.duration) return false;
        if (buttonDescription != null ? !buttonDescription.equals(that.buttonDescription) : that.buttonDescription != null)
            return false;
        if (buttonGroup != null ? !buttonGroup.equals(that.buttonGroup) : that.buttonGroup != null) return false;
        if (buttonId != null ? !buttonId.equals(that.buttonId) : that.buttonId != null) return false;
        if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null) return false;
        if (pushId != null ? !pushId.equals(that.pushId) : that.pushId != null) return false;
        if (timeSent != null ? !timeSent.equals(that.timeSent) : that.timeSent != null) return false;
        if (triggeringPush != null ? !triggeringPush.equals(that.triggeringPush) : that.triggeringPush != null)
            return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (variantId != null ? !variantId.equals(that.variantId) : that.variantId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pushId != null ? pushId.hashCode() : 0;
        result = 31 * result + (groupId != null ? groupId.hashCode() : 0);
        result = 31 * result + (variantId != null ? variantId.hashCode() : 0);
        result = 31 * result + (timeSent != null ? timeSent.hashCode() : 0);
        result = 31 * result + (triggeringPush != null ? triggeringPush.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (buttonId != null ? buttonId.hashCode() : 0);
        result = 31 * result + (buttonGroup != null ? buttonGroup.hashCode() : 0);
        result = 31 * result + (buttonDescription != null ? buttonDescription.hashCode() : 0);
        result = 31 * result + (int) (duration ^ (duration >>> 32));
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

    public Optional<Instant> getTimeSent() {
        return timeSent;
    }

    public Optional<AssociatedPush> getTriggeringPush() {
        return triggeringPush;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public EventType getType() {
        return EventType.IN_APP_MESSAGE_RESOLUTION;
    }
}
