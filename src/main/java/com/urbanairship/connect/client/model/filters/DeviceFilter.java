package com.urbanairship.connect.client.model.filters;

import com.urbanairship.connect.client.model.DeviceFilterType;

public class DeviceFilter {

    private final DeviceFilterType deviceFilterType;
    private final String channel;

    public DeviceFilter(DeviceFilterType deviceFilterType, String channel) {
        this.deviceFilterType = deviceFilterType;
        this.channel = channel;
    }

    public DeviceFilterType getDeviceFilterType() {
        return deviceFilterType;
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
        if (deviceFilterType != deviceFilter.deviceFilterType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = deviceFilterType.hashCode();
        result = 31 * result + channel.hashCode();
        return result;
    }
}
