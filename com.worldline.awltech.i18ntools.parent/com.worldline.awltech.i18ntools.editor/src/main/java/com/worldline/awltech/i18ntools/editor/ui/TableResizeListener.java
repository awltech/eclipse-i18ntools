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
