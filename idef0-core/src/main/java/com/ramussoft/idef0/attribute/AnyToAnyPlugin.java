package com.ramussoft.idef0.attribute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
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
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.core.attribute.simple.TextPersistent;
import com.ramussoft.core.impl.IEngineImpl;
import com.ramussoft.database.StringCollator;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.jdbc.ExecutionCallback;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.jdbc.RowMapper;

public class AnyToAnyPlugin extends AbstractAttributePlugin {

    @Override
    public AttributeConverter getAttributeConverter() {
        return new AbstractAttributeConverter() {

            @Override
            public Object toObject(List<Persistent>[] persistents,
                                   long elementId, long attributeId, IEngine engine) {
                return persistents[0];
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Persistent>[] toPersistens(Object object,
                                                   long elementId, long attributeId, IEngine engine) {
                List<Persistent> list;
                if (object == null) {
                    list = new ArrayList<Persistent>(0);
                } else {
                    list = new ArrayList<Persistent>((List<Persistent>) object);
                }

                return new List[]{list};
            }

        };
    }

    @Override
    public String getTypeName() {
        return "AnyToAny";
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
        return "IDEF0";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Persistent>[] getAttributePersistents() {
        return new Class[]{AnyToAnyPersistent.class};
    }

    @SuppressWarnings("unchecked")
    @Override
    public void copyAttribute(Engine sourceEngine, Engine destinationEngine,
                              Attribute sourceAttribute, Attribute destinationAttribute,
                              Element sourceElement, Element destinationElement,
                              EngineParalleler paralleler) {
        if (sourceElement != null) {
            List<AnyToAnyPersistent> list = (List<AnyToAnyPersistent>) sourceEngine
                    .getAttribute(sourceElement, sourceAttribute);
            List<AnyToAnyPersistent> dest = new ArrayList<AnyToAnyPersistent>(
                    list.size());
            for (AnyToAnyPersistent s : list) {
                AnyToAnyPersistent d = new AnyToAnyPersistent();
                Element element = paralleler.getElement(s.getOtherElement());
                if (element != null) {
                    d.setOtherElement(getId(element));
                    d.setElementStatus(s.getElementStatus());
                    dest.add(d);
                }
            }
            destinationEngine.setAttribute(destinationElement,
                    destinationAttribute, dest);
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
                                                final IEngine engine) {

        final HashMap<Long, Attribute> attributesCache = new HashMap<Long, Attribute>();
        final HashMap<Long, Qualifier> qualifiersCache = new HashMap<Long, Qualifier>();

        JDBCTemplate template = ((IEngineImpl) engine).getTemplate();
        String prefix = ((IEngineImpl) engine).getPrefix();

        StringBuffer sb = JDBCTemplate.toSqlArray(elementIds);

        String sql = "SELECT element_id FROM " + prefix
                + "attribute_any_to_any_elements WHERE other_element in("
                + sb.toString() + ")";

        String gSQL;
        final List<String> functions = new ArrayList<String>();
        gSQL = "SELECT * FROM {0}elements WHERE element_id IN("
                + "SELECT other_element FROM {0}attribute_other_elements WHERE element_id in("
                + "SELECT element_id FROM {0}attribute_other_elements WHERE other_element in("
                + sql
                + ")) "
                + "AND attribute_id IN (SELECT attribute_id FROM {0}attributes WHERE attribute_name=? AND attribute_system=true)) ORDER BY element_name";

        template.query(MessageFormat.format(gSQL, prefix), new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                String elementName = getElementName(engine, attributesCache,
                        qualifiersCache, rs);
                if (elementName.trim().length() > 0
                        && !functions.contains(elementName))
                    functions.add(elementName);
                return null;
            }
        }, new Object[]{IDEF0Plugin.F_SECTOR_FUNCTION}, true);

        gSQL = "SELECT * FROM {0}elements WHERE element_id IN("
                + "SELECT other_element FROM {0}attribute_other_elements WHERE element_id in("
                + "SELECT element_id FROM {0}attribute_other_elements WHERE other_element in("
                + sb
                + ")) "
                + "AND attribute_id IN (SELECT attribute_id FROM {0}attributes WHERE attribute_name=? AND attribute_system=true)) ORDER BY element_name";

        template.query(MessageFormat.format(gSQL, prefix), new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                String elementName = getElementName(engine, attributesCache,
                        qualifiersCache, rs);
                if (elementName.trim().length() > 0
                        && !functions.contains(elementName))
                    functions.add(elementName);
                return null;
            }
        }, new Object[]{IDEF0Plugin.F_SECTOR_FUNCTION}, true);

        gSQL = "SELECT * FROM {0}elements WHERE element_id IN(SELECT parent_element_id FROM {0}attribute_hierarchicals WHERE element_id IN("
                + "SELECT element_id FROM {0}attribute_longs WHERE value in("
                + sql
                + ")AND attribute_id IN (SELECT attribute_id FROM {0}attributes WHERE attribute_name=? AND attribute_system=true) "
                + ")) ORDER BY element_name";

