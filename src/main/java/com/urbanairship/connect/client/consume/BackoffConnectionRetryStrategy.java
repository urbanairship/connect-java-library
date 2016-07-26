package com.urbanairship.connect.client.consume;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.RandomUtils;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link ConnectionRetryStrategy} that dictactes retry behavior based up on a configured maximum
 * number of attempts and pause between attempts that utilizes exponential backoff with jitter.
 */
public final class BackoffConnectionRetryStrategy implements ConnectionRetryStrategy {

    private final int maxAttempts;
    private final long interval;
    private final long maxWaitSeconds;

    public static Builder newBuilder() {
        return new Builder();
    }

    private BackoffConnectionRetryStrategy(int maxAttempts, long interval, long maxWaitSeconds) {
        this.maxAttempts = maxAttempts;
        this.interval = interval;
        this.maxWaitSeconds = maxWaitSeconds;
    }

    @Override
    public boolean shouldRetry(int previousAttempts) {
        return previousAttempts < maxAttempts;
    }

    @Override
    public long getPauseMillis(int previousAttempts) {
        if (previousAttempts <= 0) {
            return 0L;
        }

        long exponentialBackoff = (long) (interval * Math.pow(2D, (previousAttempts - 1)));
        long jitter = (long) (0.5D * exponentialBackoff);
        long upperBound = Math.min(TimeUnit.SECONDS.toMillis(maxWaitSeconds), exponentialBackoff + jitter);
        long lowerBound = exponentialBackoff - jitter;
        if (lowerBound >= upperBound) {
            lowerBound = upperBound - interval;
        }

        return RandomUtils.nextLong(lowerBound, upperBound);
    }

    public static final class Builder {

        private int maxAttempts = -1;
        private long interval = -1L;
        private long maxWaitSeconds = -1L;

        private Builder() { }

        public Builder setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder setInterval(long interval) {
            this.interval = interval;
            return this;
        }

        public Builder setMaxWaitSeconds(long maxWaitSeconds) {
            this.maxWaitSeconds = maxWaitSeconds;
            return this;
        }

        public ConnectionRetryStrategy build() {
            Preconditions.checkArgument(maxAttempts > 0, "Max attempts must be > 0");
            Preconditions.checkArgument(interval > 0, "Interval must be > 0");
            Preconditions.checkArgument(maxWaitSeconds > 0, "Max wait seconds must be > 0");

            return new BackoffConnectionRetryStrategy(maxAttempts, interval, maxWaitSeconds);
        }
    }
}
