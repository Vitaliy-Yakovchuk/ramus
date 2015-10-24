package com.ramussoft.gui.attribute;

import info.clearthought.layout.TableLayout;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.QualifierAdapter;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.core.attribute.simple.OtherElementPropertyPersistent;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.common.AttributePreferenciesEditor;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.QualifierImporter;
import com.ramussoft.gui.common.event.CloseMainFrameAdapter;
import com.ramussoft.gui.qualifier.table.TableNode;
import com.ramussoft.gui.qualifier.table.TabledAttributePlugin;
import com.ramussoft.gui.qualifier.table.ValueGetter;
import com.ramussoft.gui.qualifier.table.event.Closeable;

public class OtherElementPlugin extends AbstractAttributePlugin implements
        TabledAttributePlugin {

    private Hashtable<Qualifier, RowSetValue> sets = new Hashtable<Qualifier, RowSetValue>();
    private OtherElementEditor otherElementEditor;
    private Attribute attribute;

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("Core", "OtherElement", false);
    }

    @Override
    public void setFramework(GUIFramework framework) {
        super.setFramework(framework);

        final QualifierListener listener = new QualifierAdapter() {
            @Override
            public void qualifierDeleted(QualifierEvent event) {
                RowSetValue value = sets.get(event.getOldQualifier());
                if (value != null) {
                    value.rowSet.close();
                    sets.remove(event.getOldQualifier());
                }
            }
        };

        framework.getEngine().addQualifierListener(listener);

        framework.addCloseMainFrameListener(new CloseMainFrameAdapter() {

            @Override
            public void closed() {
                for (RowSetValue value : sets.values()) {
                    try {
                        value.rowSet.close();
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }
                sets.clear();
                OtherElementPlugin.this.framework.getEngine()
                        .removeQualifierListener(listener);
            }

        });
    }

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
        OtherElementValueGetter getter = (OtherElementValueGetter) getValueGetter(
                attribute, engine, framework, null);

        return new OtherElementTableCellEditor(getter.rowSet, getter.attribute,
                framework, attribute);
    }

    @Override
    public String getName() {
        return "Core";
    }

    @Override
    public AttributeEditor getAttributeEditor(final Engine engine,
                                              final AccessRules rules, final Element element,
                                              final Attribute attribute, String propertiesPrefix,
                                              AttributeEditor old) {
        if (old != null) {
            if ((old == otherElementEditor)
                    && (this.attribute.equals(attribute)))
                return otherElementEditor;
            else
                old.close();
        }
        OtherElementPropertyPersistent p = (OtherElementPropertyPersistent) engine
                .getAttribute(null, attribute);

        final Qualifier qualifier = engine.getQualifier(p.getQualifier());

        this.attribute = attribute;

        otherElementEditor = new OtherElementEditor(engine, rules, qualifier,
                framework, propertiesPrefix);
        return otherElementEditor;
    }

    @Override
    public AttributePreferenciesEditor getAttributePreferenciesEditor() {
        return new AttributePreferenciesEditor() {

            private JComboBox qualifierBox = new JComboBox();

            private JComboBox attributeBox = new JComboBox();

            @Override
            public void apply(Attribute attribute, Engine engine,
                              AccessRules accessRules) {
                OtherElementPropertyPersistent p = new OtherElementPropertyPersistent();
                p.setQualifier(((Qualifier) qualifierBox.getSelectedItem())
                        .getId());
                p.setQualifierAttribute(((Attribute) attributeBox
                        .getSelectedItem()).getId());
                engine.setAttribute(null, attribute, p);
            }

            @Override
            public boolean canApply() {
                return (qualifierBox.getSelectedItem() != null)
                        && (attributeBox.getSelectedItem() != null);
            }

            @Override
            public JComponent createComponent(Attribute attribute,
                                              Engine engine, AccessRules accessRules) {
                double[][] size = {
                        {5, TableLayout.FILL, 5, TableLayout.FILL, 5},
                        {5, TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5}};
                JPanel panel = new JPanel(new TableLayout(size));
                panel.add(new JLabel(GlobalResourcesManager
                        .getString("OtherElement.Qualifier")), "1, 1");
                panel.add(new JLabel(GlobalResourcesManager
                        .getString("OtherElement.Attribute")), "3, 1");
                for (Qualifier qualifier : engine.getQualifiers()) {
                    qualifierBox.addItem(qualifier);
                }
                if (attribute != null) {
                    OtherElementPropertyPersistent p = (OtherElementPropertyPersistent) engine
                            .getAttribute(null, attribute);
                    qualifierBox.setSelectedItem(engine.getQualifier(p
                            .getQualifier()));
                    long attributeId = p.getQualifierAttribute();
                    if (attributeId >= 0)
                        attributeBox.setSelectedItem(engine
                                .getAttribute(attributeId));
                    qualifierBox.setEnabled(false);
                }

                qualifierBox.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            loadAttributes();
                        }
                    }
                });

                loadAttributes();

                panel.add(qualifierBox, "1, 3");
                panel.add(attributeBox, "3, 3");

                return panel;
            }

            private void loadAttributes() {
                attributeBox.removeAllItems();
                Qualifier qualifier = (Qualifier) qualifierBox
                        .getSelectedItem();
                if (qualifier == null)
                    return;
                for (Attribute attribute : qualifier.getAttributes()) {
                    attributeBox.addItem(attribute);
                }
            }

        };
    }

    @Override
    public ValueGetter getValueGetter(Attribute attribute, Engine engine,
                                      GUIFramework framework, final Closeable model) {

        OtherElementPropertyPersistent p = (OtherElementPropertyPersistent) engine
                .getAttribute(null, attribute);

        final Qualifier q = engine.getQualifier(p.getQualifier());

        final Attribute attr = engine.getAttribute(p.getQualifierAttribute());

        final RowSet rowSet;

        if ((q != null) && (attr != null)) {
            RowSetValue value = sets.get(q);
            if ((value != null) && (!value.attribute.equals(attr))) {
                value.rowSet.close();
                sets.remove(q);
                value = null;
            }

            if (value == null) {
                value = new RowSetValue(attr, new RowSet(engine, q,
                        new Attribute[]{attr}));
                sets.put(q, value);
            }

            rowSet = value.rowSet;
        } else
            rowSet = null;

        return new OtherElementValueGetter(attr, rowSet, q);
    }

    private class OtherElementValueGetter implements ValueGetter {

        private Attribute attribute;

        private RowSet rowSet;

        private Qualifier qualifier;

        public OtherElementValueGetter(Attribute attribute, RowSet rowSet,
                                       Qualifier qualifier) {
            this.attribute = attribute;
            this.rowSet = rowSet;
            this.qualifier = qualifier;
        }

        @Override
        public Object getValue(TableNode node, int index) {
            if ((qualifier == null) || (attribute == null))
                return GlobalResourcesManager
                        .getString("AttributeOrQualifierNotFound");
            Object object = node.getValueAt(index);
            if (object == null)
                return null;
            Row row = rowSet.findRow((Long) object);
            if (row == null)
                return null;
            return new RowAttributeWrapper(row, attribute);
        }

    }

    ;

    public static class RowAttributeWrapper {

        private Row row;

        private Attribute attribute;

        public RowAttributeWrapper(Row row, Attribute attribute) {
            this.row = row;
            this.attribute = attribute;
        }

        public Row getRow() {
            return row;
        }

        public Attribute getAttribute() {
            return attribute;
        }

        @Override
        public String toString() {
            Object value = row.getAttribute(attribute);
            if (value == null)
                return "";
            return value.toString();
        }
    }

    ;

    @Override
    public boolean isCellEditable() {
        return true;
    }

    @Override
    public Attribute createSyncAttribute(Engine engine,
                                         QualifierImporter importer, Attribute sourceAttribute) {

        OtherElementPropertyPersistent pp = (OtherElementPropertyPersistent) importer
                .getSourceValue(null, sourceAttribute);

        if (pp == null)
            return null;

        Qualifier o = null;

        for (Qualifier q : importer.getSourceQualifiers()) {
            if (q.getId() == pp.getQualifier())
                o = q;
        }
        Qualifier qualifier = null;
        if (o == null) {
            o = importer.getSource().getQualifier(pp.getQualifier());
            if (o != null) {
                qualifier = engine.getQualifierByName(o.getName());
                if (qualifier == null)
                    o = null;
            }
        }

        if (o != null) {

            if (qualifier == null)
                qualifier = importer.getDestination(o);

            Attribute result = null;
            List<Attribute> attributes = engine.getAttributes();
            for (Attribute attr : attributes) {
                if (isSameAttribute(sourceAttribute, attr)) {
                    OtherElementPropertyPersistent opp = (OtherElementPropertyPersistent) engine
                            .getAttribute(null, attr);
                    if ((opp != null)
                            && (opp.getQualifier() == qualifier.getId())) {
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

            return result;
        }
        return null;
    }

    @Override
    public void syncElement(Engine engine, QualifierImporter importer,
                            Element sourceElement, Attribute sourceAttribute) {
        Long id = (Long) importer
                .getSourceValue(sourceElement, sourceAttribute);
        if (id == null)
            return;
        Element dest = importer.getDestinationElement(id);
        if (dest != null)
            engine.setAttribute(importer.getDestination(sourceElement),
                    importer.getDestination(sourceAttribute), dest.getId());
        else {
            Element sourceElementValue = importer.getSource().getElement(id);
            if (sourceElementValue == null)
                return;
            Qualifier sq = importer.getSource().getQualifier(
                    sourceElementValue.getQualifierId());
            Qualifier qualifier = importer.getDestination(sq);

            if (qualifier == null) {
                qualifier = engine.getQualifierByName(sq.getName());
            }
            if (qualifier == null)
                return;
            Element d = engine.getElement(sourceElementValue.getName(),
                    qualifier.getId());
            if (d == null)
                return;
            engine.setAttribute(importer.getDestination(sourceElement),
                    importer.getDestination(sourceAttribute), d.getId());
        }

    }

    @Override
    public void syncAttribute(Engine engine, QualifierImporter importer,
                              Attribute sourceAttribute) {
        OtherElementPropertyPersistent pp = (OtherElementPropertyPersistent) importer
                .getSourceValue(null, sourceAttribute);

        Qualifier o = null;

        for (Qualifier q : importer.getSourceQualifiers()) {
            if (q.getId() == pp.getQualifier())
                o = q;
        }

        if (o == null)
            o = importer.getSource().getQualifier(pp.getQualifier());
        Qualifier qualifier = importer.getDestination(o);

        if (qualifier == null)
            qualifier = engine.getQualifierByName(o.getName());

        OtherElementPropertyPersistent opp = new OtherElementPropertyPersistent();
        opp.setQualifier(qualifier.getId());
        Attribute attribute = importer.getDestinationAttribute(pp
                .getQualifierAttribute());
        if (attribute != null)
            opp.setQualifierAttribute(attribute.getId());
        engine
                .setAttribute(null, importer.getDestination(sourceAttribute),
                        opp);
    }

    private class RowSetValue {

        private Attribute attribute;

        private RowSet rowSet;

        public RowSetValue(Attribute attribute, RowSet rowSet) {
            this.attribute = attribute;
            this.rowSet = rowSet;
        }
    }

    ;
}
