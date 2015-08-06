package com.urbanairship.connect.client.model.responses;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.urbanairship.connect.client.model.OptionalTypeAdapterFactory;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class OptionalTesting {

    @Test
    public void testOptionalParsing() throws Exception {

        Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .create();

        OptionalClass nonempty = new OptionalClass(Optional.of("test"));
        String json = gson.toJson(nonempty);
        OptionalClass parsed = gson.fromJson(json, OptionalClass.class);
        assertEquals("test", parsed.getValue().get());

        OptionalClass empty = new OptionalClass(Optional.empty());
        json = gson.toJson(empty);
        parsed = gson.fromJson(json, OptionalClass.class);
        assertFalse(parsed.getValue().isPresent());
    }

    private static final class OptionalClass {

        private final Optional<String> value;

        private OptionalClass() {
            this(Optional.empty());
        }

        public OptionalClass(Optional<String> value) {
            this.value = value;
        }

        public Optional<String> getValue() {
            return value;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("value", value)
                .toString();
        }
    }
}
