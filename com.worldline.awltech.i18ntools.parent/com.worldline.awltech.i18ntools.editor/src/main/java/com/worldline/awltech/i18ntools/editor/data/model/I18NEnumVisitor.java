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
package com.worldline.awltech.i18ntools.editor.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * AST Visitor that locates all the literals of a given enumeration. Will return
 * no literal at all if the enumeration is not a ResourceBundle.
 * 
 * @author mvanbesien
 * 
 */
public class I18NEnumVisitor extends ASTVisitor {

	private final List<String> literals = new ArrayList<>();

	private String resourceBundleName;

	private I18NEnumVisitor() {
	}

	public List<String> getLiterals() {
		return Collections.unmodifiableList(this.literals);
	}

	public String getResourceBundleName() {
		return resourceBundleName;
	}

	public static I18NEnumVisitor create(ICompilationUnit compilationUnit) {
		I18NEnumVisitor visitor = new I18NEnumVisitor();

		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setIgnoreMethodBodies(true);
		parser.setResolveBindings(true);
		parser.setSource(compilationUnit);
		ASTNode createdAST = parser.createAST(new NullProgressMonitor());

		createdAST.accept(visitor);

		return visitor;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		// We only validate the root enum, i.e. the one which parent is a
		// Compilation Unit.
		return node.getParent() instanceof CompilationUnit;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		// We are in an enum, and we only want to validate it... So we bypass
		// parsing of all other types.
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		// We bypass all the method parsing...
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		if (node.arguments().size() == 1) {
			Expression expression = (Expression) node.arguments().get(0);
			if (expression instanceof StringLiteral) {
				this.literals.add(((StringLiteral) expression).getLiteralValue());
			}
		}
		// We return as we have the information we want.
		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if ("getBundle".equals(node.getName().getFullyQualifiedName()) && node.arguments().size() > 0) {
			IMethodBinding resolvedMethodBinding = node.resolveMethodBinding();
			if (resolvedMethodBinding != null) {
				ITypeBinding declaringClass = resolvedMethodBinding.getDeclaringClass();
				if (declaringClass != null) {
					if ("ResourceBundle".equals(declaringClass.getName())
							&& "java.util".equals(declaringClass.getPackage().getName())) {
						Expression expression = (Expression) node.arguments().get(0);
						if (expression instanceof StringLiteral) {
							this.resourceBundleName = ((StringLiteral) expression).getLiteralValue();
						}

					}
				}
			}
		}
		// We return as we have the information we want.
		return false;
	}
}
