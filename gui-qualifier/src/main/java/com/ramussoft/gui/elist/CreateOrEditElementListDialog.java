package com.ramussoft.gui.elist;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.attribute.ElementListPreferenciesEditor;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;

public abstract class CreateOrEditElementListDialog extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = -22907499366870594L;

    private GUIFramework framework;

    private Engine engine;

    private AccessRules rules;

    private JTextField nameField = new JTextField();

    private ElementListPreferenciesEditor editor;

    public CreateOrEditElementListDialog(GUIFramework framework) {
        super(framework.getMainFrame(), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setTitle(GlobalResourcesManager
                .getString("CreateElementListDialog.Title"));
        this.framework = framework;
        this.engine = framework.getEngine();
        this.rules = framework.getAccessRules();
        init();
        this.pack();
        centerDialog();
        this.setMinimumSize(getSize());
        Options.loadOptions(this);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b)
            Options.saveOptions(CreateOrEditElementListDialog.this);
    }

    private void init() {
        double[][] size = {
                {DIV_SPACE, TableLayout.MINIMUM, DIV_SPACE, TableLayout.FILL,
                        DIV_SPACE}, {DIV_SPACE, TableLayout.FILL, DIV_SPACE}};

        JPanel panel = new JPanel(new TableLayout(size));

        panel.add(
                new JLabel(GlobalResourcesManager.getString("ElementListName")),
                "1, 1");

        panel.add(nameField, "3, 1");

        Attribute attribute = getAttribute();
        if (attribute != null)
            nameField.setText(attribute.getName());

        JPanel main = new JPanel(new BorderLayout());
        main.add(panel, BorderLayout.NORTH);

        AttributePlugin attributePlugin = framework
                .findAttributePlugin(getAttributeType());

        editor = (ElementListPreferenciesEditor) attributePlugin
                .getAttributePreferenciesEditor();
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(editor.createComponent(getAttribute(), engine, rules),
                BorderLayout.CENTER);
        main.add(panel2, BorderLayout.CENTER);
        setMainPane(main);
    }

    protected abstract Attribute getAttribute();

    private AttributeType getAttributeType() {
        return new AttributeType("Core", "ElementList", false, false, false);
    }

    @Override
    protected void onOk() {
        if (!isOk()) {
            JOptionPane.showMessageDialog(this, GlobalResourcesManager
                    .getString("SomeNeededPreferenciesAreNotSeted"));
            return;
        }

        ((Journaled) engine).startUserTransaction();
        Attribute attribute = getAttribute();
        if (attribute == null)
            attribute = engine.createAttribute(getAttributeType());
        attribute.setName(nameField.getText());
        editor.apply(attribute, engine, rules);
        engine.updateAttribute(attribute);

        Qualifier q1 = editor.getQualifier1();
        Qualifier q2 = editor.getQualifier2();

        q1.getAttributes().add(attribute);
        q2.getAttributes().add(attribute);
        engine.updateQualifier(q1);
        engine.updateQualifier(q2);

        ((Journaled) engine).commitUserTransaction();

        super.onOk();
    }

    private boolean isOk() {
        return editor.canApply();
    }

}
