package com.ramussoft.pb.frames.components;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class IntegerEditor extends AbstractCellEditor implements
        TableCellEditor {

    private class IntEditor extends JSpinField {
    }

    ;

    private final IntEditor spinField = new IntEditor();

    public Component getTableCellEditorComponent(final JTable table, final Object value,
                                                 final boolean isSelected, final int row, final int column) {
        spinField.setValue(((Integer) value).intValue());
        return spinField;
    }

    public Object getCellEditorValue() {
        return spinField.getValue();
    }

    public void setMinimum(final int minimum) {
        spinField.setMinimum(minimum);
    }

    public void setMaxumum(final int maximum) {
        spinField.setMaximum(maximum);
    }
}
