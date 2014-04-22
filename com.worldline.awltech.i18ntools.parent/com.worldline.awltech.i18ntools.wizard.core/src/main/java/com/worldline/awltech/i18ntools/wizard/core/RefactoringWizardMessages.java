package com.worldline.awltech.i18ntools.wizard.core;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Enumeration containing internationalisation-related messages and API.
 * 
 * @generated com.worldline.awltech.i18ntools.wizard
 */
public enum RefactoringWizardMessages {
	JOB_NAME, WIZARD_TITLE, WIZARD_LABEL_ENUM_PACKAGE, WIZARD_BUTTON_SELECT, WIZARD_LABEL_ENUM_NAME, WIZARD_LABEL_ENUM_LITERAL, WIZARD_BUTTON_OK, WIZARD_BUTTON_CANCEL, ERROR_UPDATE_CU, ERROR_UPDATE_PROPERTIES, GROUP_MESSAGE_TITLE, GROUP_ADVANCED_TITLE, LABEL_SOURCE_FOLDER, LABEL_RESOURCE_FOLDER, ERROR_SAVING_CU, ERROR_LOAD_PROPERTIES, ERROR_SEEK_SOURCEFOLDER, ERROR_SEEK_PACKAGE, WIZARD_TOOLTIP_ENUM_PACKAGE, WIZARD_TOOLTIP_ENUM_NAME, WIZARD_TOOLTIP_ENUM_LITERAL, TOOLTIP_SOURCE_FOLDER, TOOLTIP_RESOURCE_FOLDER, LABEL_VERSION, WARNING_FLUSH_LOCALPREFS, ERROR_CREATE_PACKAGE, ERROR_CREATE_CU, ERROR_READ_TEMPLATE, ERROR_CLOSE_TEMPLATE;

	/*
	 * ResourceBundle instance
	 */
	private static ResourceBundle resourceBundle = ResourceBundle.getBundle("RefactoringWizardMessages");

	/*
	 * Returns value of the message
	 */
	public String value() {
		if (RefactoringWizardMessages.resourceBundle == null
				|| !RefactoringWizardMessages.resourceBundle.containsKey(this.name())) {
			return "!!" + this.name() + "!!";
		}

		return RefactoringWizardMessages.resourceBundle.getString(this.name());
	}

	/*
	 * Returns value of the formatted message
	 */
	public String value(final Object... args) {
		if (RefactoringWizardMessages.resourceBundle == null
				|| !RefactoringWizardMessages.resourceBundle.containsKey(this.name())) {
			return "!!" + this.name() + "!!";
		}

		return MessageFormat.format(RefactoringWizardMessages.resourceBundle.getString(this.name()), args);
	}

}
