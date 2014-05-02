package com.worldline.awltech.i18ntools.editor.ui;

import java.util.Locale;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * Editor's combo label provider.
 * 
 * @author mvanbesien
 * 
 */
public class EditorComboLabelProvider implements ILabelProvider {

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
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof Locale) {
			Locale locale = (Locale) element;
			return locale.getDisplayName();
		}
		return null;
	}

}
