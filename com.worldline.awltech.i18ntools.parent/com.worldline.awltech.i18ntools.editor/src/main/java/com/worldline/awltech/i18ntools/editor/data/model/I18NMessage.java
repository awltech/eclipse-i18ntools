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

/**
 * Message with status
 * 
 * @author mvanbesien
 * 
 */
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
