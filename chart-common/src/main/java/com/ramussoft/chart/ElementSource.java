package com.ramussoft.chart;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;

public class ElementSource extends AbstractSource implements XMLElement {

    ElementSource(ChartSource source) {
        super(source);
    }

    private static final String ELEMENT_ID = "element-id";

    private static final String SOURCE = "source";

    private Element element;

    public void setElement(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

    @Override
    public void save(ChartSaveXMLReader reader) throws SAXException {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", ELEMENT_ID, ELEMENT_ID, "CDATA", Long
                .toString(element.getId()));
        reader.startElement(SOURCE, attrs);
        reader.endElement(SOURCE);
    }

    @Override
    public void load(Node node, Engine engine) {
        NamedNodeMap attributes = node.getAttributes();
        element = engine.getElement(Long.parseLong(attributes.getNamedItem(
                ELEMENT_ID).getNodeValue()));
    }

}
