package com.urbanairship.connect.client.model.responses;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;

import java.util.Optional;

public class InAppMessageResolutionEvent implements EventBody {


    //TODO add an enum for the various types of events

    public static final String BUTTON_CLICK = "BUTTON_CLICK";
    public static final String MESSAGE_CLICK = "MESSAGE_CLICK";
    public static final String USER_DISMISSED = "USER_DISMISSED";

    public static final ImmutableSet<String> VALID_TYPES = ImmutableSet.of(BUTTON_CLICK, MESSAGE_CLICK, USER_DISMISSED);

    @SerializedName("push_id")
    private final String pushId;

    @SerializedName("group_id")
    private final Optional<String> groupId;

    @SerializedName("variant_id")
    private final Optional<Integer> variantId;

    @SerializedName("converting_push")
    private final Optional<AssociatedPush> convertingPush;

    private final String type;

    @SerializedName("button_id")
    private final Optional<String> buttonId;

    @SerializedName("button_group")
    private final Optional<String> buttonGroup;

    @SerializedName("button_description")
    private final Optional<String> buttonDescription;

    private final long duration;


    public InAppMessageResolutionEvent(String pushId, Optional<String> groupId, Optional<Integer> variantId, Optional<AssociatedPush> convertingPush, String type, Optional<String> buttonId, Optional<String> buttonGroup, Optional<String> buttonDescription, long duration) {
        this.pushId = pushId;
        this.groupId = groupId;
        this.variantId = variantId;
        this.convertingPush = convertingPush;
        this.type = type;
        this.buttonId = buttonId;
        this.buttonGroup = buttonGroup;
        this.buttonDescription = buttonDescription;
        this.duration = duration;
    }

    private InAppMessageResolutionEvent() {
        this(null, Optional.<String>empty(), Optional.<Integer>empty(), Optional.<AssociatedPush>empty(), null, Optional.<String>empty(), Optional.<String>empty(), Optional.<String>empty(), 0);
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
        private Optional<AssociatedPush> convertingPush = Optional.empty();
        private String type;
        private long duration;
        private Optional<String> buttonId = Optional.empty();
        private Optional<String> buttonGroup = Optional.empty();
        private Optional<String> buttonDescription = Optional.empty();

        public Builder setPushId(String pushId) {
            this.pushId = pushId;
            return this;
        }

        public Builder setGroupId(Optional<String> groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder setVariantId(Optional<Integer> variantId) {
            this.variantId = variantId;
            return this;
        }

        public Builder setConvertingPush(Optional<AssociatedPush> convertingPush) {
            this.convertingPush = convertingPush;
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
            return new InAppMessageResolutionEvent(pushId, groupId, variantId, convertingPush, type, buttonId, buttonGroup, buttonDescription, duration);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InAppMessageResolutionEvent)) return false;

        InAppMessageResolutionEvent that = (InAppMessageResolutionEvent) o;

        if (getDuration() != that.getDuration()) return false;
        if (getPushId() != null ? !getPushId().equals(that.getPushId()) : that.getPushId() != null) return false;
        if (getGroupId() != null ? !getGroupId().equals(that.getGroupId()) : that.getGroupId() != null) return false;
        if (getVariantId() != null ? !getVariantId().equals(that.getVariantId()) : that.getVariantId() != null)
            return false;
        if (getConvertingPush() != null ? !getConvertingPush().equals(that.getConvertingPush()) : that.getConvertingPush() != null)
            return false;
        if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null) return false;
        if (getButtonId() != null ? !getButtonId().equals(that.getButtonId()) : that.getButtonId() != null)
            return false;
        if (getButtonGroup() != null ? !getButtonGroup().equals(that.getButtonGroup()) : that.getButtonGroup() != null)
            return false;
        return !(getButtonDescription() != null ? !getButtonDescription().equals(that.getButtonDescription()) : that.getButtonDescription() != null);

    }

    @Override
    public int hashCode() {
        int result = getPushId() != null ? getPushId().hashCode() : 0;
        result = 31 * result + (getGroupId() != null ? getGroupId().hashCode() : 0);
        result = 31 * result + (getVariantId() != null ? getVariantId().hashCode() : 0);
        result = 31 * result + (getConvertingPush() != null ? getConvertingPush().hashCode() : 0);
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        result = 31 * result + (getButtonId() != null ? getButtonId().hashCode() : 0);
        result = 31 * result + (getButtonGroup() != null ? getButtonGroup().hashCode() : 0);
        result = 31 * result + (getButtonDescription() != null ? getButtonDescription().hashCode() : 0);
        result = 31 * result + (int) (getDuration() ^ (getDuration() >>> 32));
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

    public Optional<AssociatedPush> getConvertingPush() {
        return convertingPush;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public EventType getType() {
        return EventType.IN_APP_MESSAGE_RESOLUTION;
    }
}
