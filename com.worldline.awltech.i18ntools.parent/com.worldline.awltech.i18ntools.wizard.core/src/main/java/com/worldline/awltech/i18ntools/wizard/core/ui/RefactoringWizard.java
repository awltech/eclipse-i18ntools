package com.worldline.awltech.i18ntools.wizard.core.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.worldline.awltech.i18ntools.wizard.core.Activator;
import com.worldline.awltech.i18ntools.wizard.core.RefactoringWizardMessages;
import com.worldline.awltech.i18ntools.wizard.core.ui.validation.FieldTextValidator;
import com.worldline.awltech.i18ntools.wizard.core.ui.validation.PackageTextValidator;
import com.worldline.awltech.i18ntools.wizard.core.ui.validation.SWTDialogValidator;
import com.worldline.awltech.i18ntools.wizard.core.ui.validation.TypeTextValidator;

/**
 * Refactoring graphical wizard implementation.
 * 
 * @author mvanbesien
 * 
 */
public class RefactoringWizard {

	private static final String IMAGE_PATH = "icons/export_wiz.gif";

	private static final int LABEL_WIDTH = 150;

	private static final int TEXT_WIDTH = 400;

	private Shell shell;

	private boolean keepOpen = true;

	private boolean isOK = false;

	private String literalPrefix = null;

	private String resourceBundleName = null;

	private String packageName = null;

	private final IJavaProject javaProject;

	private Display display;

	private String javaSourceFolder;

	private String resourceFolder;

	public boolean open() {
		this.display = Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
		this.display.syncExec(new Runnable() {
			@Override
			public void run() {
				RefactoringWizard.this.internalOpen();
			}
		});
		return this.isOK;
	}

	private void internalOpen() {
		final RefactoringWizardConfiguration configuration = new RefactoringWizardConfiguration(
				this.javaProject.getProject());
		// Create the background of the dialog

		this.shell = new Shell(this.display, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE);
		this.shell.setLayout(new FormLayout());
		this.shell.setText(RefactoringWizardMessages.WIZARD_TITLE.value());
		this.shell.setImage(Activator.getDefault().getImage(RefactoringWizard.IMAGE_PATH));

		// Message group
		final Group messageGroup = new Group(this.shell, SWT.NONE);
		messageGroup.setText(RefactoringWizardMessages.GROUP_MESSAGE_TITLE.value());
		messageGroup.setLayout(new FormLayout());
		FormDataBuilder.on(messageGroup).top().horizontal();

		final Label resourceBundlePackageLabel = new Label(messageGroup, SWT.NONE);
		resourceBundlePackageLabel.setText(RefactoringWizardMessages.WIZARD_LABEL_ENUM_PACKAGE.value());
		resourceBundlePackageLabel.setToolTipText(RefactoringWizardMessages.WIZARD_TOOLTIP_ENUM_PACKAGE.value());
		FormDataBuilder.on(resourceBundlePackageLabel).top().left().width(RefactoringWizard.LABEL_WIDTH);

		final Button resourceBundlePackageButton = new Button(messageGroup, SWT.PUSH);
		resourceBundlePackageButton.setText(RefactoringWizardMessages.WIZARD_BUTTON_SELECT.value());
		FormDataBuilder.on(resourceBundlePackageButton).top().right().width(80).height(25);

		final Text resourceBundlePackageText = new Text(messageGroup, SWT.BORDER);
		resourceBundlePackageText.setText(configuration.getResourceBundlePackage());
		FormDataBuilder.on(resourceBundlePackageText).top().right(resourceBundlePackageButton)
				.left(resourceBundlePackageLabel);

		resourceBundlePackageButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final IPackageFragment packageFragment = this.openAndGetPackage();
				if (packageFragment != null) {
					resourceBundlePackageText.setText(packageFragment.getElementName());
					resourceBundlePackageText.update();
				}
			}

