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
