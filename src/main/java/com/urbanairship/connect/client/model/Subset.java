/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class Subset {

    private enum SubsetType {
        SAMPLE, PARTITION
    }

    private final SubsetType type;
    private final Optional<Float> proportion;
    private final Optional<Integer> count;
    private final Optional<Integer> selection;

    public static Subset createSampleSubset(Float proportion) {
        return new Builder()
            .setType(SubsetType.SAMPLE)
            .setProportion(proportion)
            .build();
    }

    public static Subset createPartitionSubset(Integer count, Integer selection) {
        return new Builder()
            .setType(SubsetType.PARTITION)
            .setCount(count)
            .setSelection(selection)
            .build();
    }

    private Subset(SubsetType type, Optional<Float> proportion, Optional<Integer> count, Optional<Integer> selection) {
        this.type = type;
        this.proportion = proportion;
        this.count = count;
        this.selection = selection;
    }

    public SubsetType getType() {
        return type;
    }

    public Optional<Float> getProportion() {
        return proportion;
    }

    public Optional<Integer> getCount() {
        return count;
    }

    public Optional<Integer> getSelection() {
        return selection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subset)) return false;

        Subset subset = (Subset) o;

        if (!count.equals(subset.count)) return false;
        if (!proportion.equals(subset.proportion)) return false;
        if (!selection.equals(subset.selection)) return false;
        if (type != subset.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + proportion.hashCode();
        result = 31 * result + count.hashCode();
        result = 31 * result + selection.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Subset{" +
            "type=" + type +
            ", proportion=" + proportion +
            ", count=" + count +
            ", selection=" + selection +
            '}';
    }

    private static final class Builder {
        private SubsetType type;
        private Float proportion = null;
        private Integer count = null;
        private Integer selection = null;

        private Builder() {}

        private Builder setType(SubsetType value) {
            this.type = value;
            return this;
        }

        private Builder setProportion(Float value) {
            this.proportion = value;
            return this;
        }

        private Builder setCount(Integer value) {
            this.count = value;
            return this;
        }

        private Builder setSelection(Integer value) {
            this.selection = value;
            return this;
        }

        private Subset build() {
            if (type == SubsetType.SAMPLE) {
                Preconditions.checkArgument(proportion != null,
                    "If the subset is of type SAMPLE, proportion may not be null");
                Preconditions.checkArgument(selection == null && count == null,
                    "If the subset is of type SAMPLE, selection and count must be null");
            } else if (type == SubsetType.PARTITION) {
                Preconditions.checkArgument(proportion == null,
                    "If the subset is of type PARTITION, proportion must be null");
                Preconditions.checkArgument(selection != null && count != null,
                    "If the subset is of type PARTITION, selection and count may not be null");
                Preconditions.checkArgument(selection < count,
                    "Selection must be less than count");
            }

            return new Subset(type, Optional.fromNullable(proportion), Optional.fromNullable(count), Optional.fromNullable(selection));
        }
    }
}
