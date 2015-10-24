package com.ramussoft.gui.attribute.table;

import javax.swing.AbstractListModel;
import javax.swing.JTable;

public class TableHeaderModel extends AbstractListModel {

    /**
     *
     */
    private static final long serialVersionUID = -1915729314769027298L;

    private JTable table;

    public TableHeaderModel(JTable table) {
        this.table = table;
    }

    @Override
    public Object getElementAt(int index) {
        return index;
    }

    @Override
    public int getSize() {
        return table.getRowCount();
    }

}
