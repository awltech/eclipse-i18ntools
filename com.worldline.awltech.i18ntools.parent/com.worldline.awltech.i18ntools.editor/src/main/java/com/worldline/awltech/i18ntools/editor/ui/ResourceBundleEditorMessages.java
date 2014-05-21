/**
 * I18N Tools
 *
 * Copyright (C) 2014 Worldline or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
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
	ERROR_INPUTNOTFILE("ERROR_INPUTNOTFILE"), ERROR_INPUTNOTCU("ERROR_INPUTNOTCU"), ERROR_FAILTOPARSECODE("ERROR_FAILTOPARSECODE"), LABEL_OPTIONSGROUP("LABEL_OPTIONSGROUP"), LABEL_TABLEGROUP("LABEL_TABLEGROUP"), LABEL_LOCALECOMBO("LABEL_LOCALECOMBO"), LABEL_LOCALEBUTTON("LABEL_LOCALEBUTTON"), LABEL_NEWKEYLABEL("LABEL_NEWKEYLABEL"), LABEL_NEWKEYBUTTON("LABEL_NEWKEYBUTTON"), LABEL_KEYCOLUMN("LABEL_KEYCOLUMN"), LABEL_DEFAULTCOLUMN("LABEL_DEFAULTCOLUMN"), LABEL_LOCALECOLUMN("LABEL_LOCALECOLUMN"), LOCALEDIALOG_TITLE("LOCALEDIALOG_TITLE"), LOCALEDIALOG_MESSAGE("LOCALEDIALOG_MESSAGE")
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
