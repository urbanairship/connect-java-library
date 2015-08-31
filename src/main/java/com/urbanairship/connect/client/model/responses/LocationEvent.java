package com.urbanairship.connect.client.model.responses;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class LocationEvent implements EventBody
{
    private final double latitude;
    private final double longitude;
    private final boolean foreground;
    @SerializedName("session_id")
    private final Optional<String> sessionId;

    private LocationEvent() {
        this(0, 0, false, Optional.<String>empty());
    }

    public LocationEvent(double latitude, double longitude, boolean foreground, Optional<String> sessionId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.foreground = foreground;
        this.sessionId = sessionId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
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
        return GsonUtil.getGson().fromJson(json, LocationEvent.class);
    }

    public byte[] serializeToJSONBytes() {
        return GsonUtil.getGson().toJson(this).toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationEvent)) return false;

        LocationEvent that = (LocationEvent) o;

        if (foreground != that.foreground) return false;
        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Double.compare(that.longitude, longitude) != 0) return false;
        if (!sessionId.equals(that.sessionId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (foreground ? 1 : 0);
        result = 31 * result + sessionId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "LocationEvent{" +
            "latitude=" + latitude +
            ", longitude=" + longitude +
            ", foreground=" + foreground +
            ", sessionId=" + sessionId +
            '}';
    }

    @Override
    public EventType getType() {
        return EventType.LOCATION;
    }
}
