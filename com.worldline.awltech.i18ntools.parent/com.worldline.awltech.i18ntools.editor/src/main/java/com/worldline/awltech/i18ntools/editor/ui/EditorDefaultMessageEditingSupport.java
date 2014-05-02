package com.worldline.awltech.i18ntools.editor.ui;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;

import com.worldline.awltech.i18ntools.editor.data.model.I18NEntry;
import com.worldline.awltech.i18ntools.editor.data.model.I18NMessage;

/**
 * Editing support for default message column cells
 * @author mvanbesien
 *
 */
public class EditorDefaultMessageEditingSupport extends EditingSupport {

	private final TableViewer viewer;

	private final CellEditor cellEditor;

	public EditorDefaultMessageEditingSupport(TableViewer viewer) {
		super(viewer);
		this.viewer = viewer;
		this.cellEditor = new TextCellEditor(viewer.getTable());
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return this.cellEditor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		if (element instanceof I18NEntry) {
			I18NMessage defaultMessage = ((I18NEntry) element).getDefaultMessage();
			return defaultMessage != null ? defaultMessage.getValue() : null;
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof I18NEntry) {
			I18NEntry i18nMessage = (I18NEntry) element;
			i18nMessage.updateDefaultMessage(String.valueOf(value));
			this.viewer.update(element, null);
		}
	}
	
	@Override
	protected void initializeCellEditorValue(CellEditor cellEditor, ViewerCell cell) {
		Object value = getValue(cell.getElement());
		cellEditor.setValue(value != null ? value : "");
	}

}
