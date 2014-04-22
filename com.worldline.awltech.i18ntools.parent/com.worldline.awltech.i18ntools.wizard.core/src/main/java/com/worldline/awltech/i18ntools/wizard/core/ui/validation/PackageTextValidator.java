package com.worldline.awltech.i18ntools.wizard.core.ui.validation;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;

/**
 * Validator for valid java package names.
 * 
 * @author mvanbesien
 * 
 */
public class PackageTextValidator implements TextValidator {

	private static final String JAVA_VERSION = "1.7";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.worldline.awltech.i18ntools.wizard.core.ui.validation.TextValidator
	 * #validate(java.lang.String)
	 */
	@Override
	public IStatus validate(final String input) {
		return JavaConventions.validatePackageName(input, PackageTextValidator.JAVA_VERSION,
				PackageTextValidator.JAVA_VERSION);
	}

}