        template.query(MessageFormat.format(gSQL, prefix), new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                String elementName = getElementName(engine, attributesCache,
                        qualifiersCache, rs);
                if (elementName.trim().length() > 0
                        && !functions.contains(elementName))
                    functions.add(elementName);
                return null;
            }
        }, new Object[]{IDEF0Plugin.F_LINK}, true);

        if (functions.size() > 0) {
            Collections.sort(functions, new Comparator<String>() {

                @Override
                public int compare(String arg0, String arg1) {
                    return StringCollator.compare(arg0, arg1);
                }
            });
            DeleteStatus deleteStatus = new DeleteStatus();
            deleteStatus.setPluginName(getName());
            StringBuffer buffer = new StringBuffer();
            for (String s : functions)
                if (!s.trim().equals("")) {
                    buffer.append("<br>");
                    buffer.append(s);
                }
            deleteStatus.setPluginAnswer("{Warning.ElementsUsedAtFunctions}"
                    + buffer.toString());
            return deleteStatus;
        }
        return null;
    }

    private String getElementName(final IEngine engine,
                                  final HashMap<Long, Attribute> attributesCache,
                                  final HashMap<Long, Qualifier> qualifiersCache, ResultSet rs)
            throws SQLException {
        Element element = engine.getElement(rs.getLong("element_id"));
        if (element == null)
            return "";

        Qualifier qualifier;
        if (!qualifiersCache.containsKey(element.getQualifierId())) {
            qualifier = engine.getQualifier(element.getQualifierId());
            qualifiersCache.put(element.getQualifierId(), qualifier);
        } else
            qualifier = qualifiersCache.get(element.getQualifierId());

        if (qualifier == null || qualifier.getAttributeForName() < 0l)
            return "";

        Attribute attribute;
        if (!attributesCache.containsKey(qualifier.getAttributeForName())) {
            attribute = engine.getAttribute(qualifier.getAttributeForName());
            attributesCache.put(qualifier.getAttributeForName(), attribute);
        } else
            attribute = attributesCache.get(qualifier.getAttributeForName());
        if (attribute == null)
            return "";

        List<Persistent>[] bAttribute = engine.getBinaryAttribute(
                element.getId(), attribute.getId());
        if (bAttribute.length == 0)
            return "";

        List<Persistent> list = bAttribute[0];
        if (list.size() == 0)
            return "";

        if (list.get(0) instanceof TextPersistent)
            return String.valueOf(((TextPersistent) list.get(0)).getValue());
        else if (list.get(0) instanceof DFDSName)
            return String.valueOf(((DFDSName) list.get(0)).getShortName());

        return "";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void replaceElements(Engine engine, Element[] oldElements,
                                Element newElement) {
        if (engine.getDeligate() instanceof IEngineImpl) {
            IEngineImpl engineImpl = (IEngineImpl) engine.getDeligate();
            JDBCTemplate template = engineImpl.getTemplate();
            String prefix = engineImpl.getPrefix();
            long[] elementIds = new long[oldElements.length];
            for (int i = 0; i < oldElements.length; i++) {
                elementIds[i] = oldElements[i].getId();
            }
            String sql = "SELECT element_id FROM " + prefix
                    + "attribute_any_to_any_elements WHERE other_element IN("
                    + JDBCTemplate.toSqlArray(elementIds) + ")";
            if (!(Boolean) template.query(sql, new ExecutionCallback() {

                @Override
                public Object execute(PreparedStatement statement)
                        throws SQLException {
                    ResultSet rs = statement.executeQuery();
                    boolean has = rs.next();
                    rs.close();
                    return has;
                }

            }, false))
                return;
        }

        Qualifier qualifier = IDEF0Plugin.getBaseStreamQualifier(engine);
        Attribute added = IDEF0Plugin.getStreamAddedAttribute(engine);
        List<Attribute> attributes = new ArrayList<Attribute>(1);
        attributes.add(added);
        Hashtable<Element, Object[]> res = engine.getElements(qualifier,
                attributes);
        Enumeration<Element> keys = res.keys();
        while (keys.hasMoreElements()) {
            Element key = keys.nextElement();
            Object[] value = res.get(key);
            updateElements(engine, key, (List<AnyToAnyPersistent>) value[0],
                    oldElements, newElement, added);
        }
    }

    private void updateElements(Engine engine, Element stream,
                                List<AnyToAnyPersistent> value, Element[] oldElements,
                                Element newElement, Attribute attribute) {
        boolean add = false;
        for (Element element : oldElements) {
            if (isPresent(value, element)) {
                add = true;
                break;
            }
        }
        if (add) {
            ArrayList<AnyToAnyPersistent> list = new ArrayList<AnyToAnyPersistent>(
                    value.size());

            if (!isPresent(value, newElement)) {
                AnyToAnyPersistent p = new AnyToAnyPersistent();
                p.setOtherElement(newElement.getId());
                list.add(p);
            }

            for (AnyToAnyPersistent p : value) {
                boolean toAdd = true;
                for (Element element : oldElements) {
                    if (element.getId() == p.getOtherElement()) {
                        toAdd = false;
                        break;
                    }
                }
                if (toAdd)
                    list.add(p);
            }

            engine.setAttribute(stream, attribute, list);
        }
    }

    private boolean isPresent(List<AnyToAnyPersistent> list, Element element) {
        for (AnyToAnyPersistent p : list)
            if (p.getOtherElement() == element.getId())
                return true;
        return false;
    }

}
