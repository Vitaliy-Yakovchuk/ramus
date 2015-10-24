package com.ramussoft.chart.gui;

import javax.swing.table.TableCellEditor;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.table.TableNode;
import com.ramussoft.gui.qualifier.table.TabledAttributePlugin;
import com.ramussoft.gui.qualifier.table.ValueGetter;
import com.ramussoft.gui.qualifier.table.event.Closeable;

public class ChartLinkAttributePlugin extends AbstractAttributePlugin implements
        TabledAttributePlugin {

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("Chart", "Link");
    }

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
        return null;
    }

    @Override
    public AttributeEditor getAttributeEditor(Engine engine, AccessRules rules,
                                              Element element, Attribute attribute,
                                              AttributeEditor oldAttributeEditor) {
        if (oldAttributeEditor != null)
            oldAttributeEditor.close();
        return new ChartAttributeEditor(framework, (Long) engine.getAttribute(
                element, attribute));
    }

    @Override
    public String getName() {
        return "Chart";
    }

    @Override
    public String getString(String key) {
        return ChartResourceManager.getString(key);
    }

    @Override
    public ValueGetter getValueGetter(Attribute attribute, final Engine engine,
                                      GUIFramework framework, Closeable model) {
        return new ValueGetter() {

            @Override
            public Object getValue(TableNode node, int index) {
                Long long1 = (Long) node.getValueAt(index);
                if (long1 == null)
                    return null;
                return ChartResourceManager.getString("Chart");
            }
        };
    }

}
