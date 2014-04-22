package com.worldline.awltech.i18ntools.wizard.core.modules;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

/**
 * AST Visitor implementation that deeply scans the source code, to locate the
 * AST Node corresponding to the user selection offset and length.
 * 
 * @author mvanbesien
 * 
 */
public class ASTNodeFinder extends ASTVisitor {

	/**
	 * Estimated parent node
	 */
	private ASTNode estimatedParentNode;

	/**
	 * Parent node
	 */
	private ASTNode realParentNode;

	/**
	 * Node, can be null if not found at the end of the process
	 */
	private ASTNode foundNode;

	/**
	 * User selection's offset
	 */
	private final int offset;

	/**
	 * User selection's length
	 */
	private final int length;

	/*
	 * Private constructor
	 */
	private ASTNodeFinder(final int offset, final int length) {
		this.offset = offset;
		this.length = length;
	}

	/**
	 * @return found node. Can be null if could not be resolved.
	 */
	public ASTNode getFoundNode() {
		return this.foundNode;
	}

	/**
	 * @return parent node. If found node can not be found, it will return the
	 *         closest node, according to the initial offset and length.
	 */
	public ASTNode getParentNode() {
		return this.realParentNode != null ? this.realParentNode : this.estimatedParentNode;
	}

	/**
	 * Builds new NodeFinder. It will process the passed node, to find the child
	 * node, which is the closest to the offset and length passed.
	 * 
	 * @param node
	 * @param offset
	 * @param length
	 * @return
	 */
	public static ASTNodeFinder create(final ASTNode node, final int offset, final int length) {
		final ASTNodeFinder finder = new ASTNodeFinder(offset, length);
		node.accept(finder);
		return finder;
	}

