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

	public ResourceBundleWrapper(final IJavaProject javaProject, final String packageName,
			final String resourceBundleName) {
		this.javaProject = javaProject;
		this.packageName = packageName;
		this.resourceBundleName = resourceBundleName;
		this.configuration = new RefactoringWizardConfiguration(javaProject.getProject());
	}

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
			try {
				this.enumJavaCompilationUnit = ipf.createCompilationUnit(javaUnitName, contents, false,
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

		this.addImportToCompilationUnitIfMissing(nodeFinder.getParentNode(), this.packageName + "."
				+ this.resourceBundleName);

		if (locationInParent.isChildListProperty()) {
			final List<Object> list = (List<Object>) parent.getStructuralProperty(locationInParent);
			if (nodeFinder != null) {
				final int index = list.indexOf(nodeFinder.getFoundNode());
				list.remove(nodeFinder.getFoundNode());
				list.add(index, replacement);
			}
		} else {
			parent.setStructuralProperty(locationInParent, replacement);
		}

		for (final Expression parameter : resolver.getMessageParameters()) {
			final Expression newParameter = ASTTreeCloner.clone(parameter);
			replacement.arguments().add(newParameter);
		}

		this.properties.put(literalName, resolver.getMessagePattern());
		return true;
	}

	@SuppressWarnings("unchecked")
	private void addImportToCompilationUnitIfMissing(final ASTNode node, final String newImportName) {
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

	public CompilationUnit getEnumDomCompilationUnit() {
		return this.enumDomCompilationUnit;
	}

	public Properties getProperties() {
		return this.properties;
	}

	public IFile getPropertiesFile() {
		return this.propertiesFile;
	}

}
