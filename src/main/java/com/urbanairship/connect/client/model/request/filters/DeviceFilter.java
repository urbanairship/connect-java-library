/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model.request.filters;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;

public final class DeviceFilter {

    private final DeviceFilterType deviceFilterType;
    private final String identifier;

    public DeviceFilter(DeviceFilterType deviceFilterType, String identifier) {
        Preconditions.checkArgument(StringUtils.isNotBlank(identifier), "Device filter identifier must be provided");
        this.deviceFilterType = Preconditions.checkNotNull(deviceFilterType, "Device filter type must be provided");
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

    @Override
    public String toString() {
        return "DeviceFilter{" +
            "deviceFilterType=" + deviceFilterType +
            ", identifier='" + identifier + '\'' +
            '}';
    }

    public static final JsonSerializer<DeviceFilter> SERIALIZER = new JsonSerializer<DeviceFilter>() {
        @Override
        public JsonElement serialize(DeviceFilter deviceFilter, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject obj = new JsonObject();
            obj.addProperty(deviceFilter.getDeviceFilterType().getSerializedValue(), deviceFilter.getIdentifier());

            return obj;
        }
    };
}
