package com.ramussoft.common.event;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;

public class AttributeEvent extends Event {

    /**
     *
     */
    private static final long serialVersionUID = 6214953891279476522L;

    private Element element;

    private Attribute attribute;

    private Object oldValue;

    private Object newValue;

    public AttributeEvent(Engine engine, Element element, Attribute attribute,
                          Object oldValue, Object newValue) {
        this(engine, element, attribute, oldValue, newValue, false);
    }

    public AttributeEvent(Engine engine, Element element, Attribute attribute,
                          Object oldValue, Object newValue, boolean journaled) {
        super(engine, journaled);
        this.element = element;
        this.attribute = attribute;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Element getElement() {
        return element;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    @Override
    public String toString() {
        return "AttributeEvent [attribute=" + attribute + ", element="
                + element + ", newValue=" + newValue + ", oldValue=" + oldValue
                + "]";
    }

}
