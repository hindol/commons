package com.github.hindol.commons.core;

import java.util.List;

/**
 * Interface definition for a de-duplicator.
 */
public interface Deduplicator<T> {

    boolean isNew(T element);

    List<T> newSinceLast();

    List<T> newSinceLast(boolean reset);

    List<T> reset();
}
