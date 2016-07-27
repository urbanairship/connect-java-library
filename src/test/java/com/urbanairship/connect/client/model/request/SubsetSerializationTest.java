package com.urbanairship.connect.client.model.request;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.urbanairship.connect.client.model.GsonUtil;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class SubsetSerializationTest {

    private static final JsonParser parser = new JsonParser();

    @Test
    public void testSerializeSampleSubset() throws Exception {
        Subset subset = Subset.createSampleSubset(0.435f);

        JsonElement obj = GsonUtil.getGson().toJsonTree(subset);

        JsonElement expected = parser.parse("{\"type\":\"SAMPLE\",\"proportion\":0.435}");

        assertEquals(expected.getAsJsonObject().get("type").getAsString(), obj.getAsJsonObject().get("type").getAsString());
        assertEquals(expected.getAsJsonObject().get("proportion").getAsFloat(), obj.getAsJsonObject().get("proportion").getAsFloat(), 0.0001f);
    }

    @Test
    public void testSerializePartitionSubset() throws Exception {
        int count = RandomUtils.nextInt(2, 100);
        int selection = RandomUtils.nextInt(0, count);

        Subset subset = Subset.createPartitionSubset()
                .setCount(count)
                .setSelection(selection)
                .build();

        JsonElement obj = GsonUtil.getGson().toJsonTree(subset);

        JsonElement expected = parser.parse(String.format("{\"type\":\"%s\",\"count\":%d,\"selection\":%d}", "PARTITION", count, selection));

        assertEquals(expected, obj);
    }

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testInvalidProportion() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Proportion value must be between 0 and 1");

        Subset.createSampleSubset(2f);
    }

    @Test
    public void testMissingCountPartition() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Values for selection and count must be set for PARTITION subset");

        Subset.createPartitionSubset()
                .setSelection(0)
                .build();
    }

    @Test
    public void testMissingSelectionPartition() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Values for selection and count must be set for PARTITION subset");

        Subset.createPartitionSubset()
                .setCount(1)
                .build();
    }

    @Test
    public void testInvalidCount() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Count must be > 0");

        Subset.createPartitionSubset()
                .setCount(0)
                .setSelection(1)
                .build();
    }

    @Test
    public void testInvalidSelection() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Selection must be less than count");

        Subset.createPartitionSubset()
                .setCount(2)
                .setSelection(5)
                .build();
    }
}