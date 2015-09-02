package com.urbanairship.connect.client.model.responses;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;

import java.util.Optional;

public class InAppMessageDisplayEvent implements EventBody {

    @SerializedName("push_id")
    private final String pushId;

    @SerializedName("group_id")
    private final Optional<String> groupId;

    @SerializedName("variant_id")
    private final Optional<Integer> variantId;

    @SerializedName("converting_push")
    private final Optional<AssociatedPush> convertingPush;

    private InAppMessageDisplayEvent() {
        this(null, Optional.<String>empty(), Optional.<Integer>empty(), Optional.<AssociatedPush>empty());
    }

    public InAppMessageDisplayEvent(String pushId, Optional<String> groupId, Optional<Integer> variantId, Optional<AssociatedPush> convertingPush) {
        this.pushId = pushId;
        this.groupId = groupId;
        this.variantId = variantId;
        this.convertingPush = convertingPush;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String pushId;
        private Optional<String> groupId = Optional.empty();
        private Optional<Integer> variantId = Optional.empty();
        private Optional<AssociatedPush> convertingPush = Optional.empty();

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

        public Builder setConvertingPush(AssociatedPush convertingPush) {
            this.convertingPush = Optional.of(convertingPush);
            return this;
        }

        public InAppMessageDisplayEvent build() {
            Preconditions.checkNotNull(pushId);
            return new InAppMessageDisplayEvent(pushId, groupId, variantId, convertingPush);
        }
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
                ", convertingPush=" + convertingPush +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InAppMessageDisplayEvent)) return false;

        InAppMessageDisplayEvent that = (InAppMessageDisplayEvent) o;

        if (pushId != null ? !pushId.equals(that.pushId) : that.pushId != null) return false;
        if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null) return false;
        if (variantId != null ? !variantId.equals(that.variantId) : that.variantId != null) return false;
        return !(convertingPush != null ? !convertingPush.equals(that.convertingPush) : that.convertingPush != null);

    }

    @Override
    public int hashCode() {
        int result = pushId != null ? pushId.hashCode() : 0;
        result = 31 * result + (groupId != null ? groupId.hashCode() : 0);
        result = 31 * result + (variantId != null ? variantId.hashCode() : 0);
        result = 31 * result + (convertingPush != null ? convertingPush.hashCode() : 0);
        return result;
    }
}