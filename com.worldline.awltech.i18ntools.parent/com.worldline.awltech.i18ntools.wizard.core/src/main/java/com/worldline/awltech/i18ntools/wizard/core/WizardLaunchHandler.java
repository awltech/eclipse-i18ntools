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
package com.worldline.awltech.i18ntools.wizard.core;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * Eclipse Command Handler for the wizard. This class is used to retrieve the
 * context of the refactoring, when the user uses the keyboard shortcut.
 * 
 * @author mvanbesien
 * 
 */
public class WizardLaunchHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		// Retrieves workbench
		final IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);

		// Retrieves the text selection information
		ITextEditor textEditor = null;
		if (part instanceof ITextEditor) {
			textEditor = (ITextEditor) part;
		} else {
			return null;
		}
		
		final ISelection selection = textEditor.getSelectionProvider().getSelection();
		TextSelection textSelection = null;
		if (selection instanceof TextSelection) {
			textSelection = (TextSelection) selection;
		} else {
			return null;
		}

		final int offset = textSelection.getOffset();
		final int length = textSelection.getLength();

		// Retrieves the current opened source information.
		final IEditorInput editorInput = HandlerUtil.getActiveEditorInput(event);
		final Object adapter = editorInput.getAdapter(IJavaElement.class);

		ICompilationUnit compilationUnit = null;
		if (adapter instanceof ICompilationUnit) {
			compilationUnit = (ICompilationUnit) adapter;
		} else {
			return null;
		}

		// Launches the process.
		final RefactoringJob resourceBundleHelperJob = new RefactoringJob();
		resourceBundleHelperJob.setCompilationUnit(compilationUnit).setSelection(offset, length);
		resourceBundleHelperJob.addJobChangeListener(new AfterRefactoringSourceRelocator(textEditor, offset));
		resourceBundleHelperJob.schedule();

		return null;
	}

}
