package com.ramussoft.gui.attribute;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

import com.ramussoft.gui.common.GlobalResourcesManager;

public class BooleanEditor extends DefaultCellEditor {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private JComboBox box;

    public BooleanEditor() {
        super(new JComboBox());
        box = (JComboBox) getComponent();
        box.addItem("");
        box.addItem(GlobalResourcesManager.getString("Option.Yes"));
        box.addItem(GlobalResourcesManager.getString("Option.No"));
    }

    @Override
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value, boolean isSelected, int row, int column) {
        if (value == null)
            box.setSelectedIndex(0);
        else if ((Boolean) value)
            box.setSelectedIndex(1);
        else
            box.setSelectedIndex(2);
        return box;
    }

    @Override
    public Object getCellEditorValue() {
        if (box.getSelectedIndex() == 1)
            return Boolean.TRUE;
        if (box.getSelectedIndex() == 2)
            return Boolean.FALSE;
        return null;
    }

}