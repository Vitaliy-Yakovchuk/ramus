package com.ramussoft.pb.frames.components;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import com.ramussoft.localefix.DecimalFormatWithFix;

public class DoubleCellEditor extends DefaultCellEditor {

    private Object value;

    public static final DecimalFormat format = new DecimalFormatWithFix();

    public DoubleCellEditor() {
        super(new JTextField());
        getComponent().setName("Table.editor");
        ((JTextField) getComponent()).setHorizontalAlignment(SwingConstants.RIGHT);
    }

    @Override
    public boolean stopCellEditing() {
        final String s = (String) super.getCellEditorValue();
        if ("".equals(s)) {
            return super.stopCellEditing();
        }

        try {
            value = format.parse(s);
            if (value instanceof Long)
                value = new Double(value.toString());
            else if (value instanceof Integer)
                value = new Double(value.toString());
        } catch (final Exception e) {
            ((JComponent) getComponent()).setBorder(new LineBorder(Color.red));
            return false;
        }
        return super.stopCellEditing();
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, Object value,
                                                 final boolean isSelected, final int row, final int column) {
        this.value = null;
        ((JComponent) getComponent()).setBorder(new LineBorder(Color.black));
        if (value != null)
            value = format.format(value);
        return super.getTableCellEditorComponent(table, value, isSelected, row,
                column);
    }

    @Override
    public Object getCellEditorValue() {
        return value;
    }
}
