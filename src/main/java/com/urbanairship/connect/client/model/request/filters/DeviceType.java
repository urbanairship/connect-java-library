package com.urbanairship.connect.client.model.request.filters;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public enum DeviceType {

    IOS("ios"), ANDROID("android"), AMAZON("amazon");

    private final String serializedValue;

    DeviceType(String serializedValue) {
        this.serializedValue = serializedValue;
    }

    public String getSerializedValue() {
        return serializedValue;
    }

    public static final JsonSerializer<DeviceType> SERIALIZER = new JsonSerializer<DeviceType>() {
        @Override
        public JsonElement serialize(DeviceType deviceType, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(deviceType.getSerializedValue());
        }
    };
}
