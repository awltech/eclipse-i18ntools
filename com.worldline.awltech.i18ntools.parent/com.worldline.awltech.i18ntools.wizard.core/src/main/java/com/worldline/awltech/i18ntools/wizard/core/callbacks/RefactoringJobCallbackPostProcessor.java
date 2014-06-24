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
package com.worldline.awltech.i18ntools.wizard.core.callbacks;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jdt.core.ICompilationUnit;

import com.worldline.awltech.i18ntools.wizard.core.Activator;
import com.worldline.awltech.i18ntools.wizard.core.RefactoringWizardMessages;
import com.worldline.awltech.i18ntools.wizard.core.api.IRefactoringJobCallback;

/**
 * Job Listener implementation that looks for callbacks contributed through
 * extension point, to be executed after the refactoring process.
 * 
 * @author mvanbesien
 * 
 */
public class RefactoringJobCallbackPostProcessor implements IJobChangeListener {

	/**
	 * Compilation unit, the refactoring occurred into
	 */
	private final ICompilationUnit impactedCompilationUnit;

	/**
	 * Creates new post processor
	 * 
	 * @param compilationUnit
	 */
	public RefactoringJobCallbackPostProcessor(ICompilationUnit compilationUnit) {
		this.impactedCompilationUnit = compilationUnit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(org.eclipse
	 * .core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void aboutToRun(IJobChangeEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.jobs.IJobChangeListener#awake(org.eclipse.core
	 * .runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void awake(IJobChangeEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.jobs.IJobChangeListener#done(org.eclipse.core
	 * .runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void done(IJobChangeEvent event) {
		for (IRefactoringJobCallback callback : RefactoringJobCallbacksManager.INSTANCE.getRegisteredCallbacks()) {
			try {
				if (callback != null) {
					callback.execute(this.impactedCompilationUnit);
				}
			} catch (Exception e) {
				Activator
						.getDefault()
						.getLog()
						.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
								RefactoringWizardMessages.ERROR_EXECUTING_CALLBACK.value(), e));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.jobs.IJobChangeListener#running(org.eclipse.
	 * core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void running(IJobChangeEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.jobs.IJobChangeListener#scheduled(org.eclipse
	 * .core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void scheduled(IJobChangeEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.jobs.IJobChangeListener#sleeping(org.eclipse
	 * .core.runtime.jobs.IJobChangeEvent)
	 */
	@Override
	public void sleeping(IJobChangeEvent event) {
	}

}
