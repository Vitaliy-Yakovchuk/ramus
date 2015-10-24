package com.ramussoft.common.event;

import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;

public class ElementEvent extends Event {

    /**
     *
     */
    private static final long serialVersionUID = 4836354466277304703L;

    private Element oldElement;

    private Element newElement;

    private long qualifierId;

    public ElementEvent(Engine engine, Element oldElement, Element newElement,
                        long qualifierId) {
        this(engine, oldElement, newElement, qualifierId, false);
    }

    public ElementEvent(Engine engine, Element oldElement, Element newElement,
                        long qualifierId, boolean journaled) {
        super(engine, journaled);
        this.oldElement = oldElement;
        this.newElement = newElement;
        this.qualifierId = qualifierId;
    }

    public Element getOldElement() {
        return oldElement;
    }

    public Element getNewElement() {
        return newElement;
    }

    public long getQualifierId() {
        return qualifierId;
    }
}
