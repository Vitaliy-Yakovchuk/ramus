package com.ramussoft.chart;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;

public class FilterSource extends AbstractSource {

    private static final String FILTER_VELUE = "filter-value";

    private String value;

    private Attribute attribute;

    FilterSource(ChartSource source) {
        super(source);
    }

    @Override
    public void load(Node node, Engine engine) {
        NamedNodeMap attributes = node.getAttributes();
        setAttribute(engine.getAttribute(Long.parseLong(attributes.getNamedItem(
                ATTRIBUTE_ID).getNodeValue())));
        setValue(attributes.getNamedItem(
                FILTER_VELUE).getNodeValue());
    }

    @Override
    public void save(ChartSaveXMLReader reader) throws SAXException {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", ATTRIBUTE_ID, ATTRIBUTE_ID, "CDATA", Long
                .toString(getAttribute().getId()));
        attrs.addAttribute("", FILTER_VELUE, FILTER_VELUE, "CDATA", getValue());
        reader.startElement(SOURCE, attrs);
        reader.endElement(SOURCE);
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param attribute the attribute to set
     */
    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    /**
     * @return the attribute
     */
    public Attribute getAttribute() {
        return attribute;
    }

}
