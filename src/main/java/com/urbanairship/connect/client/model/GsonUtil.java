package com.urbanairship.connect.client.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.urbanairship.connect.client.model.filters.DeviceFilter;
import com.urbanairship.connect.client.model.filters.DeviceFilterSerializer;
import com.urbanairship.connect.client.model.responses.DeviceInfo;
import com.urbanairship.connect.client.model.responses.DeviceInfoAdapter;
import com.urbanairship.connect.client.model.responses.Event;
import com.urbanairship.connect.client.model.responses.EventAdapter;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class GsonUtil {
    private final static JsonParser parser = new JsonParser();
    private final static Gson gson;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DeviceInfo.class, new DeviceInfoAdapter());
        builder.registerTypeAdapterFactory(new OptionalTypeAdapterFactory());
        builder.registerTypeAdapter(Event.class, new EventAdapter());
        builder.registerTypeAdapter(DeviceFilter.class, new DeviceFilterSerializer());
        gson = builder.create();
    }

    public static JsonObject parseJSONfromBytes(byte[] bytes) {
        String byteString = new String(bytes, StandardCharsets.UTF_8);
        return  parser.parse(byteString).getAsJsonObject();
    }

    public static byte[] serializeToJSONBytes(Object serializableObject, Type type) {
        return gson.toJson(serializableObject, type).toString().getBytes(StandardCharsets.UTF_8);
    }

    public static Gson getGson() {
        return gson;
    }

    public static JsonParser getParser() {
        return parser;
    }
}
