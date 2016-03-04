package com.github.hindol.commons.file;

import com.github.hindol.commons.core.Service;
import com.github.hindol.commons.file.DirectoryWatchService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public interface FileWatchService extends Service {

    void register(OnFileChangeListener listener, Path firstPath, Path... otherPaths) throws IOException;

    void register(OnFileChangeListener listener, Collection<Path> paths) throws IOException;
}
