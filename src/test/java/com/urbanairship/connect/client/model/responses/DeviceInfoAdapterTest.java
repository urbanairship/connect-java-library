package com.urbanairship.connect.client.model.responses;

import com.google.gson.JsonObject;
import com.urbanairship.connect.client.model.DeviceFilterType;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class DeviceInfoAdapterTest {


    @Test
    public void deserialize() throws Exception {
        final DeviceInfoAdapter deviceInfoAdapter = new DeviceInfoAdapter();

        final JsonObject deviceInfo = new JsonObject();

        final String channel = UUID.randomUUID().toString();

        deviceInfo.addProperty("android_channel", channel);
        final JsonObject attributes = new JsonObject();
        final JsonObject identifiers = new JsonObject();

        attributes.addProperty("yes", "no");
        identifiers.addProperty("balooga", "whale");
        deviceInfo.add("attributes", attributes);
        deviceInfo.add("identifiers", identifiers);

        final DeviceInfo deserialized = deviceInfoAdapter.deserialize(deviceInfo, DeviceInfo.class, null);

        System.out.println(deserialized);
        assertEquals(deserialized.getAttributes().get("yes"), "no");
        assertEquals(deserialized.getIdentifiers().get("balooga"), "whale");
        assertEquals(deserialized.getPlatform(), DeviceFilterType.ANDROID);
        assertEquals(deserialized.getChannelId(), channel);
    }

    @Test
    public void serialize() throws Exception {

    }

}