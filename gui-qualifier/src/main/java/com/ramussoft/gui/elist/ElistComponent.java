package com.ramussoft.gui.elist;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.AttributeAdapter;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.AttributeListener;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.simple.ElementListPropertyPersistent;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.qualifier.Commands;

public class ElistComponent extends JPanel implements Commands {

    /**
     *
     */
    private static final long serialVersionUID = -3732302919476703371L;

    private Engine engine;

    private AccessRules rules;

    private ArrayList<Attribute> list = new ArrayList<Attribute>();

    private GUIFramework framework;

    private AbstractTableModel model = new AbstractTableModel() {

        /**
         *
         */
        private static final long serialVersionUID = -2679376108201149550L;

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getRowCount() {
            return list.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return list.get(rowIndex).getName();
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            Attribute attribute = list.get(rowIndex);
            attribute.setName(value.toString());
            startUserTransaction();
            engine.updateAttribute(attribute);
            endUserTransaction();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            Attribute attribute = list.get(rowIndex);
            return rules.canUpdateAttribute(attribute.getId());
        }

        @Override
        public String getColumnName(int column) {
            return GlobalResourcesManager
                    .getString("AttributeType.Core.ElementList");
        }

    };

    private AttributeListener listener = new AttributeAdapter() {

        @Override
        public void attributeCreated(AttributeEvent event) {
            if (!isElementList(((Attribute) event.getNewValue())
                    .getAttributeType()))
                return;
            list.add((Attribute) event.getNewValue());
            model.fireTableRowsInserted(list.size() - 1, list.size() - 1);
        }

        @Override
        public void attributeDeleted(AttributeEvent event) {
            if (!isElementList(((Attribute) event.getOldValue())
                    .getAttributeType()))
                return;
            int index = list.indexOf(event.getOldValue());
            list.remove(index);
            model.fireTableRowsDeleted(index, index);
        }

        @Override
        public void attributeUpdated(AttributeEvent event) {
            if (!isElementList(event.getAttribute().getAttributeType()))
                return;
            Attribute attribute = (Attribute) event.getNewValue();
            int index = list.indexOf(attribute);
            list.set(index, attribute);
            Collections.sort(list);
            model.fireTableDataChanged();
        }

    };

    private JTable table;

    public ElistComponent(GUIFramework framework) {
        super(new BorderLayout());
        this.engine = framework.getEngine();
        this.rules = framework.getAccessRules();
        this.framework = framework;
        engine.addAttributeListener(listener);
        List<Attribute> attributes = engine.getAttributes();
        for (Attribute attribute : attributes) {
            if (isElementList(attribute.getAttributeType())) {
                list.add(attribute);
            }
        }
        Collections.sort(list);
        JScrollPane pane = new JScrollPane();
        table = new JXTable(model) {

            /**
             *
             */
            private static final long serialVersionUID = 1224738724440092584L;

            @Override
            public boolean editCellAt(int row, int column, EventObject e) {
                if (e == null)
                    return super.editCellAt(row, column, e);
                return false;
            }
        };
        pane.setViewportView(table);
        this.add(pane, BorderLayout.CENTER);
        framework.addActionListener(FULL_REFRESH, new ActionListener() {
            @Override
            public void onAction(
                    com.ramussoft.gui.common.event.ActionEvent event) {
                list.clear();
                List<Attribute> attributes = engine.getAttributes();
                for (Attribute attribute : attributes) {
                    if (isElementList(attribute.getAttributeType())) {
                        list.add(attribute);
                    }
                }
                Collections.sort(list);
                model.fireTableDataChanged();
            }
        });
    }

    protected void startUserTransaction() {
        ((Journaled) engine).startUserTransaction();

    }

    protected void endUserTransaction() {
        ((Journaled) engine).commitUserTransaction();
    }

    private boolean isElementList(AttributeType type) {
        return ("ElementList".equals(type.getTypeName()))
                && ("Core".equals(type.getPluginName()));
    }

    public void close() {
        engine.removeAttributeListener(listener);
    }

    public JTable getTable() {
        return table;
    }

    public boolean canDeleteAttributes() {
        int[] rows = table.getSelectedRows();
        if (rows.length == 0)
            return false;
        for (int row : rows) {
            if (!canDeleteAttribute(row))
                return false;
        }
        return true;
    }

    private boolean canDeleteAttribute(int row) {
        int index = table.convertRowIndexToModel(row);
        Attribute attribute = list.get(index);
        if (!rules.canDeleteAttribute(attribute.getId()))
            return false;

        ElementListPropertyPersistent p = (ElementListPropertyPersistent) engine
                .getAttribute(null, attribute);

        if (!rules.canUpdateQualifier(p.getQualifier1()))
            return false;
        if (!rules.canUpdateQualifier(p.getQualifier2()))
            return false;
        return true;
    }

    public void deleteElements() {
        int[] rows = table.getSelectedRows();
        ((Journaled) engine).startUserTransaction();
        for (int row : rows)
            deleteElement(row);
        ((Journaled) engine).commitUserTransaction();
    }

    private void deleteElement(int row) {
        int index = table.convertRowIndexToModel(row);
        Attribute attribute = list.get(index);
        ElementListPropertyPersistent p = (ElementListPropertyPersistent) engine
                .getAttribute(null, attribute);
        Qualifier q1 = engine.getQualifier(p.getQualifier1());
        if (q1 != null) {
            q1.getAttributes().remove(attribute);
            engine.updateQualifier(q1);
        }
        Qualifier q2 = engine.getQualifier(p.getQualifier2());
        if (q2 != null) {
            q2.getAttributes().remove(attribute);
            engine.updateQualifier(q2);
        }
        engine.deleteAttribute(attribute.getId());
    }

    public void openElementLists() {
        for (int row : table.getSelectedRows()) {
            int index = table.convertRowIndexToModel(row);
            this.framework.propertyChanged(ElistPlugin.OPEN_ELEMENT_LIST,
                    list.get(index));
        }
    }

    public void openElementListsInTables() {
        for (int row : table.getSelectedRows()) {
            int index = table.convertRowIndexToModel(row);
            this.framework.propertyChanged(
                    ElistPlugin.OPEN_ELEMENT_LIST_IN_TABLE, list.get(index));
        }
    }

    public Attribute getSelectedAttribute() {
        int row = table.getSelectedRow();
        int index = table.convertRowIndexToModel(row);
        return list.get(index);
    }
}
