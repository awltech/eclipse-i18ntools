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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import com.worldline.awltech.i18ntools.wizard.core.Activator;
import com.worldline.awltech.i18ntools.wizard.core.RefactoringWizardMessages;
import com.worldline.awltech.i18ntools.wizard.core.ui.RefactoringWizardConfiguration;

/**
 * Class that wraps the Resource Bundle information. It contains information
 * about the Enumeration as well as the current processed source code and the
 * Resource Bundle object.
 * 
 * @author mvanbesien
 * 
 */
public class ResourceBundleWrapper {

	/**
	 * Currently processed Java Project
	 */
	private final IJavaProject javaProject;

	/**
	 * Package name containing the Enumeration
	 */
	private final String packageName;

	/**
	 * Name of the resource bundle.
	 */
	private final String resourceBundleName;

	/**
	 * Enumeration AST Compilation Unit
	 */
	private CompilationUnit enumDomCompilationUnit;

	/**
	 * Resource Bundle's contents as properties
	 */
	private Properties properties;

	/**
	 * Enumeration Java Compilation Unit
	 */
	private ICompilationUnit enumJavaCompilationUnit;

	/**
	 * Properties File instance
	 */
	private IFile propertiesFile;

	/**
	 * Project's local configuration.
	 */
	private final RefactoringWizardConfiguration configuration;

	/**
	 * Tells whether the message should be prefixed by its key.
	 */
	private boolean prefixMessageByKey;

	/**
	 * Creates Resource Bundle Wrapper, in specified project.
	 * 
	 * @param javaProject
	 *            : current project
	 * @param packageName
	 *            : name of the package that will contain the enumeration
	 * @param resourceBundleName
	 *            : used for enumeration and properties file names.
	 * @param prefixMessageByKey 
	 */
	public ResourceBundleWrapper(final IJavaProject javaProject, final String packageName,
			final String resourceBundleName, boolean prefixMessageByKey) {
		this.javaProject = javaProject;
		this.packageName = packageName;
		this.resourceBundleName = resourceBundleName;
		this.configuration = new RefactoringWizardConfiguration(javaProject.getProject());
		this.prefixMessageByKey = prefixMessageByKey;
	}

	/**
	 * Replaces, in the initial source code, a selected string by a literal and
	 * enriches the enumeration and the properties file.
	 * 
	 * @param nodeFinder
	 * @param literalName
	 * @param resolver
	 * @return
	 */
	public boolean replaceLiteral(final ASTNodeFinder nodeFinder, final String literalName,
			final ASTExpressionResolver resolver) {
		if (this.enumDomCompilationUnit == null) {
			this.loadCompilationUnit();
		}
		if (this.properties == null) {
			this.loadProperties();
		}
		return this.effectiveAddLiteral(nodeFinder, literalName, resolver);
	}

