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
		Activator.getDefault().getLog()
				.log(new Status(this.severity, Activator.PLUGIN_ID, this.message, this.throwable));
	}

}
