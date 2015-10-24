package com.ramussoft.gui.attribute.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.event.EventListenerList;
import javax.swing.table.AbstractTableModel;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.ElementAdapter;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementListener;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.gui.attribute.AttributeEditorView.ElementAttribute;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.qualifier.table.TableNode;
import com.ramussoft.gui.qualifier.table.TabledAttributePlugin;
import com.ramussoft.gui.qualifier.table.TreeTableNode;
import com.ramussoft.gui.qualifier.table.ValueGetter;
import com.ramussoft.gui.qualifier.table.RowTreeTableModel.Localizer;
import com.ramussoft.gui.qualifier.table.event.CloseEvent;
import com.ramussoft.gui.qualifier.table.event.CloseListener;
import com.ramussoft.gui.qualifier.table.event.Closeable;

public class TableEditorModel extends AbstractTableModel implements Closeable {

    /**
     *
     */
    private static final long serialVersionUID = 5017842189275445779L;

    public static String UPDATE_TABLE_VALUE = "UpdateTableValue";

    private List<Attribute> attributes;

    private List<Element> elements;

    private ValueGetter[] valueGetters;

    private List<Object[]> data;

    private Localizer[] localizers;

    private Engine engine;

    private EventListenerList closeListeners = new EventListenerList();

    private AttributePlugin[] plugins;

    private Attribute tableAttribute;

    private Element tableElement;

    private boolean[] savedColumns;

    private boolean editable;

    private GUIFramework framework;

    private ElementAttributeListener listener = new ElementAttributeListener() {
        @Override
        public void attributeChanged(AttributeEvent event) {
            Element element = event.getElement();
            int index = elements.indexOf(element);
            if (index >= 0) {
                int i = attributes.indexOf(event.getAttribute());
                if (i >= 0) {
                    data.get(index)[i] = event.getNewValue();
                    fireTableCellUpdated(index, i);
                }
            }
            if (StandardAttributesPlugin.getTableElementIdAttribute(engine)
                    .equals(event.getAttribute())) {
                if (event.getNewValue() != null) {
                    if (event.getNewValue().equals(tableElement.getId())) {
                        elements.add(element);
                        Object[] objects = new Object[attributes.size()];
                        for (int i = 0; i < objects.length; i++) {
                            objects[i] = engine.getAttribute(element,
                                    attributes.get(i));
                        }
                        data.add(objects);

                        fireTableRowsInserted(data.size() - 1, data.size() - 1);
                    } else if ((event.getOldValue() != null)
                            && (event.getOldValue()
                            .equals(tableElement.getId()))) {
                        if (index >= 0) {
                            elements.remove(index);
                            data.remove(index);
                            fireTableRowsDeleted(index, index);
                        }
                    }
                }
            }
        }
    };

    private ElementListener elementListener = new ElementAdapter() {

        public void elementDeleted(com.ramussoft.common.event.ElementEvent event) {
            Element element = event.getOldElement();
            int index = elements.indexOf(element);
            if (index >= 0) {
                elements.remove(index);
                data.remove(index);
                fireTableRowsDeleted(index, index);
            }
        }

        ;
    };

    private ActionListener tableValueListener = new ActionListener() {

        @Override
        public void onAction(ActionEvent event) {
            updateElementAttribute(event);
        }

    };

    private void updateElementAttribute(ActionEvent event) {
        ElementAttribute elementAttribute = (ElementAttribute) event.getValue();
        int index = elements.indexOf(elementAttribute.element);
        if (index >= 0) {
            int i = attributes.indexOf(elementAttribute.attribute);
            if (i >= 0) {
                data.get(index)[i] = event.getMetadata();
                fireTableCellUpdated(index, i);
            }
        }
    }

