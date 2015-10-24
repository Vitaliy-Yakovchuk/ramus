package com.ramussoft.report.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.ramussoft.common.Qualifier;
import com.ramussoft.report.data.plugin.Connection;

public class Rows extends ArrayList<Row> implements Cloneable {

    /**
     *
     */
    private static final long serialVersionUID = -8533818489494822944L;

    protected final Data data;

    private RowSet rowSet;

    protected int position = -1;

    protected Rows parent;

    private String defaultAttribute;

    private String qualifierName;

    public Rows(RowSet rowSet, Data data) {
        this(rowSet, data, true);
    }

    @SuppressWarnings("unchecked")
    public Rows(RowSet rowSet, Data data, boolean load, int initialCapacity) {
        super(initialCapacity);
        this.data = data;
        this.setRowSet(rowSet);
        if (load)
            addAll((Collection) rowSet.getAllRows());
    }

    @SuppressWarnings("unchecked")
    public Rows(RowSet rowSet, Data data, boolean load) {
        this.data = data;
        this.setRowSet(rowSet);
        if (load)
            addAll((Collection) rowSet.getAllRows());
    }

    public Row getRowById(long id) {
        return (Row) getRowSet().findRow(id);
    }

    public Row next() {
        position++;
        if (position < size())
            return get(position);
        else
            return null;
    }

    public void first() {
        position = -1;
    }

    public Row addRow(long elementId) {
        Row row = (Row) getRowSet().findRow(elementId);
        if (row != null)
            add(row);
        return row;
    }

    public Rows getConnection(String name) {
        RowSet set = getRowSet();
        Qualifier qualifier = null;
        if (set != null)
            qualifier = set.getQualifier();
        Connection connection = data.getConnection(this, qualifier, name);
        RowsSum rows = new RowsSum(data);
        Qualifier qualifier2 = null;
        if (qualifier != null) {
            qualifier2 = connection.getOpposite(data, qualifier);
            if (qualifier2 != null) {
                ((Rows) rows).setRowSet(data.getRowSet(qualifier2));
            }
        }
        for (Row row : this) {
            Rows rows2 = connection.getConnected(data, row);
            rows.addRows(rows2);
            if (qualifier2 == null)
                ((Rows) rows).setRowSet(rows2.getRowSet());
        }
        ((Rows) rows).parent = this;
        return rows;
    }

    public Rows getParent() {
        return parent;
    }

    public Row getCurrent() {
        if (position < 0)
            return null;
        if (position >= size())
            return null;
        return get(position);
    }

    public int getNumber() {
        return position + 1;
    }

    /**
     * @param defaultAttribute the defaultAttribute to set
     */
    public void setDefaultAttribute(String defaultAttribute) {
        this.defaultAttribute = defaultAttribute;
    }

    /**
     * @return the defaultAttribute
     */
    public String getDefaultAttribute() {
        return defaultAttribute;
    }

    public Object getCurrentAttribute() {
        return getCurrent().getAttribute(defaultAttribute, this);
    }

    public Object getAttribute(String attributeName) {
        return getCurrent().getAttribute(attributeName, this);
    }

    public Row getRow(long id) {
        return (Row) getRowSet().findRow(id);
    }

    public Row getRow(String name) {
        return (Row) getRowSet().findRow(
                data.getEngine()
                        .getElement(name, rowSet.getQualifier().getId())
                        .getId());
    }

    /**
     * @param rowSet the rowSet to set
     */
    public void setRowSet(RowSet rowSet) {
        this.rowSet = rowSet;
    }

    /**
     * @return the rowSet
     */
    public RowSet getRowSet() {
        return rowSet;
    }

    public void setParent(Rows parent) {
        this.parent = parent;
    }

    public Table asTable() {
        return toTable();
    }

    public Table toTable() {
        int columnCount = 0;
        Rows parent = this;
        while (parent != null) {
            columnCount++;
            parent = parent.parent;
        }

        int[] positions = new int[columnCount];

        parent = this;

        for (int i = columnCount - 1; i >= 0; i--) {
            positions[i] = parent.position;
            parent.position = -1;
            parent = parent.parent;
        }

        Table table = new Table(columnCount);

        while (this.next() != null) {
            parent = this;
            Row[] rows = new Row[columnCount];
            for (int i = columnCount - 1; i >= 0; i--) {
                rows[i] = parent.getCurrent();
                parent = parent.parent;
            }
            table.addRows(rows);
        }

        parent = this;

        for (int i = columnCount - 1; i >= 0; i--) {
            parent.position = positions[i];
            parent = parent.parent;
        }

        return table;
    }

    public int getRowCount() {
        return size();
    }

    public Row getRow(int row) {
        return get(row);
    }

    public int getSize() {
        return size();
    }

    public Rows getUniqueRows() {
        Rows rows = new Rows(rowSet, data, false);
        rows.parent = parent;
        for (Row row : this)
            if (rows.indexOf(row) < 0)
                rows.add(row);
        return rows;
    }

    /**
     * @param qualifierName the qualifierName to set
     */
    public void setQualifierName(String qualifierName) {
        this.qualifierName = qualifierName;
    }

    /**
     * @return the qualifierName
     */
    public String getQualifierName() {
        return qualifierName;
    }

    public Rows sort() {
        Rows res = (Rows) this.clone();
        Collections.sort(res);
        return res;
    }
}
