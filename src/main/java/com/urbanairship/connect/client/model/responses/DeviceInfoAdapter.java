/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model.responses;

import com.google.common.base.Optional;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.urbanairship.connect.client.model.DeviceFilterType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceInfoAdapter implements JsonDeserializer<DeviceInfo>, JsonSerializer<DeviceInfo> {

    @Override
    public DeviceInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject deviceJson = json.getAsJsonObject();
        DeviceInfo.Builder deviceInfoBuilder = DeviceInfo.newBuilder();

        List<Map.Entry<String, JsonElement>> platforms = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : deviceJson.entrySet()) {
            final Optional<DeviceFilterType> deviceType = Optional.fromNullable(
                    DeviceFilterType.getDeviceType(entry.getKey()));

            if (deviceType.isPresent() && deviceType.get().isChannelType()) {
                platforms.add(entry);
            }
        }

        if (platforms.size() > 1) {
            throw new JsonParseException("Multiple platforms defined, maximum of one platform allowed");
        }

        // get named user id if present
        Optional<JsonElement> parsedNamedUserId = Optional.fromNullable(deviceJson.get(DeviceFilterType.NAMED_USER.getKey()));
        if (parsedNamedUserId.isPresent()) {
            Optional<String> namedUserId = Optional.fromNullable(parsedNamedUserId.get().getAsString());
            deviceInfoBuilder.setNamedUsedId(namedUserId);
        }

        Optional<Map.Entry<String, JsonElement>> platformOptional = Optional.fromNullable(platforms.get(0));
        if (!platformOptional.isPresent()) {
            throw new JsonParseException("Unable to parse device platform from json");
        } else {
            deviceInfoBuilder.setChanneId(platformOptional.get().getValue().getAsString());
            deviceInfoBuilder.setPlatform(DeviceFilterType.getDeviceType(platformOptional.get().getKey()));
        }

        if (deviceJson.has("attributes")) {
            for (Map.Entry<String, JsonElement> entry : deviceJson.getAsJsonObject("attributes").entrySet()) {
                deviceInfoBuilder.addAttribute(entry.getKey(), entry.getValue().getAsString());
            }
        }

        if (deviceJson.has("identifiers")) {
            for (Map.Entry<String, JsonElement> entry : deviceJson.getAsJsonObject("identifiers").entrySet()) {
                deviceInfoBuilder.addIdentifier(entry.getKey(), entry.getValue().getAsString());
            }
        }

        return deviceInfoBuilder.build();
    }

    @Override
    public JsonElement serialize(DeviceInfo src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject deviceInfo = new JsonObject();

        String platformChannel = src.getPlatform().getKey();
        deviceInfo.addProperty(platformChannel, src.getChannelId());
        if (src.getNamedUsedId().isPresent()) {
            deviceInfo.addProperty(DeviceFilterType.NAMED_USER.getKey(), src.getNamedUsedId().get());
        }

        if (!src.getAttributes().isEmpty()) {
            JsonObject attributes = new JsonObject();
            for (Map.Entry<String, String> entry : src.getAttributes().entrySet()) {
                attributes.addProperty(entry.getKey(), entry.getValue());
            }
            deviceInfo.add("attributes", attributes);
        }

        if(!src.getIdentifiers().isEmpty()) {
            JsonObject identifiers = new JsonObject();
            for (Map.Entry<String, String> entry : src.getIdentifiers().entrySet()) {
                identifiers.addProperty(entry.getKey(), entry.getValue());
            }
            deviceInfo.add("identifiers", identifiers);
        }

        return deviceInfo;
    }
}
