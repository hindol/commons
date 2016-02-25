package com.github.hindol.commons.core;

import java.util.*;

public class InMemoryDeduplicator<T> implements Deduplicator<T> {

    private final Set<? super T> mBackingSet;
    private List<T> mNewSinceLast;

    public InMemoryDeduplicator() {
        mBackingSet = new HashSet<>();
        mNewSinceLast = new ArrayList<>();
    }

    public InMemoryDeduplicator(Set<? super T> backingSet) {
        mBackingSet = backingSet;
        mNewSinceLast = new ArrayList<>();
    }

    @Override
    public boolean isNew(T element) {
        if (mBackingSet.add(element)) {
            mNewSinceLast.add(element);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<T> newSinceLast() {
        return newSinceLast(false);
    }

    @Override
    public List<T> newSinceLast(boolean reset) {
        List<T> copy = mNewSinceLast;
        mNewSinceLast = new ArrayList<>();

        if (reset) {
            mBackingSet.clear();
        }

        return copy;
    }

    @Override
    public List<T> reset() {
        return newSinceLast(true);
    }
}
