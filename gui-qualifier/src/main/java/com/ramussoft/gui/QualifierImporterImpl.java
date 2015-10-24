package com.ramussoft.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.simple.HierarchicalPersistent;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.QualifierImporter;

public class QualifierImporterImpl implements QualifierImporter {

    public static final int ELEMENT_IMPORT_TYPE_UPDATE = 0;

    public static final int ELEMENT_IMPORT_TYPE_SKIP = 1;

    private Engine source;

    private Qualifier[] sourceQualifiers;

    private Hashtable<Qualifier, Qualifier> qualifiers = new Hashtable<Qualifier, Qualifier>();

    private Hashtable<Element, Element> elements = new Hashtable<Element, Element>();

    private Hashtable<Attribute, Attribute> attributes = new Hashtable<Attribute, Attribute>();

    private GUIFramework framework;

    private Engine engine;

    private Hashtable<Element, Boolean> createdElemets = new Hashtable<Element, Boolean>();

    private Row[] sourceRows;

    private ArrayList<Qualifier> tableSourceQualifiers;

    public QualifierImporterImpl(Engine source, Engine destination,
                                 GUIFramework framework, Qualifier[] sourceQualifiers,
                                 Row[] sourceRows) {
        this.source = source;
        this.sourceQualifiers = sourceQualifiers;
        this.engine = destination;
        this.framework = framework;
        this.sourceRows = sourceRows;
    }

    @Override
    public Qualifier getDestination(Qualifier source) {
        if (source == null)
            return null;
        return qualifiers.get(source);
    }

    @Override
    public Attribute getDestination(Attribute source) {
        if (source == null)
            return null;
        return attributes.get(source);
    }

    @Override
    public Element getDestination(Element source) {
        if (source == null)
            return null;
        return elements.get(source);
    }

    @Override
    public Object getSourceValue(Element element, Attribute attribute) {
        return source.getAttribute(element, attribute);
    }

    @Override
    public Qualifier[] getSourceQualifiers() {
        return sourceQualifiers;
    }

