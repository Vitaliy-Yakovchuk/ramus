package com.ramussoft.core.attribute.standard;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import com.ramussoft.common.AbstractPlugin;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Delete;
import com.ramussoft.common.DeleteStatus;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.attribute.AttributePlugin;
import com.ramussoft.common.event.AttributeAdapter;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.ElementAdapter;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementEvent;
import com.ramussoft.common.event.QualifierAdapter;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.event.StreamAdapter;
import com.ramussoft.common.event.StreamEvent;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.core.attribute.simple.ElementListPersistent;
import com.ramussoft.core.attribute.simple.ElementListPropertyPersistent;
import com.ramussoft.core.attribute.simple.HierarchicalPersistent;
import com.ramussoft.core.attribute.simple.HierarchicalPlugin;
import com.ramussoft.core.attribute.simple.LongPersistent;
import com.ramussoft.core.history.Record;
import com.ramussoft.core.impl.IEngineImpl;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.jdbc.RowMapper;

public class StandardAttributesPlugin extends AbstractPlugin {

    private static final String TABLE_QUALIFIER = "TableQualifier_";

    public static final String STANDARD_ATTRIBUTES_PLUGIN = "StandardAttributesPlugin";

    /**
     * Name attribute for qualifiers and attributes, return {@link Attribute}.
     */

    public static final String ATTRIBUTE_NAME = "AttributeName";

    /**
     * Attribute for string attribute, for attribute type string, type:
     * {@link Attribute}.
     */

    public static final String ATTRIBUTE_TYPE_NAME = "AttributeTypeName";

    /**
     * Attribute for long, id to an real attribute id, type {@link Attribute}.
     */

    public static final String ATTRIBUTE_ID = "AttributeId";

    /**
     * Attribute for qualifier id, return type {@link Attribute}.
     */

    public static final String QUALIFIER_ID = "QualifierId";

    /**
     * ElementList attribute, projection with qualifiers and attributes.
     */

    public static final String QUALIFIER_ATTRIBUTES = "QualifierAttributes";

    /**
     * Qualifier with elements, that are equal to not system qualifiers.
     */

    public static final String QUALIFIERS_QUALIFIER = "QualifiersQualifier";

    /**
     * Qualifier with links to not system attributes.
     */

    public static final String ATTRIBUTES_QUALIFIER = "AttributesQualifier";

    /**
     * Qualifier with all saved icons.
     */

    public static final String ICONS_QUALIFIER = "IconsQualifier";

    /**
     * Attribute to set icons.
     */

    public static final String ICONS_ATTRIBUTE = "IconsAttribute";

    public static final String TABLE_ELEMENT_ID_ATTRIBUTE = "TableElementIdAttribute";

    public static final String ICONS_QUALIFIER_LEAFS = "IconsQualifierLeafs";

    public static final String ICONS_QUALIFIER_OPEN_FOLDERS = "IconsQualifierOpenFolders";

    public static final String ICONS_QUALIFIER_CLOSED_FOLDERS = "IconsQualifierClosedFolders";

    public static final String QUALIFIER_HISTORY = "HistoryQualifier";

    public static final String ATTRIBUTE_HISTORY_ELEMENT = "HistoryElementAttribute";

    public static final String ATTRIBUTE_HISTORY_ATTRIBUTE = "HistoryAttributeAttribute";

    public static final String ATTRIBUTE_HISTORY_TIME = "HistoryTimeAttribute";

    private static boolean autocreateFromQualifiers = false;

    private static boolean defaultDisableAutoupdate = false;

    public static String PROPERTIES = "/properties/standard.xml";

    public static String HISTORY_QUALIFIER_KEY = "HISTORY_QUALIFIER_";

    private static Object lock = new Object();

    private boolean disableAutoupdate = false;

    private Qualifier attributes;

    private Qualifier qualifiers;

    private Attribute aList;

    private Attribute aQualifierId;

    private Attribute aAttributeId;

    private Attribute nameAttribute;

    private Attribute attributeTypeName;

    private Qualifier iconsQualifier;

    private Attribute iconsAttribute;

    private Attribute iconsQualifierLeafs;

    private Attribute iconsQualifierOpenFolders;

    private Attribute iconsQualifierClosedFolders;

    private Attribute tableElementIdAttribute;

    private Attribute historyTime;

    private Attribute historyElement;

    private Attribute historyAttribute;

    private Thread currentThread;

    private Object threadLocker = new Object();

    private Vector<Long> tableQualifierIds = new Vector<Long>();

    private Qualifier historyQualifier;

    private Hashtable<Long, List<Long>> historyQualifiers = new Hashtable<Long, List<Long>>();

    public StandardAttributesPlugin() {
        synchronized (lock) {
            if (defaultDisableAutoupdate) {
                defaultDisableAutoupdate = false;
                this.disableAutoupdate = true;
            }
        }
    }

    @Override
    public String getName() {
        return "Core";
    }

