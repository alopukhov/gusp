package io.github.alopukhov.gusp.lifecycle;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.util.concurrent.Service.State.*;
import static java.util.Objects.requireNonNull;

/**
 * Adapter from guava services to spring context lifecycle.
 * Provides a way for guava service to be managed by spring.
 * Create it manually or annotate bean with {@link io.github.alopukhov.gusp.annotations.WithSmartLifecycle}.
 *
 * <p>AutoStartup is enabled by default.
 * <p>Offers two features:
 * <ul>
 *     <li>asyncStart - if enabled start method will not wait until service transfers to running state</li>
 *     <li>stopOnDestroy - if enabled {@link #stop()} will be called in {@link #destroy()} call.
 *     Default is disabled as this should be set with care.
 *     </li>
 * </ul>
 *
 * This class is not thread safe in terms of setting properties.
 *
 * @see io.github.alopukhov.gusp.annotations.WithSmartLifecycle
 * @see io.github.alopukhov.gusp.annotations.EnableGusp
 * @see WithSmartLifecyclePostprocessor
 */
@SuppressWarnings("UnstableApiUsage")
public class ServiceSmartLifecycle implements SmartLifecycle, DisposableBean {
    private static final Set<State> RUNNING_STATES = EnumSet.of(STARTING, RUNNING, STOPPING);
    private static final Set<State> TERMINAL_STATES = EnumSet.of(TERMINATED, FAILED);

    private final Service service;

    private boolean autoStartup = true;
    private boolean asyncStart;
    private boolean stopOnDestroy;
    private int phase;

    public ServiceSmartLifecycle(Service service) {
        this.service = requireNonNull(service, "service");
    }

    public Service getService() {
        return service;
    }

    @Override
    public boolean isAutoStartup() {
        return autoStartup;
    }

    public void setAutoStartup(boolean autoStartup) {
        this.autoStartup = autoStartup;
    }

    /**
     * Starts provided service.
     * If asyncStart feature is disabled awaits until service transfers to running state.
     *
     * @throws IllegalStateException if service not in NEW/STARTING/RUNNING state or
     *                               awaitRunning throws Exception.
     */
    @Override
    public void start() {
        final State state = service.state();
        switch (state) {
            case NEW:
                service.startAsync();
                break;
            case STARTING:
            case RUNNING:
                break;
            default:
                throw new IllegalStateException("Can't start service in state " + state);
        }
        if (!asyncStart) {
            service.awaitRunning();
        }
    }

    public boolean isAsyncStart() {
        return asyncStart;
    }

    public void setAsyncStart(boolean asyncStart) {
        this.asyncStart = asyncStart;
    }

    @Override
    public void stop(Runnable callback) {
        AsyncStopSupportListener asyncStopSupport = new AsyncStopSupportListener(callback);
        service.addListener(asyncStopSupport, Runnable::run);
        service.stopAsync();
        if (TERMINAL_STATES.contains(service.state())) {
            //Execute callback if service was stopped before adding listener.
            //Listener is responsible for executing callback only once
            asyncStopSupport.executeCallback();
        }
    }

    @Override
    public void stop() {
        service.stopAsync();
        try {
            service.awaitTerminated();
        } catch (IllegalStateException e) {
            if (service.state() != State.FAILED) {
                throw e;
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        if (stopOnDestroy) {
            stop();
        }
    }

    public boolean isStopOnDestroy() {
        return stopOnDestroy;
    }

    public void setStopOnDestroy(boolean stopOnDestroy) {
        this.stopOnDestroy = stopOnDestroy;
    }

    @Override
    public boolean isRunning() {
        return RUNNING_STATES.contains(service.state());
    }

    @Override
    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    private static class AsyncStopSupportListener extends Service.Listener {
        private final AtomicBoolean callbackExecuted = new AtomicBoolean();
        private final Runnable callback;

        private AsyncStopSupportListener(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void terminated(State from) {
            executeCallback();
        }

        @Override
        public void failed(State from, Throwable failure) {
            executeCallback();
        }

        public void executeCallback() {
            if (callbackExecuted.compareAndSet(false, true)) {
                callback.run();
            }
        }
    }
}