	/**
	 * Main method. All AST Visitor implemented here should delegate to it.
	 * 
	 * @param astNode
	 * @return
	 */
	private boolean internalVisit(final ASTNode astNode) {

		if (this.estimatedParentNode == null) {
			this.estimatedParentNode = astNode;
		} else if (this.estimatedParentNode.getStartPosition() < astNode.getStartPosition()
				&& astNode.getStartPosition() <= this.offset) {
			this.estimatedParentNode = astNode;
		}

		if (astNode.getStartPosition() == this.offset && astNode.getLength() == this.length) {
			this.foundNode = astNode;
			this.realParentNode = astNode.getParent();
		}

		return astNode.getStartPosition() <= this.offset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * AnnotationTypeDeclaration)
	 */
	@Override
	public boolean visit(final AnnotationTypeDeclaration node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * AnnotationTypeMemberDeclaration)
	 */
	@Override
	public boolean visit(final AnnotationTypeMemberDeclaration node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * AnonymousClassDeclaration)
	 */
	@Override
	public boolean visit(final AnonymousClassDeclaration node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * ArrayAccess)
	 */
	@Override
	public boolean visit(final ArrayAccess node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * ArrayCreation)
	 */
	@Override
	public boolean visit(final ArrayCreation node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * ArrayInitializer)
	 */
	@Override
	public boolean visit(final ArrayInitializer node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ArrayType
	 * )
	 */
	@Override
	public boolean visit(final ArrayType node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * AssertStatement)
	 */
	@Override
	public boolean visit(final AssertStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Assignment
	 * )
	 */
	@Override
	public boolean visit(final Assignment node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Block)
	 */
	@Override
	public boolean visit(final Block node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * BlockComment)
	 */
	@Override
	public boolean visit(final BlockComment node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * BooleanLiteral)
	 */
	@Override
	public boolean visit(final BooleanLiteral node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * BreakStatement)
	 */
	@Override
	public boolean visit(final BreakStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * CastExpression)
	 */
	@Override
	public boolean visit(final CastExpression node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * CatchClause)
	 */
	@Override
	public boolean visit(final CatchClause node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * CharacterLiteral)
	 */
	@Override
	public boolean visit(final CharacterLiteral node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * ClassInstanceCreation)
	 */
	@Override
	public boolean visit(final ClassInstanceCreation node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * CompilationUnit)
	 */
	@Override
	public boolean visit(final CompilationUnit node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * ConditionalExpression)
	 */
	@Override
	public boolean visit(final ConditionalExpression node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * ConstructorInvocation)
	 */
	@Override
	public boolean visit(final ConstructorInvocation node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * ContinueStatement)
	 */
	@Override
	public boolean visit(final ContinueStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * DoStatement)
	 */
	@Override
	public boolean visit(final DoStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * EmptyStatement)
	 */
	@Override
	public boolean visit(final EmptyStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * EnhancedForStatement)
	 */
	@Override
	public boolean visit(final EnhancedForStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * EnumConstantDeclaration)
	 */
	@Override
	public boolean visit(final EnumConstantDeclaration node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * EnumDeclaration)
	 */
	@Override
	public boolean visit(final EnumDeclaration node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * ExpressionStatement)
	 */
	@Override
	public boolean visit(final ExpressionStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * FieldAccess)
	 */
	@Override
	public boolean visit(final FieldAccess node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * FieldDeclaration)
	 */
	@Override
	public boolean visit(final FieldDeclaration node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * ForStatement)
	 */
	@Override
	public boolean visit(final ForStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * IfStatement)
	 */
	@Override
	public boolean visit(final IfStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * ImportDeclaration)
	 */
	@Override
	public boolean visit(final ImportDeclaration node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * InfixExpression)
	 */
	@Override
	public boolean visit(final InfixExpression node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * Initializer)
	 */
	@Override
	public boolean visit(final Initializer node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * InstanceofExpression)
	 */
	@Override
	public boolean visit(final InstanceofExpression node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Javadoc
	 * )
	 */
	@Override
	public boolean visit(final Javadoc node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * LabeledStatement)
	 */
	@Override
	public boolean visit(final LabeledStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * LineComment)
	 */
	@Override
	public boolean visit(final LineComment node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * MarkerAnnotation)
	 */
	@Override
	public boolean visit(final MarkerAnnotation node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MemberRef
	 * )
	 */
	@Override
	public boolean visit(final MemberRef node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * MemberValuePair)
	 */
	@Override
	public boolean visit(final MemberValuePair node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * MethodDeclaration)
	 */
	@Override
	public boolean visit(final MethodDeclaration node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * MethodInvocation)
	 */
	@Override
	public boolean visit(final MethodInvocation node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodRef
	 * )
	 */
	@Override
	public boolean visit(final MethodRef node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * MethodRefParameter)
	 */
	@Override
	public boolean visit(final MethodRefParameter node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Modifier
	 * )
	 */
	@Override
	public boolean visit(final Modifier node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * NormalAnnotation)
	 */
	@Override
	public boolean visit(final NormalAnnotation node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * NullLiteral)
	 */
	@Override
	public boolean visit(final NullLiteral node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * NumberLiteral)
	 */
	@Override
	public boolean visit(final NumberLiteral node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * PackageDeclaration)
	 */
	@Override
	public boolean visit(final PackageDeclaration node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * ParameterizedType)
	 */
	@Override
	public boolean visit(final ParameterizedType node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * ParenthesizedExpression)
	 */
	@Override
	public boolean visit(final ParenthesizedExpression node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * PostfixExpression)
	 */
	@Override
	public boolean visit(final PostfixExpression node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * PrefixExpression)
	 */
	@Override
	public boolean visit(final PrefixExpression node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * PrimitiveType)
	 */
	@Override
	public boolean visit(final PrimitiveType node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * QualifiedName)
	 */
	@Override
	public boolean visit(final QualifiedName node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * QualifiedType)
	 */
	@Override
	public boolean visit(final QualifiedType node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * ReturnStatement)
	 */
	@Override
	public boolean visit(final ReturnStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SimpleName
	 * )
	 */
	@Override
	public boolean visit(final SimpleName node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SimpleType
	 * )
	 */
	@Override
	public boolean visit(final SimpleType node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * SingleMemberAnnotation)
	 */
	@Override
	public boolean visit(final SingleMemberAnnotation node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * SingleVariableDeclaration)
	 */
	@Override
	public boolean visit(final SingleVariableDeclaration node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * StringLiteral)
	 */
	@Override
	public boolean visit(final StringLiteral node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * SuperConstructorInvocation)
	 */
	@Override
	public boolean visit(final SuperConstructorInvocation node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * SuperFieldAccess)
	 */
	@Override
	public boolean visit(final SuperFieldAccess node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * SuperMethodInvocation)
	 */
	@Override
	public boolean visit(final SuperMethodInvocation node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SwitchCase
	 * )
	 */
	@Override
	public boolean visit(final SwitchCase node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * SwitchStatement)
	 */
	@Override
	public boolean visit(final SwitchStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * SynchronizedStatement)
	 */
	@Override
	public boolean visit(final SynchronizedStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TagElement
	 * )
	 */
	@Override
	public boolean visit(final TagElement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * TextElement)
	 */
	@Override
	public boolean visit(final TextElement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * ThisExpression)
	 */
	@Override
	public boolean visit(final ThisExpression node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * ThrowStatement)
	 */
	@Override
	public boolean visit(final ThrowStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * TryStatement)
	 */
	@Override
	public boolean visit(final TryStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * TypeDeclaration)
	 */
	@Override
	public boolean visit(final TypeDeclaration node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * TypeDeclarationStatement)
	 */
	@Override
	public boolean visit(final TypeDeclarationStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * TypeLiteral)
	 */
	@Override
	public boolean visit(final TypeLiteral node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * TypeParameter)
	 */
	@Override
	public boolean visit(final TypeParameter node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * VariableDeclarationExpression)
	 */
	@Override
	public boolean visit(final VariableDeclarationExpression node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * VariableDeclarationFragment)
	 */
	@Override
	public boolean visit(final VariableDeclarationFragment node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * VariableDeclarationStatement)
	 */
	@Override
	public boolean visit(final VariableDeclarationStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * WhileStatement)
	 */
	@Override
	public boolean visit(final WhileStatement node) {
		return this.internalVisit(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * WildcardType)
	 */
	@Override
	public boolean visit(final WildcardType node) {
		return this.internalVisit(node);
	}

}
