package com.urbanairship.connect.client.model.responses;

import com.google.common.base.Optional;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class InAppMessageResolutionEventTest {
    private static final JsonParser parser = new JsonParser();

    @Test
    public void testParseJSONUserDismissed() throws Exception {
        final String pushId = "0e9c711a-bf9d-4b2c-8309-2c5b9e3a5d69";
        final String groupdId = "0e9c711a-bf9d-4b2c-8309-2c5b9e3a5d69";
        final String sessionId = "e65e56a0-8e32-4d26-bba4-43541fff0ec5";
        final int variant = 0;
        final String type = "USER_DISMISSED";
        final int duration = 1470;

        final String raw = "{\n" +
                "    \"id\": \"d1599dd2-2dc4-11e6-9a45-90e2ba02f390\",\n" +
                "    \"offset\": \"512149\",\n" +
                "    \"occurred\": \"2016-06-08T22:03:17.418Z\",\n" +
                "    \"processed\": \"2016-06-08T22:03:24.823Z\",\n" +
                "    \"device\": {\n" +
                "        \"android_channel\": \"3a8e2cd0-81b2-4559-a559-67eaae7f3c12\",\n" +
                "        \"named_user_id\": \"dj\",\n" +
                "        \"attributes\": {\n" +
                "            \"locale_variant\": \"\",\n" +
                "            \"app_version\": \"325\",\n" +
                "            \"device_model\": \"SAMSUNG-SGH-I747\",\n" +
                "            \"connection_type\": \"CELL\",\n" +
                "            \"app_package_name\": \"com.urbanairship.goat\",\n" +
                "            \"iana_timezone\": \"America/Los_Angeles\",\n" +
                "            \"push_opt_in\": \"false\",\n" +
                "            \"locale_country_code\": \"US\",\n" +
                "            \"device_os\": \"4.4.2\",\n" +
                "            \"locale_timezone\": \"-25200\",\n" +
                "            \"carrier\": \"AT&T\",\n" +
                "            \"locale_language_code\": \"en\",\n" +
                "            \"location_enabled\": \"false\",\n" +
                "            \"background_push_enabled\": \"true\",\n" +
                "            \"ua_sdk_version\": \"6.2.2\",\n" +
                "            \"location_permission\": \"SYSTEM_LOCATION_DISABLED\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"body\": {\n" +
                "        \"push_id\": \"" + pushId + "\",\n" +
                "        \"group_id\": \"" + groupdId + "\",\n" +
                "        \"variant_id\":\"" + variant + "\",\n" +
                "        \"session_id\": \"" + sessionId + "\",\n" +
                "        \"type\": \"" + type + "\",\n" +
                "        \"duration\": " + duration + "\n" +
                "    },\n" +
                "    \"type\": \"IN_APP_MESSAGE_RESOLUTION\"\n" +
                "}";
        final Event event = GsonUtil.getGson().fromJson(raw, Event.class);

        assertEquals(event.getEventType(), EventType.IN_APP_MESSAGE_RESOLUTION);

        final InAppMessageResolutionEvent inAppEvent = (InAppMessageResolutionEvent) event.getEventBody();

        assertEquals(Optional.<String>absent(), inAppEvent.getButtonId());
        assertEquals(Optional.<String>absent(), inAppEvent.getButtonGroup());
        assertEquals(Optional.<String>absent(), inAppEvent.getButtonDescription());
        assertEquals(Optional.<AssociatedPush>absent(), inAppEvent.getTriggeringPush());
        assertEquals(Optional.<String>absent(), inAppEvent.getTimeSent());
        assertEquals(duration, inAppEvent.getDuration().get().longValue());
        assertEquals(variant, inAppEvent.getVariantId().get().intValue());
        assertEquals(sessionId, inAppEvent.getSessionId().get());
        assertEquals(type, inAppEvent.getResolutionType());
        assertEquals(pushId, inAppEvent.getPushId());
        assertEquals(groupdId, inAppEvent.getGroupId().get());

        final JsonElement jsonElement = GsonUtil.getGson().toJsonTree(event);

        assertEquals("IN_APP_MESSAGE_RESOLUTION", jsonElement.getAsJsonObject().get("type").getAsString());

        final JsonObject body = jsonElement.getAsJsonObject().get("body").getAsJsonObject();

        assertEquals(null, body.get("time_sent"));
        assertEquals(null, body.get("button_description"));
        assertEquals(null, body.get("button_group"));
        assertEquals(null, body.get("button_id"));
        assertEquals(null, body.get("triggering_push"));

        assertEquals(pushId, body.get("push_id").getAsString());
        assertEquals(groupdId, body.get("group_id").getAsString());
        assertEquals(variant, body.get("variant_id").getAsInt());
        assertEquals(duration, body.get("duration").getAsInt());
        assertEquals(type, body.get("type").getAsString());
        assertEquals(sessionId, body.get("session_id").getAsString());
    }

    @Test
    public void testParseJSONExpired() throws Exception {
        final String pushId = "0e9c711a-bf9d-4b2c-8309-2c5b9e3a5d69";
        final String groupdId = "0e9c711a-bf9d-4b2c-8309-2c5b9e3a5d69";
        final String sessionId = "e65e56a0-8e32-4d26-bba4-43541fff0ec5";
        final int variant = 0;
        final String type = "EXPIRED";

        final String timeSent = "2016-06-08T22:03:17.418Z";
        final DateTime expectedTimeSent = new DateTime(2016, 6, 8, 22, 3, 17, 418, DateTimeZone.UTC);
        final String raw = "{\n" +
                "    \"id\": \"d1599dd2-2dc4-11e6-9a45-90e2ba02f390\",\n" +
                "    \"offset\": \"512149\",\n" +
                "    \"occurred\": \"2016-06-08T22:03:17.418Z\",\n" +
                "    \"processed\": \"2016-06-08T22:03:24.823Z\",\n" +
                "    \"device\": {\n" +
                "        \"android_channel\": \"3a8e2cd0-81b2-4559-a559-67eaae7f3c12\",\n" +
                "        \"named_user_id\": \"dj\",\n" +
                "        \"attributes\": {\n" +
                "            \"locale_variant\": \"\",\n" +
                "            \"app_version\": \"325\",\n" +
                "            \"device_model\": \"SAMSUNG-SGH-I747\",\n" +
                "            \"connection_type\": \"CELL\",\n" +
                "            \"app_package_name\": \"com.urbanairship.goat\",\n" +
                "            \"iana_timezone\": \"America/Los_Angeles\",\n" +
                "            \"push_opt_in\": \"false\",\n" +
                "            \"locale_country_code\": \"US\",\n" +
                "            \"device_os\": \"4.4.2\",\n" +
                "            \"locale_timezone\": \"-25200\",\n" +
                "            \"carrier\": \"AT&T\",\n" +
                "            \"locale_language_code\": \"en\",\n" +
                "            \"location_enabled\": \"false\",\n" +
                "            \"background_push_enabled\": \"true\",\n" +
                "            \"ua_sdk_version\": \"6.2.2\",\n" +
                "            \"location_permission\": \"SYSTEM_LOCATION_DISABLED\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"body\": {\n" +
                "        \"push_id\": \"" + pushId + "\",\n" +
                "        \"group_id\": \"" + groupdId + "\",\n" +
                "        \"variant_id\":\"" + variant + "\",\n" +
                "        \"session_id\": \"" + sessionId + "\",\n" +
                "        \"type\": \"" + type + "\",\n" +
                "        \"time_sent\": \"" + timeSent + "\",\n" +
                "        \"triggering_push\": {" +
                "            \"push_id\": \"" + pushId + "\",\n" +
                "           \"group_id\": \"" + groupdId + "\",\n" +
                "           \"variant_id\":\"" + variant + "\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"type\": \"IN_APP_MESSAGE_RESOLUTION\"\n" +
                "}";
        final Event event = GsonUtil.getGson().fromJson(raw, Event.class);

        assertEquals(event.getEventType(), EventType.IN_APP_MESSAGE_RESOLUTION);

        final InAppMessageResolutionEvent inAppEvent = (InAppMessageResolutionEvent) event.getEventBody();

        assertEquals(Optional.<String>absent(), inAppEvent.getButtonId());
        assertEquals(Optional.<String>absent(), inAppEvent.getButtonGroup());
        assertEquals(Optional.<String>absent(), inAppEvent.getButtonDescription());

        assertEquals(groupdId, inAppEvent.getTriggeringPush().get().getGroupId().get());
        assertEquals(pushId, inAppEvent.getTriggeringPush().get().getPushId().get());
        assertEquals(variant, inAppEvent.getTriggeringPush().get().getVariantId().get().intValue());

        assertEquals(expectedTimeSent, inAppEvent.getTimeSent().get());
        assertEquals(Optional.absent(), inAppEvent.getDuration());
        assertEquals(variant, inAppEvent.getVariantId().get().intValue());
        assertEquals(sessionId, inAppEvent.getSessionId().get());
        assertEquals(type, inAppEvent.getResolutionType());
        assertEquals(pushId, inAppEvent.getPushId());
        assertEquals(groupdId, inAppEvent.getGroupId().get());

        final JsonElement jsonElement = GsonUtil.getGson().toJsonTree(event);

        assertEquals("IN_APP_MESSAGE_RESOLUTION", jsonElement.getAsJsonObject().get("type").getAsString());

        final JsonObject body = jsonElement.getAsJsonObject().get("body").getAsJsonObject();
        assertEquals(pushId, body.get("push_id").getAsString());
        assertEquals(groupdId, body.get("group_id").getAsString());
        assertEquals(variant, body.get("variant_id").getAsInt());
        assertEquals(timeSent, body.get("time_sent").getAsString());
        assertEquals(null, body.get("button_description"));
        assertEquals(null, body.get("button_group"));
        assertEquals(null, body.get("button_id"));
        assertEquals(null, body.get("duration"));
        assertEquals(type, body.get("type").getAsString());
        assertEquals(sessionId, body.get("session_id").getAsString());

        final JsonObject triggering_push = body.get("triggering_push").getAsJsonObject();
        assertEquals(pushId, triggering_push.get("push_id").getAsString());
        assertEquals(groupdId, triggering_push.get("group_id").getAsString());
        assertEquals(variant, triggering_push.get("variant_id").getAsInt());
    }

    @Test
    public void testParseJSONButton() throws Exception {
        final String pushId = "0e9c711a-bf9d-4b2c-8309-2c5b9e3a5d69";
        final String groupdId = "0e9c711a-bf9d-4b2c-8309-2c5b9e3a5d69";
        final String sessionId = "e65e56a0-8e32-4d26-bba4-43541fff0ec5";
        final int variant = 0;
        final String type = "BUTTON_CLICK";
        final int duration = 1471;

        final String timeSent = "2016-06-08T22:03:17.418Z";
        final DateTime expectedTimeSent = new DateTime(2016, 6, 8, 22, 3, 17, 418, DateTimeZone.UTC);
        final String buttonId = "a very unique identifier";
        final String buttonGroup = "some buttons in a group";
        final String buttonDescription = "this button is dope";
        final String raw = "{\n" +
                "    \"id\": \"d1599dd2-2dc4-11e6-9a45-90e2ba02f390\",\n" +
                "    \"offset\": \"512149\",\n" +
                "    \"occurred\": \"2016-06-08T22:03:17.418Z\",\n" +
                "    \"processed\": \"2016-06-08T22:03:24.823Z\",\n" +
                "    \"device\": {\n" +
                "        \"android_channel\": \"3a8e2cd0-81b2-4559-a559-67eaae7f3c12\",\n" +
                "        \"named_user_id\": \"dj\",\n" +
                "        \"attributes\": {\n" +
                "            \"locale_variant\": \"\",\n" +
                "            \"app_version\": \"325\",\n" +
                "            \"device_model\": \"SAMSUNG-SGH-I747\",\n" +
                "            \"connection_type\": \"CELL\",\n" +
                "            \"app_package_name\": \"com.urbanairship.goat\",\n" +
                "            \"iana_timezone\": \"America/Los_Angeles\",\n" +
                "            \"push_opt_in\": \"false\",\n" +
                "            \"locale_country_code\": \"US\",\n" +
                "            \"device_os\": \"4.4.2\",\n" +
                "            \"locale_timezone\": \"-25200\",\n" +
                "            \"carrier\": \"AT&T\",\n" +
                "            \"locale_language_code\": \"en\",\n" +
                "            \"location_enabled\": \"false\",\n" +
                "            \"background_push_enabled\": \"true\",\n" +
                "            \"ua_sdk_version\": \"6.2.2\",\n" +
                "            \"location_permission\": \"SYSTEM_LOCATION_DISABLED\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"body\": {\n" +
                "        \"push_id\": \"" + pushId + "\",\n" +
                "        \"group_id\": \"" + groupdId + "\",\n" +
                "        \"variant_id\":\"" + variant + "\",\n" +
                "        \"session_id\": \"" + sessionId + "\",\n" +
                "        \"type\": \"" + type + "\",\n" +
                "        \"duration\": " + duration + ",\n" +
                "        \"time_sent\": \"" + timeSent + "\",\n" +
                "        \"button_id\": \"" + buttonId + "\",\n" +
                "        \"button_group\": \"" + buttonGroup + "\",\n" +
                "        \"button_description\": \"" + buttonDescription + "\",\n" +
                "        \"triggering_push\": {" +
                "            \"push_id\": \"" + pushId + "\",\n" +
                "           \"group_id\": \"" + groupdId + "\",\n" +
                "           \"variant_id\":\"" + variant + "\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"type\": \"IN_APP_MESSAGE_RESOLUTION\"\n" +
                "}";
        final Event event = GsonUtil.getGson().fromJson(raw, Event.class);

        assertEquals(event.getEventType(), EventType.IN_APP_MESSAGE_RESOLUTION);

        final InAppMessageResolutionEvent inAppEvent = (InAppMessageResolutionEvent) event.getEventBody();

        assertEquals(buttonDescription, inAppEvent.getButtonDescription().get());
        assertEquals(buttonGroup, inAppEvent.getButtonGroup().get());
        assertEquals(buttonId, inAppEvent.getButtonId().get());

        assertEquals(groupdId, inAppEvent.getTriggeringPush().get().getGroupId().get());
        assertEquals(pushId, inAppEvent.getTriggeringPush().get().getPushId().get());
        assertEquals(variant, inAppEvent.getTriggeringPush().get().getVariantId().get().intValue());

        assertEquals(expectedTimeSent, inAppEvent.getTimeSent().get());
        assertEquals(duration, inAppEvent.getDuration().get().longValue());
        assertEquals(variant, inAppEvent.getVariantId().get().intValue());
        assertEquals(sessionId, inAppEvent.getSessionId().get());
        assertEquals(type, inAppEvent.getResolutionType());
        assertEquals(pushId, inAppEvent.getPushId());
        assertEquals(groupdId, inAppEvent.getGroupId().get());

        final JsonElement jsonElement = GsonUtil.getGson().toJsonTree(event);

        assertEquals("IN_APP_MESSAGE_RESOLUTION", jsonElement.getAsJsonObject().get("type").getAsString());

        final JsonObject body = jsonElement.getAsJsonObject().get("body").getAsJsonObject();
        assertEquals(pushId, body.get("push_id").getAsString());
        assertEquals(groupdId, body.get("group_id").getAsString());
        assertEquals(variant, body.get("variant_id").getAsInt());
        assertEquals(timeSent, body.get("time_sent").getAsString());
        assertEquals(buttonDescription, body.get("button_description").getAsString());
        assertEquals(buttonGroup, body.get("button_group").getAsString());
        assertEquals(buttonId, body.get("button_id").getAsString());
        assertEquals(duration, body.get("duration").getAsInt());
        assertEquals(type, body.get("type").getAsString());
        assertEquals(sessionId, body.get("session_id").getAsString());

        final JsonObject triggering_push = body.get("triggering_push").getAsJsonObject();
        assertEquals(pushId, triggering_push.get("push_id").getAsString());
        assertEquals(groupdId, triggering_push.get("group_id").getAsString());
        assertEquals(variant, triggering_push.get("variant_id").getAsInt());
    }

    @Test
    public void testBuilder() throws Exception {
        final String pushId = UUID.randomUUID().toString();
        final int variantID = 1;
        final DateTime timeSent = DateTime.now();
        final long duration = 1000L;
        final String type = "USER_DISMISSED";
        final String sessionId = UUID.randomUUID().toString();

        final InAppMessageResolutionEvent event = InAppMessageResolutionEvent.newBuilder()
                .setPushId(pushId)
                .setVariantId(variantID)
                .setTimeSent(timeSent)
                .setDuration(duration)
                .setType(type)
                .setSessionId(sessionId).build();

        assertEquals(Optional.<String>absent(), event.getButtonDescription());
        assertEquals(Optional.<String>absent(), event.getButtonGroup());
        assertEquals(Optional.<String>absent(), event.getButtonId());
        assertEquals(Optional.absent(), event.getTriggeringPush());
        assertEquals(Optional.<String>absent(), event.getGroupId());
        assertEquals(duration, event.getDuration().get().longValue());
        assertEquals(pushId, event.getPushId());
        assertEquals(variantID, event.getVariantId().get().intValue());
        assertEquals(type, event.getResolutionType());
        assertEquals(sessionId, event.getSessionId().get());
        assertEquals(timeSent, event.getTimeSent().get());
    }
}