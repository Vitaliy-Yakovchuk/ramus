package com.ramussoft.core.attribute.simple;

import java.util.ArrayList;
import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;

public class TableCache {

    private List<Row> data = new ArrayList<Row>();

    private Qualifier qualifier;

    private Engine engine;

    private Attribute attribute;

    private Element element;

    public TableCache(Engine engine, Attribute attribute, Element element) {
        this.engine = engine;
        this.attribute = attribute;
        this.element = element;
        qualifier = engine.getSystemQualifier(StandardAttributesPlugin
                .getTableQualifeirName(attribute));
        for (Element e : StandardAttributesPlugin.getTableElements(engine,
                attribute, element)) {
            Object[] objects = new Object[qualifier.getAttributes().size()];
            int i = 0;
            for (Attribute a : qualifier.getAttributes())
                if (a.getAttributeType().isLight()) {
                    objects[i] = engine.getAttribute(e, a);
                    i++;
                }
            data.add(new Row(e, objects));
        }
    }

    public void commit() {
        for (Element element : StandardAttributesPlugin.getTableElements(
                engine, attribute, this.element)) {
            boolean remove = true;
            for (Row row : data) {
                if ((row.element != null) && (row.element.equals(element))) {
                    remove = false;
                    break;
                }
            }
            if (remove)
                engine.deleteElement(element.getId());
        }
        for (Row row : data) {
            if (row.element == null) {
                row.element = engine.createElement(qualifier.getId());
                engine.setAttribute(row.element, StandardAttributesPlugin
                        .getTableElementIdAttribute(engine), element.getId());
            }
            int i = 0;
            for (Attribute a : qualifier.getAttributes())
                if (a.getAttributeType().isLight()) {
                    engine.setAttribute(row.element, a, row.objects[i]);
                    i++;
                }
        }
    }

    public class Row {

        public Row(Element element, Object[] objects) {
            this.element = element;
            this.objects = objects;
        }

        public Element element;

        public Object[] objects;
    }

    public List<Row> getRows() {
        return data;
    }

    public Row createRow() {
        Row row = new Row(null, new Object[qualifier.getAttributes().size()]);
        data.add(row);
        return row;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public Element getElement() {
        return element;
    }

    public Qualifier getQualifier() {
        return qualifier;
    }

    public Engine getEngine() {
        return engine;
    }
}
