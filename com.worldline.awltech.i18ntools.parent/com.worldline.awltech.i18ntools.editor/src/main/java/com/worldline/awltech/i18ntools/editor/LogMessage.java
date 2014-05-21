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
package com.worldline.awltech.i18ntools.editor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class LogMessage {

	private int severity;
	private String message;
	private Throwable throwable;

	private LogMessage(int severity) {
		this.severity = severity;
	}

	public static LogMessage info() {
		return new LogMessage(IStatus.INFO);
	}

	public static LogMessage warn() {
		return new LogMessage(IStatus.WARNING);
	}

	public static LogMessage error() {
		return new LogMessage(IStatus.ERROR);
	}

	public LogMessage message(String message) {
		this.message = message;
		return this;
	}

	public LogMessage throwable(Throwable throwable) {
		this.throwable = throwable;
		return this;
	}

	public void log() {
		if (this.message != null || this.throwable != null) {
			Activator.getDefault().getLog()
					.log(new Status(this.severity, Activator.PLUGIN_ID, this.message, this.throwable));
		}
	}

}
