package com.urbanairship.connect.client.model;

import com.google.gson.annotations.SerializedName;

public enum DeviceFilterType {
    @SerializedName("ios")
    IOS,
    @SerializedName("android")
    ANDROID,
    @SerializedName("amazon")
    AMAZON,
    NAMED_USER
}
