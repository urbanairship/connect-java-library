package com.urbanairship.connect.client.model.responses;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.urbanairship.connect.client.model.DeviceFilterType;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceInfoAdapter implements JsonDeserializer<DeviceInfo>, JsonSerializer<DeviceInfo> {

    @Override
    public DeviceInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject deviceJson = json.getAsJsonObject();
        DeviceInfo.Builder deviceInfoBuilder = DeviceInfo.newBuilder();

        List<Map.Entry<String, JsonElement>> platforms = deviceJson.entrySet().stream()
                .filter(entry -> DeviceFilterType.getDeviceType(entry.getKey()).isChannelType()).collect(Collectors.toList());
        if (platforms.size() > 1) {
            throw new JsonParseException("Multiple platforms defined, maximum of one platform allowed");
        }

        // get named user id if present
        Optional<JsonElement> parsedNamedUserId = Optional.ofNullable(deviceJson.get(DeviceFilterType.NAMED_USER.getKey()));
        if (parsedNamedUserId.isPresent()) {
            Optional<String> namedUserId = Optional.ofNullable(parsedNamedUserId.get().getAsString());
            deviceInfoBuilder.setNamedUsedId(namedUserId);
        }

        Optional<Map.Entry<String, JsonElement>> platformOptional = platforms.stream().findFirst();
        if (!platformOptional.isPresent()) {
            throw new JsonParseException("Unable to parse device platform from json");
        } else {
            deviceInfoBuilder.setChanneId(platformOptional.get().getValue().getAsString());
            deviceInfoBuilder.setPlatform(DeviceFilterType.getDeviceType(platformOptional.get().getKey()));
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
        return deviceInfo;
    }
}
