package com.urbanairship.connect.client.model.filters;

import com.urbanairship.connect.client.model.DeviceIdType;

public class DeviceFilter {

    private final DeviceIdType deviceIdType;
    private final String channel;

    public DeviceFilter(DeviceIdType deviceIdType, String channel) {
        this.deviceIdType = deviceIdType;
        this.channel = channel;
    }

    public DeviceIdType getDeviceIdType() {
        return deviceIdType;
    }

    public String getChannel() {
        return channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceFilter)) return false;

        DeviceFilter deviceFilter = (DeviceFilter) o;

        if (!channel.equals(deviceFilter.channel)) return false;
        if (deviceIdType != deviceFilter.deviceIdType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = deviceIdType.hashCode();
        result = 31 * result + channel.hashCode();
        return result;
    }
}
