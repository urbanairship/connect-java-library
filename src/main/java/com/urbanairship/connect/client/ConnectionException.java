package com.urbanairship.connect.client;

/**
 * An exception that indicates a failure in connecting to the Urban Airship Connect API where the cause of the failure
 * is such that a retry of the connection will be unlikely to resolve the problem and thus a retry should not be attempted
 * using the same sequence.
 *
 * An example would be a case where the request payload to the API is invalid.
 */
public final class ConnectionException extends RuntimeException {

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
