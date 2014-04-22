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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * 
 * Class that parses the user's selected AST expression and isolates its pattern
 * and parameters.
 * 
 * @author mvanbesien
 * 
 */
public final class ASTExpressionResolver {

	/**
	 * String Builder that will define the pattern
	 */
	private final StringBuilder messagePattern;

	/**
	 * List of the pattern's parameters
	 */
	private final List<Expression> messageParameters;

	/**
	 * Index of the last processed parameter.
	 */
	private int parameterIndex;

	/*
	 * Private Constructor
	 */
	private ASTExpressionResolver() {
		this.messagePattern = new StringBuilder(32);
		this.messageParameters = new ArrayList<Expression>();
		this.parameterIndex = 0;
	}

	/**
	 * Creates new ExpressionResolver from NodeFinder
	 * 
	 * @param nodeFinder
	 * @return
	 */
	public static ASTExpressionResolver create(final ASTNodeFinder nodeFinder) {
		if (nodeFinder.getFoundNode() == null || !(nodeFinder.getFoundNode() instanceof Expression)) {
			return null;
		}

		final ASTExpressionResolver resolver = new ASTExpressionResolver();
		resolver.resolveExpression((Expression) nodeFinder.getFoundNode());
		return resolver;
	}

	/**
	 * Delegates expression resolution depending on the type of the expression.
	 * 
	 * @param expression
	 */
	private void resolveExpression(final Expression expression) {
		if (expression instanceof ParenthesizedExpression) {
			this.resolveParenthesizedExpression((ParenthesizedExpression) expression);
		} else if (expression instanceof StringLiteral) {
			this.resolveStringLiteral((StringLiteral) expression);
		} else if (expression instanceof InfixExpression) {
			this.resolveInfixExpression((InfixExpression) expression);
		} else {
			this.resolveOtherExpression(expression);
		}

	}

	/**
	 * Resolves expression in parentheses
	 * 
	 * @param expression
	 */
	private void resolveParenthesizedExpression(final ParenthesizedExpression expression) {
		this.resolveExpression(expression.getExpression());
	}

	/**
	 * Resolves String Literal
	 * 
	 * @param expression
	 */
	private void resolveStringLiteral(final StringLiteral expression) {
		this.messagePattern.append(expression.getLiteralValue());
	}

	/**
	 * Resolbes Infix Expression
	 * 
	 * @param expression
	 */
	private void resolveInfixExpression(final InfixExpression expression) {
		this.resolveExpression(expression.getLeftOperand());
		this.resolveExpression(expression.getRightOperand());
		for (final Object o : expression.extendedOperands()) {
			this.resolveExpression((Expression) o);
		}

	}

	/**
	 * Resolve other type of expression, i.e. expression parameters.
	 * 
	 * @param expression
	 */
	private void resolveOtherExpression(final Expression expression) {
		this.messagePattern.append("{" + this.parameterIndex + "}");
		this.messageParameters.add(expression);
		this.parameterIndex++;
	}

	/**
	 * @return resolved message pattern
	 */
	public String getMessagePattern() {
		return this.messagePattern.toString();
	}

	/**
	 * @return resolved parameters
	 */
	public Expression[] getMessageParameters() {
		return this.messageParameters.toArray(new Expression[this.messageParameters.size()]);
	}
}
