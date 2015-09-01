package com.urbanairship.connect.client.model.responses;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.JsonParseException;
import com.urbanairship.connect.client.model.DeviceFilterType;
import com.urbanairship.connect.client.model.GsonUtil;
import com.urbanairship.connect.client.model.responses.region.Proximity;
import com.urbanairship.connect.client.model.responses.region.RegionAction;
import com.urbanairship.connect.client.model.responses.region.RegionEvent;
import com.urbanairship.connect.client.model.responses.region.RegionSource;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ModelParsingTest {

    /* Close Event Tests */

    @Test
    public void testCloseEventParsing() throws Exception {
        String sessionId = UUID.randomUUID().toString();
        CloseEvent closeEvent = new CloseEvent(sessionId);

        String json = new String(closeEvent.serializeToJSONBytes(), StandardCharsets.UTF_8);
        CloseEvent parsedCloseEvent = CloseEvent.parseJSON(json);

        assertEquals(sessionId, parsedCloseEvent.getSessionId());
        assertEquals(closeEvent, parsedCloseEvent);
    }

    /* Custom Event Tests */

    @Test
    public void testCustomMaxEventParsing() throws  Exception {
        String name = "event-name";
        Optional<Integer> value = Optional.of(60);
        String interactionType = "Landing Page";
        String interactionId = UUID.randomUUID().toString();
        Optional<String> customerId = Optional.of("George@hotmail.com");
        Optional<String> transactionId = Optional.of("selling all the shoes");
        String lastDeliveredPushId = UUID.randomUUID().toString();
        Optional<String> lastDeliveredGroupId = Optional.of(UUID.randomUUID().toString());
        Optional<Integer> lastDeliveredVariantId = Optional.of(2);
        AssociatedPush lastDelivered = new AssociatedPush(lastDeliveredPushId, lastDeliveredGroupId, lastDeliveredVariantId, Optional.<Instant>empty());
        String triggeringPushPushId = UUID.randomUUID().toString();
        Optional<String> triggeringPushGroupId = Optional.of(UUID.randomUUID().toString());
        Optional<Instant> triggeringPushTime = Optional.of(Instant.now());
        AssociatedPush triggeringPush = new AssociatedPush(triggeringPushPushId, triggeringPushGroupId, Optional.<Integer>empty(), triggeringPushTime);

        CustomEvent customEvent = new CustomEvent(name, value, transactionId, customerId, interactionId, interactionType, Optional.of(lastDelivered), Optional.of(triggeringPush));
        String json = new String(customEvent.serializeToJSONBytes(), StandardCharsets.UTF_8);

        CustomEvent parsedCustomEvent = CustomEvent.parseJSON(json);
        assertEquals(name, parsedCustomEvent.getName());
        assertEquals(value.get(), parsedCustomEvent.getValue().get());
        assertEquals(interactionType, parsedCustomEvent.getInteractionType());
        assertEquals(interactionId, parsedCustomEvent.getInteractionId());
        assertEquals(customerId.get(), parsedCustomEvent.getCustomerId().get());
        assertEquals(transactionId.get(), parsedCustomEvent.getTransactionalId().get());
        assertEquals(lastDeliveredPushId, parsedCustomEvent.getLastDelivered().get().getPushId());
        assertEquals(lastDeliveredGroupId.get(), parsedCustomEvent.getLastDelivered().get().getGroupId().get());
        assertEquals(lastDeliveredVariantId.get(), parsedCustomEvent.getLastDelivered().get().getVariantId().get());
        assertEquals(lastDelivered, parsedCustomEvent.getLastDelivered().get());
        assertEquals(triggeringPushPushId, parsedCustomEvent.getTriggeringPush().get().getPushId());
        assertEquals(triggeringPushGroupId.get(), parsedCustomEvent.getTriggeringPush().get().getGroupId().get());
        assertEquals(triggeringPushTime.get(), parsedCustomEvent.getTriggeringPush().get().getTime().get());
        assertEquals(triggeringPush, parsedCustomEvent.getTriggeringPush().get());
        assertEquals(customEvent, parsedCustomEvent);

        CustomEvent parsedFromBytesCustomEvent = CustomEvent.parseJSONfromBytes(customEvent.serializeToJSONBytes());
        assertEquals(customEvent, parsedFromBytesCustomEvent);
    }

    @Test
    public void testCustomEventWithNullsParsing() throws Exception {
        String name = "event-name";
        String interactionType = "Landing Page";
        String interactionId = UUID.randomUUID().toString();
        String lastDeliveredPushId = UUID.randomUUID().toString();
        AssociatedPush lastDelivered = new AssociatedPush(lastDeliveredPushId, Optional.<String>empty(), Optional.<Integer>empty(), Optional.<Instant>empty());
        String triggeringPushPushId = UUID.randomUUID().toString();
        String triggeringPushGroupId = UUID.randomUUID().toString();
        AssociatedPush triggeringPush = new AssociatedPush(triggeringPushPushId, Optional.of(triggeringPushGroupId), Optional.<Integer>empty(), Optional.<Instant>empty());

        CustomEvent customEvent = new CustomEvent(name, Optional.empty(), Optional.empty(), Optional.empty(), interactionId, interactionType, Optional.of(lastDelivered), Optional.of(triggeringPush));
        String json = new String(customEvent.serializeToJSONBytes(), StandardCharsets.UTF_8);

        CustomEvent parsedCustomEvent = CustomEvent.parseJSON(json);
        assertEquals(Optional.<Integer>empty(), parsedCustomEvent.getValue());
        assertEquals(Optional.<String>empty(), parsedCustomEvent.getCustomerId());
        assertEquals(Optional.<String>empty(), parsedCustomEvent.getTransactionalId());
        assertEquals(name, parsedCustomEvent.getName());
        assertEquals(interactionType, parsedCustomEvent.getInteractionType());
        assertEquals(interactionId, parsedCustomEvent.getInteractionId());
        assertEquals(lastDeliveredPushId, parsedCustomEvent.getLastDelivered().get().getPushId());
        assertEquals(lastDelivered, parsedCustomEvent.getLastDelivered().get());
        assertEquals(triggeringPushPushId, parsedCustomEvent.getTriggeringPush().get().getPushId());
        assertEquals(triggeringPushGroupId, parsedCustomEvent.getTriggeringPush().get().getGroupId().get());
        assertEquals(triggeringPush, parsedCustomEvent.getTriggeringPush().get());
        assertEquals(customEvent, parsedCustomEvent);

        CustomEvent parsedFromBytesCustomEvent = CustomEvent.parseJSONfromBytes(customEvent.serializeToJSONBytes());
        assertEquals(customEvent, parsedFromBytesCustomEvent);
    }

    /* Device Info Tests */

    @Test
    public void testDeviceInfoParsing() throws Exception {
        String iosChannel = UUID.randomUUID().toString();
        Optional<String> namedUserId = Optional.of(UUID.randomUUID().toString());
        DeviceInfo deviceInfo = DeviceInfo.newBuilder()
                .setChanneId(iosChannel)
                .setPlatform(DeviceFilterType.IOS)
                .setNamedUsedId(namedUserId)
                .build();

        String json = new String(deviceInfo.serializeToJSONBytes(), StandardCharsets.UTF_8);
        DeviceInfo parsedDeviceInfo = DeviceInfo.parseJSON(json);

        assertEquals(iosChannel, parsedDeviceInfo.getChannelId());
        assertEquals(namedUserId.get(), parsedDeviceInfo.getNamedUsedId().get());
        assertEquals(DeviceFilterType.IOS, parsedDeviceInfo.getPlatform());
    }

    @Test(expected = JsonParseException.class)
    public void testDeviceInfoMultipleChannelsParsingException() throws Exception {
        String iosChannel = UUID.randomUUID().toString();
        String androidChannel = UUID.randomUUID().toString();
        String namedUserId = UUID.randomUUID().toString();
        Map<String, String> rawJson = Maps.newHashMap();
        rawJson.put(DeviceFilterType.IOS.getKey(), iosChannel);
        rawJson.put(DeviceFilterType.ANDROID.getKey(), androidChannel);
        rawJson.put(DeviceFilterType.AMAZON.getKey(), namedUserId);
        String json = GsonUtil.getGson().toJson(rawJson);
        DeviceInfo parsedDeviceInfo = DeviceInfo.parseJSON(json);
    }

    /* Location Event Tests */

    @Test
    public void testLocationEventParsing() throws Exception {
        double latitude = 51.5033630;
        double longitude = -0.1276250;
        boolean foreground = true;
        Optional<String> sessionId = Optional.of(UUID.randomUUID().toString());
        LocationEvent locationEvent = new LocationEvent(latitude, longitude, foreground, sessionId);

        String json = new String(locationEvent.serializeToJSONBytes(), StandardCharsets.UTF_8);
        LocationEvent parsedLocationEvent = LocationEvent.parseJSON(json);

        assertTrue(latitude == parsedLocationEvent.getLatitude());
        assertTrue(longitude == parsedLocationEvent.getLongitude());
        assertTrue(parsedLocationEvent.isForeground());
        assertEquals(sessionId.get(), parsedLocationEvent.getSessionId().get());
    }

    /* Open Event Tests */

    @Test
    public void testMaxOpenEventParsing() throws Exception {
        String lastDeliveredPushId = UUID.randomUUID().toString();
        Optional<String> lastDeliveredGroupId = Optional.of(UUID.randomUUID().toString());
        Optional<AssociatedPush> lastDelivered = Optional.of(new AssociatedPush(lastDeliveredPushId, lastDeliveredGroupId, Optional.<Integer>empty(), Optional.<Instant>empty()));
        String triggeringPushPushId = UUID.randomUUID().toString();
        Optional<String> triggeringPushGroupId = Optional.of(UUID.randomUUID().toString());
        Optional<AssociatedPush> triggeringPush = Optional.of(new AssociatedPush(triggeringPushPushId, triggeringPushGroupId, Optional.<Integer>empty(), Optional.<Instant>empty()));
        Optional<String> sessionId = Optional.of(UUID.randomUUID().toString());
        OpenEvent openEvent = new OpenEvent(lastDelivered, triggeringPush, sessionId);

        String json = new String(openEvent.serializeToJSONBytes(), StandardCharsets.UTF_8);
        OpenEvent parsedOpenEvent = OpenEvent.parseJSON(json);

        assertEquals(lastDeliveredPushId, parsedOpenEvent.getLastDelivered().get().getPushId());
        assertEquals(lastDeliveredGroupId.get(), parsedOpenEvent.getLastDelivered().get().getGroupId().get());
        assertEquals(lastDelivered.get(), parsedOpenEvent.getLastDelivered().get());
        assertEquals(triggeringPushPushId, parsedOpenEvent.getTriggeringPush().get().getPushId());
        assertEquals(triggeringPushGroupId.get(), parsedOpenEvent.getTriggeringPush().get().getGroupId().get());
        assertEquals(triggeringPush.get(), parsedOpenEvent.getTriggeringPush().get());
        assertEquals(sessionId.get(), parsedOpenEvent.getSessionId().get());
    }

    @Test
    public void testOpenEventWithNullsParsing() throws Exception {
        String lastDeliveredPushId = UUID.randomUUID().toString();
        Optional<AssociatedPush> lastDelivered = Optional.of(new AssociatedPush(lastDeliveredPushId, Optional.<String>empty(), Optional.<Integer>empty(), Optional.<Instant>empty()));
        Optional<String> sessionId = Optional.of(UUID.randomUUID().toString());
        OpenEvent openEvent = new OpenEvent(lastDelivered, Optional.<AssociatedPush>empty(), sessionId);

        String json = new String(openEvent.serializeToJSONBytes(), StandardCharsets.UTF_8);
        OpenEvent parsedOpenEvent = OpenEvent.parseJSON(json);

        assertEquals(lastDeliveredPushId, parsedOpenEvent.getLastDelivered().get().getPushId());
        assertEquals(lastDelivered.get(), parsedOpenEvent.getLastDelivered().get());
        assertEquals(sessionId.get(), parsedOpenEvent.getSessionId().get());
        assertFalse(parsedOpenEvent.getTriggeringPush().isPresent());
    }

    @Test
    public void testEmptyOpenEventParsing() throws Exception {
        OpenEvent openEvent = new OpenEvent(Optional.<AssociatedPush>empty(), Optional.<AssociatedPush>empty(), Optional.<String>empty());
        String json = new String(openEvent.serializeToJSONBytes(), StandardCharsets.UTF_8);
        OpenEvent parsedOpenEvent = OpenEvent.parseJSON(json);

        assertFalse(parsedOpenEvent.getSessionId().isPresent());
        assertFalse(parsedOpenEvent.getLastDelivered().isPresent());
        assertFalse(parsedOpenEvent.getTriggeringPush().isPresent());
    }

    /* Push Body Tests */

    @Test
    public void testPushBodyParsing() throws Exception {
        String pushId = UUID.randomUUID().toString();
        Optional<String> groupId = Optional.of(UUID.randomUUID().toString());
        boolean trimmed = false;
        String payload = "eyJkZXZpY2VfdHlwZXMiOiBbImFuZHJvaWQiLCAiaW9zIl0sICJub3RpZmljYXRpb24iOiB7ImFuZHJvaWQiOiB7fSwgImlvcyI6IHsiYmFkZ2UiOiAiKzEifSwgImFsZXJ0IjogIklUIFdJTEwgV09SSyEifSwgImF1ZGllbmNlIjogImFsbCJ9";
        PushBody pushBody = new PushBody(pushId, groupId, trimmed, payload);

        String json = new String(pushBody.serializeToJSONBytes(), StandardCharsets.UTF_8);
        PushBody parsedPushBody = PushBody.parseJSON(json);

        assertEquals(pushId, parsedPushBody.getPushId());
        assertEquals(groupId.get(), parsedPushBody.getGroupId().get());
        assertFalse(parsedPushBody.isTrimmed());
        assertEquals(payload, parsedPushBody.getPayload());
    }

    /* Send Event Tests */

    @Test
    public void testSendEventParsing() throws Exception {
        String pushId = UUID.randomUUID().toString();
        Optional<String> groupId = Optional.of(UUID.randomUUID().toString());
        Optional<Integer> variantId = Optional.of(1);
        SendEvent sendEvent = new SendEvent(pushId, groupId, variantId);

        String json = new String(sendEvent.serializeToJSONBytes(), StandardCharsets.UTF_8);
        SendEvent parsedSendEvent = SendEvent.parseJSON(json);

        assertEquals(pushId, parsedSendEvent.getPushId());
        assertEquals(groupId.get(), parsedSendEvent.getGroupId().get());
        assertEquals(variantId.get(), parsedSendEvent.getVariantId().get());
    }

    /* Tag Change Tests */

    @Test
    public void testTagChangeParsing() throws Exception {
        Multimap<String, String> add = ArrayListMultimap.create();
        add.putAll("add_group_1", Lists.newArrayList("tag1", "tag2", "tag3"));
        add.putAll("add_group_2", Lists.newArrayList("tag1", "tag2"));
        add.putAll("add_group_3", Lists.newArrayList("tag1", "tag2", "tag3", "tag4"));

        Multimap<String, String> remove = ArrayListMultimap.create();
        remove.putAll("remove_group_1", Lists.newArrayList("tag1"));

        Multimap<String, String> current = ArrayListMultimap.create();
        current.putAll("current_group_2", Lists.newArrayList("tag1", "tag2", "tag3"));
        current.putAll("current_group_2", Lists.newArrayList("tag1", "tag2", "tag3"));

        TagChange tagChange = TagChange.newBuilder()
                .setTagAdd(add.asMap())
                .setTagRemove(remove.asMap())
                .setTagCurrent(current.asMap())
                .build();
        String json = new String(tagChange.serializeToJSONBytes(), StandardCharsets.UTF_8);
        TagChange parsedTagChange = TagChange.parseJSON(json);

        assertEquals(add.asMap(), parsedTagChange.getTagAdd());
        assertEquals(remove.asMap(), parsedTagChange.getTagRemove());
        assertEquals(current.asMap(), parsedTagChange.getTagCurrent());
    }

    /* Region Event Tests */

    @Test
    public void testRegionEventParsing() throws Exception {

        String beaconId = UUID.randomUUID().toString();
        double lat = 38.324420;
        double longitude = -112.148438;
        int major = 12;
        int minor = 3;
        int rssi = 100;

        Proximity proximity = Proximity.newBuilder()
            .setBeaconId(beaconId)
            .setLatitude(lat)
            .setLongitude(longitude)
            .setMajor(major)
            .setMinor(minor)
            .setRssi(rssi)
            .build();

        String regionId = UUID.randomUUID().toString();
        String sessionId = UUID.randomUUID().toString();

        RegionEvent regionEvent = RegionEvent.newBuilder()
            .setRegionId(regionId)
            .setAction(RegionAction.ENTER)
            .setSessionId(sessionId)
            .setSource(RegionSource.GIMBAL)
            .setProximity(proximity)
            .build();

        String json = new String(regionEvent.serializeToJSONBytes(), StandardCharsets.UTF_8);
        RegionEvent parsedRegionEvent = RegionEvent.parseJSON(json);

        assertEquals(parsedRegionEvent.getAction(), RegionAction.ENTER);
        assertEquals(parsedRegionEvent.getSource(), RegionSource.GIMBAL);
        assertEquals(parsedRegionEvent.getSessionId(), sessionId);
        assertEquals(parsedRegionEvent.getRegionId(), regionId);
        assertEquals(parsedRegionEvent.getProximity().get().getBeaconId(), beaconId);
        assertEquals(parsedRegionEvent.getProximity().get().getBeaconId(), beaconId);
        assertTrue(parsedRegionEvent.getProximity().get().getLat() == lat);
        assertTrue(parsedRegionEvent.getProximity().get().getLongitude() == longitude);
        assertEquals(parsedRegionEvent.getProximity().get().getMajor(), major);
        assertEquals(parsedRegionEvent.getProximity().get().getMinor(), minor);
        assertEquals(parsedRegionEvent.getProximity().get().getRssi(), rssi);
    }

        /* Rich Event Tests */

    @Test
    public void testRichDeleteEventParsing() throws Exception {
        String pushId = UUID.randomUUID().toString();
        Optional<String> groupId = Optional.of(UUID.randomUUID().toString());
        Optional<Integer> variantId = Optional.of(1);
        RichDeleteEvent richDeleteEvent = new RichDeleteEvent(pushId, groupId, variantId);

        String json = new String(richDeleteEvent.serializeToJSONBytes(), StandardCharsets.UTF_8);
        RichDeleteEvent parsedRichEvent = RichDeleteEvent.parseJSON(json);

        assertEquals(pushId, parsedRichEvent.getPushId());
        assertEquals(groupId.get(), parsedRichEvent.getGroupId().get());
        assertEquals(variantId.get(), parsedRichEvent.getVariantId().get());
    }

    @Test
    public void testRichReadEventParsing() throws Exception {
        String pushId = UUID.randomUUID().toString();
        Optional<String> groupId = Optional.of(UUID.randomUUID().toString());
        Optional<Integer> variantId = Optional.of(1);
        RichReadEvent richReadEvent = new RichReadEvent(pushId, groupId, variantId);

        String json = new String(richReadEvent.serializeToJSONBytes(), StandardCharsets.UTF_8);
        RichReadEvent parsedRichEvent = RichReadEvent.parseJSON(json);

        assertEquals(pushId, parsedRichEvent.getPushId());
        assertEquals(groupId.get(), parsedRichEvent.getGroupId().get());
        assertEquals(variantId.get(), parsedRichEvent.getVariantId().get());
    }

    @Test
    public void testRichDeliveryEventParsing() throws Exception {
        String pushId = UUID.randomUUID().toString();
        Optional<String> groupId = Optional.of(UUID.randomUUID().toString());
        Optional<Integer> variantId = Optional.of(1);
        RichDeliveryEvent richDeliveryEvent = new RichDeliveryEvent(pushId, groupId, variantId);

        String json = new String(richDeliveryEvent.serializeToJSONBytes(), StandardCharsets.UTF_8);
        RichDeliveryEvent parsedRichEvent = RichDeliveryEvent.parseJSON(json);

        assertEquals(pushId, parsedRichEvent.getPushId());
        assertEquals(groupId.get(), parsedRichEvent.getGroupId().get());
        assertEquals(variantId.get(), parsedRichEvent.getVariantId().get());
    }
}
