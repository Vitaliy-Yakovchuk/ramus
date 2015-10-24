package com.ramussoft.idef0;

import java.awt.BorderLayout;

import java.awt.event.ActionEvent;

import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.QualifierAdapter;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.AbstractUniqueView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.qualifier.Commands;
import com.ramussoft.gui.qualifier.table.RowNode;

public class ModelsView extends AbstractUniqueView implements UniqueView {

    public static final String REFRESH_ALL_MODELS = "RefreshAllModels";

    public static final String SET_UPDATE_ALL_MODELS = "SetUpdateAllModels";

    private Engine engine;

    private AccessRules rules;

    private GUIFramework framework;

    private boolean disableUpdated = false;

    private Action openDiagram = new AbstractAction() {
        {
            putValue(ACTION_COMMAND_KEY, "OpenFunction");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/open.png")));
            this.putValue(
                    ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK
                            | KeyEvent.SHIFT_MASK));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            modelsPanel.openDiagram();
        }

    };
    /*
     * private Action newModelFolder = new AbstractAction() { {
	 * putValue(ACTION_COMMAND_KEY, "CreateModelFolder"); this.putValue(
	 * SMALL_ICON, new ImageIcon(getClass().getResource(
	 * "/com/ramussoft/gui/table/folder.png"))); setEnabled(true); }
	 * 
	 * @Override public void actionPerformed(ActionEvent e) {
	 * modelsPanel.createFolder(); }
	 * 
	 * };
	 */

    private AbstractModelsPanel modelsPanel;

    private JPanel contentPanel;

    protected Object modelNode;

    private QualifierListener qualifierListener = new QualifierAdapter() {
        @Override
        public void qualifierUpdated(QualifierEvent event) {
            if (disableUpdated)
                return;
            if (IDEF0Plugin.isFunction(event.getNewQualifier())) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        contentPanel.remove(modelsPanel);
                        createModelsPanel();
                        contentPanel.doLayout();
                        contentPanel.repaint();
                    }
                });
            }
        }
    };

    public ModelsView(GUIFramework framework, Engine engine, AccessRules rules) {
        super(framework);
        this.engine = engine;
        this.rules = rules;
        this.framework = framework;
        framework.addActionListener(IDEF0ViewPlugin.ACTIVE_DIAGRAM,
                new ActionListener() {
                    @Override
                    public void onAction(
                            com.ramussoft.gui.common.event.ActionEvent event) {
                        com.ramussoft.gui.common.event.ActionEvent event2 = (com.ramussoft.gui.common.event.ActionEvent) event
                                .getValue();
                        if (event2 != null) {
                            final OpenDiagram diagram = (OpenDiagram) event2
                                    .getValue();

                            modelsPanel.showSelection(diagram);
                        }
                    }
                });

        ActionListener ltr;
        framework.addActionListener(Commands.FULL_REFRESH,
                ltr = new ActionListener() {

                    @Override
                    public void onAction(
                            com.ramussoft.gui.common.event.ActionEvent event) {
                        contentPanel.remove(modelsPanel);
                        createModelsPanel();
                        contentPanel.doLayout();
                        contentPanel.repaint();
                    }
                });
        framework.addActionListener(REFRESH_ALL_MODELS, ltr);
        framework.addActionListener(SET_UPDATE_ALL_MODELS,
                new ActionListener() {

                    @Override
                    public void onAction(
                            com.ramussoft.gui.common.event.ActionEvent event) {
                        disableUpdated = !(Boolean) event.getValue();
                        if (disableUpdated)
                            modelsPanel.close();
                    }
                });
        engine.addQualifierListener(qualifierListener);
    }

    @Override
    public String getDefaultWorkspace() {
        return "Workspace.IDEF0";
    }

    @Override
    public String getId() {
        return "ModelsView";
    }

    @Override
    public JComponent createComponent() {
        contentPanel = new JPanel(new BorderLayout());
        createModelsPanel();
        return contentPanel;
    }

    protected void createModelsPanel() {
        if (this.modelsPanel != null)
            this.modelsPanel.close();
        Qualifier modelTree = IDEF0Plugin.getModelTree(engine);
        modelTree = engine.getQualifier(modelTree.getId());
        if (modelTree == null) {
            final com.ramussoft.idef0.idef0.oldmodelsview.ModelsPanel modelsPanel = new com.ramussoft.idef0.idef0.oldmodelsview.ModelsPanel(
                    engine, framework) {
                @Override
                public boolean isDisabeUpdate() {
                    return disableUpdated;
                }
            };
            modelsPanel.getTree().getSelectionModel()
                    .addTreeSelectionListener(new TreeSelectionListener() {
                        @Override
                        public void valueChanged(TreeSelectionEvent e) {
                            openDiagram.setEnabled(modelsPanel.getTree()
                                    .getSelectionPath() != null);
                        }
                    });
            modelsPanel.getTree().setComponentPopupMenu(createPopupMenu());
            modelsPanel.getTree().addTreeSelectionListener(
                    new TreeSelectionListener() {
                        @Override
                        public void valueChanged(TreeSelectionEvent e) {
                            ch1();
                            modelNode = getActiveNode1();
                            while ((modelNode != null)
                                    && (!(modelNode instanceof ModelsNode))
                                    && (modelNode instanceof Row))
                                modelNode = ((Row) modelNode).getParent();
                            modelProperties.setEnabled((modelNode instanceof ModelsNode)
                                    && (rules
                                    .canUpdateQualifier(((ModelsNode) modelNode)
                                            .getQualifier().getId())));
                        }
                    });
            modelsPanel.getTreeModel().addTreeModelListener(
                    new TreeModelListener() {

                        @Override
                        public void treeNodesChanged(TreeModelEvent e) {
                            ch1();
                        }

                        @Override
                        public void treeNodesInserted(TreeModelEvent e) {
                            ch1();
                        }

                        @Override
                        public void treeNodesRemoved(TreeModelEvent e) {
                            ch1();
                        }

                        @Override
                        public void treeStructureChanged(TreeModelEvent e) {
                            ch1();
                        }

                    });

            contentPanel.add(modelsPanel, BorderLayout.CENTER);
            modelsPanel.getTree().setComponentPopupMenu(createPopupMenu());
            this.modelsPanel = modelsPanel;
        } else {
            final ModelsPanel modelsPanel = new ModelsPanel(engine, framework) {
                @Override
                public boolean isDisabeUpdate() {
                    return disableUpdated;
                }
            };
            modelsPanel.getTree().getTable().getSelectionModel()
                    .addListSelectionListener(new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            openDiagram.setEnabled(modelsPanel.canOpen());
                        }
                    });
            modelsPanel.getTree().setComponentPopupMenu(createPopupMenu());

            modelsPanel.getTree().getTable().getSelectionModel()
                    .addListSelectionListener(new ListSelectionListener() {

                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            ch();
                            modelNode = getActiveNode();
                            while ((modelNode != null)
                                    && (!(modelNode instanceof ModelsNode))
                                    && (modelNode instanceof RowNode))
                                modelNode = ((RowNode) modelNode).getParent();
                            modelProperties.setEnabled((modelNode instanceof ModelsNode)
                                    && (rules
                                    .canUpdateQualifier(((ModelsNode) modelNode)
                                            .getQualifier().getId())));
                        }
                    });

            contentPanel.add(modelsPanel, BorderLayout.CENTER);

            modelsPanel.getTree().getTable()
                    .setComponentPopupMenu(createPopupMenu());
            this.modelsPanel = modelsPanel;
        }
    }

    public Object getActiveNode1() {
        TreePath path = ((com.ramussoft.idef0.idef0.oldmodelsview.ModelsPanel) modelsPanel)
                .getTree().getSelectionPath();
        if (path != null)
            return path.getLastPathComponent();
        return null;
    }

    protected void ch1() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Object object = getActiveNode1();
                deleteModel
                        .setEnabled(((object instanceof com.ramussoft.idef0.idef0.oldmodelsview.ModelsNode)
                                && (((com.ramussoft.idef0.idef0.oldmodelsview.ModelsNode) object)
                                .getChildCount() == 0) && (rules
                                .canDeleteQualifier(((ModelsNode) object)
                                        .getQualifier().getId()))));
            }
        });
    }

    protected void ch() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Object object = getActiveNode();
                deleteModel.setEnabled(((object instanceof ModelsNode)
                        && (((ModelsNode) object).getChildCount() == 0) && (rules
                        .canDeleteQualifier(((ModelsNode) object)
                                .getQualifier().getId()))));
            }
        });
    }

    public Object getActiveNode() {
        int selectedRow = ((ModelsPanel) modelsPanel).getTree().getTable()
                .getSelectedRow();
        if (selectedRow < 0)
            return null;
        TreePath path = ((ModelsPanel) modelsPanel).getTree().getTable()
                .getPathForRow(selectedRow);
        if (path != null)
            return path.getLastPathComponent();
        return null;

    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        for (Action a : getActions()) {
            if (a != null) {
                String string = GlobalResourcesManager.getString((String) a
                        .getValue(Action.ACTION_COMMAND_KEY));
                if (string == null)
                    string = ResourceLoader.getString((String) a
                            .getValue(Action.ACTION_COMMAND_KEY));
                menu.add(a).setText(string);
            } else
                menu.addSeparator();
        }
        return menu;
    }

    private AbstractAction add;

    @Override
    public Action[] getActions() {
        if (add == null) {
            add = new AbstractAction() {

                {
                    putValue(ACTION_COMMAND_KEY, "CreateFunction");
                    this.putValue(SMALL_ICON, new ImageIcon(getClass()
                            .getResource("/images/create-diagram.png")));
                    this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                            KeyEvent.VK_ADD, KeyEvent.CTRL_MASK));
                    this.setEnabled(rules.canCreateQualifier());
                }

                @Override
                public void actionPerformed(ActionEvent event) {
                    CreateBaseFunctionDialog dialog = new CreateBaseFunctionDialog(
                            framework.getMainFrame(), engine,
                            framework.getAccessRules()) {
                        @Override
                        protected void createModel() {
                            super.createModel();
                            modelsPanel.createElement(qualifier);
                            framework.propertyChanged(
                                    IDEF0ViewPlugin.OPEN_DIAGRAM,
                                    new OpenDiagram(qualifier, -1l));
                        }
                    };
                    dialog.setVisible(true);
                }
            };
        }
        return new Action[]{add, openDiagram, modelProperties, deleteModel,
                null, expandAction, collapseAction};
    }

    private Action deleteModel = new AbstractAction() {

        {
            setEnabled(false);
            putValue(ACTION_COMMAND_KEY, "DeleteModel");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/delete.png")));
            this.putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (JOptionPane
                    .showConfirmDialog(
                            framework.getMainFrame(),
                            ResourceLoader
                                    .getString("are_you_shour_want_remove_active_element"),
                            UIManager.getString("OptionPane.titleText"),
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                modelsPanel.deleteSelected();
            }
        }

    };

    private Action modelProperties = new AbstractAction() {
        {
            setEnabled(false);
            putValue(ACTION_COMMAND_KEY, "ModelProperties");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/preferencies.png")));
            this.putValue(
                    ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK
                            | KeyEvent.SHIFT_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            ModelsNode node = (ModelsNode) modelNode;
            ModelPropertiesDialog dialog = new ModelPropertiesDialog(framework,
                    node.getQualifier(), engine, rules);
            dialog.setVisible(true);
        }

        ;
    };

    private ExpandAction expandAction = new ExpandAction();

    private CollapseAction collapseAction = new CollapseAction();

    @Override
    public void close() {
        modelsPanel.close();
        super.close();
        engine.removeQualifierListener(qualifierListener);
    }

    protected class CollapseAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -2888510142746145088L;

        public CollapseAction() {
            this.putValue(ACTION_COMMAND_KEY, "CollapseAll");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/collapse.png")));
            this.putValue(
                    ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK
                            | KeyEvent.SHIFT_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            modelsPanel.collapseAll();
        }

    }

    protected class ExpandAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -1143583852789406865L;

        public ExpandAction() {
            this.putValue(ACTION_COMMAND_KEY, "ExpandAll");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/expand.png")));
            this.putValue(
                    ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK
                            | KeyEvent.SHIFT_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            modelsPanel.expandAll();
        }

    }
}
