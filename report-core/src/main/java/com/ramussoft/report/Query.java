package com.ramussoft.report;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.ramussoft.common.Element;

public class Query implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4062552129475499280L;

    private Map<String, String> attributes;

    private List<Element> elements;

    public Query(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getAttribute(String atributeName) {
        return attributes.get(atributeName);
    }

    /**
     * @param elements the elements to set
     */
    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    /**
     * @return the elements
     */
    public List<Element> getElements() {
        return elements;
    }

    public void setAttributes(Hashtable<String, String> attributes) {
        this.attributes = attributes;
    }

    public void setAttribute(String attribute, String value) {
        if (this.attributes == null)
            this.attributes = new HashMap<String, String>();
        this.attributes.put(attribute, value);
    }
}
