package com.ramussoft.idef0;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.TreePath;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.attribute.AttributePreferenciesDialog;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.qualifier.AttributeOrderEditPanel;
import com.ramussoft.gui.qualifier.table.RowTreeTable;
import com.ramussoft.gui.qualifier.table.RowTreeTableComponent;
import com.ramussoft.gui.qualifier.table.SelectableTableView;
import com.ramussoft.gui.qualifier.table.TreeTableNode;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.idef.frames.ProjectOptionsPanel;

public class ModelPropertiesDialog extends BaseDialog {

    private Engine engine;

    private SelectableTableView view;

    private RowTreeTableComponent component;

    private JComboBox nameAttribute = new JComboBox();

    private JComboBox termAttribute = new JComboBox();

    private Qualifier function;

    private ProjectOptionsPanel optionsPanel;

    private AttributeOrderEditPanel attributeOrderEditPanel;

    private RowSet rowSet;

    private Attribute attributeId;

    private Action createAttributeAction = new CreateAttributeAction();

    private Action deleteAttributeAction = new DeleteAttributeAction();

    private AttributePreferencesAction attributePreferencesAction = new AttributePreferencesAction();

    private AccessRules rules;

    private GUIFramework framework;

    public ModelPropertiesDialog(GUIFramework framework,
                                 final Qualifier function, final Engine engine, AccessRules rules) {
        super(framework.getMainFrame(), true);
        setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/images/function.png")));
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.engine = engine;
        this.function = function;
        this.attributeId = (Attribute) engine.getPluginProperty("Core",
                StandardAttributesPlugin.ATTRIBUTE_ID);
        this.framework = framework;
        this.rules = rules;
        this.setTitle(ResourceLoader.getString("ModelProperties") + " - "
                + function.getName());

        Qualifier qualifier = StandardAttributesPlugin
                .getAttributesQualifier(engine);

        Attribute attributeId = StandardAttributesPlugin
                .getAttributeAttributeId(engine);

        view = new SelectableTableView(framework, engine, rules, qualifier) {
            @Override
            protected Attribute[] getAttributes() {

                if (qualifier.equals(engine.getPluginProperty("Core",
                        StandardAttributesPlugin.ATTRIBUTES_QUALIFIER)))
                    return new Attribute[]{
                            (Attribute) engine.getPluginProperty("Core",
                                    StandardAttributesPlugin.ATTRIBUTE_NAME),
                            (Attribute) engine
                                    .getPluginProperty(
                                            "Core",
                                            StandardAttributesPlugin.ATTRIBUTE_TYPE_NAME)};
                else
                    return super.getAttributes();
            }
        };
        JComponent res = view.createComponent();
        component = view.getComponent();
        this.rowSet = component.getRowSet();
        clearPopupMenu(component);

        if (qualifier.equals(engine.getPluginProperty("Core",
                StandardAttributesPlugin.ATTRIBUTES_QUALIFIER))) {
            component.getTable().setEditable(false);
        }
        JPanel panel = new JPanel(new BorderLayout());

        for (Row row : component.getModel().getRowSet().getAllRows()) {
            Long attrId = (Long) row.getAttribute(attributeId);
            for (Attribute attr : function.getAttributes()) {
                if (attr.getId() == attrId.longValue()) {
                    view.setSelectedRow(row, true);
                    break;
                }
            }
        }

        double[][] size = {{5, TableLayout.MINIMUM, 5, TableLayout.FILL, 5},
                {5, TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5}};

        JPanel attrForName = new JPanel(new TableLayout(size));

        attrForName.add(
                new JLabel(GlobalResourcesManager
                        .getString("Qualifier.AttributeForName")), "1, 1");
        attrForName.add(nameAttribute, "3, 1");

        attrForName.add(
                new JLabel(ResourceLoader.getString("Model.AttributeForTerm")),
                "1, 3");
        attrForName.add(termAttribute, "3, 3");

        nameAttribute.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                nameAttribute.removeAllItems();

                Attribute attrId = StandardAttributesPlugin
                        .getAttributeAttributeId(engine);

                for (Row row : view.getSelectedRows()) {
                    Attribute attribute = engine.getAttribute((Long) row
                            .getAttribute(attrId));
                    if (StandardAttributesPlugin.isNameType(attribute
                            .getAttributeType()))
                        nameAttribute.addItem(attribute);
                }

                JComboBox box = (JComboBox) e.getSource();
                Object comp = box.getUI().getAccessibleChild(box, 0);
                if (!(comp instanceof JPopupMenu))
                    return;
                JComponent scrollPane = (JComponent) ((JPopupMenu) comp)
                        .getComponent(0);
                Dimension size = new Dimension();
                size.width = box.getPreferredSize().width;
                size.height = scrollPane.getPreferredSize().height;
                scrollPane.setPreferredSize(size);
            }

        });

        termAttribute.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                termAttribute.removeAllItems();

                Attribute attrId = StandardAttributesPlugin
                        .getAttributeAttributeId(engine);

                for (Row row : view.getSelectedRows()) {
                    Attribute attribute = engine.getAttribute((Long) row
                            .getAttribute(attrId));
                    if (attribute.getAttributeType().getPluginName()
                            .equals("Core"))
                        if (attribute.getAttributeType().getTypeName()
                                .equals("Text")
                                || attribute.getAttributeType().getTypeName()
                                .equals("Variant"))
                            termAttribute.addItem(attribute);

                }

                JComboBox box = (JComboBox) e.getSource();
                Object comp = box.getUI().getAccessibleChild(box, 0);
                if (!(comp instanceof JPopupMenu))
                    return;
                JComponent scrollPane = (JComponent) ((JPopupMenu) comp)
                        .getComponent(0);
                Dimension size = new Dimension();
                size.width = box.getPreferredSize().width;
                size.height = scrollPane.getPreferredSize().height;
                scrollPane.setPreferredSize(size);
            }

        });

        nameAttribute.setPreferredSize(new Dimension(200, nameAttribute
                .getPreferredSize().height));
        termAttribute.setPreferredSize(new Dimension(200, termAttribute
                .getPreferredSize().height));

        panel.add(res, BorderLayout.CENTER);
        panel.add(attrForName, BorderLayout.SOUTH);
        panel.add(createToolBar(), BorderLayout.NORTH);
        component.getTable().setComponentPopupMenu(createPopupMenu());
        component.getTable().getSelectionModel()
                .addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        refreshActions();
                    }
                });
        final JTabbedPane pane = new JTabbedPane();

        pane.addTab(ResourceLoader.getString("attributes"), panel);

        attributeOrderEditPanel = new AttributeOrderEditPanel(
                function.getAttributes());

        pane.addTab(GlobalResourcesManager.getString("AttributesOrder"),
                attributeOrderEditPanel);

        DataPlugin dataPlugin = NDataPluginFactory.getDataPlugin(function,
                engine, rules);
        optionsPanel = new ProjectOptionsPanel(dataPlugin.getBaseFunction(),
                dataPlugin, this) {
            @Override
            protected JTabbedPane getTabPanel() {
                return pane;
            }
        };

        Long termAttr = dataPlugin.getBaseFunction().getProjectOptions()
                .getDeligate().getTermAttribute();

        if (termAttr == null)
            termAttr = -1l;

        for (Attribute attribute : function.getAttributes()) {
            if (StandardAttributesPlugin.isNameType(attribute
                    .getAttributeType())) {
                nameAttribute.addItem(attribute);
            }
            if (attribute.getAttributeType().getPluginName()
                    .equals("Core"))
                if (attribute.getAttributeType().getTypeName()
                        .equals("Text")
                        || attribute.getAttributeType().getTypeName()
                        .equals("Variant"))
                    termAttribute.addItem(attribute);
            if (function.getAttributeForName() == attribute.getId())
                nameAttribute.setSelectedItem(attribute);
            if (termAttr == attribute.getId())
                termAttribute.setSelectedItem(attribute);
        }

        pane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (pane.getSelectedIndex() == 1) {
                    updateAttributesInOrder(function.getAttributes());

                    attributeOrderEditPanel.refresh();
                }
            }

        });

        RowTreeTable table = component.getTable();

        table.setEditIfNullEvent(false);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if ((e.getClickCount() % 2 == 0) && (e.getClickCount() > 0)) {
                        setupAttribute();
                    }
                }
            }

        });

        pane.setSelectedIndex(0);

        this.setMainPane(pane);
        this.pack();
        //this.setMinimumSize(getSize());
        this.centerDialog();
        Options.loadOptions(this);
        refreshActions();
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        for (Action action : getActions()) {
            if (action == null)
                menu.addSeparator();
            else {
                action.putValue(Action.NAME, GlobalResourcesManager
                        .getString((String) action
                                .getValue(Action.ACTION_COMMAND_KEY)));
                menu.add(action);
            }
        }
        return menu;
    }

    protected void refreshActions() {
        RowTreeTable table = component.getTable();

        createAttributeAction.setEnabled(rules.canCreateAttribute());
        if (table.getTreeSelectionModel().getSelectionPath() == null) {
            deleteAttributeAction.setEnabled(false);
            attributePreferencesAction.setEnabled(false);
        } else {
            attributePreferencesAction.setEnabled(true);
            boolean e = true;
            TreePath[] paths = table.getTreeSelectionModel()
                    .getSelectionPaths();
            for (TreePath path : paths) {
                Row row = ((TreeTableNode) path.getLastPathComponent())
                        .getRow();
                if (row == null) {
                    e = false;
                    break;
                }
                if (row.getChildCount() > 0) {
                    e = false;
                    break;
                }

                Long long1 = (Long) row.getAttribute(attributeId);
                if (long1 == null)
                    break;
                long attrId = long1;

                if (!rules.canDeleteAttribute(attrId)) {
                    e = false;
                    break;
                }
            }
            deleteAttributeAction.setEnabled(e);
        }
    }

    private Component createToolBar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        for (Action action : getActions())
            if (action == null)
                bar.addSeparator();
            else
                bar.add(action).setFocusable(false);
        return bar;
    }

    private Action[] getActions() {
        return new Action[]{createAttributeAction, new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = 3284967628905643862L;

            {
                this.putValue(ACTION_COMMAND_KEY, "Action.SortByName");
                this.putValue(
                        SMALL_ICON,
                        new ImageIcon(getClass().getResource(
                                "/com/ramussoft/gui/table/sort-incr.png")));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                rowSet.startUserTransaction();
                rowSet.sortByName();
                rowSet.commitUserTransaction();

            }
        }, deleteAttributeAction, null, attributePreferencesAction};
    }

    private class CreateAttributeAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -2190162251129929137L;

        public CreateAttributeAction() {
            this.putValue(ACTION_COMMAND_KEY, "CreateAttribute");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/add.png")));
            this.putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new AttributePreferenciesDialog(framework).setVisible(true);
        }

    }

    ;

    private class DeleteAttributeAction extends AbstractAction {

        public DeleteAttributeAction() {
            this.putValue(ACTION_COMMAND_KEY, "DeleteAttribute");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/delete.png")));
            this.putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        }

        /**
         *
         */
        private static final long serialVersionUID = 895892525217269346L;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (JOptionPane.showConfirmDialog(framework.getMainFrame(),
                    GlobalResourcesManager
                            .getString("DeleteActiveElementsDialog.Warning"),
                    UIManager.getString("OptionPane.titleText"),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                TreePath[] paths = component.getTable().getTreeSelectionModel()
                        .getSelectionPaths();
                if (paths.length == 0) {
                    System.err
                            .println("Trying to delete element, but no elements are selected");
                    return;
                }
                for (TreePath path : paths) {
                    Row row = ((TreeTableNode) path.getLastPathComponent())
                            .getRow();
                    if (row == null) {
                        System.err
                                .println("Trying to delete node, which conatain no row");
                        return;
                    }
                    long attrId = (Long) row.getAttribute(attributeId);
                    rowSet.startUserTransaction();
                    engine.deleteAttribute(attrId);
                    rowSet.commitUserTransaction();
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    private void updateAttributesInOrder(List<Attribute> qAttributes) {

        List<Row> list = view.getSelectedRows();
        List<Attribute> attributes = new ArrayList<Attribute>(list.size());
        for (Row pr : list) {
            Attribute attribute = StandardAttributesPlugin.getAttribute(engine,
                    pr.getElement());
            attributes.add(attribute);
            if (qAttributes.indexOf(attribute) < 0)
                qAttributes.add(attribute);
        }

        List<Attribute> rem = new ArrayList<Attribute>();

        for (Attribute a : qAttributes)
            if (attributes.indexOf(a) < 0)
                rem.add(a);
        for (Attribute r : rem)
            qAttributes.remove(r);
    }

    private void clearPopupMenu(JComponent component) {
        component.setComponentPopupMenu(null);
        for (int i = 0; i < component.getComponentCount(); i++) {
            Component c = component.getComponent(i);
            if (c instanceof JComponent)
                clearPopupMenu((JComponent) c);
        }
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            Options.saveOptions(this);
        }
    }

    @Override
    protected void onOk() {
        if (nameAttribute.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, GlobalResourcesManager
                    .getString("Error.SetAttributeForName"));
            return;
        }

        updateAttributesInOrder(function.getAttributes());
        ((Journaled) engine).startUserTransaction();
        Attribute aForName = (Attribute) nameAttribute.getSelectedItem();
        if (aForName == null) {
            function.setAttributeForName(-1l);
        } else {
            function.setAttributeForName(aForName.getId());
        }
        engine.updateQualifier(function);
        Attribute aForTerm = (Attribute) termAttribute.getSelectedItem();
        if (aForTerm == null)
            optionsPanel.getProject().getDeligate().setTermAttribute(null);
        else
            optionsPanel.getProject().getDeligate()
                    .setTermAttribute(aForTerm.getId());

        optionsPanel.save(framework);
        ((Journaled) engine).commitUserTransaction();
        super.onOk();
    }

    private class AttributePreferencesAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -5171624095557878838L;

        public AttributePreferencesAction() {
            this.putValue(ACTION_COMMAND_KEY, "AttributePreferencies");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/preferencies.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setupAttribute();
        }

    }

    public void setupAttribute() {
        TreeTableNode node = component.getTable().getSelectedNode();
        if (node == null)
            return;
        Row row = node.getRow();
        if (row == null)
            return;

        AttributePreferenciesDialog d = new AttributePreferenciesDialog(
                framework, this);
        Attribute attribute = engine.getAttribute((Long) row
                .getAttribute(attributeId));
        d.setAttribute(attribute);
        d.getOKButton().setEnabled(rules.canUpdateAttribute(attribute.getId()));
        d.setVisible(true);
    }

    ;
}
