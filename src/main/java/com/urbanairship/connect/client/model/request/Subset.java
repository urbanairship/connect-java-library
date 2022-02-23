/*
Copyright 2015-2022 Airship and Contributors
*/

package com.urbanairship.connect.client.model.request;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

public abstract class Subset {

    private Subset() { }

    public static Subset createSampleSubset(float proportion) {
        Preconditions.checkArgument(proportion >= 0f && proportion <= 1f, "Proportion value must be between 0 and 1");
        return new SampleSubset(proportion);
    }

    public static final class SampleSubset extends Subset {

        private final float proportion;

        private SampleSubset(float proportion) {
            this.proportion = proportion;
        }

        public float getProportion() {
            return proportion;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SampleSubset that = (SampleSubset) o;
            return Float.compare(that.proportion, proportion) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(proportion);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("proportion", proportion)
                    .toString();
        }
    }

    public static final String SUBSET_TYPE_KEY = "type";

    public static final String SAMPLE_SUBSET_TYPE_VALUE = "SAMPLE";
    public static final String PROPORTION_KEY = "proportion";

    public static final JsonSerializer<SampleSubset> SAMPLE_SUBSET_SERIALIZER = new JsonSerializer<SampleSubset>() {
        @Override
        public JsonElement serialize(SampleSubset sampleSubset, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject obj = new JsonObject();
            obj.addProperty(SUBSET_TYPE_KEY, SAMPLE_SUBSET_TYPE_VALUE);
            obj.addProperty(PROPORTION_KEY, sampleSubset.getProportion());

            return obj;
        }
    };

    public static PartitionSubsetBuilder createPartitionSubset() {
        return new PartitionSubsetBuilder();
    }

    public static final class PartitionSubset extends Subset {

        private final int count;
        private final int selection;

        private PartitionSubset(int count, int selection) {
            this.count = count;
            this.selection = selection;
        }

        public int getCount() {
            return count;
        }

        public int getSelection() {
            return selection;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PartitionSubset that = (PartitionSubset) o;
            return count == that.count &&
                    selection == that.selection;
        }

        @Override
        public int hashCode() {
            return Objects.hash(count, selection);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("count", count)
                    .add("selection", selection)
                    .toString();
        }
    }

    public static final String PARTITION_SUBSET_TYPE_VALUE = "PARTITION";
    public static final String SELECTION_KEY = "selection";
    public static final String COUNT_KEY = "count";

    public static final JsonSerializer<PartitionSubset> PARTITION_SUBSET_SERIALIZER = new JsonSerializer<PartitionSubset>() {
        @Override
        public JsonElement serialize(PartitionSubset partitionSubset, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject obj = new JsonObject();
            obj.addProperty(SUBSET_TYPE_KEY, PARTITION_SUBSET_TYPE_VALUE);
            obj.addProperty(COUNT_KEY, partitionSubset.getCount());
            obj.addProperty(SELECTION_KEY, partitionSubset.getSelection());

            return obj;
        }
    };

    public static final class PartitionSubsetBuilder {

        private Integer count = null;
        private Integer selection = null;

        private PartitionSubsetBuilder() { }

        public PartitionSubsetBuilder setCount(int value) {
            this.count = value;
            return this;
        }

        public PartitionSubsetBuilder setSelection(int value) {
            this.selection = value;
            return this;
        }

        public Subset build() {
            Preconditions.checkArgument(selection != null && count != null,
                "Values for selection and count must be set for PARTITION subset");
            Preconditions.checkArgument(count > 0, "Count must be > 0");
            Preconditions.checkArgument(selection < count,
                "Selection must be less than count");

            return new PartitionSubset(count, selection);
        }
    }
}
