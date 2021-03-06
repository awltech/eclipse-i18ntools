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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import com.worldline.awltech.i18ntools.wizard.core.Activator;
import com.worldline.awltech.i18ntools.wizard.core.RefactoringWizardMessages;
import com.worldline.awltech.i18ntools.wizard.core.api.IRefactoringJobCallback;

/**
 * Extension Point manager that locates and load Refactoring Callback
 * implementations
 * 
 * @author manbesien
 * 
 */
public enum RefactoringJobCallbacksManager {
	INSTANCE;

	/**
	 * Extension point name
	 */
	private static final String EXTENSION_POINT_NAME = "RefactoringJobCallbacks";

	/**
	 * Extension point element name
	 */
	private static final String EXTENSION_POINT_ELEMENT = "callback";

	/**
	 * Extension point element's attribute name
	 */
	private static final String EXTENSION_POINT_ATTRIBUTE = "implementation";

	/**
	 * Registered callbacks
	 */
	private final Collection<IRefactoringJobCallback> registeredCallbacks;

	/**
	 * @return Registered callbacks
	 */
	public Collection<IRefactoringJobCallback> getRegisteredCallbacks() {
		return this.registeredCallbacks;
	}

	/**
	 * Private constructor, that loads extension point once for all.
	 */
	private RefactoringJobCallbacksManager() {

		Collection<IRefactoringJobCallback> foundCallbacks = new ArrayList<IRefactoringJobCallback>();

		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID,
				EXTENSION_POINT_NAME);

		for (IExtension extension : extensionPoint.getExtensions()) {
			for (IConfigurationElement configurationElement : extension.getConfigurationElements()) {
				if (EXTENSION_POINT_ELEMENT.equals(configurationElement.getName())) {
					try {
						Object createdExecutableExtension = configurationElement
								.createExecutableExtension(EXTENSION_POINT_ATTRIBUTE);
						if (createdExecutableExtension instanceof IRefactoringJobCallback) {
							foundCallbacks.add((IRefactoringJobCallback) createdExecutableExtension);
						}
					} catch (CoreException e) {
						Activator
								.getDefault()
								.getLog()
								.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
										RefactoringWizardMessages.ERROR_LOADING_CALLBACK.value(), e));
					}
				}
			}
		}

		this.registeredCallbacks = Collections.unmodifiableCollection(foundCallbacks);

	}
}
