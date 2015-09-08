/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model.responses;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import com.urbanairship.connect.client.model.DeviceFilterType;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;
import org.apache.commons.lang3.RandomUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.UUID;

public class EventParsingTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testParseEvent() throws Exception {
        HashMap<String, Object> rawJson = Maps.newHashMap();
        rawJson.put(Event.EVENT_ID_KEY, UUID.randomUUID().toString());
        rawJson.put(Event.TYPE_KEY, EventType.SEND.name());
        rawJson.put(Event.OCCURRED_KEY, DateTime.now().withZone(DateTimeZone.UTC).toString());
        rawJson.put(Event.PROCESSED_KEY, DateTime.now().withZone(DateTimeZone.UTC).toString());
        rawJson.put(Event.DEVICE_INFO_KEY,
                ImmutableMap.<String, String>of(DeviceFilterType.IOS.getKey(), UUID.randomUUID().toString()));
        rawJson.put(Event.EVENT_BODY_KEY,
                ImmutableMap.<String, String>of("push_id", UUID.randomUUID().toString()));
        rawJson.put(Event.OFFSET_KEY, RandomUtils.nextLong(0L, 100000L));

        String json = GsonUtil.getGson().toJson(rawJson);
        GsonUtil.getGson().fromJson(json, Event.class);
    }

    @Test
    public void testMissingEventBodyException() throws Exception {
        HashMap<String, Object> rawJson = Maps.newHashMap();
        rawJson.put(Event.EVENT_ID_KEY, UUID.randomUUID().toString());
        rawJson.put(Event.OFFSET_KEY, RandomUtils.nextLong(0L, 100000L));
        rawJson.put(Event.TYPE_KEY, EventType.OPEN.name());
        rawJson.put(Event.OCCURRED_KEY, DateTime.now().withZone(DateTimeZone.UTC).toString());
        rawJson.put(Event.PROCESSED_KEY, DateTime.now().withZone(DateTimeZone.UTC).toString());
        rawJson.put(Event.DEVICE_INFO_KEY,
                ImmutableMap.<String, String>of(DeviceFilterType.IOS.getKey(), UUID.randomUUID().toString()));

        String json = GsonUtil.getGson().toJson(rawJson);

        // expect missing body exception
        expectedException.expect(JsonParseException.class);
        expectedException.expectMessage("event body");
        GsonUtil.getGson().fromJson(json, Event.class);
    }
}
