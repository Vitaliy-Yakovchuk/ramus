package com.ramussoft.gui.common;

import java.awt.Component;
import java.text.MessageFormat;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Engine;

public class EmptyPlugin extends AbstractAttributePlugin {

    private AttributeType type;

    public EmptyPlugin(AttributeType type) {
        this.type = type;
    }

    @Override
    public AttributeType getAttributeType() {
        return type;
    }

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
        return new DefaultCellEditor(new JTextField()) {

            private Object value;

            /**
             *
             */
            private static final long serialVersionUID = 1989763011855212332L;

            @Override
            public Object getCellEditorValue() {
                return value;
            }

            @Override
            public Component getTableCellEditorComponent(JTable table,
                                                         Object value, boolean isSelected, int row, int column) {
                this.value = value;
                return super.getTableCellEditorComponent(table, value,
                        isSelected, row, column);
            }
        };
    }

    @Override
    public TableCellRenderer getTableCellRenderer(Engine engine,
                                                  AccessRules rules, Attribute attribute) {
        return new DefaultTableCellRenderer() {
            /**
             *
             */
            private static final long serialVersionUID = 6973511367750673696L;

            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value, boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                return super.getTableCellRendererComponent(table,
                        MessageFormat
                                .format(GlobalResourcesManager
                                        .getString("EmptyPlugin.Name"), type
                                        .toString()), isSelected, hasFocus,
                        row, column);
            }
        };
    }

    @Override
    public boolean isCellEditable() {
        return false;
    }

    @Override
    public String getName() {
        return "Empty";
    }

    @Override
    public String getString(String key) {
        return "";
    }

    @Override
    public Attribute createSyncAttribute(Engine engine,
                                         QualifierImporter importer, Attribute sourceAttribute) {
        return null;
    }
}
