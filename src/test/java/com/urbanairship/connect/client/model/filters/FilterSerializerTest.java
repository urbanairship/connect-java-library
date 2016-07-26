/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model.filters;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.urbanairship.connect.client.model.DeviceFilterType;
import com.urbanairship.connect.client.model.EventType;
import com.urbanairship.connect.client.model.GsonUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FilterSerializerTest {

    private static final Gson gson = GsonUtil.getGson();

    @Test
    public void testMaxFilter() throws Exception {

        DeviceFilter device1 = new DeviceFilter(DeviceFilterType.ANDROID, "c8044c8a-d5fa-4e58-91d4-54d0f70b7409");
        DeviceFilter device2 = new DeviceFilter(DeviceFilterType.IOS, "3d970087-600e-4bb6-8474-5857d438faaa");
        DeviceFilter device3 = new DeviceFilter(DeviceFilterType.NAMED_USER, "cool user");
        NotificationFilter notification = NotificationFilter.createGroupIdFilter("a30abf06-7878-4096-9535-b50ac0ad6e8e");

        Filter filter = Filter.newBuilder()
            .setLatency(20000000)
            .addDevices(device1, device2, device3)
            .addDeviceTypes(DeviceFilterType.ANDROID, DeviceFilterType.AMAZON)
            .addNotification(notification)
            .addType(EventType.OPEN)
            .build();

        String json = "{" +
              "\"device_types\":[" +
                "\"android\"," +
                "\"amazon\"" +
              "]," +
              "\"notifications\":[" +
                "{" +
                  "\"group_id\":\"a30abf06-7878-4096-9535-b50ac0ad6e8e\"" +
                "}" +
              "]," +
              "\"devices\":[" +
                "{" +
                  "\"named_user_id\":\"cool user\"" +
                "}," +
                "{" +
                  "\"android_channel\":\"c8044c8a-d5fa-4e58-91d4-54d0f70b7409\"" +
                "}," +
                "{" +
                  "\"ios_channel\":\"3d970087-600e-4bb6-8474-5857d438faaa\"" +
                "}" +
            "]," +
              "\"types\":[" +
                "\"OPEN\"" +
              "]," +
            "\"latency\":20000000" +
            "}";

        JsonObject expectedJsonObject = new JsonObject();
        expectedJsonObject.addProperty("filters", json);

        JsonObject filterJsonObject = new JsonObject();
        filterJsonObject.addProperty("filters", gson.toJson(filter));

        assertEquals(expectedJsonObject.get("device_types"), filterJsonObject.get("device_types"));
        assertEquals(expectedJsonObject.get("notifications"), filterJsonObject.get("notifications"));
        assertEquals(expectedJsonObject.get("devices"), filterJsonObject.get("devices"));
        assertEquals(expectedJsonObject.get("types"), filterJsonObject.get("types"));
        assertEquals(expectedJsonObject.get("latency"), filterJsonObject.get("latency"));
    }

    @Test
    public void testMinFilter() throws Exception {
        Filter filter = Filter.newBuilder().setLatency(100).build();
        String json = "{\"latency\":100}";

        assertEquals(json, gson.toJson(filter));
    }
}
