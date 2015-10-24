package com.ramussoft.gui.attribute;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.ramussoft.gui.common.GlobalResourcesManager;

public class BooleanRenderer extends DefaultTableCellRenderer {

    /**
     *
     */
    private static final long serialVersionUID = -495813106381546760L;

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value, boolean isSelected, boolean hasFocus, int row,
                                                   int column) {
        super.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, column);
        if (value == null)
            this.setText(null);
        else if ((Boolean) value)
            this.setText(GlobalResourcesManager.getString("Option.Yes"));
        else
            this.setText(GlobalResourcesManager.getString("Option.No"));

        return this;
    }
}