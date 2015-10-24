package com.ramussoft.excel;

import com.ramussoft.common.Attribute;

public class ImportRule {

    private Attribute attribute;

    private Attribute tableAttribute;

    private int column;

    private Object object;

    public ImportRule(Attribute attribute, Attribute tableAttribute, int column) {
        this.attribute = attribute;
        this.tableAttribute = tableAttribute;
        this.column = column;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public Attribute getTableAttribute() {
        return tableAttribute;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    /**
     * @param object the object to set
     */
    public void setObject(Object object) {
        this.object = object;
    }

    /**
     * @return the object
     */
    public Object getObject() {
        return object;
    }
}
