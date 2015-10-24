package com.ramussoft.gui.attribute;

import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.common.AbstractAttributeEditor;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.table.RowTreeTableComponent;
import com.ramussoft.gui.qualifier.table.SelectType;
import com.ramussoft.gui.qualifier.table.SelectableTableView;

public class OtherElementEditor extends AbstractAttributeEditor {
    private RowTreeTableComponent component;

    private SelectableTableView view;

    private AccessRules rules;

    private GUIFramework framework;

    private Engine engine;

    private Qualifier qualifier;

    private String propertiesPrefix;

    private JComponent resultComponent;

    public OtherElementEditor(Engine engine, AccessRules rules,
                              Qualifier qualifier, GUIFramework framework, String propertiesPrefix) {
        this.rules = rules;
        this.framework = framework;
        this.engine = engine;
        this.qualifier = qualifier;
        this.propertiesPrefix = propertiesPrefix;
    }

    @Override
    public JComponent getComponent() {
        if (resultComponent != null)
            return resultComponent;
        view = new SelectableTableView(framework, engine, rules, qualifier) {
            @Override
            public String getPropertiesPrefix() {
                return propertiesPrefix;
            }

        };
        resultComponent = view.createComponent();
        component = view.getComponent();
        view.setSelectType(SelectType.RADIO);
        return resultComponent;
    }

    @Override
    public Object getValue() {
        if (component == null) {
            System.err
                    .println("Tring to editing attribute with not created component, probebly other element does not have a qualifier");
            return null;
        }
        List<Row> rows = component.getModel().getSelectedRows();
        if (rows.size() == 0)
            return null;
        return rows.get(0).getElementId();
    }

    @Override
    public Object setValue(Object value) {
        component.getModel().clearSelection();

        if (value != null) {
            long elementId = (Long) value;
            if (elementId > -1l) {
                RowSet rowSet = component.getRowSet();
                Row row = rowSet.findRow(elementId);
                if (row != null) {
                    component.getModel().setSelectedRow(row, true);
                    int indexOfRow = component.getTable().indexOfRow(row);
                    if (indexOfRow >= 0)
                        component.getTable().scrollRowToVisible(indexOfRow);
                } else
                    return null;
            } else
                return null;
        }

        return value;
    }

    @Override
    public void close() {
        try {
            resultComponent = null;
            if (view != null) {
                view.close();
                view = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Action[] getActions() {
        return view.getActions();
    }
}
