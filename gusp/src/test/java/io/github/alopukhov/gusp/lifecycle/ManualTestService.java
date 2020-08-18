package io.github.alopukhov.gusp.lifecycle;

import com.google.common.util.concurrent.AbstractService;

@SuppressWarnings("UnstableApiUsage")
public class ManualTestService extends AbstractService implements ManualService {
    @Override
    protected void doStart() {
        notifyStarted();
    }

    @Override
    protected void doStop() {
    }

    @Override
    public void externalStop() {
        notifyStopped();
    }

    @Override
    public void externalFail(Exception cause) {
        notifyFailed(cause);
    }
}
