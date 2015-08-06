package com.urbanairship.connect.client.model.responses;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

public class HashingUtil {
    private static final HashFunction HASH_FUNCTION = Hashing.murmur3_128();
    private static final String SEPARATOR = ":";
    private static final Joiner COLON = Joiner.on(SEPARATOR);

    /**
     * Generate a 64bit hash of the input key
     *
     * We use a 64bit long as the hash code, as redshift does not support 128bit integers
     * @param key
     * @return
     */
    public static long generateHashKey(String key) {
        return HASH_FUNCTION.hashString(key, StandardCharsets.UTF_8).padToLong();
    }

    /**
     * Join a set of keys together with ':' as s separator
     * Return the hash of this composite key
     * @param keys
     * @return
     */
    public static long generateCompositeHashKey(String... keys) {
        String joinedKey = COLON.join(Lists.newArrayList(keys));
        return generateHashKey(joinedKey);
    }
}
