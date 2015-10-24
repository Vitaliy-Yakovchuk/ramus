package com.ramussoft.report.data;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;

public class RowSet extends com.ramussoft.database.common.RowSet {

    private Hashtable<String, Attribute> attrsByName = new Hashtable<String, Attribute>();

    private Hashtable<Object, Object> properties = new Hashtable<Object, Object>();

    public RowSet(Engine engine, Qualifier qualifier, final Data data) {
        this(engine, qualifier, data, new RowCreater() {

            @Override
            public com.ramussoft.database.common.Row createRow(Element element,
                                                               com.ramussoft.database.common.RowSet rowSet,
                                                               Attribute[] attributes, Object[] objects) {
                return new Row(element, (RowSet) rowSet, attributes, objects,
                        data);
            }
        });
    }

    public RowSet(Engine engine, Qualifier qualifier, final Data data,
                  RowCreater rowCreater) {
        super(engine, qualifier, getWithNameAttribute(qualifier), rowCreater,
                true);
        for (Attribute attribute : qualifier.getAttributes()) {
            attrsByName.put(attribute.getName(), attribute);
        }
    }

    private static Attribute[] getWithNameAttribute(Qualifier qualifier) {
        Attribute attribute = null;
        for (Attribute attribute2 : qualifier.getAttributes())
            if (attribute2.getId() == qualifier.getAttributeForName())
                attribute = attribute2;
        if (attribute == null)
            return new Attribute[]{};
        return new Attribute[]{attribute};
    }

    public Attribute getAttribute(String name) {
        Attribute attribute = attrsByName.get(name);
        if (attribute == null)
            throw new DataException("Error.attributeNotFound", "Attribute "
                    + name + " not found", name);
        return attribute;
    }

    public void setProperty(Object property, Object value) {
        properties.put(property, value);
    }

    public Object getProperty(Object property) {
        return properties.get(property);
    }

    public void removeProperty(Object property) {
        properties.remove(property);
    }

    public List<String> getAttributeNames() {
        List<Attribute> attributes = getQualifier().getAttributes();
        List<String> result = new ArrayList<String>(attributes.size());
        for (Attribute attribute : attributes)
            result.add(attribute.getName());
        return result;
    }

    @Override
    public com.ramussoft.database.common.Row findRow(long elementId) {
        Row row = (Row) super.findRow(elementId);
        if (row != null)
            return row.createCopy();
        return row;
    }

    @Override
    public com.ramussoft.database.common.Row findRow(String string) {
        Row row = (Row) super.findRow(string);
        if (row != null)
            return row.createCopy();
        return row;
    }

}
