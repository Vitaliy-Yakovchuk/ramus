package com.ramussoft.gui.attribute;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.localefix.DecimalFormatWithFix;

public class DoubleAttributePlugin extends AbstractAttributePlugin {

    private static final DecimalFormat format = new DecimalFormatWithFix();

    private DoubleCellRendererWithFix cellRenderer = new DoubleCellRendererWithFix();

    public static class DoubleCellEditor extends DefaultCellEditor {

        /**
         *
         */
        private static final long serialVersionUID = 1864288476820280002L;

        private Object value;

        public DoubleCellEditor() {
            super(new JTextField());
            getComponent().setName("Table.editor");
            ((JTextField) getComponent())
                    .setHorizontalAlignment(SwingConstants.RIGHT);
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
                ((JComponent) getComponent()).setBorder(new LineBorder(
                        Color.red));
                return false;
            }
            return super.stopCellEditing();
        }

        @Override
        public Component getTableCellEditorComponent(final JTable table,
                                                     Object value, final boolean isSelected, final int row,
                                                     final int column) {
            this.value = null;
            ((JComponent) getComponent())
                    .setBorder(new LineBorder(Color.black));
            if (value != null) {
                try {
                    value = format.format(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Component tableCellEditorComponent = super.getTableCellEditorComponent(table, value, isSelected, row, column);
            if (tableCellEditorComponent instanceof JTextField) {
                ((JTextField) tableCellEditorComponent).selectAll();
            }
            return tableCellEditorComponent;
        }

        @Override
        public Object getCellEditorValue() {
            return value;
        }
    }

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("Core", "Double", true);
    }

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
        return new DoubleCellEditor();
    }

    @Override
    public TableCellRenderer getTableCellRenderer(Engine engine,
                                                  AccessRules rules, Attribute attribute) {
        return cellRenderer;
    }

    @Override
    public String getName() {
        return "Core";
    }

    /**
     * @return the format
     */
    protected NumberFormat getFormat() {
        return format;
    }

}
