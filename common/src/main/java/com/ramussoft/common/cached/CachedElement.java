package com.ramussoft.common.cached;

import com.ramussoft.common.Element;

public class CachedElement {

    public CachedElement(Element element, Object[] objects, CachedQualifier qualifier) {
        this.element = element;
        this.objects = objects;
        this.qualifier = qualifier;
    }

    Object[] objects;

    Element element;

    CachedQualifier qualifier;

}
