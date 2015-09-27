package net.ko.persistence.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The Id annotation specifies the primary key property or field of an entity.
 * The Id annotation may be applied in an entity or mapped superclass.<br>
 * By default, the mapped column for the primary key of the entity is assumed to
 * be the primary key of the primary table. If no Column annotation is
 * specified, the primary key column name is assumed to be the name of the
 * primary key property or field.
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface Id {

}