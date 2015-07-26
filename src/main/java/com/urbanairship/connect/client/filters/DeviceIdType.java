package com.urbanairship.connect.client.filters;

import com.google.gson.annotations.SerializedName;

public enum DeviceIdType {
    @SerializedName("ios")
    IOS,
    @SerializedName("android")
    ANDROID,
    @SerializedName("amazon")
    AMAZON,
    NAMED_USER
}
