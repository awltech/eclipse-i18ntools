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
package com.worldline.awltech.i18ntools.wizard.core.modules;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

/**
 * Utilitary class that clones a given AST tree.
 * 
 * @author mvanbesien
 * 
 */
public final class ASTTreeCloner {

	private ASTTreeCloner() {
	}

	/**
	 * Clones the AST Tree.
	 * 
	 * @param astNode
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ASTNode> T clone(final T astNode) {
		final AST ast = astNode.getAST();
		final ASTNode createdInstance = ast.createInstance(astNode.getClass());
		final List<?> structuralPropertiesForType = astNode.structuralPropertiesForType();
		for (final Object o : structuralPropertiesForType) {
			final StructuralPropertyDescriptor descriptor = (StructuralPropertyDescriptor) o;
			if (descriptor.isChildListProperty()) {
				final List<Object> list = (List<Object>) astNode.getStructuralProperty(descriptor);
				for (final Object propertyValue : (List<Object>) astNode.getStructuralProperty(descriptor)) {
					list.add(propertyValue instanceof ASTNode ? ASTTreeCloner.clone((ASTNode) propertyValue)
							: propertyValue);
				}
			} else {
				final Object propertyValue = astNode.getStructuralProperty(descriptor);
				createdInstance
						.setStructuralProperty(descriptor,
								propertyValue instanceof ASTNode ? ASTTreeCloner.clone((ASTNode) propertyValue)
										: propertyValue);
			}
		}
		return (T) createdInstance;
	}

}