    public TableEditorModel(List<Attribute> attributes, List<Element> elements,
                            Attribute tableAttribute, Element tableElement,
                            GUIFramework framework) {

        this.framework = framework;
        this.attributes = attributes;
        this.elements = elements;
        this.valueGetters = new ValueGetter[attributes.size()];
        this.localizers = new Localizer[attributes.size()];
        this.engine = framework.getEngine();
        this.plugins = new AttributePlugin[attributes.size()];
        this.tableAttribute = tableAttribute;
        this.tableElement = tableElement;

        Qualifier qualifier = StandardAttributesPlugin
                .getTableQualifierForAttribute(engine, tableAttribute);
        this.engine.addElementAttributeListener(qualifier, listener);
        this.engine.addElementListener(qualifier, elementListener);

        this.framework
                .addActionListener(UPDATE_TABLE_VALUE, tableValueListener);

        editable = framework.getAccessRules().canUpdateElement(
                tableElement.getId(), tableAttribute.getId());
        savedColumns = new boolean[attributes.size()];
        Arrays.fill(savedColumns, true);
        for (int i = 0; i < valueGetters.length; i++) {
            Attribute attribute = attributes.get(i);
            AttributePlugin plugin = framework.findAttributePlugin(attribute);
            plugins[i] = plugin;
            ValueGetter getter = null;
            if (plugin instanceof TabledAttributePlugin) {
                getter = ((TabledAttributePlugin) plugin).getValueGetter(
                        attribute, engine, framework, this);
            }
            if (getter == null)
                valueGetters[i] = new ValueGetter() {

                    @Override
                    public Object getValue(TableNode node, int index) {
                        return node.getValueAt(index);
                    }

                };
            else
                valueGetters[i] = getter;
            localizers[i] = new Localizer() {

                @Override
                public Object getValue(Object key) {
                    return key;
                }

            };
        }
        data = new ArrayList<Object[]>(elements.size());
        for (Element element : elements) {
            Object[] objects = new Object[attributes.size()];
            for (int i = 0; i < objects.length; i++) {
                objects[i] = engine.getAttribute(element, attributes.get(i));
            }
            data.add(objects);
        }

        ActionEvent event = (ActionEvent) framework.get(UPDATE_TABLE_VALUE);
        if (event != null)
            updateElementAttribute(event);
    }

    @Override
    public int getColumnCount() {
        return attributes.size();
    }

    @Override
    public int getRowCount() {
        return elements.size();
    }

    @Override
    public Object getValueAt(final int rowIndex, int columnIndex) {
        return localizers[columnIndex].getValue(valueGetters[columnIndex]
                .getValue(new TreeTableNode(new Vector<TreeTableNode>(0)) {

                    @Override
                    public Object getValueAt(int index) {
                        return data.get(rowIndex)[index];
                    }

                }, columnIndex));
    }

    public void addCloseListener(CloseListener listener) {
        closeListeners.add(CloseListener.class, listener);
    }

    public void removeCloseListener(CloseListener listener) {
        closeListeners.remove(CloseListener.class, listener);
    }

    public CloseListener[] getCloseListeners() {
        return closeListeners.getListeners(CloseListener.class);
    }

    @Override
    public String getColumnName(int column) {
        Attribute attribute = attributes.get(column);
        String name = framework.getSystemAttributeName(attribute);
        if (name != null)
            return name;
        return attribute.getName();
    }

    public void addElement() {
        ((Journaled) engine).startUserTransaction();

        StandardAttributesPlugin.createTableElement(engine, tableAttribute,
                tableElement);

        ((Journaled) engine).commitUserTransaction();
    }

    public void removeElements(List<Element> list) {
        ((Journaled) engine).startUserTransaction();
        for (Element element : list) {
            engine.deleteElement(element.getId());
        }
        ((Journaled) engine).commitUserTransaction();
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (savedColumns[columnIndex]) {
            ((Journaled) engine).startUserTransaction();
            engine.setAttribute(elements.get(rowIndex),
                    attributes.get(columnIndex), value);
            data.get(rowIndex)[columnIndex] = value;
            ((Journaled) engine).commitUserTransaction();
            framework.remove(UPDATE_TABLE_VALUE);
        }
    }

    public void setSaveValue(int columnIndex, boolean value) {
        savedColumns[columnIndex] = value;
    }

    public void close() {
        Qualifier qualifier = StandardAttributesPlugin
                .getTableQualifierForAttribute(engine, tableAttribute);
        engine.removeElementAttributeListener(qualifier, listener);
        engine.removeElementListener(qualifier, elementListener);
        CloseEvent event = new CloseEvent(this);
        for (CloseListener listener : getCloseListeners()) {
            listener.closed(event);
        }
        framework.removeActionListener(UPDATE_TABLE_VALUE, tableValueListener);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return editable;
    }

    public boolean isEditable() {
        return editable;
    }

    public Element getElement(int row) {
        return elements.get(row);
    }

    public Attribute getAttribute(int column) {
        return attributes.get(column);
    }

    public boolean move(int index, ArrayList<Integer> list) {
        List<Object[]> move = new ArrayList<Object[]>(list.size());
        List<Element> moveE = new ArrayList<Element>(list.size());
        for (int i = list.size() - 1; i >= 0; i--) {
            int pos = list.get(i);
            if (pos < index)
                index--;
            move.add(data.remove(pos));
            moveE.add(elements.remove(pos));
            fireTableRowsDeleted(pos, pos);
        }

        for (int i = 0; i < move.size(); i++) {
            data.add(index, move.get(i));
            elements.add(index, moveE.get(i));
        }
        int lastRow = index + move.size();
        if (lastRow >= elements.size())
            lastRow = elements.size() - 1;
        if (index < lastRow)
            fireTableRowsInserted(index, lastRow);

        StandardAttributesPlugin.saveTableAttributeOrder(engine, tableElement,
                tableAttribute, elements);

        return true;
    }
}
