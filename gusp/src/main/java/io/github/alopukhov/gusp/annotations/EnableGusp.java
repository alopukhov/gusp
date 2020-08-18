package io.github.alopukhov.gusp.annotations;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable all features GUSP project has to offer.
 *
 * @see GuspConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(GuspConfiguration.class)
@Documented
public @interface EnableGusp {
}
