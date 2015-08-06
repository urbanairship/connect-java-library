package com.urbanairship.connect.client.model.filters;

import com.google.common.collect.ImmutableBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.urbanairship.connect.client.model.DeviceFilterType;

import java.lang.reflect.Type;

public class DeviceFilterSerializer implements JsonSerializer<DeviceFilter>{


    public static final String IOS_CHANNEL_KEY = "ios_channel";
    public static final String ANDROID_CHANNEL_KEY = "android_channel";
    public static final String AMAZON_CHANNEL_KEY = "amazon_channel";
    public static final String NAMED_USER_KEY = "named_user_id";

    private static final ImmutableBiMap<DeviceFilterType, String> channelNameMap = ImmutableBiMap.<DeviceFilterType, String>builder()
        .put(DeviceFilterType.IOS, IOS_CHANNEL_KEY)
        .put(DeviceFilterType.ANDROID, ANDROID_CHANNEL_KEY)
        .put(DeviceFilterType.AMAZON, AMAZON_CHANNEL_KEY)
        .put(DeviceFilterType.NAMED_USER, NAMED_USER_KEY)
        .build();

    @Override
    public JsonElement serialize(DeviceFilter src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject deviceInfo = new JsonObject();

        String platformChannel = channelNameMap.get(src.getDeviceFilterType());
        deviceInfo.addProperty(platformChannel, src.getIdentifier());
        return deviceInfo;
    }
}
