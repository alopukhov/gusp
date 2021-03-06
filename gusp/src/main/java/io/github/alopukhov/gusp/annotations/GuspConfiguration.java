package io.github.alopukhov.gusp.annotations;

import io.github.alopukhov.gusp.lifecycle.WithSmartLifecyclePostprocessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Crates support beans for all features of GUSP project.
 * Currently there is only {@link WithSmartLifecyclePostprocessor} bean but this may change in the future.
 */
@Configuration
public class GuspConfiguration {
    @Bean(name = "gusp.WithSmartLifecyclePostprocessor")
    public static WithSmartLifecyclePostprocessor withSmartLifeCyclePostprocessor() {
        return new WithSmartLifecyclePostprocessor();
    }
}
