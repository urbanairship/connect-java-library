/*
Copyright 2015 Urban Airship and Contributors
*/

package com.urbanairship.connect.client.offsets;

import com.google.common.base.Optional;

/**
 * Interface to manage stream offsets.
 */
public interface OffsetManager {

    /**
     * Gets the last stored offset.
     *
     * @return Optional<>Long</>
     */
    Optional<String> getLastOffset();

    /**
     * Update the stored offset value.
     *
     * @param offset Long
     */
    void update(String offset);
}