    public void importQualifiers(int elementImportType,
                                 boolean importTableAttributes) {
        int i = 0;
        for (Qualifier sourceQualifier : sourceQualifiers) {
            Qualifier qualifier = createQualifier(sourceQualifier,
                    sourceRows[i]);
            qualifiers.put(sourceQualifier, qualifier);
            i++;
        }

        if (importTableAttributes)
            createTableAttributes();

        for (Qualifier sourceQualifier : sourceQualifiers) {
            Qualifier qualifier = getDestination(sourceQualifier);

            Attribute attributeForDestName = null;

            for (Attribute sourceAttribute : sourceQualifier.getAttributes()) {
                Attribute destAttribute = getDestination(sourceAttribute);
                if (destAttribute == null) {
                    AttributePlugin plugin = framework
                            .findAttributePlugin(sourceAttribute
                                    .getAttributeType());
                    if (plugin != null) {
                        destAttribute = plugin.createSyncAttribute(engine,
                                this, sourceAttribute);
                        if (destAttribute != null) {
                            attributes.put(sourceAttribute, destAttribute);
                        }
                    }
                }
                if (destAttribute != null) {
                    if (qualifier.getAttributes().indexOf(destAttribute) < 0) {
                        qualifier.getAttributes().add(destAttribute);
                    }
                    if (sourceQualifier.getAttributeForName() == sourceAttribute
                            .getId())
                        attributeForDestName = destAttribute;
                }
            }

            if (attributeForDestName != null)
                qualifier.setAttributeForName(attributeForDestName.getId());

            engine.updateQualifier(qualifier);
        }

        for (Qualifier sourceQualifier : sourceQualifiers) {
            for (Attribute sourceAttribute : sourceQualifier.getAttributes()) {
                Attribute destAttribute = getDestination(sourceAttribute);
                if (destAttribute != null) {
                    AttributePlugin plugin = framework
                            .findAttributePlugin(sourceAttribute
                                    .getAttributeType());
                    if (plugin != null) {
                        plugin.syncAttribute(engine, this, sourceAttribute);
                    }
                }

            }
        }

        for (Qualifier sourceQualifier : sourceQualifiers) {
            Qualifier qualifier = getDestination(sourceQualifier);

            List<Element> sourceElements = source.getElements(sourceQualifier
                    .getId());
            List<Element> destElements = engine.getElements(qualifier.getId());
            for (Element sourceElement : sourceElements) {
                Element dest = createElement(sourceElement, destElements,
                        qualifier.getId());
                elements.put(sourceElement, dest);
            }
        }
        if (importTableAttributes)
            setupTableElements();

        Attribute hAttribute = StandardAttributesPlugin
                .getHierarchicalAttribute(source);

        for (Qualifier sourceQualifier : sourceQualifiers) {
            List<Element> sourceElements = source.getElements(sourceQualifier
                    .getId());

            boolean h = sourceQualifier.getSystemAttributes().indexOf(
                    hAttribute) >= 0;

            for (Element sourceElement : sourceElements) {
                Element dest = getDestination(sourceElement);

                if (h && (createdElemets.get(sourceElement) != null)) {

                    HierarchicalPersistent hp = (HierarchicalPersistent) getSourceValue(
                            sourceElement, hAttribute);
                    Element prev = source.getElement(hp.getPreviousElementId());
                    Element parent = source.getElement(hp.getParentElementId());
                    HierarchicalPersistent set = new HierarchicalPersistent();
                    if (prev == null)
                        set.setPreviousElementId(-1l);
                    else
                        set.setPreviousElementId(getDestination(prev).getId());

                    if (parent == null) {
                        set.setParentElementId(-1l);
                    } else {
                        Element destination = getDestination(parent);
                        if (destination != null)
                            set.setParentElementId(destination.getId());
                        else
                            set.setParentElementId(-1l);
                    }

                    engine.setAttribute(dest, StandardAttributesPlugin
                            .getHierarchicalAttribute(engine), set);
                }
            }

            Attribute[] attributes = sourceQualifier.getAttributes().toArray(
                    new Attribute[sourceQualifier.getAttributes().size()]);

            Arrays.sort(attributes, new Comparator<Attribute>() {
                @Override
                public int compare(Attribute o1, Attribute o2) {
                    AttributePlugin plugin1 = framework.findAttributePlugin(o1
                            .getAttributeType());
                    AttributePlugin plugin2 = framework.findAttributePlugin(o1
                            .getAttributeType());

                    if (plugin1 == null) {
                        if (plugin2 == null)
                            return 0;
                        else
                            return -1;
                    }

                    if (plugin2 == null)
                        return 1;

                    if (plugin1.getSyncPriority() > plugin2.getSyncPriority())
                        return 1;
                    if (plugin1.getSyncPriority() < plugin2.getSyncPriority())
                        return -1;

                    return 0;
                }
            });

            for (Attribute sourceAttribute : attributes) {
                Attribute destAttribute = getDestination(sourceAttribute);

                if (destAttribute != null) {
                    AttributePlugin plugin = framework
                            .findAttributePlugin(destAttribute
                                    .getAttributeType());
                    for (Element sourceElement : sourceElements) {
                        if ((createdElemets.get(sourceElement) != null)
                                || (elementImportType == ELEMENT_IMPORT_TYPE_UPDATE)) {

                            if (plugin != null)
                                plugin.syncElement(engine, this, sourceElement,
                                        sourceAttribute);
                        }
                    }
                }
            }

        }

    }

    private void setupTableElements() {
        List<Attribute> attributes = new ArrayList<Attribute>(1);
        attributes.add(StandardAttributesPlugin
                .getTableElementIdAttribute(source));
        Attribute toSet = StandardAttributesPlugin
                .getTableElementIdAttribute(engine);
        for (Qualifier q : tableSourceQualifiers) {
            Hashtable<Element, Object[]> table = source.getElements(q,
                    attributes);
            for (Entry<Element, Object[]> entry : table.entrySet()) {
                Long l = (Long) entry.getValue()[0];
                if (l != null) {
                    Element s = source.getElement(l);
                    Element d = getDestination(s);
                    if (d != null) {
                        Element dest = getDestination(entry.getKey());
                        engine.setAttribute(dest, toSet, d.getId());
                    }
                }
            }
        }
    }

