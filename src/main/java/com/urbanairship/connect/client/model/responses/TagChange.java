package com.urbanairship.connect.client.model.responses;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.urbanairship.connect.client.model.EventType;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

public class TagChange implements EventBody{
    public static class Builder {
        private Map<String, Collection<String>>tagAdd = null;
        private Map<String, Collection<String>>tagRemove = null;
        private Map<String, Collection<String>>tagCurrent = null;

        public Builder setTagAdd(Map<String, Collection<String>> tagAdd) {
            this.tagAdd = tagAdd;
            return this;
        }

        public Builder setTagRemove(Map<String, Collection<String>> tagRemove) {
            this.tagRemove = tagRemove;
            return this;
        }

        public Builder setTagCurrent(Map<String, Collection<String>> tagCurrent) {
            this.tagCurrent = tagCurrent;
            return this;
        }

        public TagChange build() {
            return new TagChange(tagAdd, tagRemove, tagCurrent);
        }
    }

    private final static JsonParser parser = new JsonParser();
    private final static Gson gson = new Gson();

    @SerializedName("add")
    private final Map<String, Collection<String>> tagAdd;
    @SerializedName("remove")
    private final Map<String, Collection<String>> tagRemove;
    @SerializedName("current")
    private final Map<String, Collection<String>> tagCurrent;

    private TagChange() {
        this(null, null, null);
    }

    private TagChange(Map<String, Collection<String>> tagAdd, Map<String, Collection<String>> tagRemove, Map<String, Collection<String>> tagCurrent) {
        this.tagAdd = tagAdd;
        this.tagRemove = tagRemove;
        this.tagCurrent = tagCurrent;
    }

    public Map<String, Collection<String>> getTagAdd() {
        return tagAdd;
    }

    public Map<String, Collection<String>> getTagRemove() {
        return tagRemove;
    }

    public Map<String, Collection<String>> getTagCurrent() {
        return tagCurrent;
    }

    public static TagChange parseJSONfromBytes(byte[] bytes) {
        String byteString = new String(bytes, StandardCharsets.UTF_8);
        JsonObject jsonObject = parser.parse(byteString).getAsJsonObject();
        return parseJSON(jsonObject.toString());
    }

    public static TagChange parseJSON(String json) {
        return gson.fromJson(json, TagChange.class);
    }

    public static TagChange parseJSON(JsonElement json) {
        return gson.fromJson(json, TagChange.class);
    }

    public byte[] serializeToJSONBytes() {
        return gson.toJson(this).toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TagChange tagChange = (TagChange) o;

        if (tagAdd != null ? !tagAdd.equals(tagChange.tagAdd) : tagChange.tagAdd != null) return false;
        if (tagRemove != null ? !tagRemove.equals(tagChange.tagRemove) : tagChange.tagRemove != null) return false;
        return !(tagCurrent != null ? !tagCurrent.equals(tagChange.tagCurrent) : tagChange.tagCurrent != null);

    }

    @Override
    public int hashCode() {
        int result = tagAdd != null ? tagAdd.hashCode() : 0;
        result = 31 * result + (tagRemove != null ? tagRemove.hashCode() : 0);
        result = 31 * result + (tagCurrent != null ? tagCurrent.hashCode() : 0);
        return result;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public EventType getType() {
        return EventType.TAG_CHANGE;
    }
}
