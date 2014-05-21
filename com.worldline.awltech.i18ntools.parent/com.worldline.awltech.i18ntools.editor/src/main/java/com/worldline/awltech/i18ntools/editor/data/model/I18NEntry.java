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
package com.worldline.awltech.i18ntools.editor.data.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Resource Bundle entry. Contains the key, default message and localized ones.
 * 
 * @author mvanbesien
 * 
 */
public class I18NEntry {

	public I18NEntry(String name) {
		this.name = name;
	}

	I18NEntry withMessage(Locale locale, I18NMessage message) {
		if (locale == null) {
			this.defaultMessage = message;
		} else {
			this.localizedMessages.put(locale, message);
		}
		return this;
	}

	private String name;

	private I18NMessage defaultMessage;

	private Map<Locale, I18NMessage> localizedMessages = new LinkedHashMap<>();

	public I18NMessage getDefaultMessage() {
		return defaultMessage;
	}

	public Map<Locale, I18NMessage> getLocalizedMessages() {
		return Collections.unmodifiableMap(this.localizedMessages);
	}

	public String getName() {
		return name;
	}

	public void updateDefaultMessage(String value) {
		if (this.defaultMessage == null) {
			this.defaultMessage = new I18NMessage(null, I18NMessageStatus.NEW);
		}
		this.defaultMessage.update(value);
	}

	public void updateLocalizedMessage(Locale locale, String value) {
		I18NMessage message = this.localizedMessages.get(locale);
		if (message == null) {
			message = new I18NMessage(null, I18NMessageStatus.NEW);
			this.localizedMessages.put(locale, message);
		}
		message.update(value);
	}
}
