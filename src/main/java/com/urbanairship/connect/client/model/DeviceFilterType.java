package com.urbanairship.connect.client.model;

import com.google.gson.annotations.SerializedName;

public enum DeviceFilterType {
    @SerializedName("ios")
    IOS ("ios_channel"),
    @SerializedName("android")
    ANDROID ("android_channel"),
    @SerializedName("amazon")
    AMAZON ("amazon_channel"),
    NAMED_USER ("named_user_id");

    private final String key;

    DeviceFilterType(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public static boolean isDeviceType(String key) {
        for (DeviceFilterType type : DeviceFilterType.values()) {
            if (key.equals(type.getKey()) && !type.equals(DeviceFilterType.NAMED_USER)) {
                return true;
            }
        }
        return false;
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
