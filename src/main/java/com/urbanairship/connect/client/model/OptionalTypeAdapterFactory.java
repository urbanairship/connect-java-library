package com.urbanairship.connect.client.model;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

public class OptionalTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!Optional.class.equals(type.getRawType())) {
            return null;
        }

        final ParameterizedType parameterizedOptionalType = (ParameterizedType) type.getType();
        final Type targetType = parameterizedOptionalType.getActualTypeArguments()[0];
        final TypeAdapter<?> targetTypeAdapter = gson.getAdapter(TypeToken.get(targetType));

        return (TypeAdapter<T>) newOptionalAdapter(targetTypeAdapter);
    }

    private <E> TypeAdapter<Optional<E>> newOptionalAdapter(final TypeAdapter<E> elementAdapter) {
        return new TypeAdapter<Optional<E>>() {

            public void write(JsonWriter out, Optional<E> value) throws IOException {
                if (value.isPresent()) {
                    elementAdapter.write(out, value.get());
                } else {
                    out.nullValue();
                }
            }

            public Optional<E> read(JsonReader in) throws IOException {
                return Optional.ofNullable(elementAdapter.read(in));
            }
        };
    }
}


