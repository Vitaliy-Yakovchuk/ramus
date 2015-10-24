package com.ramussoft.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Qualifier - class which can be use to crate groups of elements.
 *
 * @author zdd
 */

public class Qualifier extends Unique implements Serializable, Cloneable {

    /**
     *
     */
    private static final long serialVersionUID = -5735939312490740901L;

    private String name = "";

    private boolean system = false;

    private List<Attribute> attributes = new ArrayList<Attribute>();

    private List<Attribute> systemAttributes = new ArrayList<Attribute>();

    private long attributeForName = -1;

    /**
     * Setter for the name of the qualifier.
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for the name of the qualifier.
     */

    public String getName() {
        return name;
    }

    /**
     * Setter for system attribute.
     */

    public void setSystem(boolean system) {
        this.system = system;
    }

    /**
     * Return <code>true</code> if the Qualifier is system. This can be used to
     * show or not qualifier in public list. For example this can be used for
     * reports qualifier.
     */

    public boolean isSystem() {
        return system;
    }

    /**
     * Setter for attribute list.
     */

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    /**
     * Getter for attribute list.
     */

    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * Setter for system attribute list.
     */

    public void setSystemAttributes(List<Attribute> systemAttributes) {
        this.systemAttributes = systemAttributes;
    }

    /**
     * Getter for system attribute list. System attributes is not shown as
     * default in tables, etc.
     */

    public List<Attribute> getSystemAttributes() {
        return systemAttributes;
    }

    /**
     * Setter for attributeForName.
     */

    public void setAttributeForName(long attributeForName) {
        this.attributeForName = attributeForName;
    }

    /**
     * Getter for attributeForName, this field is attribute id which will be
     * used in toString method for element.
     */

    public long getAttributeForName() {
        return attributeForName;
    }

    @Override
    public String toString() {
        return getName();
    }

    Qualifier createCopy() {
        try {
            return (Qualifier) clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public Qualifier createSaveCopy() {
        Qualifier res = createCopy();
        res.attributes = new ArrayList<Attribute>(attributes);
        res.systemAttributes = new ArrayList<Attribute>(systemAttributes);
        return res;
    }
}
