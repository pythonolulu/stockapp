package com.javatican.stock.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NullOrNotBlankValidator implements ConstraintValidator<NullOrNotBlank, String> {

	public void initialize(NullOrNotBlank parameters) {
		// Nothing to do here
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		if (value == null) {
			return true;
		}
		if (value.length() == 0) {
			return false;
		}

		boolean isAllWhitespace = value.matches("^\\s*$");
		return !isAllWhitespace;
	}
}