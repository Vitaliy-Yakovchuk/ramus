package com.ramussoft.report.data;

import java.util.HashMap;

import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Qualifier;
import com.ramussoft.database.StringCollator;
import com.ramussoft.report.XMLReportEngine;

public class Row extends com.ramussoft.database.common.Row implements
        Comparable<Row>, Cloneable {

    protected Data data;

    private HashMap<String, Object> nameAttributes;

    private String elementStatus;

    public Row(Element element, RowSet rowSet, Attribute[] attributes,
               Object[] objects, Data data) {
        super(element, rowSet, attributes, objects);
        this.data = data;
    }

    public RowSet getRowSet() {
        return (RowSet) rowSet;
    }

    public Object getAttribute(String name) {
        return getAttribute(name, null);
    }

    public Object getAttribute(String name, Rows rows) {
        if (nameAttributes != null) {
            Object value = nameAttributes.get(name);
            if (value != null)
                return value;
        }

        Object value = XMLReportEngine.getStaticAttribute(this, rows, name);
        if (value != null)
            return value;

        value = data.getAttribute(this, name);

        if (value != null)
            return value;

        return getAttribute(getRowSet().getAttribute(name));
    }

    public Rows getConnection(String name) {
        return data.getConnection(this, rowSet.getQualifier(), name);
    }

    public Data getData() {
        return data;
    }

    public Qualifier getQualifier() {
        return data.getQualifier(element.getQualifierId());
    }

    public void setValueForAttribute(String attributeName, Object value) {
        if (nameAttributes == null)
            nameAttributes = new HashMap<String, Object>();
        nameAttributes.put(attributeName, value);
    }

    public Rows getRowsByQuery(String query) {
        return data.getRowsByQuery(this, query);
    }

    @Override
    public int compareTo(Row o) {
        return StringCollator.compare(getName(), o.getName());
    }

    public int getLevel() {
        int res = 0;
        Row r = this;
        while ((r = (Row) r.getParent()) != null)
            res++;
        return res;
    }

    public List<String> getAttributeNames() {
        return ((RowSet) rowSet).getAttributeNames();
    }

    public Row createCopy() {
        try {
            return (Row) clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getElementStatus() {
        return elementStatus;
    }

    public void setElementStatus(String elementStatus) {
        this.elementStatus = elementStatus;
    }

    @Override
    public String getName() {
        String name = super.getName();
        String st = elementStatus;
        if (st != null) {
            int index = st.indexOf('|');
            if (index >= 0)
                st = st.substring(index + 1);
        }
        if (st != null && st.trim().length() > 0)
            return name + " (" + st + ")";
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((elementStatus == null) ? 0 : elementStatus.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Row other = (Row) obj;
        if (elementStatus == null) {
            if (other.elementStatus != null)
                return false;
        } else if (!elementStatus.equals(other.elementStatus))
            return false;
        return super.equals(obj);
    }

}
