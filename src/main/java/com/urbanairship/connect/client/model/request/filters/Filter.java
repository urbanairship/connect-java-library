/*
Copyright 2015-2022 Airship and Contributors
*/

package com.urbanairship.connect.client.model.request.filters;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Filter {

    private final Set<DeviceType> deviceTypes;
    private final Set<NotificationFilter> notifications;
    private final Set<DeviceFilter> devices;
    private final Set<String> types;
    private final Optional<Long> latency;

    public static Builder newBuilder() {
        return new Builder();
    }

    private Filter(Set<DeviceType> deviceTypes,
                   Set<NotificationFilter> notifications,
                   Set<DeviceFilter> devices,
                   Set<String> types,
                   Optional<Long> latency) {
        this.deviceTypes = deviceTypes;
        this.notifications = notifications;
        this.devices = devices;
        this.types = types;
        this.latency = latency;
    }

    public Set<DeviceType> getDeviceTypes() {
        return deviceTypes;
    }

    public Set<NotificationFilter> getNotifications() {
        return notifications;
    }

    public Set<DeviceFilter> getDevices() {
        return devices;
    }

    public Set<String> getTypes() {
        return types;
    }

    public Optional<Long> getLatencyMilliseconds() {
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

    @Override
    public String toString() {
        return "Filter{" +
            "deviceTypes=" + deviceTypes +
            ", notifications=" + notifications +
            ", devices=" + devices +
            ", types=" + types +
            ", latency=" + latency +
            '}';
    }

    public static final class Builder {

        private final Set<DeviceType> deviceTypes = new HashSet<>();
        private final Set<NotificationFilter> notifications = new HashSet<>();
        private final Set<DeviceFilter> devices = new HashSet<>();
        private final Set<String> types = new HashSet<>();

        private Long latency = null;

        private Builder() {}

        public Builder addDeviceTypes(DeviceType... types) {
            Collections.addAll(deviceTypes, types);
            return this;
        }

        public Builder addNotifications(NotificationFilter... notifications) {
            Collections.addAll(this.notifications, notifications);
            return this;
        }

        public Builder addDevices(DeviceFilter... devices) {
            Collections.addAll(this.devices, devices);
            return this;
        }

        public Builder addEventTypes(String... types) {
            for (String type : types) {
                this.types.add(type.toUpperCase());
            }

            return this;
        }

        public Builder setLatency(long value) {
            this.latency = value;
            return this;
        }

        public Filter build() {
            Preconditions.checkArgument((
                    !deviceTypes.isEmpty() ||
                    !notifications.isEmpty() ||
                    !deviceTypes.isEmpty() ||
                    !types.isEmpty() ||
                    latency != null), "Cannot create an empty filter payload");

            Preconditions.checkArgument(latency == null || latency >= 0, "Latency must be positive");

            return new Filter(
                    ImmutableSet.copyOf(deviceTypes),
                    ImmutableSet.copyOf(notifications),
                    ImmutableSet.copyOf(devices),
                    ImmutableSet.copyOf(types),
                    Optional.fromNullable(latency)
            );
        }
    }

    public static final String DEVICE_TYPES_KEY = "device_types";
    public static final String DEVICES_KEY = "devices";
    public static final String NOTIFICATIONS_KEY = "notifications";
    public static final String EVENT_TYPES_KEY = "types";
    public static final String LATENCY_KEY = "latency";

    public static final JsonSerializer<Filter> SERIALIZER = new JsonSerializer<Filter>() {
        @Override
        public JsonElement serialize(Filter filter, Type type, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            if (!filter.getDeviceTypes().isEmpty()) {
                obj.add(DEVICE_TYPES_KEY, context.serialize(filter.getDeviceTypes()));
            }
            if (!filter.getDevices().isEmpty()) {
                obj.add(DEVICES_KEY, context.serialize(filter.getDevices()));
            }
            if (!filter.getNotifications().isEmpty()) {
                obj.add(NOTIFICATIONS_KEY, context.serialize(filter.getNotifications()));
            }
            if (!filter.getTypes().isEmpty()) {
                obj.add(EVENT_TYPES_KEY, context.serialize(filter.getTypes()));
            }

            if (filter.getLatencyMilliseconds().isPresent()) {
                obj.addProperty(LATENCY_KEY, filter.getLatencyMilliseconds().get());
            }

            return obj;
        }
    };

}
