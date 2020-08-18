package io.github.alopukhov.gusp.lifecycle;

import com.google.common.util.concurrent.Service;

@SuppressWarnings("UnstableApiUsage")
public interface ManualService extends Service {
    void externalStop();

    void externalFail(Exception cause);
}
