package com.urbanairship.connect.client.model.responses;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.EventType;

import java.nio.charset.StandardCharsets;

public class CloseEvent implements EventBody {

    private final static JsonParser parser = new JsonParser();
    private final static Gson gson = new Gson();

    @SerializedName("session_id")
    private final String sessionId;

    private CloseEvent() {
        this(null);
    }

    public CloseEvent(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public static CloseEvent parseJSONfromBytes(byte[] bytes) {
        String byteString = new String(bytes, StandardCharsets.UTF_8);
        JsonObject jsonObject = parser.parse(byteString).getAsJsonObject();
        return parseJSON(jsonObject.toString());
    }

    public static CloseEvent parseJSON(String json) {
        return gson.fromJson(json, CloseEvent.class);
    }

    public byte[] serializeToJSONBytes() {
        return gson.toJson(this).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CloseEvent)) return false;

        CloseEvent that = (CloseEvent) o;

        if (!sessionId.equals(that.sessionId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sessionId.hashCode();
    }

    @Override
    public EventType getType() {
        return EventType.CLOSE;
    }
}
