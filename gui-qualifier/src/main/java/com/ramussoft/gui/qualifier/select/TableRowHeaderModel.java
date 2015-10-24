package com.ramussoft.gui.qualifier.select;

import javax.swing.AbstractListModel;

import org.jdesktop.swingx.JXTreeTable;

public class TableRowHeaderModel extends AbstractListModel {
    /**
     *
     */
    private static final long serialVersionUID = -4004771244061811844L;
    private final JXTreeTable table;

    public TableRowHeaderModel(JXTreeTable table) {
        this.table = table;
    }

    public int getSize() {
        return table.getRowCount();
    }

    public Object getElementAt(final int index) {
        return table.getPathForRow(index).getLastPathComponent();
    }
}
