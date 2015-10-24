package com.ramussoft.idef0.attribute;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.AbstractAttributeEditor;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.qualifier.table.DialogedTableCellEditor;

public class DFDSNameAttributePlugin extends AbstractAttributePlugin {

    @Override
    public TableCellEditor getTableCellEditor(final Engine engine,
                                              AccessRules rules, final Attribute attribute) {
        return new DialogedTableCellEditor(engine, rules, attribute, this,
                framework) {
            private DFDSName editorValue;

            {
                field.setEditable(true);
            }

            @Override
            public Component getTableCellEditorComponent(JTable table,
                                                         Object value, boolean isSelected, int row, int column) {
                this.editorValue = (DFDSName) value;
                return super.getTableCellEditorComponent(table, value,
                        isSelected, row, column);
            }

            @Override
            public Object getCellEditorValue() {
                if (element != null) {
                    editorValue = (DFDSName) engine.getAttribute(element,
                            attribute);
                }
                if (editorValue == null) {
                    DFDSName name = new DFDSName();
                    name.setShortName(field.getText());
                    return name;
                } else {
                    DFDSName name = new DFDSName();
                    name.setShortName(field.getText());
                    name.setLongName(editorValue.getLongName());
                    return name;
                }
            }

            @Override
            protected void onApply(Object value) {
                if (value == null || ((DFDSName) value).getShortName() == null)
                    field.setText("");
                else
                    field.setText(((DFDSName) value).getShortName());
            }
        };
    }

    @Override
    public AttributeEditor getAttributeEditor(final Engine engine,
                                              AccessRules rules, final Element element, Attribute attribute,
                                              String propertiesPrefix, AttributeEditor old) {
        if (old != null)
            old.close();
        return new AbstractAttributeEditor() {

            private DFDSNamePanel dfdsName = new DFDSNamePanel(engine, element);

            private Object value;

            @Override
            public Object setValue(Object value) {
                this.value = value;
                if (value != null)
                    dfdsName.setDFDSName((DFDSName) value);
                return value;
            }

            @Override
            public Object getValue() {
                DFDSName dfdsName2 = dfdsName.getDFDSName();
                if (value == null) {
                    if (dfdsName2.getLongName().equals("")
                            && dfdsName2.getShortName().equals(""))
                        return value;
                    else
                        return dfdsName2;
                } else {
                    if (value.equals(dfdsName2))
                        return value;
                }
                return dfdsName2;
            }

            @Override
            public JComponent getComponent() {
                return dfdsName;
            }

            @Override
            public JComponent getLastComponent() {
                return dfdsName.getTextArea();
            }
        };
    }

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("IDEF0", "DFDSName", true);
    }

    @Override
    public String getName() {
        return "IDEF0";
    }

    @Override
    public String getString(String key) {
        return ResourceLoader.getString(key);
    }

}
