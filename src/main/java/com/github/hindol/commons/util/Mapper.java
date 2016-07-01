package com.github.hindol.commons.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Forward and backward mapping between any two types.
 *
 * @param <F> The first type.
 * @param <S> The second type.
 */
public class Mapper<F, S> {

    private final Map<F, S> mForwardMap;
    private final Map<S, F> mBackwardMap;

    private Mapper(Builder<F, S> builder) {
        mForwardMap = builder.mForwardMap;
        mBackwardMap = builder.mBackwardMap;
    }

    public static <F, S> Builder<F, S> builder() {
        return new Builder<>();
    }

    public S forwardMap(F first) {
        return mForwardMap.get(first);
    }

    public F backwardMap(S second) {
        return mBackwardMap.get(second);
    }

    public static class Builder<F, S> {

        private final Map<F, S> mForwardMap = new HashMap<>();
        private final Map<S, F> mBackwardMap = new HashMap<>();

        public Builder<F, S> add(F first, S second) {
            mForwardMap.put(first, second);
            mBackwardMap.put(second, first);
            return this;
        }

        public Mapper<F, S> build() {
            return new Mapper<>(this);
        }
    }
}
