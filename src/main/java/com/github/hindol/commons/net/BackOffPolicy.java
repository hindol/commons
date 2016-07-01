package com.github.hindol.commons.net;

import java.io.IOException;

public interface BackOffPolicy {

    long STOP = -1;

    long nextIntervalMillis() throws IOException;

    void reset() throws IOException;
}
