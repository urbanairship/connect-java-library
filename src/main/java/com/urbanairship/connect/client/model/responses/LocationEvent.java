package com.urbanairship.connect.client.model.responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;
import com.urbanairship.connect.client.model.OptionalTypeAdapterFactory;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class LocationEvent implements EventBody
{

    private final static Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
        .create();

    private final String latitude;
    private final String longitude;
    private final boolean foreground;
    @SerializedName("session_id")
    private final Optional<String> sessionId;

    private LocationEvent() {
        this(null, null, false, Optional.<String>empty());
    }

    public LocationEvent(String latitude, String longitude, boolean foreground, Optional<String> sessionId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.foreground = foreground;
        this.sessionId = sessionId;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public boolean isForeground() {
        return foreground;
    }

    public Optional<String> getSessionId() {
        return sessionId;
    }

    public static LocationEvent parseJSONfromBytes(byte[] bytes) {
        JsonObject jsonObject = GsonUtil.parseJSONfromBytes(bytes);
        return parseJSON(jsonObject.toString());
    }

    public static LocationEvent parseJSON(String json) {
        return gson.fromJson(json, LocationEvent.class);
    }

    public byte[] serializeToJSONBytes() {
        return gson.toJson(this).toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationEvent)) return false;

        LocationEvent that = (LocationEvent) o;

        if (foreground != that.foreground) return false;
        if (!latitude.equals(that.latitude)) return false;
        if (!longitude.equals(that.longitude)) return false;
        if (!sessionId.equals(that.sessionId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = latitude.hashCode();
        result = 31 * result + longitude.hashCode();
        result = 31 * result + (foreground ? 1 : 0);
        result = 31 * result + sessionId.hashCode();
        return result;
    }

    @Override
    public EventType getType() {
        return EventType.LOCATION;
    }
}
