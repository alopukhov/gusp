package io.github.alopukhov.gusp.annotations;

import io.github.alopukhov.gusp.lifecycle.ServiceSmartLifecycle;

import java.lang.annotation.*;

/**
 * Marker for creation {@link ServiceSmartLifecycle ServiceSmartLifecycle}.
 * Only guava services should be annotated with it.
 * <p>
 * This annotation can be used both as class level annotation or in java configuration. E.g.
 * <pre>
 *     &#064;WithSmartLifecycle
 *     &#064;Component
 *     public class AcmeService extends AbstractService {...}
 * </pre>
 * or
 * <pre>
 *     &#064;Configuration
 *     public class AcmeConf {
 *         &#064;WithSmartLifecycle
 *         &#064;Bean
 *         public Service acmeService() {...}
 *     }
 * </pre>
 * <p>
 * Postprocessor capable of processing this annotation is required.
 * Register {@link io.github.alopukhov.gusp.lifecycle.WithSmartLifecyclePostprocessor WithSmartLifecyclePostprocessor} manually or just
 * use {@link EnableGusp @EnableGusp} annotation to achieve this.
 *
 * @see EnableGusp
 * @see io.github.alopukhov.gusp.lifecycle.WithSmartLifecyclePostprocessor
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WithSmartLifecycle {
    /**
     * Suggested name for {@link ServiceSmartLifecycle ServiceSmartLifecycle}.
     * If empty (default) name will be created using annotated bean's name.
     */
    String beanName() default "";

    /**
     * Sets {@link ServiceSmartLifecycle#isAutoStartup() ServiceSmartLifecycle#isAsyncStart()}
     * for created bean.
     * Default value is true as this scenario is one of main purposes of GUSP project.
     *
     * @see org.springframework.context.SmartLifecycle#isAutoStartup()
     */
    boolean autoStartup() default true;

    /**
     * Enables asyncStart feature for created bean. Default is false (sync start).
     *
     * @see ServiceSmartLifecycle
     */
    boolean asyncStart() default false;

    /**
     * Sets phase for created bean. Default is 0.
     *
     * @see org.springframework.context.SmartLifecycle#getPhase()
     */
    int phase() default 0;

    /**
     * Sets stopOnDestroy feature for created bean. Default is false.
     *
     * @see ServiceSmartLifecycle
     */
    boolean stopOnDestroy() default false;
}
