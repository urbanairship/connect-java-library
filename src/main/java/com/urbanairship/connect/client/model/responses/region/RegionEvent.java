/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model.responses.region;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.responses.EventBody;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class RegionEvent implements EventBody {

    public static class Builder {
        private RegionAction action;
        @SerializedName("region_id")
        private String regionId;
        private RegionSource source;
        @SerializedName("session_id")
        private String sessionId;
        private Proximity proximity = null;
        @SerializedName("circular_region")
        private CircularRegion circularRegion = null;

        private Builder() {
        }

        public Builder setAction(RegionAction value) {
            this.action = value;
            return this;
        }

        public Builder setRegionId(String value) {
            this.regionId = value;
            return this;
        }

        public Builder setSource(RegionSource value) {
            this.source = value;
            return this;
        }

        public Builder setSessionId(String value) {
            this.sessionId = value;
            return this;
        }

        public Builder setProximity(Proximity value) {
            this.proximity = value;
            return this;
        }

        public Builder setCircularRegion(CircularRegion value) {
            this.circularRegion = value;
            return this;
        }

        public RegionEvent build() {
            Preconditions.checkNotNull(action, "action must be set");
            Preconditions.checkNotNull(regionId, "regionId must be set");
            Preconditions.checkNotNull(source, "source must be set");
            Preconditions.checkNotNull(sessionId, "sessionId must be set");

            return new RegionEvent(action, regionId, source, sessionId, Optional.ofNullable(proximity), Optional.ofNullable(circularRegion));
        }
    }

    private final static JsonParser parser = new JsonParser();
    private final static Gson gson = new Gson();

    private final RegionAction action;
    @SerializedName("region_id")
    private final String regionId;
    private final RegionSource source;
    @SerializedName("session_id")
    private final String sessionId;
    private final Optional<Proximity> proximity;
    @SerializedName("circular_region")
    private final Optional<CircularRegion> circularRegion;

    private RegionEvent() {
        this(null, null, null, null, Optional.<Proximity>empty(), Optional.<CircularRegion>empty());
    }

    private RegionEvent(RegionAction action,
                        String regionId,
                        RegionSource source,
                        String sessionId,
                        Optional<Proximity> proximity,
                        Optional<CircularRegion> circularRegion) {
        this.action = action;
        this.regionId = regionId;
        this.source = source;
        this.sessionId = sessionId;
        this.proximity = proximity;
        this.circularRegion = circularRegion;
    }

    public RegionAction getAction() {
        return action;
    }

    public String getRegionId() {
        return regionId;
    }

    public RegionSource getSource() {
        return source;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Optional<Proximity> getProximity() {
        return proximity;
    }

    public Optional<CircularRegion> getCircularRegion() {
        return circularRegion;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public EventType getType() {
        return EventType.REGION;
    }

    public static RegionEvent parseJSONfromBytes(byte[] bytes) {
        String byteString = new String(bytes, StandardCharsets.UTF_8);
        JsonObject jsonObject = parser.parse(byteString).getAsJsonObject();
        return parseJSON(jsonObject.toString());
    }

    public static RegionEvent parseJSON(String json) {
        return gson.fromJson(json, RegionEvent.class);
    }

    public static RegionEvent parseJSON(JsonElement json) {
        return gson.fromJson(json, RegionEvent.class);
    }

    public byte[] serializeToJSONBytes() {
        return gson.toJson(this).toString().getBytes(StandardCharsets.UTF_8);
    }
}
