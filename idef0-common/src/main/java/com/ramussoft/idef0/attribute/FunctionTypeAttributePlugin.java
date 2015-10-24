package com.ramussoft.idef0.attribute;

import static com.ramussoft.pb.Function.TYPE_ACTION;
import static com.ramussoft.pb.Function.TYPE_OPERATION;
import static com.ramussoft.pb.Function.TYPE_PROCESS;
import static com.ramussoft.pb.Function.TYPE_PROCESS_KOMPLEX;
import static com.ramussoft.pb.Function.TYPE_PROCESS_PART;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.AbstractAttributeEditor;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.gui.common.AttributeEditor;

public class FunctionTypeAttributePlugin extends AbstractAttributePlugin {

    private DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
        protected void setValue(Object value) {
            if (value == null) {
                super.setValue(null);
                return;
            }
            switch ((Integer) value) {
                case TYPE_ACTION:
                    super.setValue(ResourceLoader.getString("action"));
                    break;
                case TYPE_OPERATION:
                    super.setValue(ResourceLoader.getString("operation"));
                    break;
                case TYPE_PROCESS_PART:
                    super.setValue(ResourceLoader.getString("process_part"));
                    break;
                case TYPE_PROCESS:
                    super.setValue(ResourceLoader.getString("process"));
                    break;
                case TYPE_PROCESS_KOMPLEX:
                    super.setValue(ResourceLoader.getString("process_komplex"));
                    break;
            }
        }

        ;
    };

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("IDEF0", "Type");
    }

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
        final JComboBox box = new JComboBox();
        box.addItem(new FunctionType(ResourceLoader.getString("action"),
                TYPE_ACTION));
        box.addItem(new FunctionType(ResourceLoader.getString("operation"),
                TYPE_OPERATION));
        box.addItem(new FunctionType(ResourceLoader.getString("process_part"),
                TYPE_PROCESS_PART));
        box.addItem(new FunctionType(ResourceLoader.getString("process"),
                TYPE_PROCESS));
        box.addItem(new FunctionType(ResourceLoader
                .getString("process_komplex"), TYPE_PROCESS_KOMPLEX));
        DefaultCellEditor editor = new DefaultCellEditor(box) {
            @Override
            public Object getCellEditorValue() {
                FunctionType ft = (FunctionType) super.getCellEditorValue();
                return ft.type;
            }

            @Override
            public Component getTableCellEditorComponent(JTable table,
                                                         Object aValue, boolean isSelected, int row, int column) {
                int type = (Integer) aValue;
                Object value = null;
                for (int i = 0; i < box.getItemCount(); i++) {
                    FunctionType ft = (FunctionType) box.getItemAt(i);
                    if (ft.type == type) {
                        value = ft;
                        break;
                    }
                }
                return super.getTableCellEditorComponent(table, value,
                        isSelected, row, column);
            }
        };
        return editor;
    }

    @Override
    public AttributeEditor getAttributeEditor(Engine engine, AccessRules rules,
                                              Element element, Attribute attribute, AttributeEditor old) {
        if (old != null)
            old.close();
        return new AbstractAttributeEditor() {

            private com.ramussoft.pb.frames.components.FunctionType component = new com.ramussoft.pb.frames.components.FunctionType();

            @Override
            public Object setValue(Object value) {
                component.setType((value == null) ? -1 : (Integer) value);
                return value;
            }

            @Override
            public Object getValue() {
                int type = component.getType();
                if (type < 0)
                    return null;
                return type;
            }

            @Override
            public JComponent getComponent() {
                ResourceLoader.setJComponentsText(component);
                return component;
            }
        };
    }

    @Override
    public TableCellRenderer getTableCellRenderer(Engine engine,
                                                  AccessRules rules, Attribute attribute) {
        return renderer;
    }

    @Override
    public String getName() {
        return "IDEF0";
    }

    private class FunctionType {
        String name;
        int type;

        public FunctionType(String name, int type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String toString() {
            return name;
        }

    }

}
