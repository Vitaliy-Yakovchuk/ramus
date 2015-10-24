package com.ramussoft.gui.attribute;

import java.awt.BorderLayout;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.AttributeAdapter;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.AttributeListener;
import com.ramussoft.common.event.ElementAdapter;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementListener;
import com.ramussoft.common.event.QualifierAdapter;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.AbstractUniqueView;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.View;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.qualifier.Commands;
import com.ramussoft.gui.qualifier.table.DialogedTableCellEditor;
import com.ramussoft.gui.qualifier.table.ElementsTable;
import com.ramussoft.gui.qualifier.table.MetadataGetter;
import com.ramussoft.gui.qualifier.table.TableNode;
import com.ramussoft.gui.qualifier.table.TabledAttributePlugin;
import com.ramussoft.gui.qualifier.table.ValueGetter;
import com.ramussoft.gui.qualifier.table.event.CloseEvent;
import com.ramussoft.gui.qualifier.table.event.CloseListener;
import com.ramussoft.gui.qualifier.table.event.Closeable;

public class ElementAttributesEditor extends AbstractUniqueView implements
        UniqueView, Commands, Closeable {

    private Qualifier currentQualifier;

    private Element currentElement;

    private JXTable table;

    private AttributesEditorModel model = new AttributesEditorModel();

    private ValueGetter[] getters;

    private TableCellRenderer[] renderers;

    private Object[] values;

    private EventListenerList closeListeners = new EventListenerList();

    private boolean[] saveValues;

    private List<AttributeName> attributes = new ArrayList<AttributeName>();

    private Object metadata;

    private Object lock = new Object();

    private QualifierListener qualifierListener = new QualifierAdapter() {
        public void qualifierUpdated(QualifierEvent event) {
            if (event.getNewQualifier().equals(currentQualifier)) {
                currentQualifier = event.getNewQualifier();
                reload();
            }
        }

        ;
    };

    private ElementAttributeListener elementAttributeListener = new ElementAttributeListener() {

        @Override
        public void attributeChanged(AttributeEvent event) {
            if (event.getElement() != null) {
                if (event.getElement().equals(currentElement)) {
                    int index = indexOfAttribute(event.getAttribute());
                    if (index >= 0) {
                        values[index] = event.getNewValue();
                        synchronized (lock) {
                            table.getSelectionModel()
                                    .removeListSelectionListener(
                                            listSelectionListener);
                            model.fireTableRowsUpdated(index, index);
                            table.getSelectionModel().addListSelectionListener(
                                    listSelectionListener);
                        }

                    }
                }
            }
        }
    };

    private ElementListener elementListener = new ElementAdapter() {
        public void elementDeleted(com.ramussoft.common.event.ElementEvent event) {
            if ((currentElement != null)
                    && (currentElement.equals(event.getOldElement()))) {
                clearAll();
            }
        }

        ;
    };

    private AttributeListener attributeListener = new AttributeAdapter() {
        @Override
        public void attributeUpdated(AttributeEvent event) {
            if (currentQualifier != null) {
                int index = indexOfAttribute(event.getAttribute());
                if (index >= 0) {
                    attributes.get(index).attribute = (Attribute) event
                            .getNewValue();
                    if (currentElement != null) {
                        values[index] = framework.getEngine().getAttribute(
                                currentElement, event.getAttribute());
                    }
                    synchronized (event) {

                    }
                    synchronized (lock) {
                        table.getSelectionModel().removeListSelectionListener(
                                listSelectionListener);
                        model.fireTableRowsUpdated(index, index);
                        table.getSelectionModel().addListSelectionListener(
                                listSelectionListener);
                    }
                }
            }
        }
    };

    private ListSelectionListener listSelectionListener;

    public ElementAttributesEditor(final GUIFramework framework) {
        super(framework);
        framework.addActionListener(ACTIVATE_ELEMENT, new ActionListener() {

            @Override
            public void onAction(ActionEvent event) {
                metadata = event.getMetadata();
                Element value = (Element) event.getValue();
                if (value == null) {
                    if (values != null)
                        clearAll();
                    return;
                }
                if (value.equals(currentElement)) {
                    currentElement = value;
                    return;
                } else if ((currentElement == null)
                        || (value.getQualifierId() != currentElement
                        .getQualifierId())) {
                    Engine engine = framework.getEngine();
                    if (currentQualifier != null) {
                        clearListeners();
                    }
                    currentQualifier = framework.getEngine().getQualifier(
                            value.getQualifierId());

                    engine.addQualifierListener(qualifierListener);
                    engine.addElementAttributeListener(currentQualifier,
                            elementAttributeListener);
                    engine
                            .addElementListener(currentQualifier,
                                    elementListener);

                    currentElement = value;
                    reload();
                } else {
                    currentElement = value;
                    loadElement();
                }
            }

        });
        framework.getEngine().addAttributeListener(attributeListener);
    }

    private void clearListeners() {
        Engine engine = framework.getEngine();
        engine.removeQualifierListener(qualifierListener);
        engine.removeElementAttributeListener(currentQualifier,
                elementAttributeListener);
        engine.removeElementListener(currentQualifier, elementListener);
    }

    protected void loadElement() {
        loadCurrentQualifierAttributes();

        Engine engine = framework.getEngine();
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attr = attributes.get(i).attribute;
            values[i] = engine.getAttribute(currentElement, attr);
        }
        synchronized (lock) {
            table.getSelectionModel().removeListSelectionListener(
                    listSelectionListener);
            model.fireTableDataChanged();
            table.getSelectionModel().addListSelectionListener(
                    listSelectionListener);
        }
    }

    private void loadCurrentQualifierAttributes() {
        attributes.clear();

        for (Attribute attr : currentQualifier.getAttributes()) {
            String name = framework.getSystemAttributeName(attr);
            if (name != null)
                attributes.add(new AttributeName(attr, name));
            else
                attributes.add(new AttributeName(attr));
        }
        for (Attribute attr : currentQualifier.getSystemAttributes()) {
            String name = framework.getSystemAttributeName(attr);
            if (name != null)
                attributes.add(new AttributeName(attr, name));
        }
    }

    protected void reload() {
        CloseEvent event = new CloseEvent(this);
        CloseListener[] listeners = getCloseListeners();
        for (CloseListener listener : listeners) {
            listener.closed(event);
            removeCloseListener(listener);
        }

        loadCurrentQualifierAttributes();
        Engine engine = framework.getEngine();
        AccessRules rules = framework.getAccessRules();
        getters = new ValueGetter[attributes.size()];
        renderers = new TableCellRenderer[attributes.size()];
        values = new Object[attributes.size()];
        saveValues = new boolean[attributes.size()];
        Arrays.fill(saveValues, true);
        Arrays.fill(getters, new ValueGetter() {

            @Override
            public Object getValue(TableNode node, int index) {
                return node.getValueAt(index);
            }
        });
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attr = attributes.get(i).attribute;
            AttributePlugin plugin = framework.findAttributePlugin(attr);
            if (plugin instanceof TabledAttributePlugin) {
                ValueGetter getter = ((TabledAttributePlugin) plugin)
                        .getValueGetter(attr, engine, framework, this);
                if (getter != null)
                    getters[i] = getter;
            }
            renderers[i] = plugin.getTableCellRenderer(engine, rules, attr);
        }

        loadElement();
    }

    private TableCellEditor getCellEditor(int aRow) {
        Attribute attribute = attributes.get(aRow).attribute;

        AttributePlugin plugin = framework.findAttributePlugin(attribute);
        TableCellEditor cellEditor = plugin.getTableCellEditor(framework
                .getEngine(), framework.getAccessRules(), attribute);
        if (cellEditor == null) {
            saveValues[aRow] = false;
            cellEditor = new DialogedTableCellEditor(framework.getEngine(),
                    framework.getAccessRules(), attribute, plugin, framework);
        } else
            saveValues[aRow] = plugin.isCellEditable();
        return cellEditor;
    }

    @Override
    public String getDefaultWorkspace() {
        return "Workspace.Qualifiers";
    }

    @Override
    public String getId() {
        return "ElementAttributesEditor";
    }

    @Override
    public JComponent createComponent() {
        JScrollPane pane = new JScrollPane();
        table = new Table();
        pane.setViewportView(table);
        listSelectionListener = new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    row = table.convertRowIndexToModel(row);
                    Attribute attribute = attributes.get(row).attribute;
                    AttributeEditorView.ElementAttribute elementAttribute = new AttributeEditorView.ElementAttribute(
                            currentElement, attribute);
                    framework.propertyChanged(ACTIVATE_ATTRIBUTE,
                            elementAttribute, metadata);
                }
            }
        };
        table.getSelectionModel().addListSelectionListener(
                listSelectionListener);
        return pane;
    }

    @Override
    public Action[] getActions() {
        return new Action[]{};
    }

    private class AttributesEditorModel extends AbstractTableModel {

        /**
         *
         */
        private static final long serialVersionUID = -7468851458802770406L;

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            if (currentQualifier == null)
                return 0;
            return attributes.size();
        }

        @Override
        public Object getValueAt(final int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                AttributeName name = attributes.get(rowIndex);
                return name;
            }
            TableNode node = new TableNode() {

                @Override
                public Object getValueAt(int index) {
                    return values[rowIndex];
                }
            };
            return getters[rowIndex].getValue(node, 0);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            AccessRules accessRules = framework.getAccessRules();
            if (columnIndex == 0) {
                AttributeName attributeName = attributes.get(columnIndex);
                if (attributeName.name != null)
                    return false;
                return accessRules.canUpdateAttribute(attributeName.attribute
                        .getId());
            }

            return accessRules.canUpdateElement(currentElement.getId(),
                    attributes.get(rowIndex).attribute.getId());
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0)
                return GlobalResourcesManager
                        .getString("OtherElement.Attribute");
            return GlobalResourcesManager.getString("Attribute.Value");
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Engine engine = framework.getEngine();
            if (columnIndex == 0) {
                Attribute attribute = attributes.get(rowIndex).attribute;
                attribute.setName((String) aValue);
                ((Journaled) engine).startUserTransaction();
                engine.updateAttribute(attribute);
                ((Journaled) engine).commitUserTransaction();
            } else {
                if (!saveValues[rowIndex])
                    return;
                Attribute attribute = attributes.get(rowIndex).attribute;
                if ((aValue instanceof String)
                        && (attribute.getId() == currentQualifier
                        .getAttributeForName())) {
                    List<Element> list = engine.findElements(currentQualifier
                            .getId(), attribute, aValue);
                    for (Element element : list) {
                        if (!element.equals(currentElement)) {
                            if (JOptionPane
                                    .showConfirmDialog(
                                            table,
                                            GlobalResourcesManager
                                                    .getString("Warning.ElementsExists"),
                                            GlobalResourcesManager
                                                    .getString("ConfirmMessage.Title"),
                                            JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                                return;
                            break;
                        }
                    }
                }
                ((Journaled) engine).startUserTransaction();
                engine.setAttribute(currentElement, attribute, aValue);
                ((Journaled) engine).commitUserTransaction();
            }
        }
    }

    ;

    private class Table extends JXTable implements ElementsTable,
            MetadataGetter {

        /**
         *
         */
        private static final long serialVersionUID = 7879584959295712141L;

        public Table() {
            super(model);
        }

        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            if (convertColumnIndexToModel(column) == 1) {
                int aRow = convertRowIndexToModel(row);
                TableCellEditor editor = ElementAttributesEditor.this
                        .getCellEditor(aRow);
                if (editor != null) {
                    if (editor instanceof DialogedTableCellEditor) {
                        ((DialogedTableCellEditor) editor)
                                .setMetaValue(metadata);
                    }
                    return editor;
                }
            }
            return super.getCellEditor(row, column);
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            if (convertColumnIndexToModel(column) == 1) {
                TableCellRenderer renderer = renderers[convertRowIndexToModel(row)];
                if (renderer != null)
                    return renderer;
            }
            return super.getCellRenderer(row, column);
        }

        @Override
        public Element getElementForRow(int aRow) {
            return currentElement;
        }

        @Override
        public Object getMetadata() {
            return metadata;
        }

    }

    ;

    @Override
    public void addCloseListener(CloseListener listener) {
        closeListeners.add(CloseListener.class, listener);
    }

    @Override
    public CloseListener[] getCloseListeners() {
        return closeListeners.getListeners(CloseListener.class);
    }

    @Override
    public void removeCloseListener(CloseListener listener) {
        closeListeners.remove(CloseListener.class, listener);
    }

    ;

    private int indexOfAttribute(Attribute attribute) {
        for (int i = 0; i < attributes.size(); i++)
            if (attributes.get(i).attribute.equals(attribute))
                return i;
        return -1;
    }

    @Override
    public ActionEvent getOpenActionForSave() {
        View view = framework.getLastDinamicView();
        if (view != null) {
            ActionEvent event = view.getOpenAction();
            if (event != null)
                return event;
        }
        return super.getOpenActionForSave();
    }

    private void clearAll() {
        clearListeners();
        currentQualifier = null;
        currentElement = null;
        getters = null;
        renderers = null;
        values = null;
        saveValues = null;
        metadata = null;
        attributes.clear();
        synchronized (lock) {
            table.getSelectionModel().removeListSelectionListener(
                    listSelectionListener);
            model.fireTableDataChanged();
            table.getSelectionModel().addListSelectionListener(
                    listSelectionListener);
        }
    }

    private class AttributeName {
        Attribute attribute;

        String name;

        public AttributeName(Attribute attribute, String name) {
            this.attribute = attribute;
            this.name = name;
        }

        public AttributeName(Attribute attribute) {
            this.attribute = attribute;
        }

        public String toString() {
            if (name != null)
                return name;
            return attribute.getName();
        }
    }

    ;

    @Override
    public String getDefaultPosition() {
        return BorderLayout.EAST;
    }
}
