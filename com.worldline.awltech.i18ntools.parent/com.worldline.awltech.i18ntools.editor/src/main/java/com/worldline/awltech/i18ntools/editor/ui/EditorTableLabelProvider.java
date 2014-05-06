package com.worldline.awltech.i18ntools.editor.ui;

import java.util.Locale;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.worldline.awltech.i18ntools.editor.Activator;
import com.worldline.awltech.i18ntools.editor.data.model.I18NEntry;
import com.worldline.awltech.i18ntools.editor.data.model.I18NMessage;
import com.worldline.awltech.i18ntools.editor.data.model.I18NMessageStatus;

/**
 * Editor's table label provider.
 * 
 * @author mvanbesien
 * 
 */
public class EditorTableLabelProvider implements ITableLabelProvider {

	private static final String ICONS_MESSAGE_GIF = "/icons/message.gif";
	private static final String ICONS_MESSAGE_MODIFIED_GIF = "/icons/message-modified.gif";
	private Locale locale;

	public EditorTableLabelProvider(Locale locale) {
		this.locale = locale;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0 && element instanceof I18NEntry) {
			I18NEntry entry = (I18NEntry) element;
			if (entry.getDefaultMessage() == null || entry.getDefaultMessage().getStatus() != I18NMessageStatus.BUILT) {
				return Activator.getDefault().getImage(ICONS_MESSAGE_MODIFIED_GIF);
			}
			for (I18NMessage message : entry.getLocalizedMessages().values()) {
				if (message != null && message.getStatus() != I18NMessageStatus.BUILT) {
					return Activator.getDefault().getImage(ICONS_MESSAGE_MODIFIED_GIF);
				}
			}
			return Activator.getDefault().getImage(ICONS_MESSAGE_GIF);
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof I18NEntry) {
			I18NEntry message = (I18NEntry) element;
			switch (columnIndex) {
			case 0:
				return message.getName();
			case 1:
				I18NMessage defaultMessage = message.getDefaultMessage();
				return defaultMessage != null ? defaultMessage.getValue() : null;
			case 2:
				I18NMessage localizedMessage = message.getLocalizedMessages().get(locale);
				return localizedMessage != null ? localizedMessage.getValue() : null;
			default:
				return null;
			}
		}
		return null;
	}

}
