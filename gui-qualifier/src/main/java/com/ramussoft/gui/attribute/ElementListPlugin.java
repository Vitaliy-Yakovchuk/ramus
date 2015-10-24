package com.ramussoft.gui.attribute;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableCellEditor;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.simple.ElementListPersistent;
import com.ramussoft.core.attribute.simple.ElementListPropertyPersistent;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.common.AttributePreferenciesEditor;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.QualifierImporter;
import com.ramussoft.gui.qualifier.table.GroupNode;
import com.ramussoft.gui.qualifier.table.TableNode;
import com.ramussoft.gui.qualifier.table.TabledAttributePlugin;
import com.ramussoft.gui.qualifier.table.ValueGetter;
import com.ramussoft.gui.qualifier.table.event.Closeable;

public class ElementListPlugin extends AbstractAttributePlugin implements
        TabledAttributePlugin {

    private ElementListAttributeEditor elementListAttributeEditor;
    private Attribute attribute;
    private boolean left;

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("Core", "ElementList", false);
    }

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
        return null;
    }

    @Override
    public String getName() {
        return "Core";
    }

    @Override
    public AttributeEditor getAttributeEditor(Engine engine, AccessRules rules,
                                              Element element, Attribute attribute, String propertiesPrefix,
                                              AttributeEditor old) {

        ElementListPropertyPersistent p = (ElementListPropertyPersistent) engine
                .getAttribute(null, attribute);

        long qId = element.getQualifierId();

        Qualifier qualifier;

        boolean left;

        if (p.getQualifier1() == qId) {
            qualifier = engine.getQualifier(p.getQualifier2());
            left = false;
        } else {
            qualifier = engine.getQualifier(p.getQualifier1());
            left = true;
        }

        if (old != null) {
            if ((old == elementListAttributeEditor)
                    && (this.attribute.equals(attribute))
                    && (this.left == left)) {
                elementListAttributeEditor.setElement(element);
                return elementListAttributeEditor;
            } else
                old.close();
        }

        this.attribute = attribute;

        this.left = left;

        elementListAttributeEditor = new ElementListAttributeEditor(framework,
                qualifier, left, propertiesPrefix);
        elementListAttributeEditor.setElement(element);
        return elementListAttributeEditor;
    }

    @Override
    public AttributePreferenciesEditor getAttributePreferenciesEditor() {
        return new ElementListPreferenciesEditor();
    }

    @Override
    public ValueGetter getValueGetter(Attribute attribute, Engine engine,
                                      GUIFramework framework, Closeable model) {
        return new ValueGetter() {
            @Override
            public Object getValue(TableNode node, int index) {
                if ((node instanceof GroupNode) && (index == 0)) {
                    return ((GroupNode) node).toString();
                }
                return GlobalResourcesManager
                        .getString("AttributeType.Core.ElementList");
            }
        };
    }

    @Override
    public boolean isCellEditable() {
        return true;
    }

    @Override
    public Attribute createSyncAttribute(Engine engine,
                                         QualifierImporter importer, Attribute sourceAttribute) {

        ElementListPropertyPersistent pp = (ElementListPropertyPersistent) importer
                .getSourceValue(null, sourceAttribute);

        if (pp == null)
            return null;

        Qualifier left = null;
        Qualifier right = null;

        for (Qualifier q : importer.getSourceQualifiers()) {
            if (q.getId() == pp.getQualifier1())
                left = q;
            if (q.getId() == pp.getQualifier2())
                right = q;
        }

        if ((left != null) && (right != null)) {

            Qualifier qualifier1 = importer.getDestination(left);
            Qualifier qualifier2 = importer.getDestination(right);

            Attribute result = null;
            List<Attribute> attributes = engine.getAttributes();
            for (Attribute attr : attributes) {
                if (isSameAttribute(sourceAttribute, attr)) {
                    ElementListPropertyPersistent dpp = (ElementListPropertyPersistent) engine
                            .getAttribute(null, attr);
                    if ((dpp != null)
                            && (dpp.getQualifier1() == qualifier1.getId())
                            && (dpp.getQualifier2() == qualifier2.getId())) {
                        result = attr;
                        break;
                    }
                }
            }
            if (result != null)
                return result;

            result = engine.createAttribute(sourceAttribute.getAttributeType());
            result.setName(sourceAttribute.getName());
            engine.updateAttribute(result);

            ElementListPropertyPersistent dpp = new ElementListPropertyPersistent();
            dpp.setQualifier1(qualifier1.getId());
            dpp.setQualifier2(qualifier2.getId());
            engine.setAttribute(null, result, dpp);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void syncElement(Engine engine, QualifierImporter importer,
                            Element sourceElement, Attribute sourceAttribute) {
        List<ElementListPersistent> list = (List) importer.getSourceValue(
                sourceElement, sourceAttribute);
        List<ElementListPersistent> set = null;
        if (list != null) {
            for (ElementListPersistent p : list)
                if ((p != null) && (p.getElement1Id() == sourceElement.getId())) {
                    if (set == null)
                        set = new ArrayList<ElementListPersistent>(list.size());
                    Element element1 = importer.getDestinationElement(p
                            .getElement1Id());
                    Element element2 = importer.getDestinationElement(p
                            .getElement2Id());
                    if ((element1 != null) && (element2 != null)) {
                        ElementListPersistent d = new ElementListPersistent(
                                element1.getId(), element2.getId());
                        set.add(d);
                    }
                }
        }
        if (set != null)
            engine.setAttribute(importer.getDestination(sourceElement),
                    importer.getDestination(sourceAttribute), set);
    }

}
