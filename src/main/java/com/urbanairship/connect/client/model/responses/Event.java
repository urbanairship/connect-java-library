package com.urbanairship.connect.client.model.responses;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.EventType;

import java.time.Instant;

public class Event {
    public static class Builder {
        private String identifier;
        private EventType eventType;
        private Instant occurred;
        private Instant processed;
        private String appKey;
        private String offset;
        private DeviceInfo deviceInfo;
        private EventBody eventBody;

        public Builder setIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder setEventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder setOccurred(Instant occurred) {
            this.occurred = occurred;
            return this;
        }

        public Builder setProcessed(Instant processed) {
            this.processed = processed;
            return this;
        }

        public Builder setAppKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        public Builder setOffset(String offset) {
            this.offset = offset;
            return this;
        }

        public Builder setDeviceInfo(DeviceInfo deviceInfo) {
            this.deviceInfo = deviceInfo;
            return this;
        }

        public Builder setEventBody(EventBody eventBody) {
            this.eventBody = eventBody;
            return this;
        }

        public Event build() {
            Preconditions.checkNotNull(identifier, "Missing event UUID");
            Preconditions.checkNotNull(eventType, "Event type not set");
            Preconditions.checkNotNull(occurred, "Missing occurred timestamp");
            Preconditions.checkNotNull(processed, "Missing processed timestamp");
            Preconditions.checkNotNull(appKey, "Missing appKey");
            Preconditions.checkNotNull(deviceInfo, "Missing device info");
            Preconditions.checkNotNull(eventBody, "Missing event body");
            Preconditions.checkNotNull(offset, "Missing stream offset");
            return new Event(identifier, eventType, occurred, processed, appKey, offset, deviceInfo, eventBody);
        }
    }

    public static final String DEVICE_INFO_KEY = "device";
    public static final String EVENT_BODY_KEY = "body";
    public static final String TYPE_KEY = "type";
    public static final String APP_KEY = "app_key";
    public static final String EVENT_ID_KEY= "id";
    public static final String OCCURRED_KEY= "occurred";
    public static final String PROCESSED_KEY = "processed";
    public static final String OFFSET_KEY = "offset";

    @SerializedName(EVENT_ID_KEY)
    private String identifier;
    @SerializedName(TYPE_KEY)
    private EventType eventType;
    private Instant occurred;
    private Instant processed;
    @SerializedName(APP_KEY)
    private String appKey;
    private String offset;
    @SerializedName(DEVICE_INFO_KEY)
    private DeviceInfo deviceInfo;
    @SerializedName(EVENT_BODY_KEY)
    private EventBody eventBody;

    private Event() {
        this(null, null, null, null, null, null, null, null);
    }

    private Event(String identifier, EventType eventType, Instant occurred, Instant processed, String appKey, String offset, DeviceInfo deviceInfo, EventBody eventBody) {
        this.identifier = identifier;
        this.eventType = eventType;
        this.occurred = occurred;
        this.processed = processed;
        this.appKey = appKey;
        this.offset = offset;
        this.deviceInfo = deviceInfo;
        this.eventBody = eventBody;
    }

    public String getIdentifier() {
        return identifier;
    }

    public EventType getEventType() {
        return eventBody.getType();
    }

    public Instant getOccurred() {
        return occurred;
    }

    public Instant getProcessed() {
        return processed;
    }

    public String getOffset() {
        return offset;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public EventBody getEventBody() {
        return eventBody;
    }

    public String getAppKey() {
        return appKey;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (offset != event.offset) return false;
        if (identifier != null ? !identifier.equals(event.identifier) : event.identifier != null) return false;
        if (eventType != event.eventType) return false;
        if (occurred != null ? !occurred.equals(event.occurred) : event.occurred != null) return false;
        if (processed != null ? !processed.equals(event.processed) : event.processed != null) return false;
        if (appKey != null ? !appKey.equals(event.appKey) : event.appKey != null) return false;
        if (deviceInfo != null ? !deviceInfo.equals(event.deviceInfo) : event.deviceInfo != null) return false;
        return !(eventBody != null ? !eventBody.equals(event.eventBody) : event.eventBody != null);

    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (eventType != null ? eventType.hashCode() : 0);
        result = 31 * result + (occurred != null ? occurred.hashCode() : 0);
        result = 31 * result + (processed != null ? processed.hashCode() : 0);
        result = 31 * result + (appKey != null ? appKey.hashCode() : 0);
        result = 31 * result + (appKey != null ? appKey.hashCode() : 0);
        result = 31 * result + (deviceInfo != null ? deviceInfo.hashCode() : 0);
        result = 31 * result + (eventBody != null ? eventBody.hashCode() : 0);
        return result;
    }
}
