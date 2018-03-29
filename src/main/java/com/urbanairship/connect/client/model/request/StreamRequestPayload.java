package com.urbanairship.connect.client.model.request;

import com.google.common.base.Optional;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.urbanairship.connect.client.model.request.filters.Filter;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;

public final class StreamRequestPayload {

    private final Set<Filter> filters;
    private final Optional<Subset> subset;
    private final Optional<StartPosition> startPosition;
    private final Optional<Boolean> offsetUpdatesEnabled;

    public StreamRequestPayload(Set<Filter> filters, Optional<Subset> subset, Optional<StartPosition> startPosition,
                                Optional<Boolean> offsetUpdatesEnabled) {
        this.filters = filters;
        this.subset = subset;
        this.startPosition = startPosition;
        this.offsetUpdatesEnabled = offsetUpdatesEnabled;
    }

    public Set<Filter> getFilters() {
        return filters;
    }

    public Optional<Subset> getSubset() {
        return subset;
    }

    public Optional<StartPosition> getStartPosition() {
        return startPosition;
    }

    public Optional<Boolean> offsetUpdatesEnabled() {
        return offsetUpdatesEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StreamRequestPayload)) return false;
        StreamRequestPayload that = (StreamRequestPayload) o;
        return offsetUpdatesEnabled == that.offsetUpdatesEnabled &&
                Objects.equals(filters, that.filters) &&
                Objects.equals(subset, that.subset) &&
                Objects.equals(startPosition, that.startPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filters, subset, startPosition, offsetUpdatesEnabled);
    }

    @Override
    public String toString() {
        return "StreamRequestPayload{" +
                "filters=" + filters +
                ", subset=" + subset +
                ", startPosition=" + startPosition +
                ", offsetUpdatesEnabled=" + offsetUpdatesEnabled +
                '}';
    }

    public static final String FILTERS_KEY = "filters";
    public static final String START_KEY = "start";
    public static final String RESUME_OFFSET_KEY = "resume_offset";
    public static final String SUBSET_KEY = "subset";
    public static final String OFFSET_UPDATE_KEY = "enable_offset_updates";

    public static final JsonSerializer<StreamRequestPayload> SERIALIZER = new JsonSerializer<StreamRequestPayload>() {
        @Override
        public JsonElement serialize(StreamRequestPayload streamRequestPayload, Type type, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();

            if (!streamRequestPayload.getFilters().isEmpty()) {
                obj.add(FILTERS_KEY, context.serialize(streamRequestPayload.getFilters()));
            }

            if (streamRequestPayload.getStartPosition().isPresent()) {
                StartPosition position = streamRequestPayload.getStartPosition().get();
                if (position.isRelative()) {
                    obj.addProperty(START_KEY, position.getRelativePosition().getId());
                }
                else {
                    obj.addProperty(RESUME_OFFSET_KEY, position.getOffset());
                }
            }

            if (streamRequestPayload.getSubset().isPresent()) {
                obj.add(SUBSET_KEY, context.serialize(streamRequestPayload.getSubset().get()));
            }

            if (streamRequestPayload.offsetUpdatesEnabled().isPresent()) {
                obj.addProperty(OFFSET_UPDATE_KEY, streamRequestPayload.offsetUpdatesEnabled().get());
            }

            return obj;
        }
    };
}
