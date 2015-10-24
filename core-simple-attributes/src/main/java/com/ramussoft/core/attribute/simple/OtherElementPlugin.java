package com.ramussoft.core.attribute.simple;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.DeleteStatus;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.Unique;
import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AbstractAttributeConverter;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.attribute.EngineParalleler;
import com.ramussoft.common.attribute.FindObject;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.core.impl.IEngineImpl;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.jdbc.RowMapper;

public class OtherElementPlugin extends AbstractAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new AbstractAttributeConverter() {
            public Object toObject(java.util.List<Persistent>[] persistents,
                                   long elementId, long attributeId, IEngine engine) {
                if (elementId < 0) {
                    return toPropertyObject(persistents);
                } else {
                    List<Persistent> list = persistents[0];
                    if (list.size() == 0)
                        return null;
                    return ((OtherElementPersistent) list.get(0))
                            .getOtherElement();
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Persistent>[] toPersistens(Object object,
                                                   long elementId, long attributeId, IEngine engine) {
                if (elementId < 0) {
                    List<Persistent> list = new ArrayList<Persistent>(1);
                    list.add((OtherElementPropertyPersistent) object);
                    return new List[]{list};
                } else {
                    List<Persistent> list = new ArrayList<Persistent>(1);
                    if (object != null) {
                        list.add(new OtherElementPersistent((Long) object));
                    }
                    return new List[]{list};
                }
            }

            @Override
            public FindObject[] getFindObjects(Object object) {
                if (object == null)
                    return null;
                return new FindObject[]{new FindObject("other_element",
                        object)};
            }

        };
    }

    @Override
    public String getTypeName() {
        return "OtherElement";
    }

    @Override
    public boolean isComparable() {
        return false;
    }

    @Override
    public boolean isSystem() {
        return false;
    }

    @Override
    public String getName() {
        return "Core";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{OtherElementPersistent.class};
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePropertyPersistents() {
        return new Class[]{OtherElementPropertyPersistent.class};
    }

    private OtherElementPropertyPersistent toPropertyObject(
            java.util.List<Persistent>[] persistents) {
        if (persistents[0].size() == 0) {
            return new OtherElementPropertyPersistent();
        } else {
            return (OtherElementPropertyPersistent) persistents[0].get(0);
        }
    }

    ;

    @Override
    public void copyAttribute(Engine sourceEngine, Engine destinationEngine,
                              Attribute sourceAttribute, Attribute destinationAttribute,
                              Element sourceElement, Element destinationElement,
                              EngineParalleler paralleler) {
        if (sourceElement == null) {
            OtherElementPropertyPersistent s = (OtherElementPropertyPersistent) sourceEngine
                    .getAttribute(null, sourceAttribute);
            if (s != null) {
                OtherElementPropertyPersistent otherElementProperty = new OtherElementPropertyPersistent();
                otherElementProperty.setQualifier(getId(paralleler
                        .getQualifier(s.getQualifier())));
                otherElementProperty.setQualifierAttribute(getId(paralleler
                        .getAttribute(s.getQualifierAttribute())));
                destinationEngine.setAttribute(null, destinationAttribute,
                        otherElementProperty);

            }
        } else {
            Long l = (Long) sourceEngine.getAttribute(sourceElement,
                    sourceAttribute);
            if (l != null) {
                destinationEngine.setAttribute(destinationElement,
                        destinationAttribute, getId(paralleler.getElement(l)));
            }
        }
    }

    private long getId(Unique unique) {
        if (unique == null)
            return -1l;
        return unique.getId();
    }

    @SuppressWarnings("unchecked")
    @Override
    public DeleteStatus getElementsDeleteStatus(long[] elementIds,
                                                IEngine aEngine) {
        if (!(aEngine instanceof IEngineImpl))
            return super.getElementsDeleteStatus(elementIds, aEngine);
        IEngineImpl engine = (IEngineImpl) aEngine;
        JDBCTemplate template = engine.getTemplate();
        String prefix = engine.getPrefix();
        String sql = "SELECT element_name, qualifier_name, attribute_name "
                + "FROM {0}attribute_other_elements a, {0}attributes b, {0}qualifiers c, {0}elements d "
                + "WHERE other_element in("
                + JDBCTemplate.toSqlArray(elementIds)
                + ") AND a.attribute_id=b.attribute_id AND d.element_id=a.element_id "
                + "AND d.qualifier_id=c.qualifier_id AND b.attribute_system=false "
                + "ORDER BY qualifier_name, attribute_name, element_name";
        List<String> list = template.query(MessageFormat.format(sql, prefix),
                new RowMapper() {
                    @Override
                    public Object mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        String qName = rs.getString(2);
                        if (qName.startsWith("TableQualifier_"))
                            qName = "{AttributeType.Core.Table}";

                        return "<tr><td>" + rs.getString(1) + "</td><td> "
                                + qName + "</td><td> " + rs.getString(3)
                                + "</td></tr>";
                    }
                });
        if (list.size() == 0)
            return null;

        DeleteStatus status = new DeleteStatus();
        status.setPluginName("Core");
        StringBuffer sb = new StringBuffer();
        sb.append("<br>");
        sb.append("<table>");
        sb
                .append("<tr><td><b>{AttributeType.Core.OtherElement}</b></td><td><b>{OtherElement.Qualifier}</b></td><td><b>{OtherElement.Attribute}</b></td></tr>");
        for (String s : list) {
            sb.append(s);
        }
        sb.append("</table>");
        status.setPluginAnswer("{Warning.ElementsUsedAtOtherElements}"
                + sb.toString());

        return status;
    }

