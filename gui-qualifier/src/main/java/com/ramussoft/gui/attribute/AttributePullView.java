package com.ramussoft.gui.attribute;

import java.awt.event.ActionEvent;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.common.AbstractUniqueView;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.qualifier.Commands;
import com.ramussoft.gui.qualifier.table.RowRootCreater;
import com.ramussoft.gui.qualifier.table.RowTreeTable;
import com.ramussoft.gui.qualifier.table.RowTreeTableComponent;
import com.ramussoft.gui.qualifier.table.RowTreeTableModel;
import com.ramussoft.gui.qualifier.table.TreeTableNode;

public class AttributePullView extends AbstractUniqueView implements UniqueView,
        Commands {

    private Qualifier attributeQualifier;

    private Attribute attributeTypeName;

    private Attribute attributeName;

    private Attribute attributeId;

    private Engine engine;

    private AccessRules rules;

    private RowTreeTableComponent component;

    private CreateAttributeAction createAttributeAction = new CreateAttributeAction();

    private DeleteAttributeAction deleteAttributeAction = new DeleteAttributeAction();

    private AttributePreferencesAction attributePreferencesAction = new AttributePreferencesAction();

    private RowTreeTable table;

    private RowSet rowSet;

    public AttributePullView(GUIFramework framework) {
        super(framework);
        this.engine = framework.getEngine();
        this.rules = framework.getAccessRules();
        this.attributeQualifier = (Qualifier) engine.getPluginProperty("Core",
                StandardAttributesPlugin.ATTRIBUTES_QUALIFIER);
        this.attributeTypeName = (Attribute) engine.getPluginProperty("Core",
                StandardAttributesPlugin.ATTRIBUTE_TYPE_NAME);
        this.attributeName = (Attribute) engine.getPluginProperty("Core",
                StandardAttributesPlugin.ATTRIBUTE_NAME);
        this.attributeId = (Attribute) engine.getPluginProperty("Core",
                StandardAttributesPlugin.ATTRIBUTE_ID);
        framework.addActionListener(FULL_REFRESH, new ActionListener() {
            @Override
            public void onAction(
                    com.ramussoft.gui.common.event.ActionEvent event) {
                component.getModel().refresh();
            }
        });
    }

    @Override
    public String getId() {
        return "AttributePullView";
    }

    @Override
    public JComponent createComponent() {
        component = new RowTreeTableComponent(engine, attributeQualifier,
                rules, new RowRootCreater(), new Attribute[]{attributeName,
                attributeTypeName}, framework);
        table = component.getTable();

        component.getModel().setEditable(1, false);

        ((AbstractTableModel) table.getModel()).fireTableStructureChanged();

        Attribute type = (Attribute) engine.getPluginProperty("Core",
                StandardAttributesPlugin.ATTRIBUTE_TYPE_NAME);
        final Hashtable<String, String> names = new Hashtable<String, String>();
        for (AttributeType type2 : engine.getAttributeTypes()) {
            AttributePlugin plugin = framework.findAttributePlugin(type2);
            try {
                String key = "AttributeType." + type2.toString();
                names.put(key, plugin.getString(key));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String key = "AttributeType.Core.ElementList";
        names.put(key, GlobalResourcesManager.getString(key));

        component.getModel().setAttributeLocalizer(
                new RowTreeTableModel.Localizer() {
                    @Override
                    public Object getValue(Object key) {
                        if (key == null)
                            return "Anknown attribute type";
                        return names.get("AttributeType." + key.toString());
                    }
                }, type);

        table.getTreeSelectionModel().addTreeSelectionListener(
                new TreeSelectionListener() {
                    @Override
                    public void valueChanged(TreeSelectionEvent e) {
                        refreshActions();
                    }
                });
        rowSet = component.getRowSet();
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
        refreshActions();
        table.setComponentPopupMenu(createJPopupMenu());
        return component;
    }

    private JPopupMenu createJPopupMenu() {
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

    @Override
    public Action[] getActions() {
        return new Action[]{createAttributeAction, new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = 3284967628905643862L;

            {
                this.putValue(ACTION_COMMAND_KEY, "Action.SortByName");
                this.putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                        "/com/ramussoft/gui/table/sort-incr.png")));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                component.getRowSet().startUserTransaction();
                component.getRowSet().sortByName();
                component.getRowSet().commitUserTransaction();

            }
        }, deleteAttributeAction, null, attributePreferencesAction};
    }

    private void refreshActions() {
        createAttributeAction.setEnabled(rules.canCreateAttribute());
        if (table.getTreeSelectionModel().getSelectionPath() == null) {
            deleteAttributeAction.setEnabled(false);
            attributePreferencesAction.setEnabled(false);
        } else {
            boolean e = true;
            boolean e1 = true;
            TreePath[] paths = table.getTreeSelectionModel()
                    .getSelectionPaths();
            for (TreePath path : paths) {
                Row row = ((TreeTableNode) path.getLastPathComponent())
                        .getRow();
                if (row == null) {
                    e = false;
                    e1 = false;
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

                if (!rules.canUpdateAttribute(attrId)) {
                    e1 = false;
                    break;
                }

            }
            deleteAttributeAction.setEnabled(e);
            attributePreferencesAction.setEnabled(e1);
        }
    }

    private class CreateAttributeAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -2190162251129929137L;

        public CreateAttributeAction() {
            this.putValue(ACTION_COMMAND_KEY, "CreateAttribute");
            this.putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/add.png")));
            this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_ADD, KeyEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AttributePreferenciesDialog attributePreferenciesDialog = new AttributePreferenciesDialogWithQualifierSet(
                    framework);
            attributePreferenciesDialog.getOKButton().setEnabled(
                    framework.getAccessRules().canCreateAttribute());
            attributePreferenciesDialog.setVisible(true);
        }

    }

    ;

    private class DeleteAttributeAction extends AbstractAction {

        public DeleteAttributeAction() {
            this.putValue(ACTION_COMMAND_KEY, "DeleteAttribute");
            this.putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/delete.png")));
            this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_DELETE, 0));
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
                TreePath[] paths = table.getTreeSelectionModel()
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

    private class AttributePreferencesAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -5171624095557878838L;

        public AttributePreferencesAction() {
            this.putValue(ACTION_COMMAND_KEY, "AttributePreferencies");
            this.putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/preferencies.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setupAttribute();
        }

    }

    ;

    @Override
    public String getDefaultWorkspace() {
        return "Workspace.Qualifiers";
    }

    private void setupAttribute() {
        AttributePreferenciesDialog dialog = new AttributePreferenciesDialogWithQualifierSet(
                framework);
        TreeTableNode node = table.getSelectedNode();
        if (node == null)
            return;
        Row row = node.getRow();
        Attribute attribute = engine.getAttribute((Long) row
                .getAttribute(attributeId));
        dialog.setAttribute(attribute);
        dialog.getOKButton().setEnabled(
                rules.canUpdateAttribute(attribute.getId()));
        dialog.setVisible(true);
    }

    ;
}
