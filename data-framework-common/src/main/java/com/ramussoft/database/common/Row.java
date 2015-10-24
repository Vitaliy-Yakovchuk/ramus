package com.ramussoft.database.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.TreeNode;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.core.attribute.simple.HierarchicalPersistent;
import com.ramussoft.database.StringCollator;

public class Row implements TreeNode {

    protected Element element;

    protected Engine engine;

    private Attribute[] attributes;

    private Object[] objects;

    protected RowSet rowSet;

    protected ArrayList<Row> children = new ArrayList<Row>();

    protected Row parent;

    public Row(Element element, RowSet data, Attribute[] attributes,
               Object[] objects) {
        this.element = element;
        this.engine = data.getEngine();
        this.attributes = attributes;
        this.rowSet = data;
        this.objects = objects;
    }

    public Row() {
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        if (element == null) {
            return "No element in this row";
        }

        long id = rowSet.getQualifier().getAttributeForName();
        for (Attribute attr : attributes) {
            if (attr.getId() == id) {
                Object name = getAttribute(attr);
                if (name != null)
                    return name.toString();
            }
        }
        return element.getName();
    }

    public Object getNameObject() {
        if (element == null) {
            return "No element in this row";
        }

        long id = rowSet.getQualifier().getAttributeForName();
        for (Attribute attr : attributes) {
            if (attr.getId() == id) {
                return getAttribute(attr);
            }
        }
        Attribute attribute = engine.getAttribute(id);
        if (attribute != null)
            return engine.getAttribute(element, attribute);
        return element.getName();
    }

    public void setNameObject(Object name) {
        if (element == null) {
            return;
        }

        long id = rowSet.getQualifier().getAttributeForName();
        for (Attribute attr : attributes) {
            if (attr.getId() == id) {
                setAttribute(attr, name);
                return;
            }
        }
        Attribute attribute = engine.getAttribute(id);
        if (attribute != null)
            engine.setAttribute(element, attribute, name);
    }

    public Attribute[] getRowAttributes() {
        return attributes;
    }

    public int getAttributeIndex(Attribute attribute) {
        return rowSet.getAttributeIndex(attribute);
    }

    public Object getAttribute(Attribute attribute) {
        if (!attribute.getAttributeType().isLight()) {
            // System.err
            // .println("WARNING: Trying to get not light attribute from row set.");
            return engine.getAttribute(element, attribute);
        }
        int i = getAttributeIndex(attribute);
        if (i < 0)
            return engine.getAttribute(element, attribute);
        return objects[i];
    }

    public void setAttribute(Attribute attribute, Object object) {
        int index = getAttributeIndex(attribute);
        if (index >= 0) {
            objects[index] = object;
        }
        engine.setAttribute(element, attribute, object);
    }

    /**
     * @return the children
     */
    public List<Row> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<Row> children) {
        this.children = children;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Row) {
            if (element == null)
                return super.equals(obj);
            return element.equals(((Row) obj).element);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return element.hashCode();
    }

    public Object[] getObjects() {
        return objects;
    }

    public long getElementId() {
        if (element == null)
            return -1l;
        return element.getId();
    }

    public void setNativeParent(Row parent) {
        this.parent = parent;
    }

    public Row getParent() {
        return parent;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration children() {
        return new Vector<Row>(children).elements();
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public Row getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    @Override
    public boolean isLeaf() {
        return getChildCount() == 0;
    }

    public int getId() {
        if (parent == null)
            return 0;
        return parent.getIndex(this) + 1;
    }

    public String getCode() {
        return getNativeCode();
    }

    public String getNativeCode() {
        if (parent == null) {
            return "";
        }
        String prefic = parent.getCode();
        if (prefic.equals(""))
            return Integer.toString(getId());
        return prefic + "." + getId();
    }

    public HierarchicalPersistent getHierarchicalPersistent() {
        return (HierarchicalPersistent) objects[0];
    }

    public void setNativeHierarchicalPersistent(HierarchicalPersistent hp) {
        objects[0] = hp;
    }

    public void setHierarchicalPersistent(HierarchicalPersistent hp) {
        setAttribute(rowSet.getHAttribute(), hp);
    }

    public Object getAttribute(int index) {
        return getAttribute(attributes[index + 1]);
    }

    public void setAttribute(int index, Object object) {
        setAttribute(attributes[index + 1], object);
    }

    public Element getElement() {
        return element;
    }

    public Engine getEngine() {
        return engine;
    }

    public RowSet getRowSet() {
        return rowSet;
    }

    public void startUserTransaction() {
        rowSet.startUserTransaction();
    }

    public void endUserTransaction() {
        rowSet.commitUserTransaction();
    }

    public void updateElement() {
        Element element2 = engine.getElement(element.getId());
        if (element2 != null)
            this.element = element2;
    }

    public void setName(String value) {
        Attribute attribute = engine.getAttribute(rowSet.getQualifier()
                .getAttributeForName());
        if ((attribute != null)
                && (attribute.getAttributeType().getTypeName().equals("Text"))
                && (attribute.getAttributeType().getPluginName().equals("Core"))) {
            engine.setAttribute(element, attribute, value);
        }
    }

    public boolean canAddChild() {
        return true;
    }

    public void updateObject(int i, Object newValue) {
        objects[i] = newValue;
    }

    public void setParent(Row parent) {
        HierarchicalPersistent hp = getHierarchicalPersistent();
        if (hp == null)
            hp = new HierarchicalPersistent();
        hp.setParentElementId(parent.getElementId());
        hp.setPreviousElementId(-1l);
        if (parent.getChildCount() > 0) {
            hp.setPreviousElementId(parent.getChildAt(
                    parent.getChildCount() - 1).getElementId());
        }
        parent.children.add(this);
        this.setHierarchicalPersistent(hp);
    }

    public void sortByName() {
        Row[] rows = children.toArray(new Row[children.size()]);
        Arrays.sort(rows, new Comparator<Row>() {

            @Override
            public int compare(Row o1, Row o2) {
                return StringCollator.compare(o1.getName(), o2.getName());
            }
        });
        Row prev = null;
        for (Row row : rows) {
            HierarchicalPersistent hp = new HierarchicalPersistent(
                    row.getHierarchicalPersistent());
            if (prev == null)
                hp.setPreviousElementId(-1l);
            else
                hp.setPreviousElementId(prev.getElementId());
            if (!row.getHierarchicalPersistent().equals(hp))
                row.setHierarchicalPersistent(hp);
            prev = row;
        }
        for (Row row : rows)
            if (row.getChildCount() > 0)
                row.sortByName();
    }
}
