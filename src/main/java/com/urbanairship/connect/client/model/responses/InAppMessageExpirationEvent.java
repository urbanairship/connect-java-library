package com.urbanairship.connect.client.model.responses;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;

import java.time.Instant;
import java.util.Optional;


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

    @SerializedName("converting_push")
    private final Optional<AssociatedPush> convertingPush;

    private final String type;

    private final Optional<Instant> time;

    @SerializedName("replacing_push")
    private final Optional<AssociatedPush> replacingPush;

    private InAppMessageExpirationEvent() {
        this(null, Optional.<String>empty(), Optional.<Integer>empty(), Optional.<AssociatedPush>empty(), null, null, Optional.<AssociatedPush>empty());
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
        private Optional<String> groupId = Optional.empty();
        private Optional<Integer> variantId = Optional.empty();
        private Optional<AssociatedPush> convertingPush = Optional.empty();
        private String type;
        private Optional<Instant> time = Optional.empty();
        private Optional<AssociatedPush> replacingPush = Optional.empty();

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

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setTime(Instant time) {
            this.time = Optional.of(time);
            return this;
        }

        public Builder setReplacingPush(AssociatedPush replacingPush) {
            this.replacingPush = Optional.of(replacingPush);
            return this;
        }

        public InAppMessageExpirationEvent build() {
            Preconditions.checkNotNull(pushId);
            Preconditions.checkNotNull(type);
            Preconditions.checkNotNull(time);
            Preconditions.checkState(VALID_TYPES.contains(type));
            return new InAppMessageExpirationEvent(pushId, groupId, variantId, convertingPush, type, time, replacingPush);
        }
    }

    public InAppMessageExpirationEvent(String pushId, Optional<String> groupId, Optional<Integer> variantId, Optional<AssociatedPush> convertingPush, String type, Optional<Instant> time, Optional<AssociatedPush> replacingPush) {
        this.pushId = pushId;
        this.groupId = groupId;
        this.variantId = variantId;
        this.convertingPush = convertingPush;
        this.type = type;
        this.time = time;
        this.replacingPush = replacingPush;
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

    public Optional<AssociatedPush> getConvertingPush() {
        return convertingPush;
    }

    public Optional<Instant> getTime() {
        return time;
    }

    public Optional<AssociatedPush> getReplacingPush() {
        return replacingPush;
    }
}
