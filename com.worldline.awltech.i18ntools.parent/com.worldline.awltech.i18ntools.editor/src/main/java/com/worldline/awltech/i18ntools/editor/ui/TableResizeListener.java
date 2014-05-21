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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Table listener that resizes proportionally the columns when the table is
 * resized
 * 
 * @author mvanbesien
 * 
 */
public class TableResizeListener extends ControlAdapter {

	private Map<TableColumn, Integer> percentages = new HashMap<TableColumn, Integer>();

	public void register(TableColumn column, Integer percentage) {
		this.percentages.put(column, percentage);
	}

	@Override
	public void controlResized(ControlEvent e) {
		Table table = (Table) e.getSource();
		int tableWidth = table.getBounds().width;

		for (TableColumn column : this.percentages.keySet()) {
			column.setWidth((tableWidth - 20) * this.percentages.get(column) / 100);
		}

		table.redraw();
	}
}
