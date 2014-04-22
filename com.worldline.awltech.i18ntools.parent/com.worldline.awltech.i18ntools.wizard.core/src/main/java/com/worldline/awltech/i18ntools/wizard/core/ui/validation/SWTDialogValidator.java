package com.worldline.awltech.i18ntools.wizard.core.ui.validation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * SWT Dialog Text Validation implementation.
 * 
 * @author mvanbesien
 *
 */
public class SWTDialogValidator {

	private final Label errorLabel;

	private final Button okButton;

	private final Map<Text, TextValidator> textsToValidate = new HashMap<>();

	public SWTDialogValidator(final Label errorLabel, final Button okButton) {
		this.errorLabel = errorLabel;
		this.okButton = okButton;
	}

	public SWTDialogValidator on(final Text text, final TextValidator validator) {
		if (validator != null) {
			this.textsToValidate.put(text, validator);
			text.addModifyListener(new SWTDialogValidatorListener(this));
		}
		return this;
	}

	public void validate() {
		final Iterator<Entry<Text, TextValidator>> iterator = this.textsToValidate.entrySet().iterator();
		IStatus status = null;
		while (iterator.hasNext()) {
			final Entry<Text, TextValidator> next = iterator.next();
			status = SWTDialogValidator.getMoreSevereStatus(status, next.getValue().validate(next.getKey().getText()));
		}

		if (status.getSeverity() == IStatus.OK || status.getSeverity() == IStatus.INFO) {
			this.errorLabel.setText("");
			this.errorLabel.update();
			this.okButton.setEnabled(true);
			this.okButton.update();
		} else if (status.getSeverity() == IStatus.ERROR || status.getSeverity() == IStatus.CANCEL) {
			this.errorLabel.setText(status.getMessage());
			this.errorLabel.setForeground(this.errorLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
			this.errorLabel.update();
			this.okButton.setEnabled(false);
			this.okButton.update();
		} else if (status.getSeverity() == IStatus.WARNING) {
			this.errorLabel.setText(status.getMessage());
			this.errorLabel.setForeground(this.errorLabel.getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
			this.errorLabel.update();
			this.okButton.setEnabled(true);
			this.okButton.update();
		}
	}

	private static IStatus getMoreSevereStatus(final IStatus firstStatus, final IStatus secondStatus) {
		if (firstStatus == null) {
			return secondStatus;
		}
		if (secondStatus == null) {
			return firstStatus;
		}
		return firstStatus.getSeverity() > secondStatus.getSeverity() ? firstStatus : secondStatus;
	}
}
