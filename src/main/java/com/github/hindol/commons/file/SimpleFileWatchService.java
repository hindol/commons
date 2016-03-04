package com.github.hindol.commons.file;

import com.github.hindol.commons.utils.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SimpleFileWatchService implements FileWatchService {

    private final DirectoryWatchService mDirectoryWatchService;

    public SimpleFileWatchService() throws IOException {
        mDirectoryWatchService = new SimpleDirectoryWatchService();
    }

    @Override
    public void register(OnFileChangeListener listener, Path firstPath, Path... otherPaths)
        throws IOException {

        Set<Path> paths = CollectionUtils.newHashSet(firstPath);
        Collections.addAll(paths, otherPaths);

        register(listener, paths);
    }

    @Override
    public void register(OnFileChangeListener listener, Collection<Path> paths) throws IOException {

        Map<Path, Set<String>> dirToFileNames = new HashMap<>();

        // De-duplicates, but may not be necessary as SimpleDirectoryWatchService
        // also de-duplicates.
        for (Path path : paths) {
            path = path.toAbsolutePath();

            if (Files.isRegularFile(path)) {
                Path dir = path.getParent();
                String fileName = path.getFileName().toString();

                dirToFileNames.put(dir, CollectionUtils.newHashSet(fileName));
            }
        }

        for (Path dir : dirToFileNames.keySet()) {
            mDirectoryWatchService.register(listener, dir, dirToFileNames.get(dir));
        }
    }

    /**
     * Start this <code>SimpleFileWatchService</code> instance by spawning a new thread.
     *
     * @see #stop()
     */
    @Override
    public void start() throws Exception {
        mDirectoryWatchService.start();
    }

    /**
     * Stop this <code>SimpleFileWatchService</code> thread.
     * The killing happens lazily, giving the running thread an opportunity
     * to finish the work at hand.
     *
     * @see #start()
     */
    @Override
    public void stop() {
        mDirectoryWatchService.stop();
    }
}
