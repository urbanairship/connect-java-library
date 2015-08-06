package com.urbanairship.connect.client.model.filters;

import com.urbanairship.connect.client.model.DeviceFilterType;

public class DeviceFilter {

    private final DeviceFilterType deviceFilterType;
    private final String identifier;

    public DeviceFilter(DeviceFilterType deviceFilterType, String identifier) {
        this.deviceFilterType = deviceFilterType;
        this.identifier = identifier;
    }

    public DeviceFilterType getDeviceFilterType() {
        return deviceFilterType;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceFilter)) return false;

        DeviceFilter deviceFilter = (DeviceFilter) o;

        if (!identifier.equals(deviceFilter.identifier)) return false;
        if (deviceFilterType != deviceFilter.deviceFilterType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = deviceFilterType.hashCode();
        result = 31 * result + identifier.hashCode();
        return result;
    }
}
