package com.urbanairship.connect.client.model.request;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.urbanairship.connect.client.model.GsonUtil;
import com.urbanairship.connect.client.model.request.filters.Filter;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class StreamRequestPayloadSerializationTest {

    private static final JsonParser parser = new JsonParser();

    @Test
    public void testSerialization() throws Exception {
        StreamRequestPayload payload = new StreamRequestPayload(
                ImmutableSet.of(Filter.newBuilder().addEventTypes("OPEN").build()),
                Optional.of(Subset.createPartitionSubset()
                        .setCount(10)
                        .setSelection(5)
                        .build()
                ),
                Optional.of(StartPosition.relative(StartPosition.RelativePosition.EARLIEST))
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
                "\"start\":\"EARLIEST\"" +
            "}";

        JsonElement expected = parser.parse(json);

        assertEquals(expected, obj);
    }

    @Test
    public void testStartPositions() throws Exception {
        StreamRequestPayload payload = new StreamRequestPayload(Collections.<Filter>emptySet(), Optional.<Subset>absent(), Optional.of(StartPosition.relative(StartPosition.RelativePosition.EARLIEST)));

        JsonElement obj = GsonUtil.getGson().toJsonTree(payload);
        JsonElement expected = parser.parse("{\"start\":\"EARLIEST\"}");

        assertEquals(expected, obj);

        payload = new StreamRequestPayload(Collections.<Filter>emptySet(), Optional.<Subset>absent(), Optional.of(StartPosition.relative(StartPosition.RelativePosition.LATEST)));

        obj = GsonUtil.getGson().toJsonTree(payload);
        expected = parser.parse("{\"start\":\"LATEST\"}");

        assertEquals(expected, obj);

        long offset = RandomUtils.nextLong(10L, 10000L);
        payload = new StreamRequestPayload(Collections.<Filter>emptySet(), Optional.<Subset>absent(), Optional.of(StartPosition.offset(offset)));

        obj = GsonUtil.getGson().toJsonTree(payload);
        expected = parser.parse(String.format("{\"resume_offset\":%d}", offset));

        assertEquals(expected, obj);
    }

    @Test
    public void testEmptyPayload() throws Exception {
        StreamRequestPayload payload = new StreamRequestPayload(Collections.<Filter>emptySet(), Optional.<Subset>absent(), Optional.<StartPosition>absent());

        JsonElement obj = GsonUtil.getGson().toJsonTree(payload);
        JsonElement expected = parser.parse("{}");

        assertEquals(expected, obj);
    }
}