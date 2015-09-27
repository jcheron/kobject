package net.ko.persistence.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The Transient annotation is used to annotate a property or field of an entity
 * class, mapped superclass, or embeddable class. It specifies that the property
 * or field is not persistent.
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface Transient {

}
