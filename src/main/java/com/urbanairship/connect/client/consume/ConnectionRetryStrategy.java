package com.urbanairship.connect.client.consume;

/**
 * Strategy to dictate behavior on connection failure.
 */
public interface ConnectionRetryStrategy {

    /**
     * Given the number of previous failed connection attempts, determines whether another attempt should be made.
     *
     * @param previousAttempts number of previous failed attempts
     * @return true if another attempt should be made or false if not.
     */
    boolean shouldRetry(int previousAttempts);

    /**
     * Given the number of previous failed connection attempts, provides the number of milliseconds the connection
     * routine should pause before making another attempt.
     *
     * Note this method is intended to work in tandem with {@link #shouldRetry(int)} for same previousAttempts value
     * and this method should really only be used in the case where {@link #shouldRetry(int)} returns true for the
     * same previousAttempts value.
     *
     * @param previousAttempts number of previous failed attempts
     * @return number of milliseconds to pause before retrying.
     */
    long getPauseMillis(int previousAttempts);

}
