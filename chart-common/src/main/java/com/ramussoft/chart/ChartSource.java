package com.ramussoft.chart;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.ramussoft.chart.event.ChartSourceEvent;
import com.ramussoft.chart.event.ChartSourceListener;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;

public class ChartSource implements XMLElement {

    private static final String CHART_SOURCE = "chart-source";

    private static final String CHART_TYPE = "chart-type";

    private static final String PREFERENCES = "preferences";

    private static final String PROPERTY = "property";

    private static final String KEY = "key";

    private static final String VALUE = "value";

    private Engine engine;

    protected List<QualifierSource> qualifierSources = new ArrayList<QualifierSource>();

    private String chartType;

    private Hashtable<String, String> preferences = new Hashtable<String, String>();

    private List<ChartSourceListener> listeners = new ArrayList<ChartSourceListener>(
            2);

    public ChartSource(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void save(ChartSaveXMLReader reader) throws SAXException {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", CHART_TYPE, CHART_TYPE, "CDATA", chartType);
        reader.startElement(CHART_SOURCE, attrs);
        for (QualifierSource source : qualifierSources)
            source.save(reader);
        reader.startElement(PREFERENCES);
        for (Entry<String, String> entry : preferences.entrySet()) {
            attrs = new AttributesImpl();
            attrs.addAttribute("", KEY, KEY, "CDATA", entry.getKey());
            attrs.addAttribute("", VALUE, VALUE, "CDATA", entry.getValue());
            reader.startElement(PROPERTY, attrs);
            reader.endElement(PROPERTY);
        }
        reader.endElement(PREFERENCES);
        reader.endElement(CHART_SOURCE);
    }

    public void save(OutputStream stream) {
        ChartSaveXMLReader reader = new ChartSaveXMLReader(this);
        Transformer t;
        try {
            t = TransformerFactory.newInstance().newTransformer();
            SAXSource xmlSource = new SAXSource(reader, null);
            t.transform(xmlSource, new StreamResult(stream));
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public void load(InputStream stream) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(stream);
            load(doc, engine);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void load(Node node, Engine engine) {
        preferences.clear();
        qualifierSources.clear();
        node = node.getChildNodes().item(0);
        chartType = node.getAttributes().getNamedItem(CHART_TYPE)
                .getNodeValue();
        for (Node child = node.getFirstChild(); child != null; child = child
                .getNextSibling())
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (child.getNodeName().equals(QualifierSource.QUALIFIER)) {
                    QualifierSource source = new QualifierSource(this);
                    qualifierSources.add(source);
                    source.load(child, engine);
                } else if (child.getNodeName().equals(PREFERENCES)) {
                    for (Node p = child.getFirstChild(); p != null; p = p
                            .getNextSibling()) {
                        NamedNodeMap attributes = p.getAttributes();
                        String key = attributes.getNamedItem(KEY)
                                .getNodeValue();
                        String value = attributes.getNamedItem(VALUE)
                                .getNodeValue();
                        preferences.put(key, value);
                    }
                }
            }
    }

    public List<QualifierSource> getQualifierSources() {
        return qualifierSources;
    }

    /**
     * @param chartType the chartType to set
     */
    public void setChartType(String chartType) {
        this.chartType = chartType;
    }

    /**
     * @return the chartType
     */
    public String getChartType() {
        return chartType;
    }

    public List<Attribute> getAttributes() {
        List<Attribute> res = new ArrayList<Attribute>();
        for (QualifierSource source : qualifierSources) {
            for (AttributeSource attributeSource : source.getAttributeSources()) {
                Attribute attribute = attributeSource.getAttribute();
                if (res.indexOf(attribute) < 0)
                    res.add(attribute);
            }
        }
        return res;
    }

    public String getProperty(String key) {
        return preferences.get(key);
    }

    public String setProperty(String key, String value) {
        if (value == null)
            return preferences.remove(key);
        return preferences.put(key, value);
    }

    public String removeProperty(String key) {
        return preferences.remove(key);
    }

    public void setProperty(String key, long l) {
        setProperty(key, Long.toString(l));
    }

    public void setAttributeProperty(String key, Attribute attribute) {
        if (attribute == null)
            setProperty(key, null);
        else
            setProperty(key, attribute.getId());
    }

    public Long getLongProperty(String key) {
        String value = getProperty(key);
        if (value == null)
            return null;
        return new Long(value);
    }

    public Attribute getAttributeProperty(String key) {
        Long id = getLongProperty(key);
        if (id == null)
            return null;
        return engine.getAttribute(id);
    }

    public void addChartSourceListener(ChartSourceListener listener) {
        listeners.add(listener);
    }

    public void removeChartSourceListener(ChartSourceListener listener) {
        listeners.remove(listener);
    }

    public ChartSourceListener[] getChartSourceListeners() {
        return listeners.toArray(new ChartSourceListener[listeners.size()]);
    }

    public QualifierSource createQualifierSource() {
        return new QualifierSource(this);
    }

    public AttributeSource createAttributeSource() {
        return new AttributeSource(this);
    }

    public ElementSource createElementSource() {
        return new ElementSource(this);
    }

    public FilterSource createFilterSource() {
        return new FilterSource(this);
    }

    public void fireAttributeListChanged() {
        ChartSourceEvent event = new ChartSourceEvent();
        for (ChartSourceListener l : listeners)
            l.attributeListChanged(event);
    }

    public List<Element> getElements() {
        List<Element> res = new ArrayList<Element>();
        for (QualifierSource source : qualifierSources) {
            if (QualifierSource.ELEMENTS_LOAD_TYPE_ALL.equals(source
                    .getElementsLoadType())) {
                FilterSource filterSource = source.getFilterSources().get(0);
                Qualifier qualifier = source.getQualifier();
                if (StandardAttributesPlugin.isTableQualifier(qualifier)
                        && filterSource.getAttribute().equals(
                        StandardAttributesPlugin
                                .getTableElementIdAttribute(engine))) {
                    res.addAll(StandardAttributesPlugin
                            .getOrderedTableElements(engine,
                                    StandardAttributesPlugin
                                            .getAttributeForTable(engine,
                                                    qualifier), engine
                                            .getElement(Long
                                                    .parseLong(filterSource
                                                            .getValue()))));
                } else
                    res.addAll(engine.findElements(qualifier.getId(),
                            filterSource.getAttribute(),
                            Long.parseLong(filterSource.getValue())));
            } else
                for (ElementSource elementSource : source.getElementSources())
                    res.add(elementSource.getElement());
        }
        return res;
    }

    public void load(Element element) {
        String path = ChartDataFramework.getPreferencesPath(element,
                StandardAttributesPlugin.getAttributeNameAttribute(engine));
        load(engine.getInputStream(path));
    }

    public List<Attribute> getPropertyAttributes(String attributeValuePrefix) {
        List<Attribute> result = new ArrayList<Attribute>();
        Set<String> keySet = preferences.keySet();
        for (String key : keySet) {
            if (key.startsWith(attributeValuePrefix)) {
                result.add(getAttributeProperty(key));
            }
        }
        return result;
    }

    public void setPropertyAttributes(String attributeValuePrefix,
                                      List<Attribute> attributes) {
        Set<String> keySet2 = preferences.keySet();
        String[] keySet = keySet2.toArray(new String[keySet2.size()]);
        for (String key : keySet)
            if (key.startsWith(attributeValuePrefix))
                removeProperty(key);
        for (int i = 0; i < attributes.size(); i++)
            setAttributeProperty(attributeValuePrefix + i, attributes.get(i));
    }
}
