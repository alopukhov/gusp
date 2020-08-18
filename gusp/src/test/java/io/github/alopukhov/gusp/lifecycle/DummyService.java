package io.github.alopukhov.gusp.lifecycle;

import com.google.common.util.concurrent.AbstractService;

@SuppressWarnings("UnstableApiUsage")
public class DummyService extends AbstractService {
    @Override
    protected void doStart() {
        notifyStarted();
    }

    @Override
    protected void doStop() {
        notifyStopped();
    }
}
