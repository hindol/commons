package com.github.hindol.commons.net;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

public class ExponentialBackOffPolicyTest {

    private final BackOffPolicy mBackOffPolicy =
            new ExponentialBackOffPolicy.Builder()
                    .setRandomizationFactor(0.0)
                    .build();

    @BeforeMethod
    public void beforeMethod() throws IOException {
        mBackOffPolicy.reset();
    }

    @Test
    public void testNextIntervalMillis() throws Exception {
        assertEquals(mBackOffPolicy.nextIntervalMillis(), 500);
        assertEquals(mBackOffPolicy.nextIntervalMillis(), 750);
        assertEquals(mBackOffPolicy.nextIntervalMillis(), 1125);
    }

    @Test
    public void testReset() throws Exception {
        assertEquals(mBackOffPolicy.nextIntervalMillis(), 500);
        mBackOffPolicy.reset();
        assertEquals(mBackOffPolicy.nextIntervalMillis(), 500);
    }
}

