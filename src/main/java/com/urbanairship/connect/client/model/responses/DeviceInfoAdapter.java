package com.urbanairship.connect.client.model.responses;

import com.google.common.collect.ImmutableBiMap;
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

    public static final String IOS_CHANNEL_KEY = "ios_channel";
    public static final String ANDROID_CHANNEL_KEY = "android_channel";
    public static final String AMAZON_CHANNEL_KEY = "amazon_channel";
    public static final String NAMED_USER_KEY = "named_user_id";

    private static final ImmutableBiMap<DeviceFilterType, String> channelNameMap = ImmutableBiMap.<DeviceFilterType, String>builder()
            .put(DeviceFilterType.IOS, IOS_CHANNEL_KEY)
            .put(DeviceFilterType.ANDROID, ANDROID_CHANNEL_KEY)
            .put(DeviceFilterType.AMAZON, AMAZON_CHANNEL_KEY)
            .build();

    @Override
    public DeviceInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject deviceJson = json.getAsJsonObject();
        DeviceInfo.Builder deviceInfoBuilder = DeviceInfo.newBuilder();

        List<Map.Entry<String, JsonElement>> platforms = deviceJson.entrySet().stream()
                .filter(entry -> channelNameMap.containsValue(entry.getKey())).collect(Collectors.toList());
        if (platforms.size() > 1) {
            throw new JsonParseException("Multiple platforms defined, maximum of one platform allowed");
        }

        // get named user id if present
        Optional<JsonElement> parsedNamedUserId = Optional.ofNullable(deviceJson.get(NAMED_USER_KEY));
        if (parsedNamedUserId.isPresent()) {
            Optional<String> namedUserId = Optional.ofNullable(parsedNamedUserId.get().getAsString());
            deviceInfoBuilder.setNamedUsedId(namedUserId);
        }

        Optional<Map.Entry<String, JsonElement>> platformOptional = platforms.stream().findFirst();
        if (!platformOptional.isPresent()) {
            throw new JsonParseException("Unable to parse device platform from json");
        } else {
            deviceInfoBuilder.setChanneId(platformOptional.get().getValue().getAsString());
            deviceInfoBuilder.setPlatform(channelNameMap.inverse().get(platformOptional.get().getKey()));
        }

        return deviceInfoBuilder.build();
    }

    @Override
    public JsonElement serialize(DeviceInfo src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject deviceInfo = new JsonObject();

        String platformChannel = channelNameMap.get(src.getPlatform());
        deviceInfo.addProperty(platformChannel, src.getChannelId());
        if (src.getNamedUsedId().isPresent()) {
            deviceInfo.addProperty(NAMED_USER_KEY, src.getNamedUsedId().get());
        }
        return deviceInfo;
    }
}
