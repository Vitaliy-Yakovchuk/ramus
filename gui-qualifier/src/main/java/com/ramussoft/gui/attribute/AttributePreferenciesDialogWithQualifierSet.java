package com.ramussoft.gui.attribute;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.BorderFactory;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.qualifier.QualifierSelectComponent;

public class AttributePreferenciesDialogWithQualifierSet extends
        AttributePreferenciesDialog {

    /**
     *
     */
    private static final long serialVersionUID = -900663477737081104L;

    private QualifierSelectComponent component;

    public AttributePreferenciesDialogWithQualifierSet(GUIFramework framework) {
        super(framework);
    }

    @Override
    protected void postInit() {
        super.postInit();
        component = new QualifierSelectComponent(framework.getEngine(), false,
                false);
        component.setBorder(BorderFactory
                .createTitledBorder(GlobalResourcesManager
                        .getString("QualifierView")));
        mainPanel.add(component, BorderLayout.EAST);
    }

    @Override
    protected void postApply() {
        super.postApply();
        Engine engine = framework.getEngine();
        List<Qualifier> all = component.getAll();
        List<Qualifier> selected = component.getSelected();
        for (Qualifier qualifier : all) {
            if (selected.indexOf(qualifier) >= 0) {
                if (qualifier.getAttributes().indexOf(attribute) < 0) {
                    qualifier.getAttributes().add(attribute);
                    engine.updateQualifier(qualifier);
                }
            } else {
                if (qualifier.getAttributes().indexOf(attribute) >= 0) {
                    qualifier.getAttributes().remove(attribute);
                    engine.updateQualifier(qualifier);
                }
            }
        }
    }

    @Override
    public void setAttribute(Attribute attribute) {
        super.setAttribute(attribute);
        if (attribute != null) {
            for (Qualifier qualifier : component.getAll()) {
                if (qualifier.getAttributes().indexOf(attribute) >= 0)
                    component.setSelected(qualifier, true);
            }
        }
    }
}
