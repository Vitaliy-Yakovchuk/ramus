package com.ramussoft.database.common;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.event.EventListenerList;

import com.ramussoft.common.AdditionalPluginLoader;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.ElementAdapter;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementEvent;
import com.ramussoft.common.event.ElementListener;
import com.ramussoft.common.event.QualifierAdapter;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.simple.HierarchicalPlugin;
import com.ramussoft.core.attribute.simple.HierarchicalPersistent;

public class RowSet {

    public static interface RowCreater {
        Row createRow(Element element, RowSet data, Attribute[] attributes,
                      Object[] objects);
    }

    ;

    public static ElementLoadFilter[] elementLoadFilters;

    static {
        Iterator<ElementLoadFilter> iterator = AdditionalPluginLoader
                .loadProviders(ElementLoadFilter.class);
        List<ElementLoadFilter> list = new ArrayList<ElementLoadFilter>();
        while (iterator.hasNext())
            list.add(iterator.next());
        elementLoadFilters = list.toArray(new ElementLoadFilter[list.size()]);
    }

    protected Engine engine;

    private Attribute[] attributes;

    private Qualifier qualifier;

    protected Hashtable<Long, Row> rowHash = new Hashtable<Long, Row>();

    protected Row root;

    private List<Row> rootList;

    private Attribute hAttribute;

    private Attribute[] attributesWithH;

    private static Thread currentThread = null;

    private static final Object STATIC_LOCK = new Object();

    private EventListenerList elementListenerList = new EventListenerList();

    private Hashtable<Long, Integer> attributeIndexes = new Hashtable<Long, Integer>();

    private final boolean readOnly;

    private QualifierListener qualifierListener = new QualifierAdapter() {
        @Override
        public void qualifierUpdated(QualifierEvent event) {
            if (event.getNewQualifier().getId() == qualifier.getId()) {
                Qualifier qualifier2 = engine.getQualifier(qualifier.getId());
                if (qualifier2.getAttributeForName() != qualifier
                        .getAttributeForName()) {
                    for (Row row : getAllRows())
                        row.updateElement();
                }
                qualifier = qualifier2;
            }
        }
    };

    public static class RootRow extends Row {

        private Qualifier qualifier;

        public RootRow(Qualifier qualifier, RowSet data,
                       Attribute[] attributes, Object[] objects) {
            super(null, data, attributes, objects);
            this.qualifier = qualifier;
        }

        @Override
        public String getName() {
            return qualifier.getName();
        }

