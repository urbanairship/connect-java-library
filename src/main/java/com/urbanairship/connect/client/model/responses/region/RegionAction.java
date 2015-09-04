/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.model.responses.region;

import com.google.gson.annotations.SerializedName;

public enum RegionAction {
    @SerializedName("exit")
    EXIT,
    @SerializedName("enter")
    ENTER
}
