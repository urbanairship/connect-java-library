package com.urbanairship.connect.client.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import java.util.Objects;

public final class StartPosition {

    public enum RelativePosition {
        EARLIEST("EARLIEST"), LATEST("LATEST");

        private final String id;

        RelativePosition(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    public static StartPosition offset(long offset) {
        Preconditions.checkArgument(offset >= 0, "Offset cannot be negative");
        return new StartPosition(null, offset);
    }

    public static StartPosition relative(RelativePosition position) {
        Preconditions.checkNotNull(position);
        return new StartPosition(position, -1L);
    }

    private final RelativePosition relativePosition;
    private final long offset;

    private StartPosition(RelativePosition relativePosition, long offset) {
        this.relativePosition = relativePosition;
        this.offset = offset;
    }

    public boolean isRelative() {
        return relativePosition != null;
    }

    public RelativePosition getRelativePosition() {
        Preconditions.checkState(relativePosition != null, "Start position is an absolute offset");
        return relativePosition;
    }

    public long getOffset() {
        Preconditions.checkState(relativePosition == null, "Start position is relative");
        return offset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StartPosition that = (StartPosition) o;
        return offset == that.offset &&
                relativePosition == that.relativePosition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(relativePosition, offset);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("relativePosition", relativePosition)
                .add("offset", offset)
                .toString();
    }
}
