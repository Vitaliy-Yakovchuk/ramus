package com.ramussoft.chart;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;

public class AttributeSource extends AbstractSource implements XMLElement {

    AttributeSource(ChartSource source) {
        super(source);
    }

    private Attribute attribute;

    @Override
    public void load(Node node, Engine engine) {
        NamedNodeMap attributes = node.getAttributes();
        attribute = engine.getAttribute(Long.parseLong(attributes.getNamedItem(
                ATTRIBUTE_ID).getNodeValue()));
    }

    @Override
    public void save(ChartSaveXMLReader reader) throws SAXException {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", ATTRIBUTE_ID, ATTRIBUTE_ID, "CDATA", Long
                .toString(attribute.getId()));
        reader.startElement(SOURCE, attrs);
        reader.endElement(SOURCE);
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
