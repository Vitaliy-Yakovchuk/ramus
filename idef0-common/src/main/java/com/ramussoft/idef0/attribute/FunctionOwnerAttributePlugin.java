package com.ramussoft.idef0.attribute;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.AbstractAttributeEditor;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.idef.frames.SelectOwner;

public class FunctionOwnerAttributePlugin extends AbstractAttributePlugin {

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("IDEF0", "OunerId");
    }

    @Override
    public TableCellEditor getTableCellEditor(final Engine engine,
                                              AccessRules rules, Attribute attribute) {
        return null;
    }

    @Override
    public TableCellRenderer getTableCellRenderer(final Engine engine,
                                                  AccessRules rules, Attribute attribute) {
        return new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value == null)
                    super.setValue(value);
                else
                    super.setValue(engine.getElement((Long) value));
            }
        };
    }

    @Override
    public AttributeEditor getAttributeEditor(final Engine engine,
                                              final AccessRules rules, final Element element,
                                              Attribute attribute, AttributeEditor old) {
        if (old != null)
            old.close();
        return new AbstractAttributeEditor() {

            private SelectOwner component = new SelectOwner();

            @Override
            public Object setValue(Object value) {
                DataPlugin plugin = NDataPluginFactory.getDataPlugin(engine
                        .getQualifier(element.getQualifierId()), engine, rules);
                component.setFunction((Function) plugin
                        .findRowByGlobalId(element.getId()));
                return value;
            }

            @Override
            public Object getValue() {
                Row row = component.getOwner();
                if (row != null)
                    return row.getElement().getId();
                return null;
            }

            @Override
            public JComponent getComponent() {
                return component;
            }
        };
    }

    @Override
    public String getName() {
        return "IDEF0";
    }

}
