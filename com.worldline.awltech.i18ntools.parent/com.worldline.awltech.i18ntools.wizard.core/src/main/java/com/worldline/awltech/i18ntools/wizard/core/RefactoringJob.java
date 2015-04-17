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
package com.worldline.awltech.i18ntools.wizard.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import com.worldline.awltech.i18ntools.wizard.core.modules.ASTExpressionResolver;
import com.worldline.awltech.i18ntools.wizard.core.modules.ASTNodeFinder;
import com.worldline.awltech.i18ntools.wizard.core.modules.ResourceBundleWrapper;
import com.worldline.awltech.i18ntools.wizard.core.ui.RefactoringWizard;

/**
 * 
 * Main process. This job triggers the refactoring steps.
 * 
 * @author mvanbesien
 * 
 */
public class RefactoringJob extends WorkspaceJob {

	/**
	 * Processed compilation unit
	 */
	private ICompilationUnit compilationUnit;

	/**
	 * Selected source offset in compilation unit
	 */
	private int offset;

	/**
	 * Selected source length in compilation unit.
	 */
	private int length;

	/**
	 * Name of the resource bundle.
	 */
	private String resourceBundleName;

	/**
	 * Name of the enumeration literal to use
	 */
	private String enumerationPrefix;

	/**
	 * Package containing the Enumeration bound to resource bundle.
	 */
	private String resourceBundlePackage;
	
	/**
	 * Tells whether the message should be prefixed by key
	 */
	private boolean prefixMessageByKey;

	/**
	 * Creates the new job instance.
	 */
	public RefactoringJob() {
		super(RefactoringWizardMessages.JOB_NAME.value());
		this.setUser(true);
		this.setPriority(Job.BUILD);
	}

	/**
	 * Sets the compilation unit information
	 * 
	 * @param compilationUnit
	 * @return
	 */
	public RefactoringJob setCompilationUnit(final ICompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		return this;
	}

	/**
	 * Sets the user selection information
	 * 
	 * @param offset
	 * @param length
	 * @return
	 */
	public RefactoringJob setSelection(final int offset, final int length) {
		this.offset = offset;
		this.length = length;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core
	 * .runtime.IProgressMonitor)
	 */
	@Override
	public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {

		// 1. Resolve AST element...
		final ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(this.compilationUnit);
		final CompilationUnit astCompilationUnit = (CompilationUnit) parser.createAST(new NullProgressMonitor());
		final ASTNodeFinder nodeFinder = ASTNodeFinder.create(astCompilationUnit, this.offset, this.length);

		// 2. Open GUI to let user put its information
		final IJavaProject javaProject = this.compilationUnit.getJavaProject();
		final RefactoringWizard i18NizeWizard = new RefactoringWizard(javaProject);
		if (!i18NizeWizard.open()) {
			return Status.CANCEL_STATUS;
		}

		astCompilationUnit.recordModifications();

		this.resourceBundleName = i18NizeWizard.getResourceBundleName();
		this.enumerationPrefix = i18NizeWizard.getLiteralPrefix();
		this.resourceBundlePackage = i18NizeWizard.getResourceBundlePackage();
		this.prefixMessageByKey = i18NizeWizard.getPrefixMessageByKey();

		// 3. Extract the pattern and arguments from expression

		final ASTExpressionResolver expressionResolver = ASTExpressionResolver.create(nodeFinder);
		final ResourceBundleWrapper resourceBundleWrapper = new ResourceBundleWrapper(javaProject,
				this.resourceBundlePackage, this.resourceBundleName, this.prefixMessageByKey);
		final boolean isModified = resourceBundleWrapper.replaceLiteral(nodeFinder, this.enumerationPrefix,
				expressionResolver);

		if (isModified) {
			RefactoringJob.updateCompilationUnit(astCompilationUnit);
			RefactoringJob.updateCompilationUnit(resourceBundleWrapper.getEnumDomCompilationUnit());
			RefactoringJob.saveCompilationUnit((ICompilationUnit) resourceBundleWrapper.getEnumDomCompilationUnit()
					.getJavaElement());
			RefactoringJob.saveProperties(resourceBundleWrapper.getPropertiesFile(),
					resourceBundleWrapper.getProperties());

			// 4. We refresh the stuff
			javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		}
		return Status.OK_STATUS;
	}

	/**
	 * Saves the source code modification in compilation unit.
	 * 
	 * @param domCompilationUnit
	 */
	private static void updateCompilationUnit(final CompilationUnit domCompilationUnit) {
		try {
			final ICompilationUnit icu = (ICompilationUnit) domCompilationUnit.getJavaElement();
			final IDocument document = new Document(icu.getSource());
			final TextEdit rewrite = domCompilationUnit.rewrite(document, null);
			rewrite.apply(document);
			final String newSource = document.get();
			icu.getBuffer().setContents(newSource);
		} catch (final MalformedTreeException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, RefactoringWizardMessages.ERROR_UPDATE_CU
							.value(), e));
		} catch (final BadLocationException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, RefactoringWizardMessages.ERROR_UPDATE_CU
							.value(), e));
		} catch (final JavaModelException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, RefactoringWizardMessages.ERROR_UPDATE_CU
							.value(), e));
		}
	}

	/**
	 * Saves hhe compilation unit on FileSystem.
	 * 
	 * @param icu
	 */
	private static void saveCompilationUnit(final ICompilationUnit icu) {
		try {
			icu.save(null, true);
		} catch (final JavaModelException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, RefactoringWizardMessages.ERROR_SAVING_CU
							.value(), e));
		}
	}

	/**
	 * Dumps in memory properties in property file
	 * 
	 * @param propertiesFile
	 * @param properties
	 */
	private static void saveProperties(final IFile propertiesFile, final Properties properties) {
		FileOutputStream fileOutputStream = null;
		try {
			final File file = new File(propertiesFile.getLocation().toString());
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			file.createNewFile();
			fileOutputStream = new FileOutputStream(file);
			properties.store(fileOutputStream, null);
		} catch (final FileNotFoundException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							RefactoringWizardMessages.ERROR_UPDATE_PROPERTIES.value(), e));
		} catch (final IOException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							RefactoringWizardMessages.ERROR_UPDATE_PROPERTIES.value(), e));
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					Activator
							.getDefault()
							.getLog()
							.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
									RefactoringWizardMessages.ERROR_UPDATE_PROPERTIES.value(), e));
				}
			}
		}
	}

}