			private IPackageFragment openAndGetPackage() {
				try {
					final SelectionDialog createPackageDialog = JavaUI.createPackageDialog(
							RefactoringWizard.this.shell, RefactoringWizard.this.javaProject,
							IJavaElementSearchConstants.CONSIDER_REQUIRED_PROJECTS);
					if (createPackageDialog.open() == Window.OK) {
						final Object[] result = createPackageDialog.getResult();
						if (result.length > 0 && result[0] instanceof IPackageFragment) {
							return (IPackageFragment) createPackageDialog.getResult()[0];
						}
					}
				} catch (final JavaModelException e) {
					Activator
							.getDefault()
							.getLog()
							.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
									RefactoringWizardMessages.ERROR_SEEK_PACKAGE.value(), e));
				}
				return null;
			}
		});

		final Label resourceBundleLabel = new Label(messageGroup, SWT.NONE);
		resourceBundleLabel.setText(RefactoringWizardMessages.WIZARD_LABEL_ENUM_NAME.value());
		resourceBundleLabel.setToolTipText(RefactoringWizardMessages.WIZARD_TOOLTIP_ENUM_NAME.value());
		FormDataBuilder.on(resourceBundleLabel).top(resourceBundlePackageText).left()
				.width(RefactoringWizard.LABEL_WIDTH);

		final Text resourceBundleText = new Text(messageGroup, SWT.BORDER);
		resourceBundleText.setText(configuration.getResourceBundleName());
		FormDataBuilder.on(resourceBundleText).top(resourceBundlePackageText).right().left(resourceBundleLabel)
				.width(RefactoringWizard.TEXT_WIDTH);

		final Label enumPrefixLabel = new Label(messageGroup, SWT.NONE);
		enumPrefixLabel.setText(RefactoringWizardMessages.WIZARD_LABEL_ENUM_LITERAL.value());
		enumPrefixLabel.setToolTipText(RefactoringWizardMessages.WIZARD_TOOLTIP_ENUM_LITERAL.value());
		FormDataBuilder.on(enumPrefixLabel).top(resourceBundleText).left().width(RefactoringWizard.LABEL_WIDTH);

		final Text enumPrefixText = new Text(messageGroup, SWT.BORDER);
		enumPrefixText.setText(configuration.getLastLiteralPrefix());
		FormDataBuilder.on(enumPrefixText).top(resourceBundleText).left(enumPrefixLabel)
				.width(RefactoringWizard.TEXT_WIDTH).bottom();

		// Advanced Group
		final Group advancedGroup = new Group(this.shell, SWT.NONE);
		advancedGroup.setText(RefactoringWizardMessages.GROUP_ADVANCED_TITLE.value());
		advancedGroup.setLayout(new FormLayout());
		FormDataBuilder.on(advancedGroup).top(messageGroup).horizontal();

		final Label sourceFolderLabel = new Label(advancedGroup, SWT.NONE);
		sourceFolderLabel.setText(RefactoringWizardMessages.LABEL_SOURCE_FOLDER.value());
		sourceFolderLabel.setToolTipText(RefactoringWizardMessages.TOOLTIP_SOURCE_FOLDER.value());
		FormDataBuilder.on(sourceFolderLabel).left().top().width(RefactoringWizard.LABEL_WIDTH);

		final Text sourceFolderText = new Text(advancedGroup, SWT.BORDER);
		sourceFolderText.setText(configuration.getJavaSourceFolder());
		FormDataBuilder.on(sourceFolderText).top().left(sourceFolderLabel).right().width(RefactoringWizard.TEXT_WIDTH);

		final Label resourceFolderLabel = new Label(advancedGroup, SWT.NONE);
		resourceFolderLabel.setText(RefactoringWizardMessages.LABEL_RESOURCE_FOLDER.value());
		resourceFolderLabel.setToolTipText(RefactoringWizardMessages.TOOLTIP_RESOURCE_FOLDER.value());
		FormDataBuilder.on(resourceFolderLabel).left().top(sourceFolderText).width(RefactoringWizard.LABEL_WIDTH);

		final Text resourceFolderText = new Text(advancedGroup, SWT.BORDER);
		resourceFolderText.setText(configuration.getResourceSourceFolder());
		FormDataBuilder.on(resourceFolderText).left(resourceFolderLabel).top(sourceFolderText)
				.width(RefactoringWizard.TEXT_WIDTH).right().bottom();

		final Label errorMessage = new Label(this.shell, SWT.NONE);
		FormDataBuilder.on(errorMessage).top(advancedGroup).left().right();

		final Button okButton = new Button(this.shell, SWT.PUSH);
		okButton.setText(RefactoringWizardMessages.WIZARD_BUTTON_OK.value());
		FormDataBuilder.on(okButton).bottom().right().width(80).top(errorMessage).height(25);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {

				final String enumPrefixValue = enumPrefixText.getText().trim();
				RefactoringWizard.this.literalPrefix = "RESOURCE_BUNDLE".equals(enumPrefixValue) ? "RESOURCE__BUNDLE"
						: enumPrefixValue.replace(" ", "_");
				RefactoringWizard.this.resourceBundleName = resourceBundleText.getText().trim();
				RefactoringWizard.this.packageName = resourceBundlePackageText.getText().trim();
				RefactoringWizard.this.javaSourceFolder = sourceFolderText.getText().trim();
				RefactoringWizard.this.resourceFolder = resourceFolderText.getText().trim();

				configuration.setLastLiteralPrefix(RefactoringWizard.this.literalPrefix);
				configuration.setResourceBundleName(RefactoringWizard.this.resourceBundleName);
				configuration.setResourceBundlePackage(RefactoringWizard.this.packageName);
				configuration.setJavaSourceFolder(RefactoringWizard.this.javaSourceFolder);
				configuration.setResourceSourceFolder(RefactoringWizard.this.resourceFolder);

				RefactoringWizard.this.isOK = true;
				RefactoringWizard.this.keepOpen = false;
			}
		});

		final Button cancelButton = new Button(this.shell, SWT.PUSH);
		cancelButton.setText(RefactoringWizardMessages.WIZARD_BUTTON_CANCEL.value());
		FormDataBuilder.on(cancelButton).bottom().right(okButton).top(errorMessage).width(80).height(25);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				RefactoringWizard.this.keepOpen = false;
			}
		});

		final Label versionLabel = new Label(this.shell, SWT.NONE);
		versionLabel.setForeground(this.shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		versionLabel.setText(RefactoringWizardMessages.LABEL_VERSION.value(Activator.getDefault().getVersion()));
		FormDataBuilder.on(versionLabel).top(errorMessage).left().right(cancelButton);

		this.shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				RefactoringWizard.this.keepOpen = false;
			}
		});
		this.shell.addListener(SWT.Traverse, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				if (event.detail == SWT.TRAVERSE_ESCAPE) {
					event.doit = false;
					RefactoringWizard.this.keepOpen = false;
				} else if (event.detail == SWT.TRAVERSE_RETURN) {
					event.doit = false;

					RefactoringWizard.this.literalPrefix = enumPrefixText.getText().trim();
					RefactoringWizard.this.resourceBundleName = resourceBundleText.getText().trim();
					RefactoringWizard.this.packageName = resourceBundlePackageText.getText().trim();
					RefactoringWizard.this.javaSourceFolder = sourceFolderText.getText().trim();
					RefactoringWizard.this.resourceFolder = resourceFolderText.getText().trim();

					configuration.setLastLiteralPrefix(RefactoringWizard.this.literalPrefix);
					configuration.setResourceBundleName(RefactoringWizard.this.resourceBundleName);
					configuration.setResourceBundlePackage(RefactoringWizard.this.packageName);
					configuration.setJavaSourceFolder(RefactoringWizard.this.javaSourceFolder);
					configuration.setResourceSourceFolder(RefactoringWizard.this.resourceFolder);

					RefactoringWizard.this.isOK = true;
					RefactoringWizard.this.keepOpen = false;
				}
			}
		});

		new SWTDialogValidator(errorMessage, okButton).on(enumPrefixText, new FieldTextValidator())
				.on(resourceBundlePackageText, new PackageTextValidator())
				.on(resourceBundleText, new TypeTextValidator());

		enumPrefixText.setFocus();

		this.shell.pack();

		Rectangle bounds;
		try {
			bounds = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getBounds();
		} catch (final NullPointerException npe) {
			bounds = this.shell.getDisplay().getPrimaryMonitor().getBounds();
		}
		final Rectangle rect = this.shell.getBounds();
		this.shell
				.setLocation(bounds.x + (bounds.width - rect.width) / 2, bounds.y + (bounds.height - rect.height) / 2);
		this.shell.open();
		while (this.keepOpen && !this.shell.isDisposed()) {
			if (!this.shell.getDisplay().readAndDispatch()) {
				this.shell.getDisplay().sleep();
			}
		}
		this.shell.dispose();

	}

	public RefactoringWizard(final IJavaProject javaProject) {
		this.javaProject = javaProject;
	}

	public String getLiteralPrefix() {
		return this.literalPrefix;
	}

	public String getResourceBundleName() {
		return this.resourceBundleName;
	}

	public String getResourceBundlePackage() {
		return this.packageName;
	}
}
