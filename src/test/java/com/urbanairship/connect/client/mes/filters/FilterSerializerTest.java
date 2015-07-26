package com.urbanairship.connect.client.mes.filters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.urbanairship.connect.client.filters.DeviceFilter;
import com.urbanairship.connect.client.filters.DeviceFilterSerializer;
import com.urbanairship.connect.client.filters.Filter;
import com.urbanairship.connect.client.filters.NotificationFilter;
import com.urbanairship.connect.client.filters.OptionalSerializer;
import com.urbanairship.connect.client.filters.DeviceIdType;
import com.urbanairship.connect.client.filters.EventType;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class FilterSerializerTest {

    Gson gson = new GsonBuilder()
        .registerTypeAdapter(DeviceFilter.class, new DeviceFilterSerializer())
        .registerTypeAdapter(Optional.class, new OptionalSerializer())
        .create();

    @Test
    public void testMaxFilter() throws Exception {

        DeviceFilter device1 = new DeviceFilter(DeviceIdType.ANDROID, "c8044c8a-d5fa-4e58-91d4-54d0f70b7409");
        DeviceFilter device2 = new DeviceFilter(DeviceIdType.IOS, "3d970087-600e-4bb6-8474-5857d438faaa");
        DeviceFilter device3 = new DeviceFilter(DeviceIdType.NAMED_USER, "cool user");
        NotificationFilter notification = NotificationFilter.createGroupIdFilter("a30abf06-7878-4096-9535-b50ac0ad6e8e");

        Filter filter = Filter.newBuilder()
            .setLatency(20000000)
            .addDevices(device1, device2, device3)
            .addDeviceTypes(DeviceIdType.ANDROID, DeviceIdType.AMAZON)
            .addNotification(notification)
            .addType(EventType.OPEN)
            .build();

        String json = "{" +
              "\"device_types\":[" +
                "\"amazon\"," +
                "\"android\"" +
              "]," +
              "\"notifications\":[" +
                "{" +
                  "\"group_id\":\"a30abf06-7878-4096-9535-b50ac0ad6e8e\"" +
                "}" +
              "]," +
              "\"devices\":[" +
                "{" +
                  "\"android_channel\":\"c8044c8a-d5fa-4e58-91d4-54d0f70b7409\"" +
                "}," +
                "{" +
                  "\"named_user_id\":\"cool user\"" +
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

        assertEquals(json, gson.toJson(filter));
    }

    @Test
    public void testMinFilter() throws Exception {
        Filter filter = Filter.newBuilder().setLatency(100).build();
        String json = "{\"latency\":100}";

        assertEquals(json, gson.toJson(filter));
    }
}
