package com.github.hindol.commons.net;

import com.google.common.collect.Range;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

public final class ExponentialBackOffPolicy implements BackOffPolicy {

    private static final Range<Double> mRandomizationFactorRange = Range.closed(0.0, 1.0);

    private final long mInitialIntervalMillis;
    private final long mMaxElapsedTimeMillis;
    private final long mMaxIntervalMillis;
    private final double mMultiplier;
    private final double mRandomizationFactor;

    private long mNextIntervalMillis;
    private long mElapsedTimeMillis;

    public ExponentialBackOffPolicy(Builder builder) {
        mInitialIntervalMillis = builder.mInitialIntervalMillis;
        mMaxElapsedTimeMillis = builder.mMaxElapsedTimeMillis;
        mMaxIntervalMillis = builder.mMaxIntervalMillis;
        mMultiplier = builder.mMultiplier;
        mRandomizationFactor = builder.mRandomizationFactor;

        reset();
    }

    private long nextRandomizedIntervalMillis() {
        final long intervalMillis = mNextIntervalMillis;
        mNextIntervalMillis *= mMultiplier;

        if (mRandomizationFactor > 0) {
            double random = ThreadLocalRandom.current().nextDouble(
                    1 - mRandomizationFactor,
                    1 + mRandomizationFactor
            );
            return Math.round(intervalMillis * random);
        } else {
            return intervalMillis;
        }
    }

    @Override
    public long nextIntervalMillis() /* throws IOException */ {
        final long interval = Math.min(nextRandomizedIntervalMillis(), mMaxIntervalMillis);
        if (mElapsedTimeMillis <= mMaxElapsedTimeMillis) {
            mElapsedTimeMillis += interval;
            return interval;
        } else {
            return BackOffPolicy.STOP;
        }
    }

    @Override
    public void reset() /* throws IOException */ {
        mNextIntervalMillis = mInitialIntervalMillis;
        mElapsedTimeMillis = 0;
    }

    public static final class Builder {

        private long mInitialIntervalMillis = 500;
        private long mMaxElapsedTimeMillis = 15 * 60 * 1000;
        private long mMaxIntervalMillis = 60 * 1000;
        private double mMultiplier = 1.5;
        private double mRandomizationFactor = 0.5;

        public Builder setInitialInterval(long duration, TimeUnit unit) {
            checkArgument(duration > 0, "Initial interval must be positive.");
            mInitialIntervalMillis = unit.toMillis(duration);
            return this;
        }

        public Builder setMaxElapsedTime(long duration, TimeUnit unit) {
            checkArgument(duration > 0, "Maximum elapsed time must be positive.");
            mMaxElapsedTimeMillis = unit.toMillis(duration);
            return this;
        }

        public Builder setMaxInterval(long duration, TimeUnit unit) {
            checkArgument(duration > 0, "Maximum interval must be positive.");
            mMaxIntervalMillis = unit.toMillis(duration);
            return this;
        }

        public Builder setMultiplier(double multiplier) {
            checkArgument(multiplier >= 1.0, "Multiplier must be greater than or equal to 1.");
            mMultiplier = multiplier;
            return this;
        }

        public Builder setRandomizationFactor(double randomizationFactor) {
            checkArgument(
                    mRandomizationFactorRange.contains(randomizationFactor),
                    "Randomization factor must be in range [0.0, 1.0]."
            );
            mRandomizationFactor = randomizationFactor;
            return this;
        }

        public BackOffPolicy build() {
            checkArgument(
                    mMaxIntervalMillis >= mInitialIntervalMillis,
                    "Maximum interval cannot be smaller than initial interval."
            );
            checkArgument(
                    mMaxElapsedTimeMillis >= mInitialIntervalMillis,
                    "Maximum elapsed time cannot be smaller than initial interval."
            );
            return new ExponentialBackOffPolicy(this);
        }
    }
}
