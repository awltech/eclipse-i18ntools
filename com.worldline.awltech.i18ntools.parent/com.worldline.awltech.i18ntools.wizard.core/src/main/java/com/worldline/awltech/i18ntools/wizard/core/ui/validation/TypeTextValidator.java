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
package com.worldline.awltech.i18ntools.wizard.core.ui.validation;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;

/**
 * Validates that a text field is a valid Java type name.
 * @author mvanbesien
 *
 */
public class TypeTextValidator implements TextValidator {

	private static final String JAVA_VERSION = "1.7";

	/*
	 * (non-Javadoc)
	 * @see com.worldline.awltech.i18ntools.wizard.core.ui.validation.TextValidator#validate(java.lang.String)
	 */
	@Override
	public IStatus validate(final String input) {
		return JavaConventions.validateJavaTypeName(input, TypeTextValidator.JAVA_VERSION,
				TypeTextValidator.JAVA_VERSION);
	}

}