        @Override
        public int hashCode() {
            return qualifier.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof RootRow) {
                return qualifier.equals(((RootRow) obj).qualifier);
            }
            return super.equals(obj);
        }

    }

    ;

    protected RowCreater rowCreater = new RowCreater() {

        @Override
        public Row createRow(Element element, RowSet data,
                             Attribute[] attributes, Object[] objects) {
            if (element == null) {
                return new RootRow(qualifier, data, attributes, objects);
            }

            return new Row(element, data, attributes, objects);
        }

    };

    public void setRowCreater(RowCreater rowCreater) {
        this.rowCreater = rowCreater;
    }

    private ElementAttributeListener listener = new ElementAttributeListener() {
        @Override
        public void attributeChanged(AttributeEvent event) {
            RowSet.this.attributeChanged(event);
        }
    };

    private ElementListener elementListener = new ElementAdapter() {

        @Override
        public void elementCreated(ElementEvent event) {
            RowSet.this.elementCreated(event);
        }

        @Override
        public void elementDeleted(ElementEvent event) {
            try {
                RowSet.this.elementDeleted(event);
            } catch (Exception e) {
            }
        }
    };

    public RowSet(Engine engine, Qualifier qualifier, Attribute[] attributes) {
        this(engine, qualifier, attributes, null);
    }

    public RowSet(Engine engine, Qualifier qualifier, Attribute[] attributes,
                  RowCreater creater) {
        this(engine, qualifier, attributes, creater, false);
    }

    public RowSet(Engine engine, Qualifier qualifier, Attribute[] attributes,
                  RowCreater creater, boolean readOnly) {
        this.readOnly = readOnly;
        this.qualifier = qualifier;
        this.attributes = attributes;
        this.engine = engine;
        if (creater != null)
            this.rowCreater = creater;
        init();
    }

    public RowSet(Engine engine, Qualifier qualifier) {
        this.qualifier = qualifier;
        this.engine = engine;
        this.readOnly = false;
        this.attributes = new Attribute[]{};
    }

    private void init() {
        rowHash.clear();
        ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
        hAttribute = (Attribute) engine.getPluginProperty("Core",
                HierarchicalPlugin.HIERARHICAL_ATTRIBUTE);
        attributeList.add(hAttribute);
        for (Attribute attribute : attributes) {
            attributeList.add(attribute);
        }

        Row root = null;

        attributesWithH = attributeList.toArray(new Attribute[attributeList
                .size()]);

        for (int i = 0; i < attributesWithH.length; i++)
            attributeIndexes.put(attributesWithH[i].getId(), i);

        this.root = rowCreater.createRow(null, this, attributesWithH,
                new Object[attributesWithH.length]);
        this.rootList = this.root.getChildren();

        List<Integer> removed = new ArrayList<Integer>();

        for (int i = attributeList.size() - 1; i >= 0; i--) {
            AttributeType attributeType = attributeList.get(i)
                    .getAttributeType();
            if (!attributeType.isLight()) {
                attributeList.remove(i);
                removed.add(i);
            }
        }

        int[] rems = new int[removed.size()];
        for (int i = 0; i < rems.length; i++) {
            rems[i] = removed.get(i);
        }

        Hashtable<Element, Object[]> hash = engine.getElements(qualifier,
                attributeList);

        for (Entry<Element, Object[]> e : hash.entrySet()) {
            Element element = e.getKey();

            Object[] objects = e.getValue();

            Object[] objects2;
            if (rems.length == 0) {
                objects2 = objects;
            } else {
                objects2 = new Object[objects.length + rems.length];
                int k = 0;
                for (int i = objects.length - 1; i >= 0; i--) {
                    while ((rems.length > k)
                            && (i == rems[k] - rems.length + k)) {
                        k++;
                    }
                    objects2[i - k + rems.length] = objects[i];
                }
            }
            Row row = rowCreater.createRow(element, this, attributesWithH,
                    objects2);
            rowHash.put(element.getId(), row);
            HierarchicalPersistent p = (HierarchicalPersistent) e.getValue()[0];
            if ((p != null) && (p.getParentElementId() == -1l)
                    && (p.getPreviousElementId() == -1l)) {
                root = row;
            }
        }

        for (Entry<Long, Row> e : rowHash.entrySet()) {
            Row row = e.getValue();
            HierarchicalPersistent p = row.getHierarchicalPersistent();
            if (p == null) {
                p = new HierarchicalPersistent();
                p.setParentElementId(-1);
                p.setPreviousElementId((root == null) ? -1l : root
                        .getElementId());
                boolean start = isUserTransactionStarted();
                if (!start)
                    startUserTransaction();
                row.setAttribute(attributeList.get(0), p);
                if (!start)
                    commitUserTransaction();
                row.setNativeHierarchicalPersistent(p);
                if (root == null) {
                    root = row;
                }
            }

            Row parent = findRow(p.getParentElementId());

            if (parent == null) {
                parent = this.root;
            } else if (parent.getElementId() == row.getElementId())
                parent = this.root;

            addRow(parent.getChildren(), row);
            row.setNativeParent(parent);
        }

        resort(rootList);

        for (Entry<Long, Row> e : rowHash.entrySet()) {
            Row row = e.getValue();
            resort(row.getChildren());
        }

        filter(rootList);

        if (!readOnly) {
            engine.addElementAttributeListener(qualifier, listener);
            engine.addElementListener(qualifier, elementListener);
            engine.addQualifierListener(qualifierListener);
        }

        postInit();
    }

    private void filter(List<Row> rootList2) {
        for (Iterator<Row> iterator = rootList2.iterator(); iterator.hasNext(); ) {
            Row row = iterator.next();
            if (filter(row.getElement()))
                iterator.remove();
            filter(row.getChildren());
        }
    }

    protected boolean filter(Element element) {
        for (ElementLoadFilter filter : elementLoadFilters)
            if (!filter.load(element, this))
                return true;
        return false;
    }

    protected void postInit() {
    }

    public void commitUserTransaction() {
        if (engine instanceof Journaled) {
            ((Journaled) engine).commitUserTransaction();
        }
    }

    public void startUserTransaction() {
        if (engine instanceof Journaled) {
            ((Journaled) engine).startUserTransaction();
        }
    }

    private void resort(List<Row> list) {
        if (list.size() == 0)
            return;
        replace(list, 0, -1l);
        for (int i = 1; i < list.size(); i++) {
            replace(list, i, list.get(i - 1).getElementId());
        }
    }

    private void replace(List<Row> list, int pos, long prev) {
        for (int i = pos; i < list.size(); i++) {
            if (getPrev(list.get(i)) == prev) {
                Row row = list.remove(i);
                list.add(pos, row);
            }
        }
    }

    private long getPrev(Row row) {
        return row.getHierarchicalPersistent().getPreviousElementId();
    }

    private void addRow(List<Row> children, Row row) {
        children.add(row);
    }

    public Engine getEngine() {
        return engine;
    }

    public void close() {
        if (!readOnly) {
            engine.removeElementAttributeListener(qualifier, listener);
            engine.removeElementListener(qualifier, elementListener);
            engine.removeQualifierListener(qualifierListener);
        }
        rowHash.clear();
        root.children.clear();
        root = null;
    }

    public void refresh() {
        close();
        init();
    }

    public Row findRow(long elementId) {
        return rowHash.get(elementId);
    }

    public Row getRoot() {
        return root;
    }

    public Attribute getHAttribute() {
        return hAttribute;
    }

    public Row createRow(Row parent) {
        return createRow(parent, new ElementCreationCallback() {

            @Override
            public void created(Element element) {
            }
        });
    }

    public Row createRow(Row parent, ElementCreationCallback callback) {

        if (parent == null)
            parent = root;

        boolean started = isUserTransactionStarted();

        try {
            if (!started)
                startUserTransaction();
            Row row;
            if (parent == null)
                parent = root;
            Element element = engine.createElement(qualifier.getId());
            callback.created(element);
            synchronized (this) {
                row = rowHash.get(element.getId());
                if (row == null)
                    row = createNativeRow(element);
                row.setParent(parent);
            }

            for (RowChildListener listener : getRowChildListeners()) {
                listener.addedByThisRowSet(row);
            }

            return row;
        } finally {
            if (!started)
                commitUserTransaction();
        }
    }

    public Row createRow(Row parent, Element element) {

        if (parent == null)
            parent = root;

        boolean started = isUserTransactionStarted();

        try {
            if (!started)
                startUserTransaction();
            Row row;
            if (parent == null)
                parent = root;
            synchronized (this) {
                row = rowHash.get(element.getId());
                if (row == null)
                    row = createNativeRow(element);
                row.setParent(parent);
            }

            for (RowChildListener listener : getRowChildListeners()) {
                listener.addedByThisRowSet(row);
            }

            return row;
        } finally {
            if (!started)
                commitUserTransaction();
        }
    }

    private boolean isUserTransactionStarted() {
        if (engine instanceof Journaled) {
            return ((Journaled) engine).isUserTransactionStarted();
        }
        return false;
    }

    protected void attributeChanged(AttributeEvent event) {
        synchronized (STATIC_LOCK) {
            if (currentThread == Thread.currentThread())
                return;
        }

        if (event.getAttribute().equals(getHAttribute())) {
            HierarchicalPersistent old = (HierarchicalPersistent) event
                    .getOldValue();
            HierarchicalPersistent p = (HierarchicalPersistent) event
                    .getNewValue();
            Element e = event.getElement();
            Row row = findRow(e.getId());
            if (row != null) {
                if (old != null) {
                    Row parent = row.getParent();
                    if (parent != null) {
                        int index = parent.getChildren().indexOf(row);
                        parent.getChildren().remove(row);
                        removedFromChildren(parent, row, index);
                    }
                }
                if (p == null)
                    return;
                Row parent = findRow(p.getParentElementId());
                if (parent == null)
                    parent = root;
                List<Row> children = parent.getChildren();
                children.remove(row);

                row.setNativeParent(parent);
                int index = 0;
                int size = children.size();
                for (int i = 0; i < size; i++) {
                    Row row2 = children.get(i);
                    if (row2.getElementId() == p.getPreviousElementId()) {
                        index = i + 1;
                        break;
                    }
                }
                if (!filter(row.getElement())) {
                    children.add(index, row);
                    added(parent, row, index);
                }

            } else {
                System.err
                        .println("Warning! Unregister with current rowset row where changed. Element id: "
                                + e.getId() + ", Name: " + e.getName());
                return;
            }
        }
        for (int i = 0; i < attributesWithH.length; i++) {
            if (attributesWithH[i].equals(event.getAttribute())) {
                Row row = findRow(event.getElement().getId());
                if (row == null)
                    return;
                row.updateObject(i, event.getNewValue());
                attributeChanged(row, event.getAttribute(),
                        event.getNewValue(), event.getOldValue(),
                        event.isJournaled());
            }
        }
        if (event.getAttribute().getId() == qualifier.getAttributeForName()) {
            Row row = findRow(event.getElement().getId());
            row.updateElement();
        }
    }

    protected void attributeChanged(Row row, Attribute attribute,
                                    Object newValue, Object oldValue, boolean journaled) {
    }

    protected void added(Row parent, Row row, int index) {
        for (RowChildListener listener : getRowChildListeners())
            listener.added(parent, row, index);
    }

    protected void removedFromChildren(Row parent, Row row, int index) {

    }

    protected void elementCreated(ElementEvent event) {
        synchronized (this) {
            Element element = event.getNewElement();
            createNativeRow(element);
        }
        for (ElementListener l : getElementListeners()) {
            l.elementCreated(event);
        }
    }

    private Row createNativeRow(Element element) {
        Object[] objects = new Object[attributesWithH.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = engine.getAttribute(element, attributesWithH[i]);
        }
        if (rowHash.get(element.getId()) == null) {
            Row lastCreatedRow = rowCreater.createRow(element, this,
                    attributesWithH, objects);
            rowHash.put(element.getId(), lastCreatedRow);
            return lastCreatedRow;
        }
        return null;
    }

    protected void elementDeleted(ElementEvent event) {
        synchronized (STATIC_LOCK) {
            try {
                currentThread = Thread.currentThread();
                Row row = findRow(event.getOldElement().getId());
                if (row != null) {
                    rowHash.remove(row.getElementId());
                    removedFromChildren(row.getParent(), row, row.getParent()
                            .getIndex(row));
                    row.getParent().getChildren().remove(row);

                } else {
                    System.err
                            .println("Warning! Uregister with current rowset row where deleted. Element id: "
                                    + event.getOldElement().getId()
                                    + ", Name: "
                                    + event.getOldElement().getName());
                }
            } finally {
                currentThread = null;
            }
        }
    }

    public void deleteRow(Row row) {
        boolean start = isUserTransactionStarted();
        if (!start)
            startUserTransaction();
        int index = row.getParent().getIndex(row);
        index++;
        if (index < row.getParent().getChildCount()) {
            Row row2 = row.getParent().getChildAt(index);
            HierarchicalPersistent p = row2.getHierarchicalPersistent();
            HierarchicalPersistent persistent = row.getHierarchicalPersistent();
            if (persistent != null) {
                if (p == null)
                    p = new HierarchicalPersistent();
                p.setPreviousElementId(persistent.getPreviousElementId());
                row2.setAttribute(getHAttribute(), p);
            }
        }
        engine.deleteElement(row.getElementId());

        if (!start)
            commitUserTransaction();
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    public void addElementListener(ElementListener listener) {
        elementListenerList.add(ElementListener.class, listener);
    }

    public void removeElementListener(ElementListener listener) {
        elementListenerList.remove(ElementListener.class, listener);
    }

    public ElementListener[] getElementListeners() {
        return elementListenerList.getListeners(ElementListener.class);
    }

    public void addRowChildListener(RowChildListener listener) {
        elementListenerList.add(RowChildListener.class, listener);
    }

    public void removeRowChildListener(RowChildListener listener) {
        elementListenerList.remove(RowChildListener.class, listener);
    }

    public RowChildListener[] getRowChildListeners() {
        return elementListenerList.getListeners(RowChildListener.class);
    }

    public Qualifier getQualifier() {
        return qualifier;
    }

    public List<Row> getAllRows() {
        List<Row> res = new ArrayList<Row>();
        if (root != null) {
            addChildren(res, root.getChildren());
        }
        return res;
    }

    private void addChildren(List<Row> res, List<Row> children) {
        for (Row row : children) {
            res.add(row);
            addChildren(res, row.getChildren());
        }
    }

    public Hashtable<Long, Row> getRowHash() {
        return rowHash;
    }

    public int getAttributeIndex(Attribute attribute) {
        Integer res = attributeIndexes.get(attribute.getId());
        return (res == null) ? -1 : res.intValue();
    }

    public Attribute[] getAttributesWithH() {
        return attributesWithH;
    }

    public void sortByName() {
        getRoot().sortByName();
    }

    public void setRowQualifier(Row row, Qualifier qualifier) {
        RowSet rs = new RowSet(engine, qualifier, new Attribute[]{});
        List<Row> rows = rs.getAllRows();
        engine.setElementQualifier(row.getElementId(), qualifier.getId());
        HierarchicalPersistent hp = new HierarchicalPersistent();
        hp.setParentElementId(-1l);
        hp.setPreviousElementId(-1l);
        if (rows.size() > 0) {
            hp.setPreviousElementId(rows.get(rows.size() - 1).getElementId());
        }
        rs.close();
        Element element = row.getElement();
        element.setQualifierId(qualifier.getId());
        engine.setAttribute(element, getHAttribute(), hp);
    }

    public Row findRow(String string) {
        for (Row row : getAllRows())
            if (row.getName().equals(string))
                return row;
        return null;
    }
}
