package com.ramussoft.core.attribute.simple;

import java.sql.ResultSet;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Delete;
import com.ramussoft.common.DeleteStatus;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.Unique;
import com.ramussoft.common.attribute.AbstractAttributePlugin;
import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.attribute.AbstractAttributeConverter;
import com.ramussoft.common.attribute.EngineParalleler;
import com.ramussoft.common.event.ElementAdapter;
import com.ramussoft.common.event.ElementEvent;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.PersistentRow;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.core.impl.IEngineImpl;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.jdbc.RowMapper;

public class ElementListPlugin extends AbstractAttributePlugin {

    @Override
    public void init(Engine engine, AccessRules accessor) {
        super.init(engine, accessor);
        if (!StandardAttributesPlugin.isDisableAutoupdate(engine))
            engine.addElementListener(null, new ElementAdapter() {
                @Override
                public void beforeElementDeleted(ElementEvent event) {
                    if (event.isJournaled())
                        return;
                    long id = event.getQualifierId();
                    Qualifier q = event.getEngine().getQualifier(id);
                    cleanAttribute(q.getAttributes(), event);
                    cleanAttribute(q.getSystemAttributes(), event);
                    super.beforeElementDeleted(event);
                }

                private void cleanAttribute(List<Attribute> attributes,
                                            ElementEvent event) {
                    for (Attribute a : attributes) {
                        if (isMyType(a)) {
                            event.getEngine().setAttribute(
                                    event.getOldElement(), a,
                                    new ArrayList<Persistent>(0));
                        }
                    }
                }

            });
    }

    private boolean isMyType(Attribute a) {
        return (a.getAttributeType().getTypeName().equals(getTypeName()))
                && (a.getAttributeType().getPluginName().equals(getName()));
    }

