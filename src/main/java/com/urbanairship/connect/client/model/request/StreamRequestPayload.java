package com.urbanairship.connect.client.model.request;

import com.google.common.base.MoreObjects;
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

    public StreamRequestPayload(Set<Filter> filters, Optional<Subset> subset, Optional<StartPosition> startPosition) {
        this.filters = filters;
        this.subset = subset;
        this.startPosition = startPosition;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StreamRequestPayload that = (StreamRequestPayload) o;
        return Objects.equals(filters, that.filters) &&
                Objects.equals(subset, that.subset) &&
                Objects.equals(startPosition, that.startPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filters, subset, startPosition);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("filters", filters)
                .add("subset", subset)
                .add("startPosition", startPosition)
                .toString();
    }

    public static final String FILTERS_KEY = "filters";
    public static final String START_KEY = "start";
    public static final String RESUME_OFFSET_KEY = "resume_offset";
    public static final String SUBSET_KEY = "subset";

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

            return obj;
        }
    };
}