    private void createTableAttributes() {
        tableSourceQualifiers = new ArrayList<Qualifier>();
        for (Qualifier sourceQualifier : sourceQualifiers) {
            for (Attribute attribute : sourceQualifier.getAttributes()) {
                if (attribute.getAttributeType().toString()
                        .equals("Core.Table")) {
                    Attribute dest = getDestination(attribute);
                    if (dest == null) {
                        dest = engine.createAttribute(attribute
                                .getAttributeType());
                        dest.setName(attribute.getName());
                        engine.updateAttribute(dest);
                        attributes.put(attribute, dest);

                        String name = StandardAttributesPlugin
                                .getTableQualifeirName(dest);
                        Qualifier qualifier = engine.createSystemQualifier();
                        qualifier.setName(name);
                        qualifier.getSystemAttributes().add(
                                StandardAttributesPlugin
                                        .getTableElementIdAttribute(engine));
                        Qualifier s = StandardAttributesPlugin
                                .getTableQualifierForAttribute(source,
                                        attribute);
                        tableSourceQualifiers.add(s);
                        engine.updateQualifier(qualifier);
                        this.qualifiers.put(s, qualifier);
                    }
                }
            }
        }
        sourceQualifiers = Arrays.copyOf(sourceQualifiers,
                sourceQualifiers.length + tableSourceQualifiers.size());
        for (int i = 0; i < tableSourceQualifiers.size(); i++)
            sourceQualifiers[sourceQualifiers.length
                    - tableSourceQualifiers.size() + i] = tableSourceQualifiers
                    .get(i);
    }

    private Element createElement(Element sourceElement,
                                  List<Element> destElements, long qualifierId) {
        for (Element dest : destElements) {
            if ((dest.getName() != null)
                    && (dest.getName().equals(sourceElement.getName())))
                return dest;
        }
        createdElemets.put(sourceElement, Boolean.TRUE);
        return engine.createElement(qualifierId);
    }

    private Qualifier createQualifier(Qualifier sourceQualifier, Row row) {
        Qualifier res = engine.getQualifierByName(sourceQualifier.getName());
        if (res == null) {
            Qualifier q = StandardAttributesPlugin
                    .getQualifiersQualifier(engine);
            RowSet rs = new RowSet(engine, q, new Attribute[]{});
            Row parent = findParent(rs, row.getParent());
            if ((parent != null) && (parent.getElement() == null))
                parent = null;
            if ((parent != null)
                    && (engine.getElements(
                    StandardAttributesPlugin.getQualifier(engine,
                            parent.getElement()).getId()).size() > 0))
                parent = null;
            Element element = rs.createRow(parent).getElement();
            res = StandardAttributesPlugin.getQualifier(engine, element);
            res.setName(sourceQualifier.getName());
            engine.updateQualifier(res);
            rs.close();
        }
        return res;
    }

    private Row findParent(RowSet rs, Row row) {
        if (row.getParent() == null)
            return rs.getRoot();
        Row parent = row.getParent();
        Row parent2 = findParent(rs, parent);
        if (parent2 != null) {
            for (Row row2 : parent2.getChildren())
                if (row2.getName().equals(row.getName()))
                    return row2;
        }
        return null;
    }

    @Override
    public Element getDestinationElement(long elementId) {
        if (elementId == -1l)
            return null;
        return getDestination(source.getElement(elementId));
    }

    @Override
    public Attribute getDestinationAttribute(long attributeId) {
        Attribute attribute = source.getAttribute(attributeId);
        if (attribute == null)
            return null;
        return getDestination(attribute);
    }

    @Override
    public Engine getSource() {
        return source;
    }
}
