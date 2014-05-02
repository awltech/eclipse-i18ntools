/**
 *
 */
package com.worldline.awltech.i18ntools.editor.ui;

import java.util.Locale;
import java.util.ResourceBundle;

import java.text.MessageFormat;

/**
 * Enumeration containing internationalisation-related messages and API.
 * 
 * @generated com.worldline.awltech.i18ntools.wizard
 */
public enum ResourceBundleEditorMessages {
	ERROR_INPUTNOTFILE("ERROR_INPUTNOTFILE"), ERROR_INPUTNOTCU("ERROR_INPUTNOTCU"), ERROR_FAILTOPARSECODE("ERROR_FAILTOPARSECODE"), LABEL_OPTIONSGROUP("LABEL_OPTIONSGROUP"), LABEL_TABLEGROUP("LABEL_TABLEGROUP"), LABEL_LOCALECOMBO("LABEL_LOCALECOMBO"), LABEL_LOCALEBUTTON("LABEL_LOCALEBUTTON"), LABEL_NEWKEYLABEL("LABEL_NEWKEYLABEL"), LABEL_NEWKEYBUTTON("LABEL_NEWKEYBUTTON"), LABEL_KEYCOLUMN("LABEL_KEYCOLUMN"), LABEL_DEFAULTCOLUMN("LABEL_DEFAULTCOLUMN"), LABEL_LOCALECOLUMN("LABEL_LOCALECOLUMN")
	;

	/*
	 * Value of the key
	 */
	private final String messageKey;

	/*
	 * Constant ResourceBundle instance
	 */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("ResourceBundleEditorMessages",
			Locale.getDefault());

	/**
	 * Private Enumeration Literal constructor
	 * 
	 * @param messageKey
	 *            value
	 */
	private ResourceBundleEditorMessages(final String messageKey) {
		this.messageKey = messageKey;
	}

	/**
	 * @return the message associated with the current value
	 */
	public String value() {
		if (ResourceBundleEditorMessages.RESOURCE_BUNDLE == null
				|| !ResourceBundleEditorMessages.RESOURCE_BUNDLE.containsKey(this.messageKey)) {
			return "!!" + this.messageKey + "!!";
		}
		return ResourceBundleEditorMessages.RESOURCE_BUNDLE.getString(this.messageKey);
	}

	/**
	 * Formats and returns the message associated with the current value.
	 * 
	 * @see java.text.MessageFormat
	 * @param parameters
	 *            to use during formatting phase
	 * @return formatted message
	 */
	public String value(final Object... args) {
		if (ResourceBundleEditorMessages.RESOURCE_BUNDLE == null
				|| !ResourceBundleEditorMessages.RESOURCE_BUNDLE.containsKey(this.messageKey)) {
			return "!!" + this.messageKey + "!!";
		}
		return MessageFormat.format(ResourceBundleEditorMessages.RESOURCE_BUNDLE.getString(this.messageKey), args);
	}

}
