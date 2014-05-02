package com.worldline.awltech.i18ntools.editor.ui;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorReference;

/**
 * Editor strategy. (Unused at the moment)
 * 
 * @author mvanbesien
 * 
 */
public class ResourceBundleEditorStrategy implements IEditorMatchingStrategy {

	@Override
	public boolean matches(IEditorReference editorRef, IEditorInput input) {
		return true;
	}

}
