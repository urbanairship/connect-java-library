/*
Copyright 2015-2022 Airship and Contributors
*/

package com.urbanairship.connect.client.model.request.filters;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

public final class PredicateFilter {

    private final JsonObject value;

    public PredicateFilter(JsonObject value) {
        this.value = Preconditions.checkNotNull(value, "Predicate filter values must be provided");
    }

    public JsonObject getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PredicateFilter that = (PredicateFilter) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", value)
                .toString();
    }

    public static final JsonSerializer<PredicateFilter> SERIALIZER = new JsonSerializer<PredicateFilter>() {

        @Override
        public JsonElement serialize(PredicateFilter predicateFilter, Type type, JsonSerializationContext jsonSerializationContext) {
            return predicateFilter.getValue();
        }
    };
}
