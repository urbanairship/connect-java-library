package com.urbanairship.connect.client;

/**
 * This class is intended to notify the implementing service that a {@link com.urbanairship.connect.client.StreamHandler}
 * instance failed to connect and will thus discontinue any more attempts to connect/consume.
 */
public interface FatalExceptionHandler {

    public void handle(Exception e);
}
