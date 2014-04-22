package com.worldline.awltech.i18ntools.wizard.core.ui.validation;

import org.eclipse.core.runtime.IStatus;

/**
 * Text validation interface.
 * 
 * Used to validate the Wizard's text fields.
 * 
 * @author mvanbesien.
 * 
 */
public interface TextValidator {

	/**
	 * Validates the passed input, and returns a status whether the field's
	 * value is acceptable.
	 * 
	 * @param input
	 * @return
	 */
	public IStatus validate(String input);
}
