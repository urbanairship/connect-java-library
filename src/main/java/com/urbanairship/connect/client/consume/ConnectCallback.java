/*
Copyright 2022 Airship and Contributors
*/

package com.urbanairship.connect.client.consume;

/**
 * Defines the contract of possible state transition during connection to a stream.
 */
public interface ConnectCallback {

    void error(Throwable e);

    void connected(StatusAndHeaders statusAndHeaders);

}
