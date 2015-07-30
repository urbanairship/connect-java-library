package com.urbanairship.connect.client.model.filters;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Optional;

public class OptionalSerializer implements JsonSerializer<Optional<?>> {

    @Override
    public JsonElement serialize(Optional<?> src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonElement element;
        if (src.isPresent()) {
            element = context.serialize(src.orElse(null));
        } else {
            return null;
        }
        return element;
    }
}
