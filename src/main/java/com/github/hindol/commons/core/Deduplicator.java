package com.github.hindol.commons.core;

import java.util.Set;

/**
 * Interface definition for a de-duplicator.
 */
public interface Deduplicator<T> {

    /**
     * Add an element if and only if it is unique.
     *
     * @param element The element.
     * @return True if the element is unique and successfully added.
     */
    boolean add(T element);

    /**
     * Get the current set of elements without clearing the de-duplicator.
     *
     * @return The current set of elements.
     */
    Set<T> peek();

    /**
     * Get the current set of elements and reset the de-duplicator.
     *
     * @return The current set of elements.
     */
    Set<T> reset();
}
