package com.github.hindol.commons.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CollectionUtils {

    /**
     * Helper method to help initialize a new {@code HashSet} with values.
     *
     * @param elements The elements to initialize the set with.
     * @return The newly created and initialized Set.
     */
    @SuppressWarnings("unchecked")
    public static <E> Set<E> newHashSet(E... elements) {
        HashSet<E> set = new HashSet<>();

        Collections.addAll(set, elements);
        return set;
    }

    public static <K, V> ConcurrentMap<K, V> newConcurrentMap() {
        return new ConcurrentHashMap<>();
    }

    public static <T> Set<T> newConcurrentSet() {
        return Collections.newSetFromMap(newConcurrentMap());
    }
}
