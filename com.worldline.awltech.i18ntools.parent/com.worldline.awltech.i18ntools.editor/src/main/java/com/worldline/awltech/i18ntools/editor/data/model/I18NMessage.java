package com.worldline.awltech.i18ntools.editor.data.model;

public class I18NMessage {

	private String value;
	
	private I18NMessageStatus status;
	
	public I18NMessage(String value, I18NMessageStatus status) {
		this.value = value;
		this.status = status;
	}
	
	public I18NMessageStatus getStatus() {
		return status;
	}
	
	public void resetStatus() {
		status = I18NMessageStatus.BUILT;
	}
	
	public String getValue() {
		return value;
	}
	
	
	public void update(String newMessage) {
		if (newMessage != null && !newMessage.equals(value)) {
			this.value = newMessage;
			if (status == I18NMessageStatus.BUILT)
				status = I18NMessageStatus.DIRTY;
		}
	}
	
}
