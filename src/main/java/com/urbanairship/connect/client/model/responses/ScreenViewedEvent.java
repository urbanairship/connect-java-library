package com.urbanairship.connect.client.model.responses;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;

import java.nio.charset.StandardCharsets;

public class ScreenViewedEvent implements EventBody {
    private final long duration;
    @SerializedName("viewed_screen")
    private final String viewedScreen;
    private final Optional<String> previousScreen;
    private final String sessionId;

    private ScreenViewedEvent() {
        this(0L, null, Optional.<String>absent(), null);
    }

    public ScreenViewedEvent(long duration, String viewedScreen, Optional<String> previousScreen, String sessionId) {
        this.duration = duration;
        this.viewedScreen = viewedScreen;
        this.previousScreen = previousScreen;
        this.sessionId = sessionId;
    }

    @Override
    public EventType getType() {
        return EventType.SCREEN_VIEWED;
    }

    public long getDuration() {
        return duration;
    }

    public String getViewedScreen() {
        return viewedScreen;
    }

    public Optional<String> getPreviousScreen() {
        return previousScreen;
    }

    public String getSessionId() {
        return sessionId;
    }

    public static class Builder {
        private long duration;
        private String viewedScreen;
        private Optional<String> previousScreen = Optional.absent();
        private String sessionId;

        public Builder() {

        }

        public Builder setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        public Builder setViewedScreen(String viewedScreen) {
            this.viewedScreen = viewedScreen;
            return this;
        }

        public Builder setPreviousScreen(String previousScreen) {
            this.previousScreen = Optional.of(previousScreen);
            return this;
        }

        public Builder setSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public ScreenViewedEvent build() {
            Preconditions.checkNotNull(duration);
            Preconditions.checkNotNull(viewedScreen);
            Preconditions.checkNotNull(sessionId);
            return new ScreenViewedEvent(duration, viewedScreen, previousScreen, sessionId);
        }
    }

    public static ScreenViewedEvent parseJSONfromBytes(byte[] bytes) {
        JsonObject jsonObject = GsonUtil.parseJSONfromBytes(bytes);
        return parseJSON(jsonObject.toString());
    }

    public static ScreenViewedEvent parseJSON(String json) {
        return GsonUtil.getGson().fromJson(json, ScreenViewedEvent.class);
    }

    public byte[] serializeToJSONBytes() {
        return GsonUtil.getGson().toJson(this).getBytes(StandardCharsets.UTF_8);
    }


    @Override
    public String toString() {
        return "ScreenViewedEvent{" +
                "duration=" + duration +
                ", viewedScreen='" + viewedScreen + '\'' +
                ", previousScreen=" + previousScreen +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ScreenViewedEvent that = (ScreenViewedEvent) o;

        if (getDuration() != that.getDuration()) return false;
        if (getViewedScreen() != null ? !getViewedScreen().equals(that.getViewedScreen()) : that.getViewedScreen() != null)
            return false;
        if (getPreviousScreen() != null ? !getPreviousScreen().equals(that.getPreviousScreen()) : that.getPreviousScreen() != null)
            return false;
        return getSessionId() != null ? getSessionId().equals(that.getSessionId()) : that.getSessionId() == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (getDuration() ^ (getDuration() >>> 32));
        result = 31 * result + (getViewedScreen() != null ? getViewedScreen().hashCode() : 0);
        result = 31 * result + (getPreviousScreen() != null ? getPreviousScreen().hashCode() : 0);
        result = 31 * result + (getSessionId() != null ? getSessionId().hashCode() : 0);
        return result;
    }
}
