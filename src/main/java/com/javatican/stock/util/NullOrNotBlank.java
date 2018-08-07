package com.javatican.stock.util;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import javax.validation.Constraint;
import javax.validation.Payload;

/*
 * this is used for validate null or non-blank input string field,
 * but now use StringTrimmerEditor to convert empty/whitespace strings to null, 
 * so this validator is no longer needed.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = NullOrNotBlankValidator.class)
public @interface NullOrNotBlank {
	String message() default "{com.javatican.validation.constraints.NullOrNotBlank.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
