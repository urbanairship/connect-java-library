/*
Copyright 2015-2022 Airship and Contributors
*/

package com.urbanairship.connect.client.model.request.filters;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.urbanairship.connect.client.model.GsonUtil;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class FilterSerializerTest {

    private static final JsonParser parser = new JsonParser();

    @Test
    public void testSerializationAllOptionsPresent() throws Exception {
        String deviceId = UUID.randomUUID().toString();
        String eventType = "OPEN";
        long latency = RandomUtils.nextLong(1L, 100000L);
        String pushId = UUID.randomUUID().toString();

        Filter filter = Filter.newBuilder()
                .addDevices(new DeviceFilter(DeviceFilterType.ANDROID_CHANNEL, deviceId))
                .addDeviceTypes(DeviceType.ANDROID)
                .addEventTypes(eventType)
                .addNotifications(new NotificationFilter(NotificationFilter.Type.PUSH_ID, pushId))
                .setLatency(latency)
                .build();

        JsonElement obj = GsonUtil.getGson().toJsonTree(filter);

        String json = String.format("{" +
                "\"device_types\":[\"android\"]," +
                "\"latency\":%d," +
                "\"notifications\":[{" +
                    "\"push_id\":\"%s\"" +
                "}]," +
                "\"types\":[\"%s\"]," +
                "\"devices\":[{" +
                    "\"android_channel\":\"%s\"" +
                "}]" +
            "}", latency, pushId, eventType, deviceId);

        JsonElement expected = parser.parse(json);

        assertEquals(expected, obj);
    }

    @Test
    public void testOnlyOneProvided() throws Exception {
        String eventType = "CLOSE";
        Filter filter = Filter.newBuilder()
                .addEventTypes(eventType)
                .build();

        JsonElement obj = GsonUtil.getGson().toJsonTree(filter);

        JsonElement expected = parser.parse(String.format("{\"types\":[\"%s\"]}", eventType));

        assertEquals(expected, obj);
    }

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testEmptyFilterInvalid() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot create an empty filter payload");

        Filter.newBuilder().build();
    }

    @Test
    public void testInvalidLatency() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Latency must be positive");

        Filter.newBuilder().setLatency(-1L).build();
    }
}
