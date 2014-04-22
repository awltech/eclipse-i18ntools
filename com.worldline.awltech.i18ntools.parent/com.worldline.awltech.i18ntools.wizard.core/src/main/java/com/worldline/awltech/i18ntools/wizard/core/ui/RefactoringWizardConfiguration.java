package com.worldline.awltech.i18ntools.wizard.core.ui;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.service.prefs.BackingStoreException;

import com.worldline.awltech.i18ntools.wizard.core.Activator;
import com.worldline.awltech.i18ntools.wizard.core.RefactoringWizardMessages;

/**
 * Refactoring graphical wizard Configuration.
 * 
 * Such configuration is stored locally in the .settings folder. Hence, it is
 * adapted to the current project's internationalization process and can be
 * shared to other developers through SCMs.
 * 
 * @author mvanbesien
 * 
 */
public class RefactoringWizardConfiguration {

	private static final String SRC_MAIN_RESOURCES = "src/main/resources";

	private static final String SRC = "src";

	private static final String SRC_MAIN_JAVA = "src/main/java";

	private static final String RESBUNDLE_NAME = "ResourceBundleName";

	private static final String LAST_LITERAL_NAME = "LastLiteralPrefix";

	private static final String RESBUNDLE_DEFAULT = "Messages";

	private static final String LAST_LITERAL_DEFAULT = "STUB";

	private static final String RESBUNDLE_PACK_NAME = "ResourceBundlePackage";

	private static final String RESBUNDLE_PACK_DEFAULT = "com.acme";

	private static final String JAVA_SOURCE_FOLDER_NAME = "JavaSourceFolder";

	private static final String RESOURCES_SOURCE_FOLDER_NAME = "ResourceSourceFolder";

	private final IEclipsePreferences preferences;

	private final IProject project;

	public RefactoringWizardConfiguration(final IProject project) {
		final IScopeContext projectScope = new ProjectScope(project);
		this.preferences = projectScope.getNode(Activator.PLUGIN_ID);
		this.project = project;
	}

	public String getResourceBundlePackage() {
		return this.preferences.get(RefactoringWizardConfiguration.RESBUNDLE_PACK_NAME,
				RefactoringWizardConfiguration.RESBUNDLE_PACK_DEFAULT);
	}

	public String getResourceBundleName() {
		return this.preferences.get(RefactoringWizardConfiguration.RESBUNDLE_NAME,
				RefactoringWizardConfiguration.RESBUNDLE_DEFAULT);
	}

	public String getLastLiteralPrefix() {
		return this.preferences.get(RefactoringWizardConfiguration.LAST_LITERAL_NAME,
				RefactoringWizardConfiguration.LAST_LITERAL_DEFAULT);
	}

	public String getJavaSourceFolder() {
		return this.preferences.get(RefactoringWizardConfiguration.JAVA_SOURCE_FOLDER_NAME, this.getSourceFolder(
				RefactoringWizardConfiguration.SRC_MAIN_JAVA, RefactoringWizardConfiguration.SRC, null));
	}

	public String getResourceSourceFolder() {
		return this.preferences.get(RefactoringWizardConfiguration.RESOURCES_SOURCE_FOLDER_NAME, this.getSourceFolder(
				RefactoringWizardConfiguration.SRC_MAIN_RESOURCES, RefactoringWizardConfiguration.SRC, null));
	}

	/**
	 * Returns the first source folder available in the list. The last one
	 * should be a default value that can be returned if none is found. If last
	 * one is null, the first source folder found in the project will be
	 * returned.
	 * 
	 * @param sourceFolders
	 * @return
	 */
	private String getSourceFolder(final String... sourceFolders) {

		// We look for all the source folders specified, except the last one.
		for (int i = 0; i < sourceFolders.length - 1; i++) {
			final IFolder folder = this.project.getFolder(new Path(sourceFolders[i]));
			if (folder.exists()) {
				final IJavaElement javaElement = JavaCore.create(folder);
				try {
					if (javaElement != null && javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT
							&& ((IPackageFragmentRoot) javaElement).getKind() == IPackageFragmentRoot.K_SOURCE) {
						return sourceFolders[i];
					}
				} catch (final JavaModelException e) {
					Activator
							.getDefault()
							.getLog()
							.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
									RefactoringWizardMessages.ERROR_SEEK_SOURCEFOLDER.value(), e));
				}
			}
		}

		if (sourceFolders.length > 0 && sourceFolders[sourceFolders.length - 1] == null) {
			final IJavaProject javaProject = JavaCore.create(this.project);
			try {
				final IPackageFragmentRoot[] allPackageFragmentRoots = javaProject.getAllPackageFragmentRoots();
				for (final IPackageFragmentRoot packageFragmentRoot : allPackageFragmentRoots) {
					if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
						return packageFragmentRoot.getPath().makeRelativeTo(this.project.getFullPath()).toString();
					}
				}
			} catch (final JavaModelException e) {
				Activator
						.getDefault()
						.getLog()
						.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								RefactoringWizardMessages.ERROR_SEEK_SOURCEFOLDER.value(), e));
			}

		}

		return null;
	}

	public void setResourceBundlePackage(final String name) {
		final String currentValue = this.getResourceBundlePackage();
		if (currentValue != null && currentValue.equals(name)) {
			return;
		}
		this.preferences.put(RefactoringWizardConfiguration.RESBUNDLE_PACK_NAME, name);
		try {
			this.preferences.flush();
		} catch (final BackingStoreException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
							RefactoringWizardMessages.WARNING_FLUSH_LOCALPREFS.value(), e));
		}
	}

	public void setResourceBundleName(final String name) {
		final String currentValue = this.getResourceBundleName();
		if (currentValue != null && currentValue.equals(name)) {
			return;
		}
		this.preferences.put(RefactoringWizardConfiguration.RESBUNDLE_NAME, name);
		try {
			this.preferences.flush();
		} catch (final BackingStoreException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
							RefactoringWizardMessages.WARNING_FLUSH_LOCALPREFS.value(), e));
		}
	}

	public void setLastLiteralPrefix(final String value) {
		final String currentValue = this.getLastLiteralPrefix();
		if (currentValue != null && currentValue.equals(value)) {
			return;
		}
		this.preferences.put(RefactoringWizardConfiguration.LAST_LITERAL_NAME, value);
		try {
			this.preferences.flush();
		} catch (final BackingStoreException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
							RefactoringWizardMessages.WARNING_FLUSH_LOCALPREFS.value(), e));
		}
	}

	public void setJavaSourceFolder(final String folder) {
		final String currentValue = this.getJavaSourceFolder();
		if (currentValue != null && currentValue.equals(folder)) {
			return;
		}
		this.preferences.put(RefactoringWizardConfiguration.JAVA_SOURCE_FOLDER_NAME, folder);
		try {
			this.preferences.flush();
		} catch (final BackingStoreException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
							RefactoringWizardMessages.WARNING_FLUSH_LOCALPREFS.value(), e));
		}
	}

	public void setResourceSourceFolder(final String folder) {
		final String currentValue = this.getResourceSourceFolder();
		if (currentValue != null && currentValue.equals(folder)) {
			return;
		}
		this.preferences.put(RefactoringWizardConfiguration.RESOURCES_SOURCE_FOLDER_NAME, folder);
		try {
			this.preferences.flush();
		} catch (final BackingStoreException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
							RefactoringWizardMessages.WARNING_FLUSH_LOCALPREFS.value(), e));
		}
	}
}
