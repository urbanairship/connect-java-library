package com.urbanairship.connect.client.model.request;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * Identifies a position - either relative or absolute - within a stream of events sourced from the Airship Real-Time Data Streaming API.
 */
public final class StartPosition {

    public enum RelativePosition {
        /**
         * Specifies a position as being the earliest event available for a particular stream.
         */
        EARLIEST("EARLIEST"),

        /**
         * Specifies a position as being the end of the available events in a particular stream at the point it is
         * requested and thus implies that only events received after that point in time be included.
         */
        LATEST("LATEST");

        private final String id;

        RelativePosition(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    /**
     * Create a position specified by an exact offset.
     *
     * @param offset the offset specifying the position. An offset can be retrieved from the body of an event received from
     *               the Airship Real-Time Data Streaming API and specifies that event's position in the overall stream.
     */
    public static StartPosition offset(String offset) {
        Preconditions.checkArgument(StringUtils.isNotBlank(offset), "Offset cannot be null or empty");
        return new StartPosition(null, offset);
    }

    /**
     * Create a position specified by a relative position.
     *
     * @param position the relative position in the stream.
     */
    public static StartPosition relative(RelativePosition position) {
        Preconditions.checkNotNull(position);
        return new StartPosition(position, "");
    }

    private final RelativePosition relativePosition;
    private final String offset;

    private StartPosition(RelativePosition relativePosition, String offset) {
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

    public String getOffset() {
        Preconditions.checkState(relativePosition == null, "Start position is relative");
        return offset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StartPosition)) return false;
        StartPosition that = (StartPosition) o;
        return relativePosition == that.relativePosition &&
                Objects.equals(offset, that.offset);
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
