package com.urbanairship.connect.client.model.request.filters;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.urbanairship.connect.client.model.GsonUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DeviceTypeSerializationTest {

    private static final JsonParser parser = new JsonParser();

    @Test
    public void testSerialization() throws Exception {
        List<DeviceType> types = new ArrayList<>();
        types.add(DeviceType.AMAZON);
        types.add(DeviceType.ANDROID);
        types.add(DeviceType.IOS);

        JsonElement obj = GsonUtil.getGson().toJsonTree(types);

        JsonElement expected = parser.parse("[\"amazon\", \"android\", \"ios\"]");

        assertEquals(expected, obj);
    }
}