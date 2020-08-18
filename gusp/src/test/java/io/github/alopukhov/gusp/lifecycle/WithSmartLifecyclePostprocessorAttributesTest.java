package io.github.alopukhov.gusp.lifecycle;

import com.google.common.util.concurrent.Service;
import io.github.alopukhov.gusp.annotations.WithSmartLifecycle;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.github.alopukhov.gusp.lifecycle.ServiceSmartLifecycleAdpaterAssert.assertThatLifecycleSupport;
import static io.github.alopukhov.gusp.lifecycle.WithSmartLifecyclePostprocessor.DEFAULT_BEAN_NAME_SUFFIX;

/**
 * Test that focuses on proper annotation attributes propagation
 */
@SuppressWarnings("UnstableApiUsage")
class WithSmartLifecyclePostprocessorAttributesTest {
    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Conf.class);

    @Test
    void testDefaultAttributes() {
        assertDefaultNamedSupportBean("allDefaults")
                .hasAutoStartupEnabled()
                .hasPhase(0)
                .hasAsyncStartDisabled()
                .hasStopOnDestroyDisabled();
    }

    @Test
    void testCustomName() {
        assertSupportBean("customBeanName")
                .hasService(context.getBean("named", Service.class));
    }

    @Test
    void testAutoStartupAttribute() {
        assertDefaultNamedSupportBean("asyncStartTrue").hasAsyncStartEnabled();
        assertDefaultNamedSupportBean("asyncStartFalse").hasAsyncStartDisabled();
    }

    @Test
    void testAsyncStartAttribute() {
        assertDefaultNamedSupportBean("autoStartupTrue").hasAutoStartupEnabled();
        assertDefaultNamedSupportBean("autoStartupFalse").hasAutoStartupDisabled();
    }

    @Test
    void testCustomPhase() {
        assertDefaultNamedSupportBean("phase42").hasPhase(42);
    }

    @Test
    void testStopOnDestroyCallback() {
        assertDefaultNamedSupportBean("addStopOnDestroyCallbackTrue").hasStopOnDestroyEnabled();
        assertDefaultNamedSupportBean("addStopOnDestroyCallbackFalse").hasStopOnDestroyDisabled();
    }

    private ServiceSmartLifecycleAdpaterAssert assertDefaultNamedSupportBean(String serviceName) {
        return assertSupportBean(serviceName + DEFAULT_BEAN_NAME_SUFFIX)
                .hasService(context.getBean(serviceName, Service.class));
    }

    private ServiceSmartLifecycleAdpaterAssert assertSupportBean(String supportBeanName) {
        return assertThatLifecycleSupport(context.getBean(supportBeanName, ServiceSmartLifecycle.class))
                .describedAs(supportBeanName);
    }

    @SuppressWarnings({"DefaultAnnotationParam", "unused"})
    @Configuration
    public static class Conf {
        @Bean
        public static WithSmartLifecyclePostprocessor withSmartLifeCyclePostprocessor() {
            return new WithSmartLifecyclePostprocessor();
        }

        @Bean
        @WithSmartLifecycle
        public static Service allDefaults() {
            return new DummyService();
        }

        @Bean
        @WithSmartLifecycle(beanName = "customBeanName")
        public static Service named() {
            return new DummyService();
        }

        @Bean
        @WithSmartLifecycle(asyncStart = true)
        public static Service asyncStartTrue() {
            return new DummyService();
        }

        @Bean
        @WithSmartLifecycle(asyncStart = false)
        public static Service asyncStartFalse() {
            return new DummyService();
        }

        @Bean
        @WithSmartLifecycle(autoStartup = true)
        public static Service autoStartupTrue() {
            return new DummyService();
        }

        @Bean
        @WithSmartLifecycle(autoStartup = false)
        public static Service autoStartupFalse() {
            return new DummyService();
        }

        @Bean
        @WithSmartLifecycle(phase = 42)
        public static Service phase42() {
            return new DummyService();
        }

        @Bean
        @WithSmartLifecycle(stopOnDestroy = true)
        public static Service addStopOnDestroyCallbackTrue() {
            return new DummyService();
        }

        @Bean
        @WithSmartLifecycle(stopOnDestroy = false)
        public static Service addStopOnDestroyCallbackFalse() {
            return new DummyService();
        }
    }
}