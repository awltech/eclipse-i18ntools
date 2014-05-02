package com.worldline.awltech.i18ntools.editor.ui;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.worldline.awltech.i18ntools.editor.data.model.I18NResourceBundle;

/**
 * Editor's table content provider
 * @author mvanbesien
 *
 */
public class EditorTableContentProvider implements IStructuredContentProvider {

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof I18NResourceBundle) {
			return ((I18NResourceBundle) inputElement).getAllMessages().toArray();
		}
		return new Object[0];
	}

}
