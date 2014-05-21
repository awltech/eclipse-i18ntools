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

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

import com.worldline.awltech.i18ntools.editor.data.model.I18NResourceBundle.I18NResourceBundleBuilder;

/**
 * Implementation that, from an Enumeration based resource bundle, locates all
 * the Resource Bundle files and loads them into specific structure.
 * 
 * @author mvanbesien
 * 
 */
public class I18NDataLoader {

	private final ICompilationUnit compilationUnit;

	public I18NDataLoader(final ICompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	public I18NResourceBundle load() throws CoreException {
		if (!isResourceBundleEnumeration(compilationUnit))
			return null;

		I18NEnumVisitor visitor = I18NEnumVisitor.create(compilationUnit);

		if (visitor.getResourceBundleName() != null) {
			String resourceBundlePath = visitor.getResourceBundleName();
			Map<Locale, IFile> locales = locateResourceBundle(compilationUnit.getJavaProject(), resourceBundlePath);
			I18NResourceBundle i18nResourceBundle = new I18NResourceBundleBuilder().withEnumeration(compilationUnit)
					.withLiterals(visitor.getLiterals()).withLocales(locales).withName(resourceBundlePath).create();
			return i18nResourceBundle;
		}

		return null;
	}

	private static Map<Locale, IFile> locateResourceBundle(IJavaProject javaProject, String resourceBundlePath)
			throws CoreException {
		Map<Locale, IFile> resourceBundles = new LinkedHashMap<>();
		for (IPackageFragmentRoot pfr : javaProject.getPackageFragmentRoots()) {
			if (pfr.getResource() instanceof IContainer) {
				IContainer container = (IContainer) pfr.getResource();
				Path path = new Path(resourceBundlePath);
				IResource foundMember = container.findMember(path.addFileExtension("properties"));
				if (foundMember instanceof IFile && foundMember.exists()) {
					// We have the default resource bundle !
					resourceBundles.put(null, (IFile) foundMember);
					// Now we need to find the other languages...
					for (IResource resource : container.members()) {
						// We don't forget to skip the already found default
						// one.
						String fileNameBase = path.lastSegment().concat("_");
						if (resource != foundMember) {
							if (resource instanceof IFile && resource.exists()) {
								// The aim here is to chunk the name of the file
								// to retrieve the locale and load it.
								IFile iFile = (IFile) resource;
								if ("properties".equals(iFile.getFileExtension())
										&& iFile.getName().startsWith(fileNameBase)) {
									String localeChunk = iFile.getName().substring(fileNameBase.length(),
											iFile.getName().length() - ".properties".length());
									Locale locale = Locale.forLanguageTag(localeChunk.replace("_", "-"));
									if (locale != null) {
										resourceBundles.put(locale, iFile);
									}
								}
							}
						}
					}
				}
			}
		}
		return resourceBundles;
	}

	private static boolean isResourceBundleEnumeration(ICompilationUnit javaElement) throws CoreException {
		if (javaElement.getAllTypes().length > 0) {
			for (IType type : javaElement.getAllTypes()) {
				if (type.isEnum()) {
					for (IField field : type.getFields()) {
						String elementType = Signature.getSignatureSimpleName(field.getTypeSignature());
						if ("ResourceBundle".equals(elementType) || "java.util.ResourceBundle".equals(elementType)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

}
