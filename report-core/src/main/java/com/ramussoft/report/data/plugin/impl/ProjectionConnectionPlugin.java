package com.ramussoft.report.data.plugin.impl;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.simple.ElementListPersistent;
import com.ramussoft.core.attribute.simple.ElementListPropertyPersistent;
import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Row;
import com.ramussoft.report.data.Rows;
import com.ramussoft.report.data.plugin.AbstractConnection;
import com.ramussoft.report.data.plugin.AbstractConnectionPlugin;
import com.ramussoft.report.data.plugin.Connection;

public class ProjectionConnectionPlugin extends AbstractConnectionPlugin {

    @SuppressWarnings("unchecked")
    @Override
    public Connection getConnection(Data data, Qualifier qualifier, String name) {
        Hashtable<String, EConnection> hash = (Hashtable<String, EConnection>) data
                .get("Projections.Hash");
        List<String> nulls = (List<String>) data.get("Projections.Nulls");
        if (hash == null) {
            hash = new Hashtable<String, EConnection>();
            data.put("Projections.Hash", hash);
        }

        if (nulls == null) {
            nulls = new Vector<String>();
            data.put("Projections.Nulls", nulls);
        }

        EConnection connection = hash.get(name);
        if (connection == null) {
            if (nulls.indexOf(name) >= 0)
                return null;
            for (Attribute attribute : data.getAttributes()) {
                if ((attribute.getName().equals(name) && (attribute
                        .getAttributeType().toString()
                        .equals("Core.ElementList")))) {
                    ElementListPropertyPersistent property = (ElementListPropertyPersistent) data
                            .getEngine().getAttribute(null, attribute);
                    if (property != null) {
                        connection = new EConnection(attribute, property);
                    }
                }
            }
        }
        if (connection == null)
            nulls.add(name);
        else
            hash.put(name, connection);
        return connection;
    }

    private class EConnection extends AbstractConnection {

        Attribute attribute;

        ElementListPropertyPersistent property;

        public EConnection(Attribute attribute,
                           ElementListPropertyPersistent property) {
            this.attribute = attribute;
            this.property = property;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Rows getConnected(Data data, Row row) {
            List<ElementListPersistent> list = (List<ElementListPersistent>) data
                    .getEngine().getAttribute(row.getElement(), attribute);
            Qualifier qualifier = data.getQualifier(row.getElement()
                    .getQualifierId());
            Qualifier opposite;
            Rows result;
            if (property.getQualifier1() == qualifier.getId()) {
                opposite = data.getQualifier(property.getQualifier2());
                result = new Rows(data.getRowSet(opposite), data, false);
                for (ElementListPersistent persistent : list) {
                    result.addRow(persistent.getElement2Id());
                }
            } else if (property.getQualifier2() == qualifier.getId()) {
                opposite = data.getQualifier(property.getQualifier1());
                result = new Rows(data.getRowSet(opposite), data, false);
                for (ElementListPersistent persistent : list) {
                    result.addRow(persistent.getElement1Id());
                }
            } else
                return new Rows(null, data, false, 0);
            return result;
        }

        @Override
        public Qualifier getOpposite(Data data, Qualifier qualifier) {
            if (qualifier.getId() == property.getQualifier1())
                return data.getEngine().getQualifier(property.getQualifier2());
            return data.getEngine().getQualifier(property.getQualifier1());
        }

    }

    ;

}
