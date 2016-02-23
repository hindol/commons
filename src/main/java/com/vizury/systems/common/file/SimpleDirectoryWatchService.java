package com.vizury.systems.common.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * A simple class which can monitor files and notify interested parties
 * (i.e. listeners) of file changes.
 * <p/>
 * This class is kept lean by only keeping methods that are actually being
 * called.
 */
public class SimpleDirectoryWatchService implements DirectoryWatchService, Runnable {

    private static final Logger LOGGER = LogManager.getLogger(SimpleDirectoryWatchService.class);
    private final WatchService mWatchService;
    private final AtomicBoolean mIsRunning;
    private final ConcurrentMap<WatchKey, Path> mWatchKeyToDirectoryMap;
    private final ConcurrentMap<Path, Set<OnFileChangeListener>> mDirectoryToListenersMap;
    private final ConcurrentMap<OnFileChangeListener, Set<PathMatcher>> mListenerToFilePatternsMap;

    /**
     * A simple no argument constructor for creating a <code>SimpleDirectoryWatchService</code>.
     *
     * @throws IOException If an I/O error occurs.
     */
    public SimpleDirectoryWatchService() throws IOException {
        mWatchService = FileSystems.getDefault().newWatchService();
        mIsRunning = new AtomicBoolean(false);
        mWatchKeyToDirectoryMap = newConcurrentMap();
        mDirectoryToListenersMap = newConcurrentMap();
        mListenerToFilePatternsMap = newConcurrentMap();
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    private static <K, V> ConcurrentMap<K, V> newConcurrentMap() {
        return new ConcurrentHashMap<>();
    }

    private static <T> Set<T> newConcurrentSet() {
        return Collections.newSetFromMap(newConcurrentMap());
    }

    public static PathMatcher matcherForGlobExpression(String globPattern) {
        return FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
    }

    public static boolean matches(Path input, PathMatcher pattern) {
        return pattern.matches(input);
    }

    public static boolean matchesAny(Path input, Set<PathMatcher> patterns) {
        for (PathMatcher pattern : patterns) {
            if (matches(input, pattern)) {
                return true;
            }
        }

        return false;
    }

    private Path getDirectoryForWatchKey(WatchKey key) {
        return mWatchKeyToDirectoryMap.get(key);
    }

    private Set<OnFileChangeListener> getListeners(Path dir) {
        return mDirectoryToListenersMap.get(dir);
    }

    private Set<PathMatcher> getPatterns(OnFileChangeListener listener) {
        return mListenerToFilePatternsMap.get(listener);
    }

    private Set<OnFileChangeListener> matchedListeners(Path dir, Path file) {
        return getListeners(dir)
                .stream()
                .filter(l -> matchesAny(file, getPatterns(l)))
                .collect(Collectors.toSet());
    }

    private void notifyListeners(WatchKey key) {
        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind eventKind = event.kind();

            // Overflow occurs when the watch event queue is overflown
            // with events.
            if (eventKind.equals(OVERFLOW)) {
                // TODO: Notify all listeners.
                return;
            }

            WatchEvent<Path> pathEvent = cast(event);
            Path file = pathEvent.context();
            Path dir = getDirectoryForWatchKey(key);

            if (eventKind.equals(ENTRY_CREATE)) {
                matchedListeners(dir, file)
                        .forEach(l -> l.onFileCreate(file));
            } else if (eventKind.equals(ENTRY_MODIFY)) {
                matchedListeners(dir, file)
                        .forEach(l -> l.onFileModify(file));
            } else if (eventKind.equals(ENTRY_DELETE)) {
                matchedListeners(dir, file)
                        .forEach(l -> l.onFileDelete(file));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(OnFileChangeListener listener, String dirPath, String... globPatterns)
            throws IOException {
        Path dir = Paths.get(dirPath);

        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException(dirPath + " is not a directory.");
        }

        if (!mDirectoryToListenersMap.containsKey(dir)) {
            // May throw
            WatchKey key = dir.register(
                    mWatchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE
            );

            mWatchKeyToDirectoryMap.put(key, dir);
            mDirectoryToListenersMap.put(dir, newConcurrentSet());
        }

        getListeners(dir).add(listener);

        Set<PathMatcher> patterns = newConcurrentSet();

        for (String globPattern : globPatterns) {
            patterns.add(matcherForGlobExpression(globPattern));
        }

        if (patterns.isEmpty()) {
            patterns.add(matcherForGlobExpression("*")); // Match everything if no filter is found
        }

        mListenerToFilePatternsMap.put(listener, patterns);

        LOGGER.info("Watching files matching " + Arrays.toString(globPatterns)
                + " under " + dirPath + " for changes.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        LOGGER.info("Starting file watcher service.");

        while (mIsRunning.get()) {
            WatchKey key;
            try {
                key = mWatchService.take();
            } catch (InterruptedException e) {
                LOGGER.info(
                        DirectoryWatchService.class.getSimpleName()
                                + " service interrupted."
                );
                break;
            }

            if (null == getDirectoryForWatchKey(key)) {
                LOGGER.error("Watch key not recognized.");
                continue;
            }

            notifyListeners(key);

            // Reset key to allow further events for this key to be processed.
            boolean valid = key.reset();
            if (!valid) {
                mWatchKeyToDirectoryMap.remove(key);
                if (mWatchKeyToDirectoryMap.isEmpty()) {
                    break;
                }
            }
        }

        mIsRunning.set(false);
        LOGGER.info("Stopping file watcher service.");
    }

    /**
     * Start this <code>SimpleDirectoryWatchService</code> instance by spawning a new thread.
     *
     * @see #stop()
     */
    @Override
    public void start() {
        if (mIsRunning.compareAndSet(false, true)) {
            Thread runnerThread = new Thread(this, DirectoryWatchService.class.getSimpleName());
            runnerThread.start();
        }
    }

    /**
     * Stop this <code>SimpleDirectoryWatchService</code> thread.
     * The killing happens lazily, giving the running thread an opportunity
     * to finish the work at hand.
     *
     * @see #start()
     */
    @Override
    public void stop() {
        // Kill thread lazily
        mIsRunning.set(false);
    }
}
