package com.urbanairship.connect.client.model.responses.region;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;

public class Proximity {

    @SerializedName("beacon_id")
    private final String beaconId;
    private final int major;
    private final int minor;
    private final int rssi;
    private final double lat;
    @SerializedName("long")
    private final double longitude;

    private Proximity() {
        this(null, 0, 0, 0, 0, 0);
    }

    private Proximity(String beaconId, int major, int minor, int rssi, double lat, double longitude) {
        this.beaconId = beaconId;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
        this.lat = lat;
        this.longitude = longitude;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getBeaconId() {
        return beaconId;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRssi() {
        return rssi;
    }

    public double getLat() {
        return lat;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Proximity)) return false;

        Proximity proximity = (Proximity) o;

        if (Double.compare(proximity.lat, lat) != 0) return false;
        if (Double.compare(proximity.longitude, longitude) != 0) return false;
        if (major != proximity.major) return false;
        if (minor != proximity.minor) return false;
        if (rssi != proximity.rssi) return false;
        if (!beaconId.equals(proximity.beaconId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = beaconId.hashCode();
        result = 31 * result + major;
        result = 31 * result + minor;
        result = 31 * result + rssi;
        temp = Double.doubleToLongBits(lat);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Proximity{" +
            "beaconId='" + beaconId + '\'' +
            ", major=" + major +
            ", minor=" + minor +
            ", rssi=" + rssi +
            ", lat='" + lat + '\'' +
            ", longitude='" + longitude + '\'' +
            '}';
    }

    public static class Builder {
        @SerializedName("beacon_id")
        private String beaconId;
        private int major;
        private int minor;
        private int rssi;
        private double lat;
        @SerializedName("long")
        private double longitude;


        private Builder() {
        }

        public Builder setBeaconId(String value) {
            this.beaconId = value;
            return this;
        }

        public Builder setMajor(int value) {
            this.major = value;
            return this;
        }

        public Builder setMinor(int value) {
            this.minor = value;
            return this;
        }

        public Builder setRssi(int value) {
            this.rssi = value;
            return this;
        }

        public Builder setLatitude(double value) {
            this.lat = value;
            return this;
        }

        public Builder setLongitude(double value) {
            this.longitude = value;
            return this;
        }


        public Proximity build() {
            Preconditions.checkNotNull(beaconId, "beacon ID must be set");
            Preconditions.checkNotNull(major, "major must be set");
            Preconditions.checkNotNull(minor, "minor must be set");
            Preconditions.checkNotNull(rssi, "rssi must be set");
            Preconditions.checkNotNull(lat, "lat must be set");
            Preconditions.checkNotNull(longitude, "longitude must be set");

            return new Proximity(beaconId, major, minor, rssi, lat, longitude);
        }
    }
}
