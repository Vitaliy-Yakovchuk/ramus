/**
 *
 */
package com.ramussoft.gui.attribute;

import java.awt.BorderLayout;

import info.clearthought.layout.TableLayout;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.simple.ElementListPropertyPersistent;
import com.ramussoft.gui.common.AttributePreferenciesEditor;
import com.ramussoft.gui.common.GlobalResourcesManager;

public class ElementListPreferenciesEditor implements
        AttributePreferenciesEditor {

    private JComboBox box1 = new JComboBox();

    private JComboBox box2 = new JComboBox();

    private JTextArea variantsArea = new JTextArea();

    @Override
    public void apply(Attribute attribute, Engine engine,
                      AccessRules accessRules) {
        ElementListPropertyPersistent p = new ElementListPropertyPersistent();
        p.setQualifier1(((Qualifier) box1.getSelectedItem()).getId());
        p.setQualifier2(((Qualifier) box2.getSelectedItem()).getId());
        p.setConnectionTypes(variantsArea.getText());
        engine.setAttribute(null, attribute, p);
    }

    @Override
    public boolean canApply() {
        return !(((box1.getSelectedItem() == null) || (box2.getSelectedItem() == null)))
                && (!box1.getSelectedItem().equals(box2.getSelectedItem()));
    }

    @Override
    public JComponent createComponent(Attribute attribute, Engine engine,
                                      AccessRules accessRules) {
        JPanel basePanel = new JPanel(new BorderLayout());

        double[][] size = {
                {5, TableLayout.FILL, 5},
                {5, TableLayout.FILL, 5, TableLayout.FILL, 5,
                        TableLayout.FILL, 5, TableLayout.FILL, 5,
                        TableLayout.FILL, 5}};
        JPanel panel = new JPanel(new TableLayout(size));
        panel.add(
                new JLabel(GlobalResourcesManager
                        .getString("ElementList.Qualifier1")), "1, 1");
        panel.add(box1, "1, 3");

        panel.add(
                new JLabel(GlobalResourcesManager
                        .getString("ElementList.Qualifier2")), "1, 5");
        panel.add(box2, "1, 7");

        panel.add(
                new JLabel(GlobalResourcesManager
                        .getString("ElementList.Variants")), "1, 9");

        for (Qualifier qualifier : engine.getQualifiers()) {
            if (accessRules.canUpdateQualifier(qualifier.getId())) {
                box1.addItem(qualifier);
                box2.addItem(qualifier);
            }
        }
        if (attribute != null) {
            ElementListPropertyPersistent p = (ElementListPropertyPersistent) engine
                    .getAttribute(null, attribute);
            box1.setSelectedItem(engine.getQualifier(p.getQualifier1()));
            box2.setSelectedItem(engine.getQualifier(p.getQualifier2()));
            box1.setEnabled(false);
            box2.setEnabled(false);

            if (p.getConnectionTypes() != null)
                variantsArea.setText(p.getConnectionTypes());
        }

        basePanel.add(panel, BorderLayout.NORTH);

        basePanel.add(new JScrollPane(variantsArea), BorderLayout.CENTER);

        return basePanel;
    }

    public Qualifier getQualifier1() {
        return (Qualifier) box1.getSelectedItem();
    }

    public Qualifier getQualifier2() {
        return (Qualifier) box2.getSelectedItem();
    }
}