/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model.responses;


import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.DeviceFilterType;
import com.urbanairship.connect.client.model.GsonUtil;

import java.util.Map;

public class DeviceInfo {
    public static class Builder {
        private Optional<String> namedUsedId = Optional.<String>absent();
        private String channeId;
        private DeviceFilterType platform;
        private ImmutableMap.Builder<String, String> attributesBuilder = ImmutableMap.builder();
        private ImmutableMap.Builder<String, String> identifiersBuilder = ImmutableMap.builder();

        public Builder() {
        }

        public Builder addAttribute(String key, String value) {
            attributesBuilder.put(key, value);
            return this;
        }

        public Builder addAttributes(Map<String, String> map) {
            attributesBuilder.putAll(map);
            return this;
        }

        public Builder addIdentifiers(Map<String, String> map) {
            identifiersBuilder.putAll(map);
            return this;
        }
        public Builder addIdentifier(String key, String value) {
            identifiersBuilder.put(key, value);
            return this;
        }

        public Builder setNamedUsedId(Optional<String> namedUsedId) {
            this.namedUsedId = namedUsedId;
            return this;
        }

        public Builder setChanneId(String channeId) {
            this.channeId = channeId;
            return this;
        }

        public Builder setPlatform(DeviceFilterType platform) {
            this.platform = platform;
            return this;
        }

        public DeviceInfo build() {
            Preconditions.checkNotNull(platform, "Platform must be specified");
            Preconditions.checkNotNull(channeId, "Channel ID must be specified");
            return new DeviceInfo(channeId, platform, namedUsedId, identifiersBuilder.build(), attributesBuilder.build());
        }
    }


    private final String channelId;
    private final DeviceFilterType platform;
    @SerializedName("named_user_id")
    private final Optional<String> namedUsedId;
    private final ImmutableMap<String, String> attributes;
    private final ImmutableMap<String, String> identifiers;

    private DeviceInfo(String channelId, DeviceFilterType platform, Optional<String> namedUsedId,
                       ImmutableMap<String, String> attributes, ImmutableMap<String, String> identifiers) {
        this.channelId = channelId;
        this.platform = platform;
        this.namedUsedId = namedUsedId;
        this.attributes = attributes;
        this.identifiers = identifiers;
    }

    public Optional<String> getNamedUsedId() {
        return namedUsedId;
    }

    public String getChannelId() {
        return channelId;
    }

    public DeviceFilterType getPlatform() {
        return platform;
    }

    public ImmutableMap<String, String> getAttributes() {
        return attributes;
    }

    public ImmutableMap<String, String> getIdentifiers() {
        return identifiers;
    }

    public static DeviceInfo parseJSONfromBytes(byte[] bytes) {
        JsonObject jsonObject = GsonUtil.parseJSONfromBytes(bytes);
        return parseJSON(jsonObject.toString());
    }

    public static DeviceInfo parseJSON(String json) {
        return GsonUtil.getGson().fromJson(json, DeviceInfo.class);
    }

    public byte[] serializeToJSONBytes() {
        return GsonUtil.serializeToJSONBytes(this, DeviceInfo.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DeviceInfo that = (DeviceInfo) o;

        if (!channelId.equals(that.channelId)) return false;
        if (platform != that.platform) return false;
        if (!namedUsedId.equals(that.namedUsedId)) return false;
        if (!attributes.equals(that.attributes)) return false;
        return identifiers.equals(that.identifiers);
    }

    @Override
    public int hashCode() {
        int result = channelId.hashCode();
        result = 31 * result + platform.hashCode();
        result = 31 * result + namedUsedId.hashCode();
        result = 31 * result + attributes.hashCode();
        result = 31 * result + identifiers.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "channelId='" + channelId + '\'' +
                ", platform=" + platform +
                ", namedUsedId=" + namedUsedId +
                ", attributes=" + attributes +
                ", identifiers=" + identifiers +
                '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }
}

