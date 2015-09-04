/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model.filters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class DeviceFilterSerializer implements JsonSerializer<DeviceFilter>{

    @Override
    public JsonElement serialize(DeviceFilter src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject deviceInfo = new JsonObject();

        String platformChannel =src.getDeviceFilterType().getKey();
        deviceInfo.addProperty(platformChannel, src.getIdentifier());
        return deviceInfo;
    }
}
