package com.ramussoft.gui.attribute;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.Collator;
import java.util.Arrays;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Engine;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.AttributePreferenciesEditor;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.TextField;
import com.ramussoft.gui.common.prefrence.Options;

public class AttributePreferenciesDialog extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = 155692845357261949L;

    private static Collator collator = Collator.getInstance();
    ;

    private Engine engine;

    private TextField nameField = new TextField();

    private JComboBox typeComboBox = new JComboBox();

    protected GUIFramework framework;

    private JComponent component = null;

    protected JPanel contentPanel = new JPanel(new BorderLayout());

    private AttributePreferenciesEditor editor = null;

    protected Attribute attribute;

    private AccessRules accessRules;

    protected JPanel mainPanel;

    public AttributePreferenciesDialog(GUIFramework framework, JDialog dialog) {
        super(dialog);
        init(framework);
    }

    public AttributePreferenciesDialog(GUIFramework framework) {
        super(framework.getMainFrame());
        init(framework);
    }

    private void init(GUIFramework framework) {
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setTitle(GlobalResourcesManager
                .getString("CreateAttributeDialog.Title"));
        this.engine = framework.getEngine();
        this.framework = framework;
        this.accessRules = framework.getAccessRules();
        Options.loadOptions(this);
        init();
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Options.saveOptions(AttributePreferenciesDialog.this);
            }
        });
        setMinimumSize(getSize());
    }

    private void init() {
        double[][] size = {
                {DIV_SPACE, TableLayout.MINIMUM, DIV_SPACE, TableLayout.FILL,
                        DIV_SPACE},
                {DIV_SPACE, TableLayout.FILL, DIV_SPACE, TableLayout.FILL,
                        DIV_SPACE}};

        AttributeType[] attributeTypes = engine.getAttributeTypes();
        LocalizedType[] types = new LocalizedType[attributeTypes.length];

        for (int i = 0; i < types.length; i++) {
            AttributeType type = attributeTypes[i];
            AttributePlugin plugin = framework.findAttributePlugin(type);
            types[i] = new LocalizedType(plugin.getString("AttributeType."
                    + type.toString()), type);
        }

        Arrays.sort(types);

        for (LocalizedType type : types) {
            if (type.toString().length() > 0)
                typeComboBox.addItem(type);
        }

        JPanel panel = new JPanel(new TableLayout(size));

        panel.add(new JLabel(getString("AttributeName")), "1, 1");

        panel.add(nameField, "3, 1");

        panel.add(new JLabel(getString("AttributeTypeName")), "1, 3");

        panel.add(typeComboBox, "3, 3");

        typeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateAdditionalOptions();
                        }
                    });
                }
            }
        });

        mainPanel = new JPanel(new BorderLayout());

        contentPanel.add(panel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        postInit();

        this.setMainPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
        int x = Options.getInteger("AttributePreferenciesDialog.X",
                getLocation().x);
        int y = Options.getInteger("AttributePreferenciesDialog.Y",
                getLocation().y);
        setLocation(x, y);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateAdditionalOptions();
            }
        });
    }

    protected void postInit() {
    }

    protected void updateAdditionalOptions() {
        this.editor = null;
        AttributeType type = getAttributeType();
        AttributePlugin attributePlugin = framework.findAttributePlugin(type);
        if (attributePlugin == null) {
            System.err.println("WARNING: Attribute plugin for type: " + type
                    + " not found.");
            return;
        }
        editor = attributePlugin.getAttributePreferenciesEditor();
        boolean update = false;

        if (this.component != null) {
            update = true;
            contentPanel.remove(this.component);
        }

        this.component = null;

        if (editor != null) {
            JComponent component = editor.createComponent(attribute, engine,
                    accessRules);
            if (component != null) {
                update = true;
                contentPanel.add(component, BorderLayout.CENTER);
                this.component = component;
            }
        }
        if (update) {
            pack();
            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        pack();
        Options.setInteger("AttributePreferenciesDialog.X", getLocation().x);
        Options.setInteger("AttributePreferenciesDialog.Y", getLocation().y);
    }

    private String getString(String key) {
        return GlobalResourcesManager.getString(key);
    }

    @Override
    protected void onOk() {

        commitEditors();

        if (editor != null) {
            if (!editor.canApply()) {
                JOptionPane.showMessageDialog(this, GlobalResourcesManager
                        .getString("SomeNeededPreferenciesAreNotSeted"));
                return;
            }
        }

        try {

            if (engine instanceof Journaled) {
                ((Journaled) engine).startUserTransaction();
            }
            if (attribute == null) {
                attribute = engine.createAttribute(getAttributeType());
            }
            attribute.setName(nameField.getText());
            if (editor != null)
                editor.apply(attribute, engine, accessRules);
            engine.updateAttribute(attribute);
            postApply();
        } finally {
            if (engine instanceof Journaled) {
                ((Journaled) engine).commitUserTransaction();
            }
        }
        super.onOk();
    }

    protected void postApply() {
    }

    private AttributeType getAttributeType() {
        return ((LocalizedType) typeComboBox.getSelectedItem()).getType();
    }

    /**
     * @param attribute the attribute to set
     */
    public void setAttribute(Attribute attribute) {
        if (attribute != null) {
            typeComboBox.setEnabled(false);
            nameField.setText(attribute.getName());
            for (int i = 0; i < typeComboBox.getItemCount(); i++) {
                if (((LocalizedType) typeComboBox.getItemAt(i)).getType()
                        .equals(attribute.getAttributeType())) {
                    typeComboBox.setSelectedIndex(i);
                    break;
                }
            }
            this.setTitle(GlobalResourcesManager.getString("EditAttribute"));
        }
        this.attribute = attribute;
    }

    /**
     * @return the attribute
     */
    public Attribute getAttribute() {
        return attribute;
    }

    private class LocalizedType implements Comparable<LocalizedType> {

        private String name;

        private AttributeType type;

        public LocalizedType(String name, AttributeType type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public int compareTo(LocalizedType o) {
            try {
                return collator.compare(name, o.name);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

        public AttributeType getType() {
            return type;
        }

        @Override
        public String toString() {
            return name;
        }

    }

    ;

}
