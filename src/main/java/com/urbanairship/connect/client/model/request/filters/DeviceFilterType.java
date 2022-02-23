/*
Copyright 2015-2022 Airship and Contributors
*/

package com.urbanairship.connect.client.model.request.filters;

public enum DeviceFilterType {

    IOS_CHANNEL ("ios_channel"),
    ANDROID_CHANNEL ("android_channel"),
    AMAZON_CHANNEL ("amazon_channel"),
    NAMED_USER_ID ("named_user_id");

    private final String serializedValue;

    DeviceFilterType(String serializedValue) {
        this.serializedValue = serializedValue;
    }

    public String getSerializedValue() {
        return serializedValue;
    }
}
