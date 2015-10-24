package com.ramussoft.gui.attribute;

import javax.swing.table.TableCellEditor;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.qualifier.table.TableNode;
import com.ramussoft.gui.qualifier.table.TabledAttributePlugin;
import com.ramussoft.gui.qualifier.table.ValueGetter;
import com.ramussoft.gui.qualifier.table.event.Closeable;

public class HTMLTextAttributePlugin extends AbstractAttributePlugin implements
        TabledAttributePlugin {

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("Core", "HTMLText", false, false, true);
    }

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
        return null;
    }

    @Override
    public AttributeEditor getAttributeEditor(Engine engine, AccessRules rules,
                                              Element element, Attribute attribute, AttributeEditor editor) {
        if (editor != null)
            editor.close();
        return new HTMLEditPanel(framework.getMainFrame(), engine, rules,
                element, attribute);
    }

    @Override
    public String getName() {
        return "Core";
    }

    @Override
    public ValueGetter getValueGetter(Attribute attribute, Engine engine,
                                      GUIFramework framework, Closeable model) {
        return new ValueGetter() {
            @Override
            public Object getValue(TableNode node, int index) {
                return GlobalResourcesManager
                        .getString("AttributeType.Core.HTMLText");
            }
        };
    }

    @Override
    public boolean isCellEditable() {
        return true;
    }
}
