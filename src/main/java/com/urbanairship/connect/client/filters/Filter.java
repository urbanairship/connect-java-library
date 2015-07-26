package com.urbanairship.connect.client.filters;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Filter {

    @SerializedName("device_types")
    private final Optional<Set<DeviceIdType>> deviceTypes;
    private final Optional<Set<NotificationFilter>> notifications;
    private final Optional<Set<DeviceFilter>> devices;
    private final Optional<Set<EventType>> types;
    private final Optional<Integer> latency;

    public static Builder newBuilder() {
        return new Builder();
    }

    private Filter(Set<DeviceIdType> deviceTypes, Set<NotificationFilter> notifications, Set<DeviceFilter> devices,
                   Set<EventType> types, Integer latency) {
        this.deviceTypes = Optional.ofNullable(deviceTypes);
        this.notifications = Optional.ofNullable(notifications);
        this.devices = Optional.ofNullable(devices);
        this.types = Optional.ofNullable(types);
        this.latency = Optional.ofNullable(latency);
    }

    public Optional<Set<DeviceIdType>> getDeviceTypes() {
        return deviceTypes;
    }

    public Optional<Set<NotificationFilter>> getNotifications() {
        return notifications;
    }

    public Optional<Set<DeviceFilter>> getDevices() {
        return devices;
    }

    public Optional<Set<EventType>> getTypes() {
        return types;
    }

    public Optional<Integer> getLatency() {
        return latency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Filter)) return false;

        Filter filter = (Filter) o;

        if (!deviceTypes.equals(filter.deviceTypes)) return false;
        if (!devices.equals(filter.devices)) return false;
        if (!latency.equals(filter.latency)) return false;
        if (!notifications.equals(filter.notifications)) return false;
        if (!types.equals(filter.types)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = deviceTypes.hashCode();
        result = 31 * result + notifications.hashCode();
        result = 31 * result + devices.hashCode();
        result = 31 * result + types.hashCode();
        result = 31 * result + latency.hashCode();
        return result;
    }

    public static final class Builder {

        private Set<DeviceIdType> deviceTypes = null;
        private Set<NotificationFilter> notifications = null;
        private Set<DeviceFilter> devices = null;
        private Set<EventType> types = null;
        private Integer latency = null;

        private Builder() {}

        public Builder addDeviceType(DeviceIdType value) {
            return addDeviceTypes(value);
        }

        public Builder addDeviceTypes(DeviceIdType... value) {
            return addDeviceTypes(new HashSet<>(Arrays.asList(value)));
        }

        public Builder addDeviceTypes(Set<DeviceIdType> value) {
            this.deviceTypes = value;
            return this;
        }

        public Builder addNotification(NotificationFilter value) {
            return addNotifications(value);
        }

        public Builder addNotifications(NotificationFilter... value) {
            return addNotifications(new HashSet<>(Arrays.asList(value)));
        }

        public Builder addNotifications(Set<NotificationFilter> value) {
            this.notifications = value;
            return this;
        }

        public Builder addDevice(DeviceFilter value) {
            return addDevices(value);
        }

        public Builder addDevices(DeviceFilter... value) {
            return addDevices(new HashSet<>(Arrays.asList(value)));
        }

        public Builder addDevices(Set<DeviceFilter> value) {
            this.devices = value;
            return this;
        }


        public Builder addType(EventType value) {
            return addTypes(value);
        }

        public Builder addTypes(EventType... value) {
            return addTypes(new HashSet<>(Arrays.asList(value)));
        }

        public Builder addTypes(Set<EventType> value) {
            this.types = value;
            return this;
        }

        public Builder setLatency(Integer value) {
            this.latency = value;
            return this;
        }

        public Filter build() {
            Preconditions.checkArgument(!(deviceTypes == null && notifications == null && devices == null && types == null && latency == null),
                "Cannot create an empty filter payload");
            return new Filter(deviceTypes, notifications, devices, types, latency);
        }
    }

}
