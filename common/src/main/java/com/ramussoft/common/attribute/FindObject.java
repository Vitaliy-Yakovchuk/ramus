package com.ramussoft.common.attribute;

import java.io.Serializable;

public class FindObject implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5652445608843087856L;

    private String field;

    private Object object;

    public FindObject() {
    }

    public FindObject(String field, Object object) {
        this.field = field;
        this.object = object;
    }

    /**
     * @param field the field to set
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * @return the field
     */
    public String getField() {
        return field;
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