    @Override
    public void replaceElements(Engine engine, Element[] oldElements,
                                Element newElement) {

        for (Qualifier qualifier : getAllNotSystemQualifiers(engine)) {
            List<Attribute> attributes = new ArrayList<Attribute>();
            for (Attribute attribute : qualifier.getAttributes()) {
                if ((attribute.getAttributeType().getPluginName()
                        .equals(getName()))
                        && (attribute.getAttributeType().getTypeName()
                        .equals(getTypeName()))) {
                    OtherElementPropertyPersistent p = (OtherElementPropertyPersistent) engine
                            .getAttribute(null, attribute);
                    if (p.getQualifier() == newElement.getQualifierId())
                        attributes.add(attribute);
                }
            }
            Hashtable<Element, Object[]> hash = engine.getElements(qualifier,
                    attributes);
            Enumeration<Element> keys = hash.keys();
            while (keys.hasMoreElements()) {
                Element key = keys.nextElement();
                Object[] objects = hash.get(key);
                int aIndex = 0;
                for (Object object : objects)
                    if (object != null) {
                        Long l = (Long) object;
                        for (Element element : oldElements) {
                            if (element.getId() == l.longValue()) {
                                engine.setAttribute(key,
                                        attributes.get(aIndex), newElement
                                                .getId());
                                break;
                            }
                        }
                        aIndex++;
                    }
            }
        }

    }

    /**
     * Return qualifiers and system qualifiers for table attribute;
     */
    private List<Qualifier> getAllNotSystemQualifiers(Engine engine) {
        List<Qualifier> res = engine.getQualifiers();
        for (Attribute a : engine.getAttributes()) {
            if ((a.getAttributeType().getTypeName().equals("Table"))
                    && (a.getAttributeType().getPluginName().equals("Core"))) {
                res.add(StandardAttributesPlugin.getTableQualifierForAttribute(
                        engine, a));
            }
        }
        return res;
    }

    @Override
    public Object toUserValue(Engine engine, Attribute attribute,
                              Element element, Object value) {
        OtherElementPropertyPersistent pp = (OtherElementPropertyPersistent) engine
                .getAttribute(null, attribute);
        if (pp == null)
            return super.toUserValue(engine, attribute, element, value);
        Attribute attr2 = engine.getAttribute(pp.getQualifierAttribute());
        if (attr2 == null)
            return super.toUserValue(engine, attribute, element, value);
        Element element2 = engine.getElement((Long) value);
        if (element2 != null)
            return engine.getAttribute(element2, attr2);
        else
            return null;
    }
}
