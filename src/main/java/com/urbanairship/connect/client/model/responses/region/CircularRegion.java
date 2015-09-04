/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model.responses.region;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;

public class CircularRegion {
    @SerializedName("lat")
    private final double latitude;
    @SerializedName("long")
    private final double longitude;
    private final int radius;

    private CircularRegion() {
        this(0, 0, 0);
    }

    private CircularRegion(double latitude, double longitude, int radius) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public double getLatitude() {
        return latitude;
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

        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Double.compare(that.longitude, longitude) != 0) return false;
        if (radius != that.radius) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + radius;
        return result;
    }

    @Override
    public String toString() {
        return "CircularRegion{" +
            "latitude=" + latitude +
            ", longitude=" + longitude +
            ", radius=" + radius +
            '}';
    }

    public static class Builder {
        @SerializedName("lat")
        private double latitude;
        @SerializedName("long")
        private double longitude;
        private int radius;

        private Builder() {
        }

        public Builder setLatitude(double value) {
            this.latitude = value;
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
            Preconditions.checkNotNull(latitude, "latitude must be set");
            Preconditions.checkNotNull(longitude, "longitude must be set");
            Preconditions.checkNotNull(radius, "radius must be set");

            return new CircularRegion(latitude, longitude, radius);
        }
    }
}
