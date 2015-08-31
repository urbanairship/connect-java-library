package com.urbanairship.connect.client.model.responses.region;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;

public class CircularRegion {
    private final double lat;
    @SerializedName("long")
    private final double longitude;
    private final int radius;

    private CircularRegion() {
        this(0, 0, 0);
    }

    private CircularRegion(double lat, double longitude, int radius) {
        this.lat = lat;
        this.longitude = longitude;
        this.radius = radius;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public double getLat() {
        return lat;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getRadius() {
        return radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CircularRegion)) return false;

        CircularRegion that = (CircularRegion) o;

        if (Double.compare(that.lat, lat) != 0) return false;
        if (Double.compare(that.longitude, longitude) != 0) return false;
        if (radius != that.radius) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(lat);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + radius;
        return result;
    }

    @Override
    public String toString() {
        return "CircularRegion{" +
            "lat=" + lat +
            ", longitude=" + longitude +
            ", radius=" + radius +
            '}';
    }

    public static class Builder {
        private double lat;
        @SerializedName("long")
        private double longitude;
        private int radius;

        private Builder() {
        }

        public Builder setLatitude(double value) {
            this.lat = value;
            return this;
        }

        public Builder setLongitude(double value) {
            this.longitude = value;
            return this;
        }

        public Builder setRadius(int value) {
            this.radius = value;
            return this;
        }

        public CircularRegion build() {
            Preconditions.checkNotNull(lat, "lat must be set");
            Preconditions.checkNotNull(longitude, "longitude must be set");
            Preconditions.checkNotNull(radius, "radius must be set");

            return new CircularRegion(lat, longitude, radius);
        }
    }
}