    @Override
    public AttributeConverter getAttributeConverter() {
        return new AbstractAttributeConverter() {
            @SuppressWarnings("unchecked")
            public Object toObject(java.util.List<Persistent>[] persistents,
                                   long elementId, long attributeId, IEngine engine) {
                if (elementId < 0) {
                    return toPropertyObject(persistents);
                } else {
                    List res = persistents[0];
                    if (res != null)
                        Collections.sort(res);
                    return res;
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Persistent>[] toPersistens(Object object,
                                                   long elementId, long attributeId, IEngine engine) {
                if (elementId < 0) {
                    List<Persistent> list = new ArrayList<Persistent>(1);
                    list.add((ElementListPropertyPersistent) object);
                    return new List[]{list};
                } else
                    return new List[]{new ArrayList<Persistent>(check(object,
                            elementId, attributeId, engine))};
            }

            @SuppressWarnings("unchecked")
            private List check(Object object, long elementId, long attributeId,
                               IEngine engine) {
                List<ElementListPersistent> list = (List) object;
                ElementListPropertyPersistent p = (ElementListPropertyPersistent) toObject(
                        engine.getBinaryAttribute(-1, attributeId), -1,
                        attributeId, engine);

                if ((p.getQualifier1() == -1) || (p.getQualifier2() == -1))
                    throw new RuntimeException(
                            "Left or right qualifier not seted.");

                long mq = engine.getQualifierIdForElement(elementId);

                if (mq == p.getQualifier1()) {
                    for (int i = list.size() - 1; i >= 0; --i) {
                        ElementListPersistent e = list.get(i);
                        if ((e.getElement1Id() != elementId)
                                || (engine.getQualifierIdForElement(e
                                .getElement2Id())) != p.getQualifier2()) {
                            System.err.println("Element id not correct.");
                            list.remove(i);
                        }
                    }
                } else if (mq == p.getQualifier2()) {
                    for (int i = list.size() - 1; i >= 0; --i) {
                        ElementListPersistent e = list.get(i);
                        if ((e.getElement2Id() != elementId)
                                || (engine.getQualifierIdForElement(e
                                .getElement1Id())) != p.getQualifier1()) {
                            System.err.println("Element id not correct.");
                            list.remove(i);
                        }
                    }
                } else {
                    System.err.println("Element not correct.");
                    return new ArrayList(0);
                }

                return list;
            }
        };
    }

    @Override
    public void fillAttributeQuery(PersistentRow row, long attributeId,
                                   long elementId, ArrayList<Object> params,
                                   ArrayList<String> paramFields, IEngine engine) {
        super.fillAttributeQuery(row, attributeId, elementId, params,
                paramFields, engine);
        if (elementId >= 0) {
            ElementListPropertyPersistent p = toPropertyObject(engine
                    .getBinaryAttribute(-1l, attributeId));
            long qualifierId = engine.getQualifierIdForElement(elementId);
            if (qualifierId == p.getQualifier1()) {
                params.add(elementId);
                paramFields.add("element1_id");
            } else if (qualifierId == p.getQualifier2()) {
                params.add(elementId);
                paramFields.add("element2_id");
            } else {
                params.add(-1l);
                paramFields.add("element1_id");
            }
        }
    }

    @Override
    public String getTypeName() {
        return "ElementList";
    }

    @Override
    public boolean isComparable() {
        return false;
    }

    @Override
    public boolean isSystem() {
        return true;
    }

    @Override
    public String getName() {
        return "Core";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{ElementListPersistent.class};
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePropertyPersistents() {
        return new Class[]{ElementListPropertyPersistent.class};
    }

    private ElementListPropertyPersistent toPropertyObject(
            java.util.List<Persistent>[] persistents) {
        if (persistents[0].size() == 0) {
            return new ElementListPropertyPersistent();
        } else {
            return (ElementListPropertyPersistent) persistents[0].get(0);
        }
    }

    ;

    @Override
    public boolean isLight() {
        return false;
    }

    @Override
    public DeleteStatus getQualifierDeleteStatus(long qualifierId,
                                                 IEngine engine) {
        for (Attribute a : engine.getAttributes())
            if (isMyType(a)) {
                List<Persistent>[] lists = engine.getBinaryAttribute(-1,
                        a.getId());
                if (lists[0].size() > 0) {
                    ElementListPropertyPersistent p = (ElementListPropertyPersistent) lists[0]
                            .get(0);
                    if ((qualifierId == p.getQualifier1())
                            || (qualifierId == p.getQualifier2())) {
                        DeleteStatus ds = new DeleteStatus();
                        ds.setDelete(Delete.CAN_NOT);
                        ds.setPluginName(getName());
                        ds.setPluginAnswer("[ElementList.Using]");
                        return ds;
                    }
                }
            }
        return super.getQualifierDeleteStatus(qualifierId, engine);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void copyAttribute(Engine sourceEngine, Engine destinationEngine,
                              Attribute sourceAttribute, Attribute destinationAttribute,
                              Element sourceElement, Element destinationElement,
                              EngineParalleler paralleler) {
        if (sourceElement == null) {
            ElementListPropertyPersistent p = (ElementListPropertyPersistent) sourceEngine
                    .getAttribute(null, sourceAttribute);
            ElementListPropertyPersistent d = new ElementListPropertyPersistent();
            d.setQualifier1(getId(paralleler.getQualifier(p.getQualifier1())));
            d.setQualifier2(getId(paralleler.getQualifier(p.getQualifier2())));
            destinationEngine.setAttribute(null, destinationAttribute, d);
        } else {
            List<ElementListPersistent> list = (List<ElementListPersistent>) sourceEngine
                    .getAttribute(sourceElement, sourceAttribute);
            ArrayList<ElementListPersistent> dest = new ArrayList<ElementListPersistent>(
                    list.size());
            for (ElementListPersistent s : list) {
                ElementListPersistent d = new ElementListPersistent();
                d.setElement1Id(getId(paralleler.getElement(s.getElement1Id())));
                d.setElement2Id(getId(paralleler.getElement(s.getElement2Id())));
                if (dest.indexOf(d) < 0)
                    dest.add(d);
            }
            try {
                destinationEngine.setAttribute(destinationElement,
                        destinationAttribute, dest);
            } catch (Exception e) {
                e.printStackTrace();
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
        IEngineImpl engine = (IEngineImpl) aEngine;
        JDBCTemplate template = engine.getTemplate();
        String prefix = engine.getPrefix();
        String sql = "SELECT element_name, qualifier_name, attribute_name "
                + "FROM {0}attribute_element_lists a, {0}attributes b, {0}qualifiers c, {0}elements d "
                + "WHERE element2_id in("
                + JDBCTemplate.toSqlArray(elementIds)
                + ") AND a.attribute_id=b.attribute_id AND d.element_id=a.element1_id "
                + "AND d.qualifier_id=c.qualifier_id AND b.attribute_system=false "
                + "ORDER BY qualifier_name, attribute_name, element_name";
        List<String> list = template.query(MessageFormat.format(sql, prefix),
                new RowMapper() {
                    @Override
                    public Object mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        return "<tr><td>" + rs.getString(1) + "</td><td> "
                                + rs.getString(2) + "</td><td> "
                                + rs.getString(3) + "</td></tr>";
                    }
                });

        sql = "SELECT element_name, qualifier_name, attribute_name "
                + "FROM {0}attribute_element_lists a, {0}attributes b, {0}qualifiers c, {0}elements d "
                + "WHERE element1_id in("
                + JDBCTemplate.toSqlArray(elementIds)
                + ") AND a.attribute_id=b.attribute_id AND d.element_id=a.element2_id "
                + "AND d.qualifier_id=c.qualifier_id AND b.attribute_system=false "
                + "ORDER BY qualifier_name, attribute_name, element_name";
        List<String> list1 = template.query(MessageFormat.format(sql, prefix),
                new RowMapper() {
                    @Override
                    public Object mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        return "<tr><td>" + rs.getString(1) + "</td><td> "
                                + rs.getString(2) + "</td><td> "
                                + rs.getString(3) + "</td></tr>";
                    }
                });

        list.addAll(list1);

        if (list.size() == 0)
            return null;

        DeleteStatus status = new DeleteStatus();
        status.setPluginName("Core");
        StringBuffer sb = new StringBuffer();
        sb.append("<br>");
        sb.append("<table>");
        sb.append("<tr><td><b>{AttributeType.Core.OtherElement}</b></td><td><b>{OtherElement.Qualifier}</b></td><td><b>{OtherElement.Attribute}</b></td></tr>");
        for (String s : list) {
            sb.append(s);
        }
        sb.append("</table>");
        status.setPluginAnswer("{Warning.ElementsUsedAtOtherElements}"
                + sb.toString());

        return status;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void replaceElements(Engine engine, Element[] oldElements,
                                Element newElement) {
        for (Qualifier qualifier : getAllNotSystemQualifiers(engine))
            if (qualifier.getId() != newElement.getQualifierId()) {
                List<Attribute> attributes1 = new ArrayList<Attribute>();
                List<Attribute> attributes2 = new ArrayList<Attribute>();
                for (Attribute attribute : qualifier.getAttributes()) {
                    if ((attribute.getAttributeType().getPluginName()
                            .equals(getName()))
                            && (attribute.getAttributeType().getTypeName()
                            .equals(getTypeName()))) {
                        ElementListPropertyPersistent p = (ElementListPropertyPersistent) engine
                                .getAttribute(null, attribute);
                        if (p.getQualifier1() == newElement.getQualifierId())
                            attributes1.add(attribute);
                        if (p.getQualifier2() == newElement.getQualifierId())
                            attributes2.add(attribute);
                    }
                }

                ArrayList<Attribute> attributes = new ArrayList<Attribute>(
                        attributes1);
                attributes.addAll(attributes2);

                List<Element> elements = engine.getElements(qualifier.getId());

                int i = 0;
                for (Attribute attribute : attributes) {
                    for (Element element : elements) {
                        List<ElementListPersistent> value = (List<ElementListPersistent>) engine
                                .getAttribute(element, attribute);
                        boolean add = false;
                        for (ElementListPersistent p : value) {
                            if (i < attributes1.size()) {
                                if (isPrecent(p.getElement1Id(), oldElements)) {
                                    add = true;
                                    break;
                                }
                            } else {
                                if (isPrecent(p.getElement2Id(), oldElements)) {
                                    add = true;
                                    break;
                                }
                            }
                        }
                        if (add) {
                            boolean toAdd = true;
                            for (ElementListPersistent p : value) {
                                if (i < attributes1.size()) {
                                    if (p.getElement1Id() == newElement.getId())
                                        toAdd = false;
                                    break;
                                } else {
                                    if (p.getElement2Id() == newElement.getId())
                                        toAdd = false;
                                    break;
                                }
                            }
                            if (toAdd) {
                                ElementListPersistent p = new ElementListPersistent();
                                if (i < attributes1.size()) {
                                    p.setElement1Id(newElement.getId());
                                    p.setElement2Id(element.getId());
                                } else {
                                    p.setElement1Id(element.getId());
                                    p.setElement2Id(newElement.getId());
                                }
                                if (value.indexOf(p) < 0) {
                                    value.add(p);
                                    engine.setAttribute(element, attribute,
                                            value);
                                }
                            }
                        }
                    }
                    i++;
                }
            }
    }

    /**
     * Return qualifiers and system qualifiers for table attribute;
     */
    private List<Qualifier> getAllNotSystemQualifiers(Engine engine) {
        List<Qualifier> res = engine.getQualifiers();
        for (Attribute a : engine.getAttributes()) {
            if ((a.getAttributeType().getPluginName().equals("Table"))
                    && (a.getAttributeType().getPluginName().equals("Core"))) {
                res.add(StandardAttributesPlugin.getTableQualifierForAttribute(
                        engine, a));
            }
        }
        return res;
    }

    private boolean isPrecent(long elementId, Element[] elements) {
        for (Element e : elements)
            if (e.getId() == elementId)
                return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    public static long[] getElements(Engine engine, Attribute attribute,
                                     Element element) {
        List<ElementListPersistent> list = (List<ElementListPersistent>) engine
                .getAttribute(element, attribute);
        long[] res = new long[list.size()];
        for (int i = 0; i < res.length; i++) {
            ElementListPersistent p = list.get(i);
            if (p.getElement1Id() == element.getId())
                res[i] = p.getElement2Id();
            else
                res[i] = p.getElement1Id();
        }
        return res;
    }
}
