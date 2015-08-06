package com.urbanairship.connect.client.model.responses;


import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.DeviceFilterType;
import com.urbanairship.connect.client.model.GsonUtil;

import java.util.Optional;

public class DeviceInfo {
    public static class Builder {
        private Optional<String> namedUsedId = Optional.<String>empty();
        private String channeId;
        private DeviceFilterType platform;

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
            return new DeviceInfo(channeId, platform, namedUsedId);
        }
    }


    private final String channelId;
    private final DeviceFilterType platform;
    @SerializedName("named_user_id")
    private final Optional<String> namedUsedId;

    private DeviceInfo(String channelId, DeviceFilterType platform, Optional<String> namedUsedId) {
        this.channelId = channelId;
        this.platform = platform;
        this.namedUsedId = namedUsedId;
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

        DeviceInfo that = (DeviceInfo) o;

        if (channelId != null ? !channelId.equals(that.channelId) : that.channelId != null) return false;
        if (platform != that.platform) return false;
        return !(namedUsedId != null ? !namedUsedId.equals(that.namedUsedId) : that.namedUsedId != null);

    }

    @Override
    public int hashCode() {
        int result = channelId != null ? channelId.hashCode() : 0;
        result = 31 * result + (platform != null ? platform.hashCode() : 0);
        result = 31 * result + (namedUsedId != null ? namedUsedId.hashCode() : 0);
        return result;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

}

