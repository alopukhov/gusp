package io.github.alopukhov.gusp.lifecycle;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.springframework.context.Lifecycle;
import org.springframework.context.SmartLifecycle;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.alopukhov.gusp.lifecycle.ServiceSmartLifecycleAdpaterAssert.assertThatLifecycleSupport;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@SuppressWarnings("UnstableApiUsage")
class ServiceSmartLifecycleTest {
    private static final Map<State, Boolean> RUNNING_STATES = ImmutableMap.<State, Boolean>builder()
            .put(State.NEW, false)
            .put(State.STARTING, true)
            .put(State.RUNNING, true)
            .put(State.STOPPING, true)
            .put(State.FAILED, false)
            .put(State.TERMINATED, false)
            .build();

    @Test
    void testIsRunningForAllStates() {
        assertThat(RUNNING_STATES).describedAs("Tested states").containsOnlyKeys(State.values());
        RUNNING_STATES.forEach((state, expectedIsRunning) -> {
            // given
            Service service = createMockServiceInState(state);
            // when
            ServiceSmartLifecycle lifecycleSupport = new ServiceSmartLifecycle(service);
            // then
            assertThatLifecycleSupport(lifecycleSupport)
                    .describedAs("%s with service in state %s", ServiceSmartLifecycle.class.getSimpleName(), state)
                    .isRunning(expectedIsRunning);
        });
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"STOPPING", "TERMINATED", "FAILED"})
    public void givenServiceInNonStartableState_start_throwsISE(State nonStartableState) {
        // given
        Service mockService = createMockServiceInState(nonStartableState);
        Lifecycle lifecycleSupport = new ServiceSmartLifecycle(mockService);
        // when
        Throwable exception = catchThrowable(lifecycleSupport::start);
        // then
        assertThat(exception)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(nonStartableState.toString());
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"NEW", "STARTING", "RUNNING"})
    public void syncStartSequenceTest(State state) {
        // given
        Service mockService = createMockServiceInState(state);
        ServiceSmartLifecycle lifecycleSupport = new ServiceSmartLifecycle(mockService);
        lifecycleSupport.setAsyncStart(false);
        // when
        lifecycleSupport.start();
        // then
        verifyStartSequence(mockService, state == State.NEW, true);
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"NEW", "STARTING", "RUNNING"})
    public void asyncStartSequenceTest(State state) {
        // given
        Service mockService = createMockServiceInState(state);
        ServiceSmartLifecycle lifecycleSupport = new ServiceSmartLifecycle(mockService);
        lifecycleSupport.setAsyncStart(true);
        // when
        lifecycleSupport.start();
        // then
        verifyStartSequence(mockService, state == State.NEW, false);
    }

    @Test
    public void givenNullService_constructor_throwsNPE() {
        // when
        Throwable throwable = catchThrowable(() -> new ServiceSmartLifecycle(null));
        // then
        assertThat(throwable)
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("service");
    }

    @Test
    public void testDestroyWithStopOnDestroyEnabled() throws Exception {
        // given
        ServiceSmartLifecycle lifecycle = new ServiceSmartLifecycle(new DummyService());
        lifecycle.setStopOnDestroy(true);
        lifecycle.start();
        // when
        lifecycle.destroy();
        // then
        assertThat(lifecycle.getService().state()).isEqualTo(State.TERMINATED);
    }

    @Test
    public void testDestroyWithStopOnDestroyDisabled() throws Exception {
        // given
        ServiceSmartLifecycle lifecycle = new ServiceSmartLifecycle(new DummyService());
        lifecycle.setStopOnDestroy(false);
        lifecycle.start();
        // when
        lifecycle.destroy();
        // then
        assertThat(lifecycle.getService().state()).isEqualTo(State.RUNNING);
    }

    @Test
    public void syncStopSequenceTest() {
        // given
        ServiceSmartLifecycle lifecycleSupport = new ServiceSmartLifecycle(mock(Service.class));
        // when
        lifecycleSupport.stop();
        // then
        verifySyncStopSequence(lifecycleSupport.getService());
    }

    @Test
    public void givenServiceWhichFailsOnStop_stop_swallowsIllegalStateException() {
        // given
        Service service = new AbstractService() {
            @Override
            protected void doStart() {
                notifyStarted();
            }

            @Override
            protected void doStop() {
                notifyFailed(new Exception("test failure"));
            }
        };
        // when
        Lifecycle lifecycle = new ServiceSmartLifecycle(service);
        lifecycle.start();
        Throwable throwable = catchThrowable(lifecycle::stop);
        // then
        assertThat(throwable).doesNotThrowAnyException();
    }

    @Test
    public void givenServiceInFailedState_stop_swallowsIllegalStateException() {
        // given
        ManualTestService service = new ManualTestService();
        // when
        Lifecycle lifecycle = new ServiceSmartLifecycle(service);
        service.startAsync();
        service.externalFail(new RuntimeException("Test failure"));
        Throwable throwable = catchThrowable(lifecycle::stop);
        // then
        assertThat(throwable).doesNotThrowAnyException();
    }

    @Test
    public void givenServiceInTerminatedState_stop_doesNotSwallowIllegalStateException() {
        // given
        Service mock = createMockServiceInState(State.TERMINATED);
        IllegalStateException ise = new IllegalStateException("Test exception");
        doThrow(ise).when(mock).awaitTerminated();
        Lifecycle lifecycle = new ServiceSmartLifecycle(mock);
        // when
        Throwable throwable = catchThrowable(lifecycle::stop);
        // then
        assertThat(throwable).isSameAs(ise);
    }

    @ParameterizedTest
    @ValueSource(classes = {ManualTestService.class, ManualThreadedTestService.class})
    public void testAsyncStopSequenceOnRunningService(Class<?> serviceClass) throws Exception {
        // given
        CountDownLatch cdl = new CountDownLatch(1);
        ManualService service = (ManualService)serviceClass.getConstructor().newInstance();
        SmartLifecycle smartLifecycle = new ServiceSmartLifecycle(service);
        smartLifecycle.start();
        // when
        smartLifecycle.stop(cdl::countDown);
        // then
        assertThat(cdl.await(100, TimeUnit.MILLISECONDS)).isFalse();
        // when
        service.externalStop();
        // then
        assertThat(cdl.await(1, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    public void testAsyncStopSequenceOnRunningServiceThatFails() {
        // given
        AtomicInteger counter = new AtomicInteger();
        ManualTestService service = new ManualTestService();
        SmartLifecycle smartLifecycle = new ServiceSmartLifecycle(service);
        smartLifecycle.start();
        // when
        smartLifecycle.stop(counter::incrementAndGet);
        // then
        assertThat(counter).hasValue(0);
        // when
        service.externalFail(new RuntimeException("Test exception"));
        // then
        assertThat(counter).hasValue(1);
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"NEW", "FAILED", "TERMINATED"})
    public void testAsyncStopSequenceOnService(State state) {
        // given
        AtomicInteger counter = new AtomicInteger();
        SmartLifecycle smartLifecycle = new ServiceSmartLifecycle(createManualService(state));
        // when
        smartLifecycle.stop(counter::incrementAndGet);
        // then
        assertThat(counter).hasValue(1);
    }

    private void verifyStartSequence(Service mockService, boolean shouldStart, boolean shouldAwaitRunning) {
        InOrder inOrder = inOrder(mockService);
        inOrder.verify(mockService, atLeastOnce()).state();
        inOrder.verify(mockService, times(shouldStart? 1 : 0)).startAsync();
        inOrder.verify(mockService, times(shouldAwaitRunning? 1 : 0)).awaitRunning();
        inOrder.verifyNoMoreInteractions();
    }

    private void verifySyncStopSequence(Service mockService) {
        InOrder inOrder = inOrder(mockService);
        inOrder.verify(mockService).stopAsync();
        inOrder.verify(mockService).awaitTerminated();
        inOrder.verifyNoMoreInteractions();
    }

    private ManualTestService createManualService(State state) {
        ManualTestService service = new ManualTestService();
        if (state == State.NEW) {
            return service;
        }
        service.startAsync();
        if (state == State.TERMINATED) {
            service.externalStop();
        } else if (state == State.FAILED) {
            service.externalFail(new RuntimeException("Test failure cause"));
        }
        return service;
    }

    private Service createMockServiceInState(State state) {
        Service mock = mock(Service.class);
        when(mock.state()).thenReturn(state);
        return mock;
    }
}