	/**
	 * Loads Resource Bundle properties file.
	 */
	private void loadProperties() {
		this.properties = new Properties();
		final IProject project = this.javaProject.getProject();
		this.propertiesFile = project.getFile(new Path(this.configuration.getResourceSourceFolder() + "/"
				+ this.resourceBundleName + ".properties"));
		if (this.propertiesFile.exists()) {
			try {
				this.properties.load(this.propertiesFile.getContents());
			} catch (final IOException e) {
				Activator
						.getDefault()
						.getLog()
						.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								RefactoringWizardMessages.ERROR_LOAD_PROPERTIES.value(), e));
			} catch (final CoreException e) {
				Activator
						.getDefault()
						.getLog()
						.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								RefactoringWizardMessages.ERROR_LOAD_PROPERTIES.value(), e));
			}
		}
	}

	/**
	 * Loads the currently selected compilation unit.
	 */
	private void loadCompilationUnit() {
		final IProject project = this.javaProject.getProject();
		final IResource sourceFolderResource = project.getFolder(new Path(this.configuration.getJavaSourceFolder()));
		final IPackageFragmentRoot ipfr = this.javaProject.getPackageFragmentRoot(sourceFolderResource);

		IPackageFragment ipf = ipfr.getPackageFragment(this.packageName);
		if (!ipf.exists()) {
			try {
				ipf = ipfr.createPackageFragment(this.packageName, false, new NullProgressMonitor());
			} catch (final JavaModelException e) {
				Activator
						.getDefault()
						.getLog()
						.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								RefactoringWizardMessages.ERROR_CREATE_PACKAGE.value(), e));
			}
		}

		final String javaUnitName = this.resourceBundleName.concat(".java");
		this.enumJavaCompilationUnit = ipf.getCompilationUnit(javaUnitName);
		if (!this.enumJavaCompilationUnit.exists()) {
			final String contents = this.createJavaUnitContents();

			// Format the source code before trying to set it to the compilation unit.
			CodeFormatter formatter = ToolFactory.createCodeFormatter(this.javaProject.getOptions(true),
					ToolFactory.M_FORMAT_EXISTING);
			IDocument document = new Document(contents);

			TextEdit textEdit = formatter.format(CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS,
					contents, 0, contents.length(), 0, null);
			try {
				textEdit.apply(document);
			} catch (MalformedTreeException e1) {
				Activator
						.getDefault()
						.getLog()
						.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								RefactoringWizardMessages.ERROR_REFACTOR_TEMPLATE.value(), e1));
			} catch (BadLocationException e1) {
				Activator
						.getDefault()
						.getLog()
						.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								RefactoringWizardMessages.ERROR_REFACTOR_TEMPLATE.value(), e1));
			}
			try {
				// Set the source into the compilation unit.
				this.enumJavaCompilationUnit = ipf.createCompilationUnit(javaUnitName, document.get(), false,
						new NullProgressMonitor());
			} catch (final JavaModelException e) {
				Activator
						.getDefault()
						.getLog()
						.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, RefactoringWizardMessages.ERROR_CREATE_CU
								.value(), e));
			}
		}

		final ASTParser enumSourceParser = ASTParser.newParser(AST.JLS4);
		enumSourceParser.setProject(this.javaProject);
		enumSourceParser.setBindingsRecovery(true);
		enumSourceParser.setResolveBindings(true);
		enumSourceParser.setKind(ASTParser.K_COMPILATION_UNIT);
		enumSourceParser.setSource(this.enumJavaCompilationUnit);

		this.enumDomCompilationUnit = (CompilationUnit) enumSourceParser.createAST(new NullProgressMonitor());
		this.enumDomCompilationUnit.recordModifications();
	}

	/**
	 * Loads the Enumeration source template, and formats it with user
	 * information (name, package)
	 * 
	 * @return
	 */
	private String createJavaUnitContents() {
		StringBuilder builder = new StringBuilder();
		final InputStream stream = ResourceBundleWrapper.class
				.getResourceAsStream("/ResourceBundleEnumerationTemplate.txt");
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		try {
			while (reader.ready()) {
				builder = builder.append(reader.readLine() + "\n");
			}
		} catch (final IOException ioe) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, RefactoringWizardMessages.ERROR_READ_TEMPLATE
							.value(), ioe));
		} finally {
			try {
				reader.close();
			} catch (final IOException ioe) {
				Activator
						.getDefault()
						.getLog()
						.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
								RefactoringWizardMessages.ERROR_CLOSE_TEMPLATE.value(), ioe));
			}
		}
		return MessageFormat.format(builder.toString(), this.packageName, this.resourceBundleName,
				this.resourceBundleName);
	}

	/**
	 * Refactors the source code to replace selected source by literal.
	 * 
	 * @param nodeFinder
	 * @param literalName
	 * @param resolver
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean effectiveAddLiteral(final ASTNodeFinder nodeFinder, final String literalName,
			final ASTExpressionResolver resolver) {

		final ASTNode parent = nodeFinder.getParentNode();
		final AST ast = parent.getAST();

		final MethodInvocation replacement = ast.newMethodInvocation();
		replacement.setExpression(ast.newName(this.resourceBundleName + "." + literalName));
		replacement.setName(ast.newSimpleName("value"));

		final EnumDeclaration enumDeclaration = (EnumDeclaration) this.enumDomCompilationUnit.types().get(0);

		final EnumConstantDeclaration enumConstantDeclaration = enumDeclaration.getAST().newEnumConstantDeclaration();
		enumConstantDeclaration.setName(enumDeclaration.getAST().newSimpleName(literalName));

		enumDeclaration.enumConstants().add(enumConstantDeclaration);

		boolean hasMessageKeyConstructor = false;
		for (final Iterator<Object> iterator = enumDeclaration.bodyDeclarations().iterator(); iterator.hasNext()
				&& !hasMessageKeyConstructor;) {
			final Object next = iterator.next();
			if (next instanceof MethodDeclaration) {
				final MethodDeclaration methodDeclaration = (MethodDeclaration) next;
				if (methodDeclaration.isConstructor() && methodDeclaration.parameters().size() > 0) {
					hasMessageKeyConstructor = true;
				}
			}
		}

		if (hasMessageKeyConstructor) {
			final StringLiteral literal = enumDeclaration.getAST().newStringLiteral();
			literal.setLiteralValue(literalName);
			enumConstantDeclaration.arguments().add(literal);
		}

		StructuralPropertyDescriptor locationInParent = null;
		if (nodeFinder.getFoundNode() != null) {
			locationInParent = nodeFinder.getFoundNode().getLocationInParent();
		} else {
			// TODO
			return false;
		}

		ResourceBundleWrapper.addImportToCompilationUnitIfMissing(nodeFinder.getParentNode(), this.packageName + "."
				+ this.resourceBundleName);

		if (locationInParent.isChildListProperty()) {
			final List<Object> list = (List<Object>) parent.getStructuralProperty(locationInParent);
			final int index = list.indexOf(nodeFinder.getFoundNode());
			list.remove(nodeFinder.getFoundNode());
			list.add(index, replacement);
		} else {
			parent.setStructuralProperty(locationInParent, replacement);
		}

		for (final Expression parameter : resolver.getMessageParameters()) {
			final Expression newParameter = ASTTreeCloner.clone(parameter);
			replacement.arguments().add(newParameter);
		}
		
		String messagePattern = resolver.getMessagePattern();
		if (this.prefixMessageByKey) {
			messagePattern = String.format("[%s] %s", literalName, messagePattern);
		}
		
		this.properties.put(literalName, messagePattern);
		return true;
	}

	/**
	 * Adds an import to a compilation unit, if missing
	 * 
	 * @param node
	 * @param newImportName
	 */
	@SuppressWarnings("unchecked")
	private static void addImportToCompilationUnitIfMissing(final ASTNode node, final String newImportName) {
		final CompilationUnit compilationUnit = (CompilationUnit) node.getRoot();
		boolean hasImport = false;
		for (final Iterator<?> iterator = compilationUnit.imports().iterator(); iterator.hasNext() && !hasImport;) {
			final ImportDeclaration importDeclaration = (ImportDeclaration) iterator.next();
			final String importName = importDeclaration.getName().getFullyQualifiedName();
			if (importName.equals(newImportName)) {
				hasImport = true;
			}
		}
		if (!hasImport) {
			final ImportDeclaration newImportDeclaration = node.getAST().newImportDeclaration();
			newImportDeclaration.setName(node.getAST().newName(newImportName));
			compilationUnit.imports().add(newImportDeclaration);
		}
	}

	/**
	 * @return Enumeration DOM instance
	 */
	public CompilationUnit getEnumDomCompilationUnit() {
		return this.enumDomCompilationUnit;
	}

	/**
	 * 
	 * @return Resource Bundle's properties.
	 */
	public Properties getProperties() {
		return this.properties;
	}

	/**
	 * @return Resource Bundle's properties files.
	 */
	public IFile getPropertiesFile() {
		return this.propertiesFile;
	}

}
