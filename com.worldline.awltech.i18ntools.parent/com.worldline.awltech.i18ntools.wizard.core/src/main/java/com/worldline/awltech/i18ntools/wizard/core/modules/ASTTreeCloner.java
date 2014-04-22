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
