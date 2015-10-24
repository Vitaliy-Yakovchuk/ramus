package com.ramussoft.eval;

import java.text.ParseException;
import java.util.Date;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;

public class EObject {

    protected Element element;

    protected Attribute attribute;

    protected Object value;

    protected Engine engine;

    private boolean isBoolean;

    public EObject(Object value) {
        if (value instanceof Boolean) {
            this.value = (Boolean) value ? 1l : 0l;
            this.isBoolean = true;
        } else
            this.value = value;
    }

    public EObject(Object value, Element element, Attribute attribute,
                   Engine engine) {
        this(value, element, attribute, engine, true);
    }

    public EObject(Object value, Element element, Attribute attribute,
                   Engine engine, boolean toUserValue) {
        this((toUserValue) ? engine.toUserValue(attribute, element, value)
                : value);
        this.element = element;
        this.attribute = attribute;
        this.engine = engine;
    }

    public long longValue() {
        if (value == null)
            return 0l;
        if (value instanceof Number)
            return ((Number) value).longValue();
        return new Double(doubleValue()).longValue();
    }

    public int intValue() {
        if (value == null)
            return 0;
        if (value instanceof Number)
            return ((Number) value).intValue();
        return new Double(doubleValue()).intValue();
    }

    public double doubleValue() {
        if (value == null)
            return 0d;
        if (value instanceof Number)
            return ((Number) value).doubleValue();
        if (value instanceof Boolean)
            return ((Boolean) value) ? 1d : 0d;
        try {
            return Eval.format.parse(stringValue()).doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0d;
        }
    }

    public String stringValue() {
        if (value instanceof String)
            return (String) value;
        if (value == null)
            return "";
        if (value instanceof Number)
            return Eval.format.format(value);
        if (value instanceof Date)
            return Eval.dateFormat.format(value);
        return value.toString();
    }

    public Date dateValue() {
        if (value instanceof Date)
            return (Date) value;
        if (value instanceof Number)
            return new Date(((Number) value).longValue());
        try {
            return Eval.dateFormat.parse(stringValue());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public Element getElement() {
        return element;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return stringValue();
    }

    public boolean isString() {
        if (value == null)
            return false;
        return value instanceof String;
    }

    public Engine getEngine() {
        return engine;
    }

    public boolean booleanValue() {
        if (value == null)
            return false;
        if (value instanceof String)
            return (((String) value).length() > 0);
        return doubleValue() != 0d;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isBoolean() {
        return isBoolean;
    }

    public Number numberValue() {
        if (value instanceof Number)
            return ((Number) value);
        return doubleValue();
    }
}
