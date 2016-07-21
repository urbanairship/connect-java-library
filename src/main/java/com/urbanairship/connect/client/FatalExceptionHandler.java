/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client;

/**
 * This class is intended to notify the implementing service that a {@link MobileEventConsumerService}
 * instance failed to connect and will thus discontinue any more attempts to connect/consume.
 */
public interface FatalExceptionHandler {

    void accept(Exception e);

}
