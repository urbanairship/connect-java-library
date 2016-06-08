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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void canParsePushBody() {
        String expectedPushId = "5cc00974-ebab-480a-9324-06f8d8fc2ae4";
        String expectedPayload = "aGk=";
        boolean expectedTrimmed = true;
        String pushBodyRaw = "{\n" +
                "    \"body\": {\n" +
                "        \"payload\": \"aGk=\",\n" +
                "        \"push_id\": \"5cc00974-ebab-480a-9324-06f8d8fc2ae4\",\n" +
                "        \"trimmed\": true\n" +
                "    },\n" +
                "    \"device\": {\n" +
                "        \"ios_channel\": \"f6b36ef3-3271-40c2-a7a4-efb17d9a512f\"\n" +
                "    },\n" +
                "    \"id\": \"ef4ffe00-874d-4589-9401-6a34c43dd342\",\n" +
                "    \"occurred\": \"2016-06-06T19:03:30.189Z\",\n" +
                "    \"offset\": \"0\",\n" +
                "    \"processed\": \"2016-06-06T19:03:30.189Z\",\n" +
                "    \"type\": \"PUSH_BODY\"\n" +
                "}";
        Event foundEvent = GsonUtil.getGson().fromJson(pushBodyRaw, Event.class);
        PushBody pushBody = (PushBody) foundEvent.getEventBody();
        assertEquals(expectedPushId, pushBody.getPushId().get());
        assertEquals(expectedPayload, pushBody.getPayload());
        assertTrue(expectedTrimmed);
    }

    @Test
    public void canParsePushBodyWithGroupId() {
        String expectedGroupId = "0073e881-ae01-4770-943a-6e9ef340fb23";
        String expectedPayload = "aGk=";
        boolean expectedTrimmed = true;
        String pushBodyRaw = "{\n" +
                "    \"body\": {\n" +
                "        \"group_id\": \"0073e881-ae01-4770-943a-6e9ef340fb23\",\n" +
                "        \"payload\": \"aGk=\",\n" +
                "        \"trimmed\": true\n" +
                "    },\n" +
                "    \"device\": {\n" +
                "        \"ios_channel\": \"f6b36ef3-3271-40c2-a7a4-efb17d9a512f\"\n" +
                "    },\n" +
                "    \"id\": \"ef4ffe00-874d-4589-9401-6a34c43dd342\",\n" +
                "    \"occurred\": \"2016-06-06T19:03:30.189Z\",\n" +
                "    \"offset\": \"0\",\n" +
                "    \"processed\": \"2016-06-06T19:03:30.189Z\",\n" +
                "    \"type\": \"PUSH_BODY\"\n" +
                "}";
        Event foundEvent = GsonUtil.getGson().fromJson(pushBodyRaw, Event.class);
        PushBody pushBody = (PushBody) foundEvent.getEventBody();
        assertEquals(expectedGroupId, pushBody.getGroupId().get());
        assertEquals(expectedPayload, pushBody.getPayload());
        assertTrue(expectedTrimmed);
        String raw = GsonUtil.getGson().toJson(foundEvent);
        System.out.println(raw);

    }

    @Test
    public void canParseScreenViewed() throws Exception {
        String screenViewed = "{ id: 'a8c66fc6-210c-11e6-a195-90e2ba211bf8',\n" +
                "  offset: '463726',\n" +
                "  occurred: '2016-05-23T17:34:40.525Z',\n" +
                "  processed: '2016-05-23T17:34:55.492Z',\n" +
                "  device:\n" +
                "   { android_channel: 'aa218218-aad4-4a0c-bc6e-7ad6f9507a44',\n" +
                "     named_user_id: 'david',\n" +
                "     identifiers:\n" +
                "      { 'com.urbanairship.aaid': 'c1250591-a94b-4d56-a3ef-5f7550c3756d',\n" +
                "        session_id: '0dc71d74-59ab-4734-b88b-85f69a7e1b83' },\n" +
                "     attributes:\n" +
                "      { locale_variant: '',\n" +
                "        app_version: '373',\n" +
                "        device_model: 'Nexus 6P',\n" +
                "        connection_type: 'WIFI',\n" +
                "        app_package_name: 'com.urbanairship.goat',\n" +
                "        iana_timezone: 'America/Los_Angeles',\n" +
                "        push_opt_in: 'true',\n" +
                "        locale_country_code: 'US',\n" +
                "        device_os: '6',\n" +
                "        locale_timezone: '-25200',\n" +
                "        carrier: 'AT&T',\n" +
                "        locale_language_code: 'en',\n" +
                "        location_enabled: 'false',\n" +
                "        background_push_enabled: 'true',\n" +
                "        ua_sdk_version: '7.0.1',\n" +
                "        location_permission: 'NOT_ALLOWED' } },\n" +
                "  body:\n" +
                "   { duration: 5788,\n" +
                "     viewed_screen: 'MessageActivity',\n" +
                "     previous_screen: 'MainActivity',\n" +
                "     session_id: '738b476c-0986-4169-a4eb-fc9be7d0c486' },\n" +
                "  type: 'SCREEN_VIEWED' }";

        String inAppMessage = "{ id: '4bc01a60-1df6-11e6-a2e4-90e2ba025ad8',\n" +
                "  offset: '443577',\n" +
                "  occurred: '2016-04-29T22:04:16.413Z',\n" +
                "  processed: '2016-05-19T19:17:17.606Z',\n" +
                "  device:\n" +
                "   { ios_channel: 'e339783b-c710-4395-b7ba-5fd3a6a67746',\n" +
                "     named_user_id: 'bear',\n" +
                "     attributes:\n" +
                "      { locale_variant: '',\n" +
                "        app_version: '208',\n" +
                "        device_model: 'iPhone7,2',\n" +
                "        connection_type: 'WIFI',\n" +
                "        app_package_name: 'com.urbanairship.goat',\n" +
                "        iana_timezone: 'America/Los_Angeles',\n" +
                "        push_opt_in: 'true',\n" +
                "        locale_country_code: 'US',\n" +
                "        device_os: '9.3.1',\n" +
                "        locale_timezone: '-25200',\n" +
                "        carrier: 'AT&T',\n" +
                "        locale_language_code: 'en',\n" +
                "        location_enabled: 'false',\n" +
                "        background_push_enabled: 'true',\n" +
                "        ua_sdk_version: '7.0.1',\n" +
                "        location_permission: 'UNPROMPTED' } },\n" +
                "  body:\n" +
                "   { push_id: 'b91a7f6a-100c-459f-aa38-97616e10a799',\n" +
                "     triggering_push: { push_id: 'b91a7f6a-100c-459f-aa38-97616e10a799' },\n" +
                "     session_id: 'f96e104f-889a-41b8-976a-3f9d6f7fb1d5',\n" +
                "     type: 'EXPIRED' },\n" +
                "  type: 'IN_APP_MESSAGE_EXPIRATION' }";

        final Event event = GsonUtil.getGson().fromJson(screenViewed, Event.class);
        GsonUtil.getGson().fromJson(GsonUtil.getGson().toJson(event), Event.class);

        final Event inAppMessageEvent = GsonUtil.getGson().fromJson(inAppMessage, Event.class);

        final String s = GsonUtil.getGson().toJson(inAppMessageEvent);
    }
}
