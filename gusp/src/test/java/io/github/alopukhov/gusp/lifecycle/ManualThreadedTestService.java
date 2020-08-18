package io.github.alopukhov.gusp.lifecycle;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

@SuppressWarnings("UnstableApiUsage")
public class ManualThreadedTestService extends AbstractExecutionThreadService implements ManualService {
    private final CountDownLatch cdl = new CountDownLatch(1);
    private volatile Exception cause;

    @Override
    protected void run() throws Exception {
        awaitStopAndThrowIfNecessary();
    }

    @Override
    protected void shutDown() throws Exception {
        awaitStopAndThrowIfNecessary();
    }

    @Override
    public void externalStop() {
        cdl.countDown();
    }

    @Override
    public void externalFail(Exception cause) {
        this.cause = cause;
        cdl.countDown();
    }

    @Override
    protected Executor executor() {
        return command -> {
            Thread thread = new Thread(command);
            thread.setDaemon(true);
            thread.start();
        };
    }

    private void awaitStopAndThrowIfNecessary() throws Exception {
        cdl.await();
        if (cause != null) {
            throw cause;
        }
    }
}
