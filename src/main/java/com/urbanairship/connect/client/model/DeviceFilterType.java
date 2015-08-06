package com.urbanairship.connect.client.model;

import com.google.gson.annotations.SerializedName;

public enum DeviceFilterType {
    @SerializedName("ios")
    IOS ("ios_channel", true),
    @SerializedName("android")
    ANDROID ("android_channel", true),
    @SerializedName("amazon")
    AMAZON ("amazon_channel", true),
    NAMED_USER ("named_user_id", false);

    private final String key;
    private final boolean isChannel;

    DeviceFilterType(String key, boolean isChannel) {
        this.key = key;
        this.isChannel = isChannel;
    }

    public String getKey() {
        return this.key;
    }

    public boolean isChannelType() {
        return this.isChannel;
    }

    public static DeviceFilterType getDeviceType(String key) {
        for (DeviceFilterType type : DeviceFilterType.values()) {
            if (key.equals(type.getKey())) {
                return type;
            }
        }
        return null;
    }
}
