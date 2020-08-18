package io.github.alopukhov.gusp.lifecycle;

import com.google.common.util.concurrent.Service;
import org.assertj.core.api.AbstractObjectAssert;

import java.util.Objects;

@SuppressWarnings("UnusedReturnValue")
public class ServiceSmartLifecycleAdpaterAssert extends AbstractObjectAssert<ServiceSmartLifecycleAdpaterAssert, ServiceSmartLifecycle> {
    public static ServiceSmartLifecycleAdpaterAssert assertThatLifecycleSupport(ServiceSmartLifecycle serviceSmartLifecycle) {
        return new ServiceSmartLifecycleAdpaterAssert(serviceSmartLifecycle);
    }

    public ServiceSmartLifecycleAdpaterAssert(ServiceSmartLifecycle serviceSmartLifecycle) {
        super(serviceSmartLifecycle, ServiceSmartLifecycleAdpaterAssert.class);
    }

    @SuppressWarnings("UnstableApiUsage")
    public ServiceSmartLifecycleAdpaterAssert hasService(Service service) {
        isNotNull();
        Service actualService = actual.getService();
        if (!Objects.equals(service, actualService)) {
            failWithActualExpectedAndMessage(actualService, service,
                    "Expected to has service that is equal to %s, but was %s",
                    service, actualService);
        }
        return this;
    }

    public ServiceSmartLifecycleAdpaterAssert hasAutoStartup(boolean enabled) {
        isNotNull();
        boolean actualEnabled = actual.isAutoStartup();
        if (actualEnabled != enabled) {
            failWithMessage("Expected auto startup to be %s but it was %s",
                    toEnabledStr(enabled), toEnabledStr(actualEnabled));
        }
        return this;
    }

    public ServiceSmartLifecycleAdpaterAssert hasAutoStartupEnabled() {
        return hasAutoStartup(true);
    }

    public ServiceSmartLifecycleAdpaterAssert hasAutoStartupDisabled() {
        return hasAutoStartup(false);
    }

    public ServiceSmartLifecycleAdpaterAssert hasAsyncStart(boolean enabled) {
        isNotNull();
        boolean actualEnabled = actual.isAsyncStart();
        if (actualEnabled != enabled) {
            failWithMessage("Expected async start to be %s but it was %s",
                    toEnabledStr(enabled), toEnabledStr(actualEnabled));
        }
        return this;
    }

    public ServiceSmartLifecycleAdpaterAssert hasAsyncStartEnabled() {
        return hasAsyncStart(true);
    }

    public ServiceSmartLifecycleAdpaterAssert hasAsyncStartDisabled() {
        return hasAsyncStart(false);
    }

    public ServiceSmartLifecycleAdpaterAssert hasPhase(int phase) {
        isNotNull();
        int actualPhase = actual.getPhase();
        if (actualPhase != phase) {
            failWithMessage("Expected phase %s but it was %s", phase, actualPhase);
        }
        return this;
    }

    public ServiceSmartLifecycleAdpaterAssert isRunning(boolean isRunning) {
        isNotNull();
        boolean actualIsRunning = actual.isRunning();
        if (isRunning != actualIsRunning) {
            failWithMessage("Expected isRunning() to return %s but it was %s", isRunning, actualIsRunning);
        }
        return this;
    }

    public ServiceSmartLifecycleAdpaterAssert isRunning() {
        return isRunning(true);
    }

    public ServiceSmartLifecycleAdpaterAssert isNotRunning() {
        return isRunning(false);
    }

    public ServiceSmartLifecycleAdpaterAssert hasStopOnDestroy(boolean enabled) {
        isNotNull();
        boolean actualEnabled = actual.isStopOnDestroy();
        if (actualEnabled != enabled) {
            failWithMessage("Expected stop on destroy start to be %s but it was %s",
                    toEnabledStr(enabled), toEnabledStr(actualEnabled));
        }
        return this;
    }

    public ServiceSmartLifecycleAdpaterAssert hasStopOnDestroyEnabled() {
        return hasStopOnDestroy(true);
    }

    public ServiceSmartLifecycleAdpaterAssert hasStopOnDestroyDisabled() {
        return hasStopOnDestroy(false);
    }

    private static String toEnabledStr(boolean enabled) {
        return enabled? "enabled" : "disabled";
    }
}
