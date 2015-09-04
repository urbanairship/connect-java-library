/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.consume;

import com.google.common.base.MoreObjects;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class StatusAndHeaders {

    private final int statusCode;
    private final String statusMessage;
    private final Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public StatusAndHeaders(int statusCode, String statusMessage, Map<String, List<String>> headers) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.headers.putAll(headers);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StatusAndHeaders that = (StatusAndHeaders) o;
        return Objects.equals(statusCode, that.statusCode) &&
                Objects.equals(statusMessage, that.statusMessage) &&
                Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusCode, statusMessage, headers);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("statusCode", statusCode)
                .add("statusMessage", statusMessage)
                .add("headers", headers)
                .toString();
    }
}
