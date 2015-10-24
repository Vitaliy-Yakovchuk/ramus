package com.ramussoft.gui.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.Unique;
import com.ramussoft.core.attribute.simple.ElementListPersistent;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.common.AbstractAttributeEditor;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.table.RowTreeTableComponent;
import com.ramussoft.gui.qualifier.table.SelectableTableView;

public class ElementListAttributeEditor extends AbstractAttributeEditor {

    private RowTreeTableComponent component;

    private SelectableTableView view;

    private final GUIFramework framework;

    private final Qualifier qualifier;

    private final boolean left;

    private Unique element;

    private final String propertiesPrefix;

    private JComponent resultComponent;

    public ElementListAttributeEditor(GUIFramework framework,
                                      Qualifier qualifier, boolean left, String propertiesPrefix) {
        this.framework = framework;
        this.qualifier = qualifier;
        this.left = left;
        this.propertiesPrefix = propertiesPrefix;
    }

    @Override
    public JComponent getComponent() {
        if (resultComponent != null)
            return resultComponent;
        view = new SelectableTableView(framework, qualifier) {

            @Override
            protected Attribute[] getAttributes() {

                if (qualifier.equals(engine.getPluginProperty("Core",
                        StandardAttributesPlugin.ATTRIBUTES_QUALIFIER)))
                    return new Attribute[]{
                            (Attribute) engine.getPluginProperty("Core",
                                    StandardAttributesPlugin.ATTRIBUTE_NAME),
                            (Attribute) engine
                                    .getPluginProperty(
                                            "Core",
                                            StandardAttributesPlugin.ATTRIBUTE_TYPE_NAME)};
                else
                    return super.getAttributes();
            }

            @Override
            public String getPropertiesPrefix() {
                return propertiesPrefix;
            }
        };
        resultComponent = view.createComponent();
        component = view.getComponent();
        if (qualifier.equals(framework.getEngine().getPluginProperty("Core",
                StandardAttributesPlugin.ATTRIBUTES_QUALIFIER))) {
            component.getTable().setEditable(false);
        }
        return resultComponent;
    }

    @Override
    public Object getValue() {
        List<ElementListPersistent> list = new ArrayList<ElementListPersistent>();
        List<Row> rows = component.getModel().getSelectedRows();
        for (Row row : rows) {
            ElementListPersistent p;
            if (left) {
                p = new ElementListPersistent(row.getElementId(),
                        element.getId());
            } else {
                p = new ElementListPersistent(element.getId(),
                        row.getElementId());
            }
            list.add(p);
        }
        Collections.sort(list);
        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object setValue(Object value) {
        component.getModel().clearSelection();
        List<ElementListPersistent> list = (List<ElementListPersistent>) value;
        RowSet rowSet = component.getRowSet();
        Row row = null;
        for (ElementListPersistent p : list) {
            long id = (left) ? p.getElement1Id() : p.getElement2Id();
            row = rowSet.findRow(id);
            if (row != null)
                component.getModel().setSelectedRow(row, true);
        }
        if (row != null)
            component.getTable().scrollRowToVisible(
                    component.getTable().indexOfRow(row));
        Collections.sort(list);
        return value;
    }

    @Override
    public void close() {
        try {
            view.close();
            resultComponent = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Action[] getActions() {
        return view.getActions();
    }

    public RowTreeTableComponent getRowTreeTableComponent() {
        return component;
    }

    public void setElement(Unique element) {
        this.element = element;
    }

    public Row getSelectedRow() {
        return view.getSelectedRow();
    }
}
