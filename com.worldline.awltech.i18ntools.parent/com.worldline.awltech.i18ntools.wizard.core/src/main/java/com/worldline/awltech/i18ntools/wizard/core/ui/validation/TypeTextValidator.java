package com.worldline.awltech.i18ntools.wizard.core.ui.validation;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;

public class TypeTextValidator implements TextValidator {

	private static final String JAVA_VERSION = "1.7";

	@Override
	public IStatus validate(final String input) {
		return JavaConventions.validateJavaTypeName(input, TypeTextValidator.JAVA_VERSION,
				TypeTextValidator.JAVA_VERSION);
	}

}
