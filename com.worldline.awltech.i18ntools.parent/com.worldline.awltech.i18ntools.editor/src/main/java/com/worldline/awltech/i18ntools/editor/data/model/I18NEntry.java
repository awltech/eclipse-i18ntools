package com.worldline.awltech.i18ntools.editor.data.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

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
