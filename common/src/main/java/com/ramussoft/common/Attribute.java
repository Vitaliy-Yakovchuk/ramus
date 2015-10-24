package com.ramussoft.common;

import java.io.Serializable;
import java.text.Collator;

public class Attribute extends Unique implements Serializable,
        Comparable<Attribute> {

    /**
     *
     */
    private static final long serialVersionUID = 4835718175328701631L;

    private static Collator collator = Collator.getInstance();

    private String name = "";

    private AttributeType attributeType;

    private boolean system;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Attribute o) {
        return collator.compare(getName(), o.getName());
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

}
