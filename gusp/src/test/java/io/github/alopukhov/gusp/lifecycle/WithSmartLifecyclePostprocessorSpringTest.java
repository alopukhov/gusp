package io.github.alopukhov.gusp.lifecycle;

import com.google.common.util.concurrent.Service;
import io.github.alopukhov.gusp.annotations.WithSmartLifecycle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static com.google.common.util.concurrent.Service.State.RUNNING;
import static com.google.common.util.concurrent.Service.State.TERMINATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SuppressWarnings("UnstableApiUsage")
class WithSmartLifecyclePostprocessorSpringTest {
    @Test
    public void testAutoStartServices() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Conf.class);
        Collection<Service> serviceBeans = context.getBeansOfType(Service.class).values();
        assertThat(serviceBeans)
                .hasSize(2)
                .extracting(Service::state)
                .containsOnly(RUNNING);
        context.stop();
        assertThat(serviceBeans).extracting(Service::state).containsOnly(TERMINATED);
    }

    @Test
    public void testAnnotationOnPrototypeBean() {
        // given
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(ConfWithPrototypeBean.class);
        // when
        Throwable throwable = catchThrowable(context::refresh);
        // then
        assertThat(throwable).isInstanceOf(BeanDefinitionValidationException.class);
    }

    @Configuration
    public static class Conf {
        @Bean
        public static WithSmartLifecyclePostprocessor withSmartLifeCyclePostprocessor() {
            return new WithSmartLifecyclePostprocessor();
        }

        @WithSmartLifecycle
        @Component("a")
        public static class AnnotatedDummyService extends DummyService {
        }

        @WithSmartLifecycle
        @Bean(name = "b")
        public DummyService dummyService() {
            return new DummyService();
        }
    }

    @Configuration
    public static class ConfWithPrototypeBean {
        @Bean
        public static WithSmartLifecyclePostprocessor withSmartLifeCyclePostprocessor() {
            return new WithSmartLifecyclePostprocessor();
        }

        @WithSmartLifecycle
        @Bean
        @Scope("prototype")
        public DummyService dummyService() {
            return new DummyService();
        }
    }
}