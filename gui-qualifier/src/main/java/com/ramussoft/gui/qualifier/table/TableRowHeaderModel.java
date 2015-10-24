package com.ramussoft.gui.qualifier.table;

import javax.swing.AbstractListModel;

public class TableRowHeaderModel extends AbstractListModel {
    /**
     *
     */
    private static final long serialVersionUID = -4004771244061811844L;
    private final RowTreeTable table;

    public TableRowHeaderModel(RowTreeTable table) {
        this.table = table;
    }

    public int getSize() {
        return table.getRowCount();
    }

    public Object getElementAt(final int index) {
        TreeTableNode treeTableNode = (TreeTableNode) table
                .getPathForRow(index).getLastPathComponent();
        return treeTableNode;
    }
}
