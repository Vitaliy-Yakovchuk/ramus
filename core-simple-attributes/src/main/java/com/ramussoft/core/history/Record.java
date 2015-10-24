package com.ramussoft.core.history;

import java.io.Serializable;
import java.util.Date;

import com.ramussoft.common.Element;

public class Record implements Serializable, Comparable<Record> {

    /**
     *
     */
    private static final long serialVersionUID = -784020829346361794L;

    private Date date;

    private Object value;

    private Element element;

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

    @Override
    public int compareTo(Record o) {
        return date.compareTo(o.date);
    }
}
