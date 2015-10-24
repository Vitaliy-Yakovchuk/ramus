package com.ramussoft.chart;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.ramussoft.common.Engine;

public interface XMLElement {

    void save(ChartSaveXMLReader reader) throws SAXException;

    void load(Node node, Engine engine);

}
