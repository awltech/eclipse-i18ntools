package com.worldline.awltech.i18ntools.editor.ui;

import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import com.worldline.awltech.i18ntools.editor.Activator;
import com.worldline.awltech.i18ntools.editor.data.model.I18NDataLoader;
import com.worldline.awltech.i18ntools.editor.data.model.I18NResourceBundle;
import com.worldline.awltech.i18ntools.editor.ui.ResourceBundleEditorMessages;

/**
 * Editor
 * 
 * @author mvanbesien
 * 
 */
public class ResourceBundleEditor extends EditorPart {

	private Locale currentLocale = null;
	private I18NResourceBundle resourceBundle;
	private TableViewer tableViewer;
	private ComboViewer comboViewer;
	private EditorLocalizedMessageEditingSupport editorLocalizedMessageEditingSupport;
	private TableResizeListener tableResizeListener;

	@Override
	public void doSave(IProgressMonitor monitor) {
		resourceBundle.save();
		firePropertyChange(PROP_DIRTY);
	}

	@Override
	public void doSaveAs() {
		// Does nothing as not accepted
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {

		setSite(site);
		setInput(input);

		if (!(input instanceof FileEditorInput)) {
			throw new PartInitException(ResourceBundleEditorMessages.ERROR_INPUTNOTFILE.value());
		}
		FileEditorInput editorInput = (FileEditorInput) input;
		IFile file = editorInput.getFile();
		IJavaElement javaElement = JavaCore.create(file);
		if (!(javaElement instanceof ICompilationUnit)) {
			throw new PartInitException(ResourceBundleEditorMessages.ERROR_INPUTNOTCU.value());
		}

		ICompilationUnit compilationUnit = (ICompilationUnit) javaElement;
		try {
			this.resourceBundle = new I18NDataLoader(compilationUnit).load();
		} catch (CoreException e) {
			throw new PartInitException(ResourceBundleEditorMessages.ERROR_FAILTOPARSECODE.value(), e);
		}
		this.setPartName(this.resourceBundle.getName()+".i18n");
	}

	@Override
	public boolean isDirty() {
		return resourceBundle.isDirty();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite background = new Composite(parent, SWT.NONE);
		background.setLayout(new FormLayout());

		Group optionsGroup = new Group(background, SWT.NONE);
		optionsGroup.setText(ResourceBundleEditorMessages.LABEL_OPTIONSGROUP.value());
		optionsGroup.setLayout(new FormLayout());
		FormDataBuilder.on(optionsGroup).horizontal().bottom();
		Group tableGroup = new Group(background, SWT.NONE);

		tableGroup.setText(ResourceBundleEditorMessages.LABEL_TABLEGROUP.value());
		tableGroup.setLayout(new FormLayout());
		FormDataBuilder.on(tableGroup).horizontal().top().bottom(optionsGroup);

		Label localeSelectionLabel = new Label(optionsGroup, SWT.NONE);
		localeSelectionLabel.setText(ResourceBundleEditorMessages.LABEL_LOCALECOMBO.value());
		FormDataBuilder.on(localeSelectionLabel).left().top(0, 8).width(100);

		Button localeSelectionButton = new Button(optionsGroup, SWT.PUSH);
		localeSelectionButton.setText(ResourceBundleEditorMessages.LABEL_LOCALEBUTTON.value());
		FormDataBuilder.on(localeSelectionButton).right().width(120).height(25).top();

		Combo localSelectionCombo = new Combo(optionsGroup, SWT.READ_ONLY);
		FormDataBuilder.on(localSelectionCombo).left(localeSelectionLabel).top().right(localeSelectionButton);

		Label addLiteralLabel = new Label(optionsGroup, SWT.NONE);
		addLiteralLabel.setText(ResourceBundleEditorMessages.LABEL_NEWKEYLABEL.value());
		FormDataBuilder.on(addLiteralLabel).left().bottom().width(100).top(localSelectionCombo, 8);

		Button addLiteralButton = new Button(optionsGroup, SWT.PUSH);
		addLiteralButton.setText(ResourceBundleEditorMessages.LABEL_NEWKEYBUTTON.value());
		FormDataBuilder.on(addLiteralButton).right().width(120).height(25).bottom().top(localSelectionCombo);

		final Text addLiteralText = new Text(optionsGroup, SWT.BORDER);
		FormDataBuilder.on(addLiteralText).left(addLiteralLabel).right(addLiteralButton).bottom()
				.top(localSelectionCombo);

		Table table = new Table(tableGroup, SWT.BORDER);
		FormDataBuilder.on(table).fill();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn keyColumn = new TableColumn(table, SWT.NONE);
		keyColumn.setText(ResourceBundleEditorMessages.LABEL_KEYCOLUMN.value());

		TableColumn defaultColumn = new TableColumn(table, SWT.NONE);
		defaultColumn.setText(ResourceBundleEditorMessages.LABEL_DEFAULTCOLUMN.value());

		TableColumn localeColumn = new TableColumn(table, SWT.NONE);
		localeColumn.setText(ResourceBundleEditorMessages.LABEL_LOCALECOLUMN.value());

		this.tableResizeListener = new TableResizeListener();
		table.addControlListener(tableResizeListener);
		tableResizeListener.register(keyColumn, 20);
		tableResizeListener.register(defaultColumn, 40);
		tableResizeListener.register(localeColumn, 40);

		this.comboViewer = new ComboViewer(localSelectionCombo);
		this.comboViewer.setContentProvider(new EditorComboContentProvider());
		this.comboViewer.setLabelProvider(new EditorComboLabelProvider());
		this.comboViewer.setInput(this.resourceBundle);

		this.tableViewer = new TableViewer(table) {

			@Override
			public void update(Object element, String[] properties) {
				super.update(element, properties);
				firePropertyChange(PROP_DIRTY);
			}
		};

		TableViewerColumn defaultColumnViewer = new TableViewerColumn(tableViewer, defaultColumn);
		TableViewerColumn localizedColumnViewer = new TableViewerColumn(tableViewer, localeColumn);

		defaultColumnViewer.setEditingSupport(new EditorDefaultMessageEditingSupport(tableViewer));
		this.editorLocalizedMessageEditingSupport = new EditorLocalizedMessageEditingSupport(tableViewer);
		localizedColumnViewer.setEditingSupport(this.editorLocalizedMessageEditingSupport);

		this.comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				Object first = ((IStructuredSelection) selection).getFirstElement();
				if (first instanceof Locale) {
					currentLocale = (Locale) first;
					tableViewer.setLabelProvider(new EditorTableLabelProvider(currentLocale));
					editorLocalizedMessageEditingSupport.setLocale(currentLocale);
					tableViewer.setInput(resourceBundle);
				}

			}
		});

		this.tableViewer.setContentProvider(new EditorTableContentProvider());
		this.tableViewer.setLabelProvider(new EditorTableLabelProvider(null));
		this.tableViewer.setInput(this.resourceBundle);

		localeSelectionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(new Shell(),
						new EditorComboLabelProvider());
				dialog.setMessage(ResourceBundleEditorMessages.LOCALEDIALOG_MESSAGE.value());
				dialog.setTitle(ResourceBundleEditorMessages.LOCALEDIALOG_TITLE.value());
				dialog.setImage(Activator.getDefault().getImage("/icons/i18neditor.png"));
				dialog.setMultipleSelection(false);
				dialog.setElements(Locale.getAvailableLocales());
				if (dialog.open() == Window.OK && dialog.getResult().length > 0) {
					Locale newLocale = (Locale) dialog.getFirstResult();
					resourceBundle.addLocale(newLocale);
					comboViewer.setInput(resourceBundle);
					comboViewer.setSelection(new StructuredSelection(newLocale));
					ResourceBundleEditor.this.firePropertyChange(PROP_DIRTY);
				}
			}
		});

		addLiteralButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String value = addLiteralText.getText().trim();
				if (JavaConventions.validateFieldName(value, "1.7", "1.7").getSeverity() == IStatus.OK) {
					resourceBundle.addLiteral(value);
					tableViewer.setInput(resourceBundle);
					addLiteralText.setText("");
					addLiteralText.update();
					ResourceBundleEditor.this.firePropertyChange(PROP_DIRTY);
				}
			}
		});

	}

	@Override
	public void setFocus() {
		// Does nothing
	}

}
