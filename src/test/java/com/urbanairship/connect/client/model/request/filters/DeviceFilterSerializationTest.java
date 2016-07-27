package com.urbanairship.connect.client.model.request.filters;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.urbanairship.connect.client.model.GsonUtil;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class DeviceFilterSerializationTest {

    private static final JsonParser parser = new JsonParser();

    @Test
    public void testSerialization() throws Exception {
        verifyFilterForType(DeviceFilterType.AMAZON_CHANNEL, "amazon_channel");
        verifyFilterForType(DeviceFilterType.ANDROID_CHANNEL, "android_channel");
        verifyFilterForType(DeviceFilterType.IOS_CHANNEL, "ios_channel");
        verifyFilterForType(DeviceFilterType.NAMED_USER_ID, "named_user_id");
    }

    private void verifyFilterForType(DeviceFilterType type, String expectedKey) {
        String value = UUID.randomUUID().toString();

        DeviceFilter filter = new DeviceFilter(type, value);

        JsonElement obj = GsonUtil.getGson().toJsonTree(filter);

        JsonElement expected = parser.parse(String.format("{\"%s\":\"%s\"}", expectedKey, value));

        assertEquals(expected, obj);
    }
}