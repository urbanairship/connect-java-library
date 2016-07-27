package com.urbanairship.connect.client.model.request.filters;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.urbanairship.connect.client.model.GsonUtil;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class NotificationFilterSerializationTest {

    private static final JsonParser parser = new JsonParser();

    @Test
    public void testSerialization() throws Exception {
        verifyFilterForType(NotificationFilter.Type.GROUP_ID, "group_id");
        verifyFilterForType(NotificationFilter.Type.PUSH_ID, "push_id");
    }

    private void verifyFilterForType(NotificationFilter.Type type, String expectedKey) {
        String value = UUID.randomUUID().toString();

        NotificationFilter filter = new NotificationFilter(type, value);

        JsonElement obj = GsonUtil.getGson().toJsonTree(filter);

        JsonElement expected = parser.parse(String.format("{\"%s\":\"%s\"}", expectedKey, value));

        assertEquals(expected, obj);
    }
}