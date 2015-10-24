package com.ramussoft.idef0.idef0.oldmodelsview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.idef0.AbstractModelsPanel;
import com.ramussoft.idef0.IDEF0ViewPlugin;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.idef0.OpenDiagram;
import com.ramussoft.idef0.TreeModel;

public abstract class ModelsPanel extends AbstractModelsPanel {

    private Engine engine;

    private JTree tree;

    private TreeModel treeModel = new TreeModel();

    private GUIFramework framework;

    private ModelsRootNode rootNode;

    public ModelsPanel(Engine engine, GUIFramework framework) {
        super();
        this.engine = engine;
        this.framework = framework;
        init();
    }

    private void init() {
        treeModel.setRoot(createRoot());

        tree = new JTree(treeModel) {
            @Override
            public TreeCellRenderer getCellRenderer() {
                TreeCellRenderer renderer = super.getCellRenderer();
                if (renderer == null)
                    return null;
                ((DefaultTreeCellRenderer) renderer).setLeafIcon(new ImageIcon(
                        getClass().getResource("/images/function.png")));
                return renderer;
            }
        };

        tree.setCellRenderer(new Renderer());

        tree.setEditable(true);

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ((e.getButton() == MouseEvent.BUTTON1)
                        && (e.getClickCount() == 2)) {
                    openDiagram();
                }
            }

        });

        tree.setRootVisible(true);

        JScrollPane pane = new JScrollPane();
        pane.setViewportView(tree);
        this.add(pane, BorderLayout.CENTER);
    }

    private TreeNode createRoot() {
        rootNode = new ModelsRootNode(engine, treeModel) {
            @Override
            public boolean isDisabeUpdate() {
                return ModelsPanel.this.isDisabeUpdate();
            }
        };
        return rootNode;
    }

    private class Renderer extends DefaultTreeCellRenderer {

        private Icon note = new ImageIcon(getClass().getResource(
                "/images/note.png"));

        private Icon function = new ImageIcon(getClass().getResource(
                "/images/function.png"));

        private Icon model = new ImageIcon(getClass().getResource(
                "/images/idef0-model.png"));

        {
            setLeafIcon(note);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus) {
            if (value instanceof ModelsNode) {
                setOpenIcon(model);
                setClosedIcon(model);
                setIcon(model);
            } else if (((TreeNode) value).getChildCount() > 0) {
                setOpenIcon(function);
                setClosedIcon(function);
                setIcon(function);
            } else {
                setOpenIcon(note);
                setClosedIcon(note);
                setIcon(note);
                setLeafIcon(note);
            }

            super.getTreeCellRendererComponent(tree, value, sel, expanded,
                    leaf, row, hasFocus);

            if (value instanceof Row) {
                Row row2 = (Row) value;
                if (row2.getElement() != null) {
                    String code = getIDEF0Kod(row2);
                    setText(code + " " + getText());
                }
            }

            return this;
        }
    }

    ;

    private static String getRecIDEF0Kod(final Row function) {
        final Row f = function.getParent();
        if (f == null || f.getParent() == null
                || (f.getParent().getParent() == null))
            return "";
        String id = Integer.toString(function.getId());
        if (id.length() > 1)
            id = "." + id + ".";
        return getRecIDEF0Kod(f) + id;
    }

    /**
     * Метод визначає код функціонального блоку у відповідності до стандарту
     * IDEF0
     *
     * @param function Функціональний блок, для якого буде визначений його код.
     * @return Код функціонального блока у відповідності до стандарту IDEF0.
     */

    public static String getIDEF0Kod(final Row function) {
        final Row f = function.getParent();
        if (f == null)
            return "A-0";
        if ((f.getParent() == null) || (f.getElement() == null))
            return "A0";
        return "A" + getRecIDEF0Kod(function);
    }

    public void openDiagram() {
        final TreePath path = tree.getSelectionPath();
        if (path != null) {
            if (engine.getPluginProperty("IDEF0", "DataPlugin") == null) {
                framework.showAnimation(GlobalResourcesManager
                        .getString("Wait.DataLoading"));
                Thread thread = new Thread() {
                    @Override
                    public void run() {

                        NDataPluginFactory.getDataPlugin(null, engine,
                                framework.getAccessRules());

                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                openDiagram(path);
                                framework.hideAnimation();
                            }
                        });
                    }
                };
                thread.start();
            } else
                openDiagram(path);
        }
    }

    private void openDiagram(TreePath path) {
        Object object = path.getLastPathComponent();
        if (object instanceof ModelsNode) {
            Qualifier qualifier = ((ModelsNode) object).getQualifier();
            OpenDiagram open = new OpenDiagram(qualifier, -1l);
            framework.propertyChanged(IDEF0ViewPlugin.OPEN_DIAGRAM, open);
        } else {
            Row row = (Row) object;
            if (row.getChildCount() == 0)
                row = row.getParent();

            if (row instanceof ModelsNode) {
                Qualifier qualifier = ((ModelsNode) row).getQualifier();
                OpenDiagram open = new OpenDiagram(qualifier, -1l);
                framework.propertyChanged(IDEF0ViewPlugin.OPEN_DIAGRAM, open);
            } else {

                if ((row != null) && (row.getElement() != null)) {
                    Qualifier qualifier = engine.getQualifier(row.getElement()
                            .getQualifierId());
                    OpenDiagram open = new OpenDiagram(qualifier,
                            row.getElementId());
                    framework.propertyChanged(IDEF0ViewPlugin.OPEN_DIAGRAM,
                            open);
                }
            }
        }
    }

    public JTree getTree() {
        return tree;
    }

    public void close() {
        rootNode.close();
    }

    public TreeModel getTreeModel() {
        return treeModel;
    }

    public ModelsRootNode getRootNode() {
        return rootNode;
    }

    public void createFolder() {
        JOptionPane.showMessageDialog(this,
                "It is not passible to create folder for this version of data");
    }

    @Override
    public void showSelection(final OpenDiagram diagram) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                TreePath sel = null;

                for (int row = 0; row < getTree().getRowCount(); row++) {
                    TreePath path = getTree().getPathForRow(row);
                    Object object = path.getLastPathComponent();
                    if (diagram.getFunctionId() == -1l) {
                        if (object instanceof ModelsNode) {
                            if (((ModelsNode) object).getQualifier().equals(
                                    diagram.getQualifier())) {
                                sel = path;
                                break;
                            }
                        }
                    } else {
                        Row row2 = (Row) object;
                        if ((row2.getElement() != null)
                                && (row2.getElementId() == diagram
                                .getFunctionId())) {
                            sel = path;
                        }
                    }
                }

                if (sel != null) {
                    if (!getTree().isExpanded(sel))
                        getTree().expandPath(sel);
                    getTree().setSelectionPath(sel);
                }

            }
        });
    }

    @Override
    public void createElement(Qualifier qualifier) {
    }

    @Override
    public void deleteSelected() {
        ModelsNode node = (ModelsNode) getActiveNode();
        ((Journaled) node.getEngine()).startUserTransaction();
        node.getEngine().deleteQualifier(node.getQualifier().getId());
        ((Journaled) node.getEngine()).commitUserTransaction();
    }

    public Object getActiveNode() {
        TreePath path = getTree().getSelectionPath();
        if (path != null)
            return path.getLastPathComponent();
        return null;
    }

    @Override
    public void expandAll() {
        if (getTree().getRowCount() == 0) {
            javax.swing.tree.TreeModel model = getTree().getModel();
            if (model != null && model.getRoot() != null)
                getTree().expandPath(new TreePath(model.getRoot()));
        }
        for (int i = 0; i < getTree().getRowCount(); i++)
            getTree().expandRow(i);
    }

    @Override
    public void collapseAll() {
        for (int i = getTree().getRowCount() - 1; i >= 0; i--)
            getTree().collapseRow(i);
    }
}
