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
package com.worldline.awltech.i18ntools.editor.ui;

import java.util.Locale;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;

import com.worldline.awltech.i18ntools.editor.data.model.I18NEntry;
import com.worldline.awltech.i18ntools.editor.data.model.I18NMessage;

/**
 * Editing support for localized messages column cells
 * 
 * @author mvanbesien
 * 
 */
public class EditorLocalizedMessageEditingSupport extends EditingSupport {

	private final TableViewer viewer;

	private final CellEditor cellEditor;

	private Locale locale;

	public EditorLocalizedMessageEditingSupport(TableViewer viewer) {
		super(viewer);
		this.viewer = viewer;
		this.cellEditor = new TextCellEditor(viewer.getTable());
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
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
		if (element instanceof I18NEntry && this.locale != null) {
			I18NMessage i18nMessage = ((I18NEntry) element).getLocalizedMessages().get(this.locale);
			return i18nMessage != null ? i18nMessage.getValue() : null;
		}

		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof I18NEntry && this.locale != null) {
			I18NEntry i18nMessage = (I18NEntry) element;
			i18nMessage.updateLocalizedMessage(this.locale, String.valueOf(value));
			this.viewer.update(element, null);
		}
	}

	@Override
	protected void initializeCellEditorValue(CellEditor cellEditor, ViewerCell cell) {
		Object value = getValue(cell.getElement());
		cellEditor.setValue(value != null ? value : "");
	}

}
