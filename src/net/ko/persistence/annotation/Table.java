package net.ko.persistence.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The Table annotation specifies the primary table for the annotated entity.
 * Additional tables may be specified using SecondaryTable or SecondaryTables
 * annotation.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Table {

	/**
	 * (Optional) The name of the table.
	 */
	String name() default "";

	/**
	 * (Optional) The catalog of the table.
	 */
	String catalog() default "";

	/**
	 * (Optional) The schema of the table.
	 */
	String schema() default "";

	/**
	 * (Optional) Unique constraints that are to be placed on the table. These
	 * are only used if table generation is in effect. These constraints apply
	 * in addition to any constraints specified by the Column and JoinColumn
	 * annotations and constraints entailed by primary key mappings.
	 */
	UniqueConstraint[] uniqueConstraints() default {};
}