package com.ramussoft.gui.attribute;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Metadata;
import com.ramussoft.gui.common.AbstractAttributePlugin;

public class DateAttributePlugin extends AbstractAttributePlugin {

    public static final DateFormat DATE_INSTANCE;

    static {
        if (Metadata.CORPORATE) {
            DATE_INSTANCE = new SimpleDateFormat("dd.MM.yyyy");
        } else {
            DATE_INSTANCE = DateFormat.getDateInstance(DateFormat.MEDIUM);
        }
    }

    private DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer() {

        /**
         *
         */
        private static final long serialVersionUID = -2741786218125253416L;

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            Component component = super.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus, row, column);
            if (value != null) {
                if (component instanceof JLabel)
                    ((JLabel) component).setText(DATE_INSTANCE.format(value));
                else {
                    TableCellRenderer renderer = table
                            .getDefaultRenderer(Date.class);
                    return renderer.getTableCellRendererComponent(table, value,
                            isSelected, hasFocus, row, column);
                }
            }
            return component;
        }

    };

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("Core", "Date", true);
    }

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
        return new DateChooserCellEditor();
    }

    @Override
    public TableCellRenderer getTableCellRenderer(Engine engine,
                                                  AccessRules rules, Attribute attribute) {
        return defaultTableCellRenderer;
    }

    @Override
    public String getName() {
        return "Core";
    }

}
