package com.worldline.awltech.i18ntools.wizard.core.ui.validation;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

public class SWTDialogValidatorListener implements ModifyListener {

	private final SWTDialogValidator validator;

	public SWTDialogValidatorListener(final SWTDialogValidator validator) {
		this.validator = validator;
	}

	@Override
	public void modifyText(final ModifyEvent e) {
		this.validator.validate();
	}

}
