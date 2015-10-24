package com.ramussoft.gui.attribute;


import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.AbstractAttributePlugin;

public class BooleanAttributePlugin extends AbstractAttributePlugin {

    private BooleanRenderer renderer = new BooleanRenderer();

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("Core", "Boolean", true);
    }

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
        return new BooleanEditor();
    }

    @Override
    public TableCellRenderer getTableCellRenderer(Engine engine,
                                                  AccessRules rules, Attribute attribute) {
        return renderer;
    }

    @Override
    public String getName() {
        return "Core";
    }
}
