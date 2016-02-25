package com.github.hindol.commons.core;

import java.util.*;

public class InMemoryDeduplicator<T> implements Deduplicator<T> {

    private final boolean mKeepOrder;
    private Set<T> mElements;

    public InMemoryDeduplicator() {
        this(true);
    }

    public InMemoryDeduplicator(boolean keepOrder) {
        mKeepOrder = keepOrder;
        mElements = newSet(mKeepOrder);
    }

    private Set<T> newSet(Collection<? extends T> source, boolean ordered) {
        return ordered ? new LinkedHashSet<>(source) : new HashSet<>(source);
    }

    private Set<T> newSet(boolean ordered) {
        return newSet(Collections.emptySet(), ordered);
    }

    @Override
    public boolean add(T element) {
        return mElements.add(element);
    }

    @Override
    public Set<T> peek() {
        return Collections.unmodifiableSet(mElements);
    }

    @Override
    public Set<T> reset() {
        Set<T> existing = mElements;
        mElements = newSet(mKeepOrder);

        return existing;
    }
}
