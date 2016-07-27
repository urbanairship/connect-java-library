/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model.request.filters;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public final class NotificationFilter {

    public enum Type {
        GROUP_ID("group_id"), PUSH_ID("push_id");

        private final String serializedKey;

        Type(String serializedKey) {
            this.serializedKey = serializedKey;
        }

        String getSerializedKey() {
            return serializedKey;
        }
    }

    private final Type type;
    private final String value;

    public NotificationFilter(Type type, String value) {
        Preconditions.checkArgument(StringUtils.isNotBlank(value), "Notification filter values must be provided");
        this.type = Preconditions.checkNotNull(type, "Notification filter type must be provided");
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationFilter that = (NotificationFilter) o;
        return type == that.type &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("value", value)
                .toString();
    }

    public static final JsonSerializer<NotificationFilter> SERIALIZER = new JsonSerializer<NotificationFilter>() {

        @Override
        public JsonElement serialize(NotificationFilter notificationFilter, java.lang.reflect.Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject obj = new JsonObject();
            obj.addProperty(notificationFilter.getType().getSerializedKey(), notificationFilter.getValue());

            return obj;
        }
    };
}
