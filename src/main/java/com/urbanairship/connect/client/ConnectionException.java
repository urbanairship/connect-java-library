package com.urbanairship.connect.client;

/**
 * An exception that indicates a failure in connecting to the Airship Real-Time Data Streaming API where the cause of the failure
 * is such that a retry of the connection will be unlikely to resolve the problem and thus a retry should not be attempted
 * using the same sequence.
 *
 * An example would be a case where the request payload to the API is invalid.
 */
public final class ConnectionException extends RuntimeException {

    /**
     * The http error code we received from the API.
     */
    private final int errorCode;

    public ConnectionException(String message) {
        super(message);
        this.errorCode = -1;
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = -1;
    }

    public ConnectionException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ConnectionException(String message, int errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
