package net.ko.persistence.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Class annotated with this annotation is an entity class.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Entity {
	String name() default "";
}
