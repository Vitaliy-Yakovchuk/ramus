package com.ramussoft.gui.elist;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.simple.ElementListPersistent;

public class ElistTableModel extends AbstractTableModel {

    /**
     *
     */
    private static final long serialVersionUID = -4855667278930883635L;

    private ElistTablePanel left;

    private ElistTablePanel top;

    private Hashtable<Element, List<ElementListPersistent>> data = new Hashtable<Element, List<ElementListPersistent>>();

    private Engine engine;

    private AccessRules rules;

    private Attribute attribute;

    private boolean revert = false;

    private ElementAttributeListener q1Listener = new ElementAttributeListener() {
        @Override
        public void attributeChanged(AttributeEvent event) {
            elementAttributeChanged(event);
        }
    };

    private ElementAttributeListener q2Listener = new ElementAttributeListener() {
        @Override
        public void attributeChanged(AttributeEvent event) {
            elementAttributeChanged(event);
        }
    };

    public ElistTableModel(Engine engine, AccessRules rules,
                           Attribute attribute, ElistTablePanel left, ElistTablePanel top) {
        this.engine = engine;
        this.rules = rules;
        this.attribute = attribute;
        this.left = left;
        this.top = top;
        int count = left.getRowCount();
        for (int i = 0; i < count; i++) {
            Element element = left.getElement(i);
            load(element);
        }
        engine.addElementAttributeListener(left.getQualifier(), q1Listener);
        engine.addElementAttributeListener(top.getQualifier(), q2Listener);
    }

    protected void elementAttributeChanged(AttributeEvent event) {
        if ((event.isJournaled())
                && (event.getAttribute().getId() == attribute.getId())) {
            updateData(event);
        }
    }

    @SuppressWarnings("unchecked")
    protected void updateData(AttributeEvent event) {
        List<ElementListPersistent> list = data.get(event.getElement());
        if (list == null) {
            int count = left.getRowCount();
            for (int i = 0; i < count; i++) {
                Element element = left.getElement(i);
                load(element);
            }
        } else {
            data.put(event.getElement(),
                    (List<ElementListPersistent>) event.getNewValue());
        }
        fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
        return top.getRowCount();
    }

    @Override
    public int getRowCount() {
        return left.getRowCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Element element = left.getElement(rowIndex);
        List<ElementListPersistent> value = data.get(element);
        if (value == null) {
            value = new ArrayList<ElementListPersistent>();
            data.put(element, value);
        }
        Element c = top.getElement(columnIndex);
        for (ElementListPersistent p : value) {
            if (revert) {
                if (p.getElement1Id() == c.getId())
                    return p;

            } else {
                if (p.getElement2Id() == c.getId())
                    return p;
            }
        }
        return null;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        Element element = left.getElement(rowIndex);
        List<ElementListPersistent> list = data.get(element);
        Element c = top.getElement(columnIndex);
        ElementListPersistent p = new ElementListPersistent();
        if (revert) {
            p.setElement1Id(c.getId());
            p.setElement2Id(element.getId());
        } else {
            p.setElement1Id(element.getId());
            p.setElement2Id(c.getId());
        }
        if (value != null) {
            if (value instanceof Boolean) {
                if ((Boolean) value)
                    list.add(p);
                else
                    list.remove(p);
            } else {
                int index = list.indexOf(p);
                p.setConnectionType(value.toString());
                if (p.getConnectionType().length() == 0) {
                    if (index >= 0)
                        list.remove(index);
                } else {
                    if (index >= 0)
                        list.get(index).setConnectionType(value.toString());
                    else
                        list.add(p);
                }
            }
        } else {
            list.remove(p);
        }
        ((Journaled) engine).startUserTransaction();
        engine.setAttribute(element, attribute, new ArrayList(list));
        ((Journaled) engine).commitUserTransaction();
    }

    @SuppressWarnings("unchecked")
    private void load(Element element) {
        List<ElementListPersistent> value = (List<ElementListPersistent>) engine
                .getAttribute(element, attribute);
        data.put(element, value);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Boolean.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        Element r = left.getElement(rowIndex);
        Element c = top.getElement(columnIndex);
        return (rules.canUpdateElement(r.getId(), attribute.getId()))
                && (rules.canUpdateElement(c.getId(), attribute.getId()));
    }

    public void close() {
        engine.removeElementAttributeListener(left.getQualifier(), q1Listener);
        engine.removeElementAttributeListener(top.getQualifier(), q2Listener);
    }

    public void setRevert(boolean revert) {
        this.revert = revert;
    }
}
