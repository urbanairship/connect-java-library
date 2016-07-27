/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.urbanairship.connect.client.model.request.StreamRequestPayload;
import com.urbanairship.connect.client.model.request.Subset;
import com.urbanairship.connect.client.model.request.filters.DeviceFilter;
import com.urbanairship.connect.client.model.request.filters.DeviceType;
import com.urbanairship.connect.client.model.request.filters.Filter;
import com.urbanairship.connect.client.model.request.filters.NotificationFilter;

public final class GsonUtil {

    private final static Gson gson;
    static {
        gson = new GsonBuilder()
                .registerTypeAdapter(DeviceType.class, DeviceType.SERIALIZER)
                .registerTypeAdapter(DeviceFilter.class, DeviceFilter.SERIALIZER)
                .registerTypeAdapter(Filter.class, Filter.SERIALIZER)
                .registerTypeAdapter(NotificationFilter.class, NotificationFilter.SERIALIZER)
                .registerTypeAdapter(Subset.SampleSubset.class, Subset.SAMPLE_SUBSET_SERIALIZER)
                .registerTypeAdapter(Subset.PartitionSubset.class, Subset.PARTITION_SUBSET_SERIALIZER)
                .registerTypeAdapter(StreamRequestPayload.class, StreamRequestPayload.SERIALIZER)
                .create();
    }

    public static Gson getGson() {
        return gson;
    }

    private GsonUtil() { }
}
