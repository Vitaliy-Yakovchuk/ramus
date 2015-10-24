package com.ramussoft.chart;

import java.util.ArrayList;

import java.util.List;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;

public class QualifierSource extends AbstractSource implements XMLElement {

    QualifierSource(ChartSource source) {
        super(source);
    }

    private static final String ELEMENTS = "elements";

    private static final String ATTRIBUTES = "attributes";

    private static final String QUALIFIER_ID = "qualifier-id";

    private static final String FILTERS = "filters";

    private static final String ELEMENTS_LOAD_TYPE = "elements-load-type";

    public static final String ELEMENTS_LOAD_TYPE_ALL = "all";

    public static final String ELEMENTS_LOAD_TYPE_SELECTED = "selected";

    static final String QUALIFIER = "qualifier";

    private Qualifier qualifier;

    private List<ElementSource> elementSources = new ArrayList<ElementSource>();

    private List<AttributeSource> attributeSources = new ArrayList<AttributeSource>();

    private List<FilterSource> filterSources = new ArrayList<FilterSource>();

    private String elementsLoadType = ELEMENTS_LOAD_TYPE_SELECTED;

    public void setQualifier(Qualifier qualifier) {
        this.qualifier = qualifier;
    }

    public Qualifier getQualifier() {
        return qualifier;
    }

    public List<ElementSource> getElementSources() {
        return elementSources;
    }

    @Override
    public void save(ChartSaveXMLReader reader) throws SAXException {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", QUALIFIER_ID, QUALIFIER_ID, "CDATA", Long
                .toString(qualifier.getId()));
        attrs.addAttribute("", ELEMENTS_LOAD_TYPE, ELEMENTS_LOAD_TYPE, "CDATA",
                elementsLoadType);
        reader.startElement(QUALIFIER, attrs);
        reader.startElement(ELEMENTS);
        for (ElementSource source : elementSources)
            source.save(reader);
        reader.endElement(ELEMENTS);
        reader.startElement(ATTRIBUTES);
        for (AttributeSource source : attributeSources)
            source.save(reader);
        reader.endElement(ATTRIBUTES);
        reader.startElement(FILTERS);
        for (FilterSource source : filterSources)
            source.save(reader);
        reader.endElement(FILTERS);
        reader.endElement(QUALIFIER);
    }

    @Override
    public void load(Node node, Engine engine) {
        elementSources.clear();
        attributeSources.clear();
        filterSources.clear();
        Long id = new Long(node.getAttributes().getNamedItem(QUALIFIER_ID)
                .getNodeValue());

        Node loadType = node.getAttributes().getNamedItem(ELEMENTS_LOAD_TYPE);
        if (loadType != null) {
            elementsLoadType = loadType.getNodeValue();
        }

        qualifier = engine.getQualifier(id.longValue());
        for (Node sChild = node.getFirstChild(); sChild != null; sChild = sChild
                .getNextSibling())
            if (sChild.getNodeType() == Node.ELEMENT_NODE) {
                if (sChild.getNodeName().equals(ELEMENTS)) {
                    for (Node child = sChild.getFirstChild(); child != null; child = child
                            .getNextSibling())
                        if (child.getNodeType() == Node.ELEMENT_NODE) {
                            ElementSource source = new ElementSource(
                                    chartSource);
                            elementSources.add(source);
                            source.load(child, engine);
                        }
                } else if (sChild.getNodeName().equals(ATTRIBUTES)) {
                    for (Node child = sChild.getFirstChild(); child != null; child = child
                            .getNextSibling())
                        if (child.getNodeType() == Node.ELEMENT_NODE) {
                            AttributeSource source = new AttributeSource(
                                    chartSource);
                            attributeSources.add(source);
                            source.load(child, engine);
                        }
                } else if (sChild.getNodeName().equals(FILTERS)) {
                    for (Node child = sChild.getFirstChild(); child != null; child = child
                            .getNextSibling())
                        if (child.getNodeType() == Node.ELEMENT_NODE) {
                            FilterSource source = new FilterSource(chartSource);
                            filterSources.add(source);
                            source.load(child, engine);
                        }
                }
            }
    }

    public List<AttributeSource> getAttributeSources() {
        return attributeSources;
    }

    /**
     * @param elementsLoadType the elementsLoadType to set
     */
    public void setElementsLoadType(String elementsLoadType) {
        this.elementsLoadType = elementsLoadType;
    }

    /**
     * @return the elementsLoadType
     */
    public String getElementsLoadType() {
        return elementsLoadType;
    }

    public List<FilterSource> getFilterSources() {
        return filterSources;
    }
}
