package com.worldline.awltech.i18ntools.wizard.core;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * Job post listener that, after the job execution, relocates the editor to the
 * updated source.
 * 
 * @author mvanbesien
 * 
 */
public class AfterRefactoringSourceRelocator implements IJobChangeListener {

	/**
	 * Offset of selected source
	 */
	private final int offset;

	/**
	 * Text editor instance.
	 */
	private final ITextEditor textEditor;

	/**
	 * Creates new relocator instance.
	 * 
	 * @param textEditor
	 * @param offset
	 */
	public AfterRefactoringSourceRelocator(final ITextEditor textEditor, final int offset) {
		this.textEditor = textEditor;
		this.offset = offset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(org.eclipse
	 * .core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void aboutToRun(final IJobChangeEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.jobs.IJobChangeListener#awake(org.eclipse.core
	 * .runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void awake(final IJobChangeEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.jobs.IJobChangeListener#done(org.eclipse.core
	 * .runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void done(final IJobChangeEvent event) {
		final Display d = Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
		d.asyncExec(new Runnable() {

			@Override
			public void run() {
				AfterRefactoringSourceRelocator.this.textEditor.selectAndReveal(
						AfterRefactoringSourceRelocator.this.offset, 0);
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.jobs.IJobChangeListener#running(org.eclipse.
	 * core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void running(final IJobChangeEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.jobs.IJobChangeListener#scheduled(org.eclipse
	 * .core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void scheduled(final IJobChangeEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.jobs.IJobChangeListener#sleeping(org.eclipse
	 * .core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void sleeping(final IJobChangeEvent event) {
	}

}
