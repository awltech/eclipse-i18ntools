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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Popup action, input of the process.
 * 
 * @author mvanbesien
 * 
 */
public class WizardLaunchAction implements IObjectActionDelegate {

	/**
	 * Current opened editor input
	 */
	private IEditorInput editorInput;

	/**
	 * Current opened editor instance
	 */
	private ITextEditor textEditor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(final IAction action) {

		// 1. Retrieve the text selection
		final ISelection selection = this.textEditor.getSelectionProvider().getSelection();
		TextSelection textSelection = null;
		if (selection instanceof TextSelection) {
			textSelection = (TextSelection) selection;
		}

		if (textSelection == null) {
			return;
		}

		final int offset = textSelection.getOffset();
		final int length = textSelection.getLength();

		final Object adapter = this.editorInput.getAdapter(IJavaElement.class);
		ICompilationUnit compilationUnit = null;
		if (adapter instanceof ICompilationUnit) {
			compilationUnit = (ICompilationUnit) adapter;
		}

		if (compilationUnit == null) {
			return;
		}
		final RefactoringJob resourceBundleHelperJob = new RefactoringJob();
		resourceBundleHelperJob.setCompilationUnit(compilationUnit).setSelection(offset, length);
		resourceBundleHelperJob.addJobChangeListener(new AfterRefactoringSourceRelocator(this.textEditor, offset));
		resourceBundleHelperJob.schedule();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
	 * .IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {

		this.editorInput = null;

		if (selection instanceof IStructuredSelection) {
			final Object o = ((IStructuredSelection) selection).getFirstElement();
			if (o instanceof IEditorInput) {
				this.editorInput = (IEditorInput) o;
			}
		}

		action.setEnabled(this.editorInput != null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {

		this.textEditor = null;

		if (targetPart instanceof ITextEditor) {
			this.textEditor = (ITextEditor) targetPart;
		}

		action.setEnabled(this.textEditor != null);
	}

}
