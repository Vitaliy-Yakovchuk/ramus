package com.ramussoft.common.persistent;

import com.ramussoft.common.attribute.Attribute;

/**
 * Abstract persistent, already contain fields to connect persistent object to
 * element.
 *
 * @author zdd
 */

public abstract class AbstractPersistent extends Persistent {

    /**
     *
     */
    private static final long serialVersionUID = 4203077636941360751L;

    private long attribute = -1;

    private long element = -1;

    public void setAttributeId(long attribute) {
        this.attribute = attribute;
    }

    @Attribute(id = 0, autoset = true, primary = true)
    public long getAttributeId() {
        return attribute;
    }

    public void setElementId(long element) {
        this.element = element;
    }

    @Element(id = 1, autoset = true, primary = true)
    public long getElementId() {
        return element;
    }

}
