package net.ko.persistence.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The UniqueConstraint annotation is used to specify that a unique constraint
 * is to be included in the generated DDL for a primary or secondary table.
 */
@Target({})
@Retention(RUNTIME)
public @interface UniqueConstraint {

	/**
	 * (Required) An array of the column names that make up the constraint.
	 */
	String[] columnNames();

}
