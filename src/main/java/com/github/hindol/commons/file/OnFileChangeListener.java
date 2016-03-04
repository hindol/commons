package com.github.hindol.commons.file;

import java.nio.file.Path;

/**
 * Interface definition for a callback to be invoked when a file under
 * watch is changed.
 */
public interface OnFileChangeListener {

    /**
     * Called when the file is created.
     * @param filePath The file path.
     */
    default void onFileCreate(Path filePath) {}

    /**
     * Called when the file is modified.
     * @param filePath The file path.
     */
    default void onFileModify(Path filePath) {}

    /**
     * Called when the file is deleted.
     * @param filePath The file path.
     */
    default void onFileDelete(Path filePath) {}
}