    @Override
    public boolean canDeleteElements(long[] elementIds, IEngine engine) {
        Vector<Long> vector = new Vector<Long>();
        for (long elementId : elementIds) {
            long qualifierId = engine.getQualifierIdForElement(elementId);
            Long l = new Long(qualifierId);
            if (vector.indexOf(l) < 0) {
                vector.add(l);
                if (qualifierId == qualifiers.getId()) {
                    List<Persistent>[] lists = engine.getBinaryAttribute(
                            elementId, aQualifierId.getId());
                    Long long1;
                    if (lists[0].size() == 0)
                        long1 = null;
                    else
                        long1 = ((LongPersistent) lists[0].get(0)).getValue();
                    if (long1 == null)
                        continue;
                    if (!rules.canDeleteQualifier(long1)) {
                        DeleteStatus status = new DeleteStatus();
                        status.setDelete(Delete.CAN_NOT);
                        return false;
                    }
                    continue;
                }
            }
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void init(final Engine engine, final AccessRules accessor) {
        super.init(engine, accessor);
        engine.setPluginProperty(getName(), STANDARD_ATTRIBUTES_PLUGIN, this);
        if ((attributes != null) || (qualifiers != null)) {
            throw new RuntimeException("Plugin has already been inted");
        }

        qualifiers = engine.getSystemQualifier(QUALIFIERS_QUALIFIER);

        attributes = engine.getSystemQualifier(ATTRIBUTES_QUALIFIER);

        iconsQualifier = engine.getSystemQualifier(ICONS_QUALIFIER);

        historyQualifier = engine.getSystemQualifier(QUALIFIER_HISTORY);

        if (historyQualifier == null) {
            historyQualifier = engine.createSystemQualifier();
            historyQualifier.setName(QUALIFIER_HISTORY);

            historyElement = engine.createSystemAttribute(new AttributeType(
                    "Core", "Long", true));
            historyElement.setName(ATTRIBUTE_HISTORY_ELEMENT);
            engine.updateAttribute(historyElement);

            historyAttribute = engine.createSystemAttribute(new AttributeType(
                    "Core", "Long", true));

            historyAttribute.setName(ATTRIBUTE_HISTORY_ATTRIBUTE);
            engine.updateAttribute(historyAttribute);

            historyTime = engine.createSystemAttribute(new AttributeType(
                    "Core", "Date", true));
            historyTime.setName(ATTRIBUTE_HISTORY_TIME);
            engine.updateAttribute(historyTime);

            historyQualifier.getSystemAttributes().add(historyElement);
            historyQualifier.getSystemAttributes().add(historyAttribute);
            historyQualifier.getSystemAttributes().add(historyTime);

            engine.updateQualifier(historyQualifier);
        } else {
            historyElement = engine
                    .getSystemAttribute(ATTRIBUTE_HISTORY_ELEMENT);
            historyAttribute = engine
                    .getSystemAttribute(ATTRIBUTE_HISTORY_ATTRIBUTE);
            historyTime = engine.getSystemAttribute(ATTRIBUTE_HISTORY_TIME);
        }

        for (Qualifier qualifier : engine.getSystemQualifiers()) {
            if (qualifier.getName().startsWith(TABLE_QUALIFIER))
                tableQualifierIds.add(qualifier.getId());
        }

        if (((qualifiers == null) && (attributes != null))
                || ((qualifiers != null) && (attributes == null))) {
            throw new RuntimeException(
                    "Attributes and qualifiers must inited both in the same time.");
        }
        if (qualifiers == null) {
            initQualifiers(engine);
        } else {
            loadAttributes();
        }

        iconsAttribute = engine.getSystemAttribute(ICONS_ATTRIBUTE);
        iconsQualifierLeafs = engine.getSystemAttribute(ICONS_QUALIFIER_LEAFS);
        iconsQualifierOpenFolders = engine
                .getSystemAttribute(ICONS_QUALIFIER_OPEN_FOLDERS);
        iconsQualifierClosedFolders = engine
                .getSystemAttribute(ICONS_QUALIFIER_CLOSED_FOLDERS);
        tableElementIdAttribute = engine
                .getSystemAttribute(TABLE_ELEMENT_ID_ATTRIBUTE);
        if (iconsQualifier == null) {
            createIconsQualifier(engine);
        }

        if (tableElementIdAttribute == null) {
            createTableElementIdAttribute(engine);
        }

        engine.setPluginProperty(getName(), ATTRIBUTES_QUALIFIER, attributes);
        engine.setPluginProperty(getName(), QUALIFIERS_QUALIFIER, qualifiers);
        engine.setPluginProperty(getName(), ATTRIBUTE_NAME, nameAttribute);
        engine.setPluginProperty(getName(), QUALIFIER_ID, aQualifierId);
        engine.setPluginProperty(getName(), ATTRIBUTE_TYPE_NAME,
                attributeTypeName);
        engine.setPluginProperty(getName(), ATTRIBUTE_ID, aAttributeId);

        engine.setPluginProperty(getName(), QUALIFIER_ATTRIBUTES, aList);

        engine.setPluginProperty(getName(), ICONS_QUALIFIER, iconsQualifier);

        engine.setPluginProperty(getName(), ICONS_ATTRIBUTE, iconsAttribute);

        engine.setPluginProperty(getName(), ICONS_QUALIFIER_LEAFS,
                iconsQualifierLeafs);

        engine.setPluginProperty(getName(), ICONS_QUALIFIER_OPEN_FOLDERS,
                iconsQualifierOpenFolders);

        engine.setPluginProperty(getName(), ICONS_QUALIFIER_CLOSED_FOLDERS,
                iconsQualifierClosedFolders);

        engine.addQualifierListener(new QualifierAdapter() {

            @Override
            public void qualifierCreated(QualifierEvent event) {
                if (!autocreateFromQualifiers)
                    return;
                if (disableAutoupdate)
                    return;
                if (event.isJournaled())
                    return;
                synchronized (threadLocker) {
                    if (Thread.currentThread() == currentThread)
                        return;
                }

                try {
                    synchronized (threadLocker) {
                        currentThread = Thread.currentThread();
                    }

                    Engine e = event.getEngine();
                    Element element = e.createElement(qualifiers.getId());
                    e.setAttribute(element, aQualifierId, event
                            .getNewQualifier().getId());
                    e.setAttribute(element, nameAttribute, "");
                } finally {
                    synchronized (threadLocker) {
                        currentThread = null;
                    }
                }
            }

            @Override
            public void qualifierDeleted(QualifierEvent event) {
                Qualifier qualifier = event.getOldQualifier();
                if ((qualifier.isSystem())
                        && (qualifier.getName().startsWith(TABLE_QUALIFIER))) {
                    tableQualifierIds.remove(new Long(qualifier.getId()));
                }

                if (event.isJournaled())
                    return;
                if (disableAutoupdate)
                    return;
                synchronized (threadLocker) {
                    if (Thread.currentThread() == currentThread)
                        return;
                }

                try {
                    synchronized (threadLocker) {
                        currentThread = Thread.currentThread();
                    }
                    Engine e = event.getEngine();
                    List<Element> elements = e.findElements(qualifiers.getId(),
                            aQualifierId, event.getOldQualifier().getId());
                    if (elements.size() != 1) {
                        return;
                    }
                    e.deleteElement(elements.get(0).getId());
                } finally {
                    synchronized (threadLocker) {
                        currentThread = null;
                    }
                }
            }

            @Override
            public void qualifierUpdated(QualifierEvent event) {
                Qualifier qualifier = event.getNewQualifier();
                if ((qualifier.isSystem())
                        && (qualifier.getName().startsWith(TABLE_QUALIFIER)))
                    tableQualifierIds.add(qualifier.getId());

                if (event.isJournaled())
                    return;
                if (disableAutoupdate)
                    return;
                List<Attribute> attributesToRemove = getAttributesToRemove(event);
                List<Element> allElements = null;
                for (Attribute attribute : attributesToRemove) {
                    if (attribute.getAttributeType().toString()
                            .equals("Core.Table")) {
                        if (allElements == null) {
                            allElements = engine.getElements(event
                                    .getNewQualifier().getId());
                        }
                        for (Element element : allElements) {
                            for (Element element2 : getTableElements(engine,
                                    attribute, element)) {
                                engine.deleteElement(element2.getId());
                            }
                        }
                    }
                }

                if (event.getNewQualifier().isSystem())
                    return;
                synchronized (threadLocker) {
                    if (Thread.currentThread() == currentThread)
                        return;
                }

                try {
                    synchronized (threadLocker) {
                        currentThread = Thread.currentThread();
                    }

                    Engine e = event.getEngine();
                    List<Element> elements = e.findElements(qualifiers.getId(),
                            aQualifierId, event.getNewQualifier().getId());
                    if (elements.size() != 1) {
                        // throw new RuntimeException(
                        // System.err
                        // .println("Not regidtered qualifier was updated, or data integrity wrong.");
                        return;
                    }
                    e.setAttribute(elements.get(0), nameAttribute, event
                            .getNewQualifier().getName());

                    List<Attribute> list = event.getNewQualifier()
                            .getAttributes();
                    List<ElementListPersistent> pList = new ArrayList<ElementListPersistent>();
                    for (Attribute a : list) {
                        if (isSystem(a.getAttributeType()))
                            continue;
                        List<Element> ems = e.findElements(attributes.getId(),
                                aAttributeId, a.getId());
                        if (ems.size() != 1) {
                            throw new RuntimeException(
                                    "Fatal error, not registered attribute was removed, or data integrity wrong.");
                        }
                        pList.add(new ElementListPersistent(elements.get(0)
                                .getId(), ems.get(0).getId()));
                    }
                    e.setAttribute(elements.get(0), aList, pList);
                } finally {
                    synchronized (threadLocker) {
                        currentThread = null;
                    }
                }
            }

            private List<Attribute> getAttributesToRemove(QualifierEvent event) {
                List<Attribute> res = new ArrayList<Attribute>(0);
                diff(event.getOldQualifier().getAttributes(), event
                        .getNewQualifier().getAttributes(), res);
                diff(event.getOldQualifier().getSystemAttributes(), event
                        .getNewQualifier().getSystemAttributes(), res);
                return res;
            }

            private void diff(List<Attribute> attributes,
                              List<Attribute> attributes2, List<Attribute> res) {
                for (Attribute attribute : attributes)
                    if (attributes2.indexOf(attribute) < 0) {
                        res.add(attribute);
                    }
            }

        });

        engine.addAttributeListener(new AttributeAdapter() {

            @Override
            public void attributeCreated(AttributeEvent event) {
                if (event.isJournaled())
                    return;
                if (disableAutoupdate)
                    return;
                if (isSystem(event.getAttribute().getAttributeType()))
                    return;
                synchronized (threadLocker) {
                    if (Thread.currentThread() == currentThread)
                        return;
                }

                try {
                    synchronized (threadLocker) {
                        currentThread = Thread.currentThread();
                    }

                    long lastId = getLastId();

                    HierarchicalPersistent hp = new HierarchicalPersistent();
                    hp.setIconId(-1l);
                    hp.setParentElementId(-1l);
                    hp.setPreviousElementId(lastId);

                    Engine e = event.getEngine();
                    Element element = e.createElement(attributes.getId());
                    e.setAttribute(element, aAttributeId, event.getAttribute()
                            .getId());
                    e.setAttribute(element, getHierarchicalAttribute(engine),
                            hp);
                    e.setAttribute(element, nameAttribute, "");
                    e.setAttribute(element, attributeTypeName, event
                            .getAttribute().getAttributeType().toString());

                } finally {
                    synchronized (threadLocker) {
                        currentThread = null;
                    }
                }
            }

            private long getLastId() {
                try {
                    if (engine.getDeligate() instanceof IEngineImpl) {
                        IEngineImpl impl = ((IEngineImpl) engine.getDeligate());
                        String prefix = impl.getPrefix();
                        String sql = "SELECT element_id FROM "
                                + prefix
                                + "elements WHERE element_id NOT IN (SELECT previous_element_id FROM "
                                + prefix
                                + "attribute_hierarchicals) AND qualifier_id=?";
                        JDBCTemplate template = impl.getTemplate();
                        Object res = template.queryForObject(sql,
                                new RowMapper() {
                                    @Override
                                    public Object mapRow(ResultSet rs,
                                                         int rowNum) throws SQLException {
                                        return rs.getLong(1);
                                    }
                                }, attributes.getId(), true);
                        if (res != null)
                            return ((Long) res).longValue();
                        return -1l;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                List<Attribute> attrs = new ArrayList<Attribute>();
                attrs.add(getHierarchicalAttribute(engine));
                Hashtable<Element, Object[]> hash = engine.getElements(
                        attributes, attrs);
                Element element = null;
                for (Element e1 : hash.keySet()) {
                    element = e1;
                    for (Object[] objects : hash.values()) {
                        HierarchicalPersistent hp = (HierarchicalPersistent) objects[0];
                        if (hp != null) {
                            if (hp.getPreviousElementId() == element.getId()) {
                                element = null;
                                break;
                            }
                        }
                    }
                    if (element != null)
                        break;
                }
                if (element == null)
                    return -1l;
                return element.getId();
            }

            @Override
            public void attributeDeleted(AttributeEvent event) {
                if (event.isJournaled())
                    return;
                if (disableAutoupdate)
                    return;
                if (((Attribute) event.getOldValue()).getAttributeType()
                        .toString().equals("Core.Table")) {
                    Qualifier qualifier = getTableQualifierForAttribute(engine,
                            (Attribute) event.getOldValue());
                    engine.deleteQualifier(qualifier.getId());
                }

                if (isSystem(((Attribute) event.getOldValue())
                        .getAttributeType()))
                    return;
                synchronized (threadLocker) {
                    if (Thread.currentThread() == currentThread)
                        return;
                }
                try {
                    synchronized (threadLocker) {
                        currentThread = Thread.currentThread();
                    }

                    Engine e = event.getEngine();
                    List<Element> elements = e.findElements(attributes.getId(),
                            aAttributeId, event.getAttribute().getId());
                    if (elements.size() != 1) {
                        throw new RuntimeException(
                                "Fatal error, not registered qualifier was removed, or data integrity wrong.");
                    }
                    e.deleteElement(elements.get(0).getId());
                } finally {
                    synchronized (threadLocker) {
                        currentThread = null;
                    }
                }
            }

            @Override
            public void attributeUpdated(AttributeEvent event) {
                if (event.isJournaled())
                    return;
                if (disableAutoupdate)
                    return;
                if (isSystem(event.getAttribute().getAttributeType()))
                    return;
                synchronized (threadLocker) {
                    if (Thread.currentThread() == currentThread)
                        return;
                }
                try {
                    synchronized (threadLocker) {
                        currentThread = Thread.currentThread();
                    }

                    Engine e = event.getEngine();
                    List<Element> elements = e.findElements(attributes.getId(),
                            aAttributeId, event.getAttribute().getId());
                    if (elements.size() != 1) {
                        return;
                        // throw new RuntimeException(
                        // "Fatal error, not registered attribute was removed, or data integrity wrong.");
                    }
                    e.setAttribute(elements.get(0), nameAttribute, event
                            .getAttribute().getName());
                } finally {
                    synchronized (threadLocker) {
                        currentThread = null;
                    }
                }
            }

            @Override
            public void beforeAttributeDeleted(AttributeEvent event) {
                if (event.isJournaled())
                    return;
                if (disableAutoupdate)
                    return;
                Attribute attribute = event.getAttribute();
                Engine engine = event.getEngine();
                if (attribute.getAttributeType().toString()
                        .equals("Core.Table")) {
                    Qualifier qualifier = getTableQualifierForAttribute(engine,
                            attribute);
                    if (qualifier != null) {
                        for (Element element : engine.getElements(qualifier
                                .getId())) {
                            engine.deleteElement(element.getId());
                        }
                    }
                }
            }

        });

        engine.addElementAttributeListener(qualifiers,
                new ElementAttributeListener() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void attributeChanged(AttributeEvent event) {
                        if (event.isJournaled())
                            return;
                        if (disableAutoupdate)
                            return;

                        if (event.getAttribute().equals(aQualifierId)) {
                            Engine e = event.getEngine();
                            Qualifier q = e.getQualifier((Long) event
                                    .getNewValue());
                            List<Element> elements = e.findElements(
                                    qualifiers.getId(), aQualifierId, q.getId());
                            if (elements.size() != 1) {
                                // throw new RuntimeException(
                                // System.err
                                // .println("Not regidtered qualifier was updated, or data integrity wrong.");
                                return;
                            }

                            List<Attribute> list = q.getAttributes();
                            List<ElementListPersistent> pList = new ArrayList<ElementListPersistent>();
                            for (Attribute a : list) {
                                List<Element> ems = e.findElements(
                                        attributes.getId(), aAttributeId,
                                        a.getId());
                                if (ems.size() != 1) {
                                    throw new RuntimeException(
                                            "Fatal error, not registered attribute was removed, or data integrity wrong.");
                                }
                                pList.add(new ElementListPersistent(elements
                                        .get(0).getId(), ems.get(0).getId()));
                            }
                            Element element = elements.get(0);
                            element.setQualifierId(qualifiers.getId());
                            e.setAttribute(element, aList, pList);
                        }

                        synchronized (threadLocker) {
                            if (Thread.currentThread() == currentThread)
                                return;
                        }
                        Engine e = event.getEngine();

                        try {
                            synchronized (threadLocker) {
                                currentThread = Thread.currentThread();
                            }

                            if (event.getAttribute().equals(nameAttribute)) {
                                Qualifier qualifier = e.getQualifier((Long) e
                                        .getAttribute(event.getElement(),
                                                aQualifierId));
                                qualifier.setName((String) event.getNewValue());
                                e.updateQualifier(qualifier);
                            } else if (event.getAttribute().equals(aList)) {
                                Long attribute = (Long) e.getAttribute(
                                        event.getElement(), aQualifierId);
                                if (attribute == null)
                                    return;
                                Qualifier qualifier = e.getQualifier(attribute);
                                List<ElementListPersistent> list = (List<ElementListPersistent>) e
                                        .getAttribute(event.getElement(), aList);
                                List<Attribute> attrs = new ArrayList<Attribute>();

                                for (ElementListPersistent p : list) {
                                    Element attr = e.getElement(p
                                            .getElement2Id());
                                    Attribute a = e.getAttribute(((Long) e
                                            .getAttribute(attr, aAttributeId)));
                                    attrs.add(a);
                                }

                                List<Attribute> al = qualifier.getAttributes();

                                for (int i = al.size() - 1; i >= 0; i--) {
                                    if (attrs.indexOf(al.get(i)) < 0) {
                                        al.remove(i);
                                    }
                                }

                                for (Attribute a : attrs) {
                                    if (al.indexOf(a) < 0)
                                        al.add(a);
                                }
                                e.updateQualifier(qualifier);
                            }

                        } finally {
                            synchronized (threadLocker) {
                                currentThread = null;
                            }
                        }

                    }
                });

        engine.addElementAttributeListener(attributes,
                new ElementAttributeListener() {

                    @Override
                    public void attributeChanged(AttributeEvent event) {
                        if (event.isJournaled())
                            return;
                        if (disableAutoupdate)
                            return;
                        synchronized (threadLocker) {
                            if (Thread.currentThread() == currentThread)
                                return;
                        }
                        Engine e = event.getEngine();

                        try {
                            synchronized (threadLocker) {
                                currentThread = Thread.currentThread();
                            }

                            if (event.getAttribute().equals(nameAttribute)) {
                                Attribute attribute = e.getAttribute((Long) e
                                        .getAttribute(event.getElement(),
                                                aAttributeId));
                                attribute.setName((String) event.getNewValue());
                                e.updateAttribute(attribute);
                            }

                        } finally {
                            synchronized (threadLocker) {
                                currentThread = null;
                            }
                        }
                    }
                });

        engine.addElementAttributeListener(null,
                new ElementAttributeListener() {

                    @Override
                    public void attributeChanged(AttributeEvent event) {
                        if (disableAutoupdate)
                            return;
                        Element element = event.getElement();
                        if (element == null)
                            return;
                        try {
                            if (tableQualifierIds.indexOf(new Long(element
                                    .getQualifierId())) >= 0) {
                                Object parentElementId;
                                if (event.getAttribute().equals(
                                        tableElementIdAttribute))
                                    parentElementId = event.getNewValue();
                                else
                                    parentElementId = engine.getAttribute(
                                            element, tableElementIdAttribute);
                                if (parentElementId != null) {
                                    Qualifier table = engine
                                            .getQualifier(element
                                                    .getQualifierId());
                                    Attribute attribute = getAttributeForTable(
                                            engine, table);
                                    Element parent = engine
                                            .getElement((Long) parentElementId);
                                    AttributeEvent event2 = new AttributeEvent(
                                            engine, parent, attribute, null,
                                            null, event.isJournaled());
                                    for (ElementAttributeListener listener : engine
                                            .getElementAttributeListeners(-1))
                                        listener.attributeChanged(event2);
                                    for (ElementAttributeListener listener : engine
                                            .getElementAttributeListeners(parent
                                                    .getQualifierId()))
                                        listener.attributeChanged(event2);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (event.isJournaled())
                            return;
                        List<Long> attrs = historyQualifiers.get(element
                                .getQualifierId());
                        if (attrs != null)
                            if (attrs.indexOf(event.getAttribute().getId()) >= 0) {
                                Element data = engine
                                        .createElement(historyQualifier.getId());
                                engine.setAttribute(data, event.getAttribute(),
                                        event.getNewValue());
                                engine.setAttribute(data, historyElement,
                                        element.getId());
                                engine.setAttribute(data, historyAttribute,
                                        event.getAttribute().getId());
                                engine.setAttribute(data, historyTime,
                                        new Date());
                            }
                    }
                });

        engine.addElementListener(qualifiers, new ElementAdapter() {

            @Override
            public void elementCreated(ElementEvent event) {
                if (event.isJournaled())
                    return;
                if (disableAutoupdate)
                    return;
                synchronized (threadLocker) {
                    if (Thread.currentThread() == currentThread)
                        return;
                }

                Engine e = event.getEngine();
                try {
                    synchronized (threadLocker) {
                        currentThread = Thread.currentThread();
                    }

                    Qualifier qualifier = e.createQualifier();
                    e.setAttribute(event.getNewElement(), aQualifierId,
                            qualifier.getId());

                } finally {
                    synchronized (threadLocker) {
                        currentThread = null;
                    }
                }

            }

            @Override
            public void beforeElementDeleted(ElementEvent event) {
                if (event.isJournaled())
                    return;
                if (disableAutoupdate)
                    return;
                synchronized (threadLocker) {
                    if (Thread.currentThread() == currentThread)
                        return;
                }

                Engine e = event.getEngine();
                long qualifierId = event.getOldElement().getQualifierId();

                if ((qualifierId != qualifiers.getId())
                        && (qualifierId != attributes.getId()))
                    return;

                try {
                    synchronized (threadLocker) {
                        currentThread = Thread.currentThread();
                    }

                    if (qualifierId == qualifiers.getId()) {
                        Object attribute = e.getAttribute(
                                event.getOldElement(), aQualifierId);
                        if (attribute != null)
                            e.deleteQualifier((Long) attribute);
                    } else {
                        throw new RuntimeException(
                                "Element for attribute can not be deleted");
                    }

                } finally {
                    synchronized (threadLocker) {
                        currentThread = null;
                    }
                }
            }

        });

        engine.addElementListener(null, new ElementAdapter() {
            @Override
            public void beforeElementDeleted(ElementEvent event) {
                if (disableAutoupdate)
                    return;
                Element element = event.getOldElement();
                if (element == null)
                    return;
                try {
                    if (tableQualifierIds.indexOf(new Long(element
                            .getQualifierId())) >= 0) {
                        Object parentElementId;
                        parentElementId = engine.getAttribute(element,
                                tableElementIdAttribute);
                        if (parentElementId != null) {
                            Qualifier table = engine.getQualifier(element
                                    .getQualifierId());
                            Attribute attribute = getAttributeForTable(engine,
                                    table);
                            Element parent = engine
                                    .getElement((Long) parentElementId);
                            AttributeEvent event2 = new AttributeEvent(engine,
                                    parent, attribute, null, null, event
                                    .isJournaled());
                            for (ElementAttributeListener listener : engine
                                    .getElementAttributeListeners(-1))
                                listener.attributeChanged(event2);
                            for (ElementAttributeListener listener : engine
                                    .getElementAttributeListeners(parent
                                            .getQualifierId()))
                                listener.attributeChanged(event2);
                        }
                    } else {
                        Qualifier qualifier = engine.getQualifier(element
                                .getQualifierId());
                        for (Attribute attribute : qualifier.getAttributes()) {
                            if (attribute.getAttributeType().toString()
                                    .equals("Core.Table")) {
                                for (Element element2 : getTableElements(
                                        engine, attribute, element)) {
                                    engine.deleteElement(element2.getId());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                List<Long> attrs = historyQualifiers.get(element
                        .getQualifierId());
                if (attrs != null) {
                    List<Element> list = engine.findElements(
                            historyQualifier.getId(), historyElement,
                            element.getId());
                    for (Element element2 : list)
                        engine.deleteElement(element2.getId());
                }
            }
        });

        engine.addStreamListener(new StreamAdapter() {

            @Override
            public void streamUpdated(StreamEvent event) {
                if (event.isJournaled())
                    return;
                String path = event.getPath();
                if (path.equals(PROPERTIES)) {
                    loadHistoryQualifiers((!disableAutoupdate)
                            && (!event.isJournaled()));
                }
            }
        });

        loadHistoryQualifiers(false);
    }

    private void loadHistoryQualifiers(boolean updateQualifier) {
        Properties ps = new Properties();
        try {
            InputStream in = engine.getInputStream(PROPERTIES);
            if (in != null) {
                ps.loadFromXML(in);
                in.close();
            }
            List<Attribute> attributes;
            if (updateQualifier) {
                attributes = historyQualifier.getAttributes();
                attributes.clear();
            } else
                attributes = new ArrayList<Attribute>();
            Hashtable<Long, List<Long>> old = historyQualifiers;
            historyQualifiers = new Hashtable<Long, List<Long>>();
            Enumeration<Object> e = ps.keys();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                if (key.startsWith(HISTORY_QUALIFIER_KEY)) {
                    String value = ps.getProperty(key);
                    if (value != null) {
                        StringTokenizer st = new StringTokenizer(value);
                        List<Long> list = new ArrayList<Long>();
                        while (st.hasMoreTokens()) {
                            Long id = new Long(st.nextToken());
                            boolean add = true;
                            for (Attribute a : attributes) {
                                if (a.getId() == id.longValue())
                                    add = false;
                            }

                            if (add) {
                                Attribute attribute = engine.getAttribute(id);
                                if (attribute != null)
                                    attributes.add(attribute);
                                else
                                    id = null;
                            }

                            if (id != null)
                                list.add(id);
                        }
                        if (list.size() > 0)
                            historyQualifiers.put(
                                    new Long(key
                                            .substring(HISTORY_QUALIFIER_KEY
                                                    .length())), list);
                    }
                }
            }
            if (updateQualifier) {
                engine.updateQualifier(historyQualifier);
                for (Long key : old.keySet()) {
                    List<Long> attrs = historyQualifiers.get(key);
                    if (attrs != null) {
                        List<Long> oldList = old.get(key);
                        for (Long ol : attrs)
                            oldList.remove(ol);
                    }
                }
                for (Long key : old.keySet()) {
                    List<Long> attrs = old.get(key);
                    for (Long attr : attrs) {
                        List<Element> elements = engine.findElements(
                                historyQualifier.getId(), historyAttribute,
                                attr);
                        for (Element element : elements) {
                            Long id = (Long) engine.getAttribute(element,
                                    historyElement);
                            try {
                                Element element2 = engine.getElement(id);
                                if (element2.getQualifierId() == key
                                        .longValue())
                                    engine.deleteElement(element.getId());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Attribute getAttributeForTable(Engine engine, Qualifier table) {
        String id = table.getName().substring(TABLE_QUALIFIER.length());
        return engine.getAttribute(Long.parseLong(id));
    }

    protected boolean isSystem(AttributeType attributeType) {
        PluginFactory factory = (PluginFactory) engine.getPluginProperty(
                "Core", "PluginFactory");

        if (factory == null)
            return false;

        AttributePlugin plugin = factory.getAttributePlugin(attributeType);
        if (plugin == null) {
            System.err.println("WARNING: Attribute plugin for attribute type "
                    + attributeType + " not found");
            return false;
        }

        return plugin.isSystem();
    }

    private void createTableElementIdAttribute(Engine engine) {
        tableElementIdAttribute = engine
                .createSystemAttribute(new AttributeType("Core", "Long", true));
        tableElementIdAttribute.setName(TABLE_ELEMENT_ID_ATTRIBUTE);
        engine.updateAttribute(tableElementIdAttribute);
    }

    private void createIconsQualifier(Engine engine) {
        iconsQualifier = engine.createSystemQualifier();
        iconsAttribute = engine.createSystemAttribute(new AttributeType("Core",
                "Icon", false));
        iconsAttribute.setName(ICONS_ATTRIBUTE);
        engine.updateAttribute(iconsAttribute);
        iconsQualifierOpenFolders = engine
                .createSystemAttribute(new AttributeType("Core", "Text", true));
        iconsQualifierOpenFolders.setName(ICONS_QUALIFIER_OPEN_FOLDERS);
        engine.updateAttribute(iconsQualifierOpenFolders);

        iconsQualifierClosedFolders = engine
                .createSystemAttribute(new AttributeType("Core", "Text", true));
        iconsQualifierClosedFolders.setName(ICONS_QUALIFIER_CLOSED_FOLDERS);
        engine.updateAttribute(iconsQualifierClosedFolders);

        iconsQualifierLeafs = engine.createSystemAttribute(new AttributeType(
                "Core", "Text", true));
        iconsQualifierLeafs.setName(ICONS_QUALIFIER_LEAFS);
        engine.updateAttribute(iconsQualifierLeafs);

        iconsQualifier.getSystemAttributes().add(iconsAttribute);
        iconsQualifier.getSystemAttributes().add(iconsQualifierOpenFolders);
        iconsQualifier.getSystemAttributes().add(iconsQualifierClosedFolders);
        iconsQualifier.getSystemAttributes().add(iconsQualifierLeafs);
        iconsQualifier.setName(ICONS_QUALIFIER);
        engine.updateQualifier(iconsQualifier);
    }

    private void loadAttributes() {
        for (Attribute attribute : qualifiers.getSystemAttributes()) {
            if (attribute.getName().equals(QUALIFIER_ATTRIBUTES))
                aList = attribute;
            else if (attribute.getName().equals(QUALIFIER_ID))
                aQualifierId = attribute;
            else if (attribute.getName().equals(ATTRIBUTE_NAME))
                nameAttribute = attribute;

        }

        for (Attribute attribute : attributes.getSystemAttributes()) {
            if (attribute.getName().equals(ATTRIBUTE_ID)) {
                aAttributeId = attribute;
            } else if (attribute.getName().equals(ATTRIBUTE_TYPE_NAME))
                attributeTypeName = attribute;
        }
    }

    private void initQualifiers(Engine engine) {
        qualifiers = engine.createSystemQualifier();
        attributes = engine.createSystemQualifier();
        qualifiers.setName(QUALIFIERS_QUALIFIER);
        attributes.setName(ATTRIBUTES_QUALIFIER);
        aList = engine.createSystemAttribute(new AttributeType("Core",
                "ElementList", false));

        aList.setName(QUALIFIER_ATTRIBUTES);

        aQualifierId = engine.createSystemAttribute(new AttributeType("Core",
                "Long", true));

        aQualifierId.setName(QUALIFIER_ID);

        engine.updateAttribute(aQualifierId);

        aAttributeId = engine.createSystemAttribute(new AttributeType("Core",
                "Long", true));

        aAttributeId.setName(ATTRIBUTE_ID);

        engine.updateAttribute(aAttributeId);

        nameAttribute = engine.createSystemAttribute(new AttributeType("Core",
                "Text", true));

        nameAttribute.setName(ATTRIBUTE_NAME);
        engine.updateAttribute(nameAttribute);

        attributeTypeName = engine.createSystemAttribute(new AttributeType(
                "Core", "Text", true));
        attributeTypeName.setName(ATTRIBUTE_TYPE_NAME);
        engine.updateAttribute(attributeTypeName);

        ElementListPropertyPersistent property = new ElementListPropertyPersistent();
        property.setQualifier1(qualifiers.getId());
        property.setQualifier2(attributes.getId());
        engine.setAttribute(null, aList, property);

        qualifiers.getSystemAttributes().add(aList);
        qualifiers.getSystemAttributes().add(aQualifierId);
        qualifiers.getSystemAttributes().add(nameAttribute);
        qualifiers.setAttributeForName(nameAttribute.getId());

        Attribute h = (Attribute) engine.getPluginProperty("Core",
                HierarchicalPlugin.HIERARHICAL_ATTRIBUTE);

        qualifiers.getSystemAttributes().add(h);

        attributes.getSystemAttributes().add(aList);
        attributes.getSystemAttributes().add(h);
        attributes.getSystemAttributes().add(aAttributeId);
        attributes.getSystemAttributes().add(nameAttribute);
        attributes.getSystemAttributes().add(attributeTypeName);
        attributes.setAttributeForName(nameAttribute.getId());

        engine.updateQualifier(qualifiers);
        engine.updateQualifier(attributes);
        engine.updateAttribute(aList);
    }

    public static long getQualifierId(Engine engine, long elementId) {
        Element element = engine.getElement(elementId);
        return getQualifierId(engine, element);
    }

    public static long getQualifierId(Engine engine, Element element) {
        Attribute qId = (Attribute) engine.getPluginProperty("Core",
                QUALIFIER_ID);
        return (Long) engine.getAttribute(element, qId);
    }

    public static Qualifier getQualifier(Engine engine, Element element) {
        return engine.getQualifier(getQualifierId(engine, element));
    }

    public static Qualifier getAttributesQualifier(Engine engine) {
        StandardAttributesPlugin plugin = getStandardPlugin(engine);
        return plugin.attributes;
    }

    public static Long getAttributeId(Engine engine, Element element) {
        StandardAttributesPlugin plugin = getStandardPlugin(engine);
        return (Long) engine.getAttribute(element, plugin.aAttributeId);
    }

    public static Element getElementForAttribute(Engine engine,
                                                 Attribute attribute) {
        StandardAttributesPlugin plugin = getStandardPlugin(engine);
        List<Element> elements = engine.findElements(plugin.attributes.getId(),
                plugin.aAttributeId, attribute.getId());
        if (elements.size() > 0)
            return elements.get(0);
        return null;
    }

    public static Attribute getAttributeTypeNameAttribute(Engine engine) {
        StandardAttributesPlugin plugin = getStandardPlugin(engine);
        return plugin.attributeTypeName;
    }

    public static Attribute getAttributeNameAttribute(Engine engine) {
        StandardAttributesPlugin plugin = getStandardPlugin(engine);
        return plugin.nameAttribute;
    }

    public static Qualifier getQualifiersQualifier(Engine engine) {
        StandardAttributesPlugin plugin = getStandardPlugin(engine);

        return plugin.qualifiers;
    }

    public static Attribute getAttributeQualifierId(Engine engine) {
        StandardAttributesPlugin plugin = getStandardPlugin(engine);
        return plugin.aQualifierId;
    }

    public static Attribute getAttributeAttributeId(Engine engine) {
        StandardAttributesPlugin plugin = getStandardPlugin(engine);
        return plugin.aAttributeId;
    }

    public static Attribute getQualifierAttributesAttribute(Engine engine) {
        StandardAttributesPlugin plugin = getStandardPlugin(engine);
        return plugin.aList;
    }

    public static Attribute getIconsAttribute(Engine engine) {
        return (Attribute) engine.getPluginProperty("Core", ICONS_ATTRIBUTE);
    }

    public static Attribute getOpenIconsAttribute(Engine engine) {
        return (Attribute) engine.getPluginProperty("Core",
                ICONS_QUALIFIER_OPEN_FOLDERS);
    }

    public static Attribute getClosedIconsAttribute(Engine engine) {
        return (Attribute) engine.getPluginProperty("Core",
                ICONS_QUALIFIER_CLOSED_FOLDERS);
    }

    public static Attribute getLeafIconsAttribute(Engine engine) {
        return (Attribute) engine.getPluginProperty("Core",
                ICONS_QUALIFIER_LEAFS);
    }

    public static Qualifier getIconsQualifier(Engine engine) {
        return (Qualifier) engine.getPluginProperty("Core", ICONS_QUALIFIER);
    }

    /**
     * @param autocreateFromQualifiers the autocreateFromQualifiers to set
     */
    public static void setAutocreateFromQualifiers(
            boolean autocreateFromQualifiers) {
        StandardAttributesPlugin.autocreateFromQualifiers = autocreateFromQualifiers;
    }

    /**
     * @return the autocreateFromQualifiers
     */
    public static boolean isAutocreateFromQualifiers() {
        return autocreateFromQualifiers;
    }

    /**
     * @param disableAutoupdate the disableAutoupdate to set
     */
    public static void setDisableAutoupdate(Engine engine,
                                            boolean disableAutoupdate) {
        StandardAttributesPlugin sap = (StandardAttributesPlugin) engine
                .getPluginProperty("Core", STANDARD_ATTRIBUTES_PLUGIN);
        sap.disableAutoupdate = disableAutoupdate;
    }

    /**
     * @return the disableAutoupdate
     */
    public static boolean isDisableAutoupdate(Engine engine) {
        StandardAttributesPlugin sap = (StandardAttributesPlugin) engine
                .getPluginProperty("Core", STANDARD_ATTRIBUTES_PLUGIN);
        return sap.disableAutoupdate;
    }

    public static Attribute getAttribute(Engine engine, Element element) {
        return engine.getAttribute(getAttributeId(engine, element));
    }

    public static String getTableQualifeirName(Attribute attribute) {
        return TABLE_QUALIFIER + attribute.getId();
    }

    public static String getTableQualifeirName(long attributeId) {
        return TABLE_QUALIFIER + attributeId;
    }

    public static Attribute getTableElementIdAttribute(Engine engine) {
        StandardAttributesPlugin plugin = getStandardPlugin(engine);
        return plugin.tableElementIdAttribute;
    }

    private static StandardAttributesPlugin getStandardPlugin(Engine engine) {
        return (StandardAttributesPlugin) engine.getPluginProperty("Core",
                STANDARD_ATTRIBUTES_PLUGIN);
    }

    public static Qualifier getTableQualifierForAttribute(Engine engine,
                                                          Attribute attribute) {
        return engine.getSystemQualifier(getTableQualifeirName(attribute));
    }

    public static Qualifier getTableQualifierForAttribute(Engine engine,
                                                          long attributeId) {
        return engine.getSystemQualifier(getTableQualifeirName(attributeId));
    }

    public static List<Element> getTableElements(Engine engine,
                                                 Attribute attribute, Element element) {
        Qualifier qualifier = engine
                .getSystemQualifier(getTableQualifeirName(attribute));

        if (qualifier == null)
            return new ArrayList<Element>(0);
        return engine.findElements(qualifier.getId(),
                getTableElementIdAttribute(engine), element.getId());
    }

    public static Element createTableElement(Engine engine,
                                             Attribute attribute, Element element) {
        Qualifier qualifier = engine
                .getSystemQualifier(getTableQualifeirName(attribute));
        Element res = engine.createElement(qualifier.getId());
        engine.setAttribute(res, getTableElementIdAttribute(engine),
                element.getId());
        return res;
    }

    public static Element getAttributeElement(Engine engine, Attribute attribute) {
        StandardAttributesPlugin plugin = getStandardPlugin(engine);
        List<Element> list = engine.findElements(plugin.attributes.getId(),
                plugin.aAttributeId, attribute.getId());
        if (list.size() > 0)
            return list.get(0);
        return null;
    }

    public static Attribute getHierarchicalAttribute(Engine engine) {
        return (Attribute) engine.getPluginProperty("Core",
                HierarchicalPlugin.HIERARHICAL_ATTRIBUTE);
    }

    public static Element getElement(Engine engine, long qualifierId) {
        StandardAttributesPlugin plugin = getStandardPlugin(engine);
        Qualifier qualifier = getQualifiersQualifier(engine);
        List<Element> list = engine.findElements(qualifier.getId(),
                plugin.aQualifierId, qualifierId);
        if (list.size() > 0)
            return list.get(0);
        return null;
    }

    public static boolean isTableQualifier(Qualifier qualifier) {
        return (qualifier.isSystem())
                && (qualifier.getName().startsWith(TABLE_QUALIFIER));
    }

    public static Element getElementForTableElement(Engine engine,
                                                    Element element) {
        StandardAttributesPlugin plugin = getStandardPlugin(engine);
        Long id = (Long) engine.getAttribute(element,
                plugin.tableElementIdAttribute);
        if (id == null)
            return null;
        return engine.getElement(id);
    }

    /**
     * Даний метод вимикає або вмикає всі автооновлення (тригири).
     */

    public static void setDefaultDisableAutoupdate(boolean b) {
        synchronized (lock) {
            defaultDisableAutoupdate = b;
        }
    }

    public static Attribute[] getElementLists(Engine engine) {
        List<Attribute> list = engine.getAttributes();
        List<Attribute> res = new ArrayList<Attribute>();

        for (Attribute attribute : list) {
            if (attribute.getAttributeType().toString()
                    .equals("Core.ElementList")) {
                res.add(attribute);
            }
        }

        return res.toArray(new Attribute[res.size()]);
    }

    public static void updateTableElementsElement(Engine engine,
                                                  List<Element> listToMove, Element element) {
        Attribute attribute = getTableElementIdAttribute(engine);
        for (Element element2 : listToMove) {
            engine.setAttribute(element2, attribute, element.getId());
        }
    }

    public static Qualifier getHistoryQualifier(Engine engine) {
        StandardAttributesPlugin plugin = getStandardPlugin(engine);
        return plugin.historyQualifier;
    }

    public static Hashtable<Long, List<Long>> getHistoryQualifiers(Engine engine) {
        StandardAttributesPlugin plugin = getStandardPlugin(engine);
        return plugin.historyQualifiers;
    }

    public static List<Record> getHistory(Engine engine, Element element,
                                          Attribute attribute) {
        StandardAttributesPlugin plugin = getStandardPlugin(engine);
        List<Element> elements = engine.findElements(
                plugin.historyQualifier.getId(), plugin.historyElement,
                element.getId());
        List<Record> res = new ArrayList<Record>();

        for (Element element2 : elements) {
            Long id = (Long) engine.getAttribute(element2,
                    plugin.historyAttribute);
            if (id.longValue() == attribute.getId()) {
                Record record = new Record();
                record.setDate((Date) engine.getAttribute(element2,
                        plugin.historyTime));
                record.setValue(engine.getAttribute(element2, attribute));
                record.setElement(element2);
                res.add(record);
            }
        }

        Collections.sort(res);
        return res;
    }

    public static boolean hasHistory(Engine engine, Element element,
                                     Attribute attribute) {
        StandardAttributesPlugin plugin = getStandardPlugin(engine);
        List<Element> elements = engine.findElements(
                plugin.historyQualifier.getId(), plugin.historyElement,
                element.getId());
        for (Element element2 : elements) {
            Long id = (Long) engine.getAttribute(element2,
                    plugin.historyAttribute);
            if (id.longValue() == attribute.getId())
                return true;
        }
        return false;
    }

    public static void saveTableAttributeOrder(Engine engine,
                                               Element tableElement, Attribute tableAttribute,
                                               List<Element> elements) {
        String path = getTablePath(tableElement.getId(), tableAttribute.getId());
        OutputStream os = engine.getOutputStream(path);
        try {
            StringBuffer sb = new StringBuffer();
            for (Element element : elements) {
                sb.append(element.getId());
                sb.append(' ');
            }

            os.write(sb.toString().getBytes("UTF-8"));

            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Element> getOrderedTableElements(Engine engine,
                                                        Attribute attribute, Element element) {
        List<Element> elements = getTableElements(engine, attribute, element);

        String path = getTablePath(element.getId(), attribute.getId());
        byte[] bs = engine.getStream(path);
        if (bs == null)
            return elements;

        List<Element> res = new ArrayList<Element>(elements.size());

        try {
            StringTokenizer st = new StringTokenizer(new String(bs, "UTF-8"));
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                long id = Long.parseLong(s);
                Element e = null;
                for (Element element2 : elements) {
                    if (element2.getId() == id) {
                        e = element2;
                        break;
                    }
                }
                if (e != null) {
                    res.add(e);
                    elements.remove(e);
                }
            }
            res.addAll(elements);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return res;
    }

    public static String getTablePath(long elementId, long attributeId) {
        return "/elements/" + elementId + "/" + attributeId
                + "/Core/table-order.txt";
    }

    public static Element getParentElement(Engine engine, long elementId) {
        Attribute h = getHierarchicalAttribute(engine);
        Element element = engine.getElement(elementId);
        if (element == null)
            return null;
        HierarchicalPersistent hp = (HierarchicalPersistent) engine
                .getAttribute(element, h);
        if (hp != null)
            return engine.getElement(hp.getParentElementId());
        return null;
    }

    public static Element createElement(Engine engine, long qualifierId,
                                        long parentElementId) {
        Element element = engine.createElement(qualifierId);
        Attribute h = getHierarchicalAttribute(engine);

        HierarchicalPersistent hp = new HierarchicalPersistent();
        hp.setPreviousElementId(-1);
        hp.setParentElementId(parentElementId);
        engine.setAttribute(element, h, hp);

        return element;
    }

    public static boolean isNameType(AttributeType attributeType) {
        return attributeType.getTypeName().equals("Text")
                || attributeType.getTypeName().equals("DFDSName");
    }
}
