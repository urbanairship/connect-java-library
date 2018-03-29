package com.urbanairship.connect.client.model.request;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.urbanairship.connect.client.model.GsonUtil;
import com.urbanairship.connect.client.model.request.filters.Filter;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class StreamRequestPayloadSerializationTest {

    private static final JsonParser parser = new JsonParser();

    @Test
    public void testSerialization() {
        StreamRequestPayload payload = new StreamRequestPayload(
                ImmutableSet.of(Filter.newBuilder().addEventTypes("OPEN").build()),
                Optional.of(Subset.createPartitionSubset()
                        .setCount(10)
                        .setSelection(5)
                        .build()
                ),
                Optional.of(StartPosition.relative(StartPosition.RelativePosition.EARLIEST)),
                Optional.of(true)
        );

        JsonElement obj = GsonUtil.getGson().toJsonTree(payload);

        String json = "{" +
                "\"filters\":[{" +
                    "\"types\": [\"OPEN\"]" +
                "}]," +
                "\"subset\":{" +
                    "\"type\":\"PARTITION\"," +
                    "\"count\":10," +
                    "\"selection\":5" +
                "}," +
                "\"start\":\"EARLIEST\"," +
                "\"enable_offset_updates\": true" +
            "}";

        JsonElement expected = parser.parse(json);

        assertEquals(expected, obj);
    }

    @Test
    public void testStartEarliest() {
        StreamRequestPayload payload = new StreamRequestPayload(Collections.<Filter>emptySet(), Optional.<Subset>absent(), Optional.of(StartPosition.relative(StartPosition.RelativePosition.EARLIEST)), Optional.<Boolean>absent());

        JsonElement obj = GsonUtil.getGson().toJsonTree(payload);
        JsonElement expected = parser.parse("{\"start\":\"EARLIEST\"}");

        assertEquals(expected, obj);
    }

    @Test
    public void testStartLatest() {
        StreamRequestPayload payload = new StreamRequestPayload(Collections.<Filter>emptySet(), Optional.<Subset>absent(), Optional.of(StartPosition.relative(StartPosition.RelativePosition.LATEST)), Optional.<Boolean>absent());

        JsonElement obj = GsonUtil.getGson().toJsonTree(payload);
        JsonElement expected = parser.parse("{\"start\":\"LATEST\"}");

        assertEquals(expected, obj);
    }

    @Test
    public void testStartAbsolute() {
        String offset = RandomStringUtils.randomAlphanumeric(32);
        StreamRequestPayload payload = new StreamRequestPayload(Collections.<Filter>emptySet(), Optional.<Subset>absent(), Optional.of(StartPosition.offset(offset)), Optional.<Boolean>absent());

        JsonElement obj = GsonUtil.getGson().toJsonTree(payload);
        JsonElement expected = parser.parse(String.format("{\"resume_offset\":\"%s\"}", offset));

        assertEquals(expected, obj);
    }

    @Test
    public void testEmptyPayload() {
        StreamRequestPayload payload = new StreamRequestPayload(Collections.<Filter>emptySet(), Optional.<Subset>absent(), Optional.<StartPosition>absent(), Optional.<Boolean>absent());

        JsonElement obj = GsonUtil.getGson().toJsonTree(payload);
        JsonElement expected = parser.parse("{}");

        assertEquals(expected, obj);
    }
}