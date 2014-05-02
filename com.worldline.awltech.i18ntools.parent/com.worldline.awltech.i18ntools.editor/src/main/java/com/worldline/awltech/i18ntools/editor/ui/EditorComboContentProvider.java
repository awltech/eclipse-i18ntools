package com.worldline.awltech.i18ntools.editor.ui;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.worldline.awltech.i18ntools.editor.data.model.I18NResourceBundle;

public class EditorComboContentProvider implements IStructuredContentProvider {

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof I18NResourceBundle) {
			Set<Locale> locales = new LinkedHashSet<>(((I18NResourceBundle) inputElement).getLocales().keySet());
			locales.remove(null);
			return locales.toArray();
		}
		return new Object[0];
	}

}
