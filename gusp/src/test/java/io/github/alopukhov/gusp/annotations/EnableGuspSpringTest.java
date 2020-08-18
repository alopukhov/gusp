package io.github.alopukhov.gusp.annotations;

import io.github.alopukhov.gusp.lifecycle.WithSmartLifecyclePostprocessor;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.*;

class EnableGuspSpringTest {
    @Test
    void confWithEnableGuspHasWithSmartLifecyclePostprocessor() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Conf.class);
        WithSmartLifecyclePostprocessor bean = context.getBean(WithSmartLifecyclePostprocessor.class);
        assertNotNull(bean);
    }

    @Configuration
    @EnableGusp
    public static class Conf {
    }
}