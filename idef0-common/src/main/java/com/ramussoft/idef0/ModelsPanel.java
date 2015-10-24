package com.ramussoft.idef0;

import java.awt.BorderLayout;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.database.common.RowSet.RootRow;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.qualifier.QualifierView;
import com.ramussoft.gui.qualifier.table.RootCreater;
import com.ramussoft.gui.qualifier.table.RowNode;
import com.ramussoft.gui.qualifier.table.RowRootCreater;
import com.ramussoft.gui.qualifier.table.RowTreeTable;
import com.ramussoft.gui.qualifier.table.RowTreeTableComponent;
import com.ramussoft.gui.qualifier.table.RowTreeTableModel;
import com.ramussoft.gui.qualifier.table.Rows;
import com.ramussoft.gui.qualifier.table.TreeTableNode;
import com.ramussoft.pb.idef.visual.IDEF0Object;

public abstract class ModelsPanel extends AbstractModelsPanel {

    private Engine engine;

    private GUIFramework framework;

    private RowTreeTableComponent tree;

    private List<ModelsNode> modelsNodes = new ArrayList<ModelsNode>();

    private boolean closed = false;

    public ModelsPanel(Engine engine, GUIFramework framework) {
        this.engine = engine;
        this.framework = framework;
        init();
    }

    private void init() {

        tree = new RowTreeTableComponent(engine,
                IDEF0Plugin.getModelTree(engine), framework.getAccessRules(),
                new ModelRowCreator(),
                new Attribute[]{StandardAttributesPlugin
                        .getAttributeNameAttribute(engine)}, framework) {
            @Override
            protected RowTreeTableModel createRowTreeTableModel(
                    final Engine engine, Qualifier qualifier,
                    AccessRules accessRules, RootCreater rootCreater,
                    Attribute[] attributes, GUIFramework framework) {
                RowTreeTableModel rowTreeTableModel = new RowTreeTableModel(
                        engine, qualifier, attributes, accessRules,
                        rootCreater, framework) {
                    @Override
                    protected RowNode newRowNode(Row row) {
                        Long id = (Long) row
                                .getAttribute(StandardAttributesPlugin
                                        .getAttributeQualifierId(engine));
                        if (id != null) {
                            Qualifier qualifier = engine.getQualifier(id);
                            if (qualifier != null) {
                                ModelsNode node = newModelsNode(row, qualifier);
                                node.getTreeTableModel().setTable(
                                        tree.getTable());
                                node.getTreeTableModel().setModelSupport(
                                        tree.getModel().getModelSupport());
                                return node;
                            }
                        }
                        return super.newRowNode(row);
                    }

                    @Override
                    public boolean isCellEditable(Object node, int column) {
                        if (super.isCellEditable(node, column)) {
                            if (node instanceof ModelsNode)
                                return true;
                        }
                        return false;
                    }
                };
                return rowTreeTableModel;
            }

            @Override
            protected RowTreeTable createTable(AccessRules accessRules,
                                               GUIFramework framework, AttributePlugin[] plugins) {
                return new RowTreeTable(accessRules, model.getRowSet(),
                        plugins, framework, model) {

                    long treeModelsId = IDEF0Plugin.getModelTree(engine)
                            .getId();

                    @Override
                    public Transferable createTransferable() {
                        final int[] is = getSelectedRows();
                        final ArrayList<Integer> al = new ArrayList<Integer>();
                        long id = IDEF0Plugin.getModelTree(engine).getId();
                        Rows rows = new Rows();
                        for (final int i : is) {
                            al.add(i);
                            TreeTableNode node = (TreeTableNode) getPathForRow(
                                    i).getLastPathComponent();
                            if ((node != null) && (node.getRow() != null)) {
                                Row row = node.getRow();
                                rows.add(row);
                                if (row.getElement().getQualifierId() != id)
                                    return null;
                            }
                        }
                        exporting = true;
                        return new ArrayTransferable(al, rows);
                    }

                    @Override
                    public boolean importData(Transferable t, boolean on,
                                              int aIndex) {
                        int index = aIndex;
                        long id = IDEF0Plugin.getModelTree(engine).getId();
                        if (index >= getRowCount())
                            index--;
                        if (index < 0)
                            return false;
                        TreeTableNode node = (TreeTableNode) getPathForRow(
                                index).getLastPathComponent();
                        if (node.getRow() != null)
                            if (node.getRow().getElement().getQualifierId() != id)
                                return false;

                        return super.importData(t, on, aIndex);
                    }

                    @Override
                    protected Icon getDefaultIcon(Row row) {
                        if (row.getElement().getQualifierId() != treeModelsId) {
                            if (row.getChildCount() == 0)
                                return note;
                            else
                                return function;
                        }
                        if (row.getAttribute(StandardAttributesPlugin
                                .getAttributeQualifierId(engine)) == null)
                            return null;
                        return ModelsPanel.this.model;
                    }
                };
            }
        };

        fixTable((TreeTableNode) tree.getModel().getRoot());

        tree.getTable().addMouseListener(new MouseAdapter() {
            private int[] lastSelectedRows;
            private long lastClickTime;

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if ((e.getClickCount() % 2 == 0) && (e.getClickCount() > 0)) {
                        openDiagram();
                    } else {
                        if ((e.getClickCount() == 1)
                                && (System.currentTimeMillis() - lastClickTime < QualifierView.EDIT_NAME_CLICK_DELAY)
                                && (Arrays.equals(lastSelectedRows, tree
                                .getTable().getSelectedRows()))) {
                            if (!tree.getTable().isEditing()) {
                                editTableField();
                            }
                        } else {
                            lastClickTime = System.currentTimeMillis();
                            lastSelectedRows = tree.getTable()
                                    .getSelectedRows();
                        }
                    }
                }
            }

        });

        tree.getTable().setEditIfNullEvent(false);
        tree.getTable().getInputMap()
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "EditCell");
        tree.getTable().getActionMap().put("EditCell", new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = 3229634866196074563L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if ((tree.getTable().getSelectedRow() >= 0)
                        && (tree.getTable().getSelectedColumn() >= 0))
                    editTableField();
            }
        });

        JScrollPane pane = new JScrollPane();
        pane.setViewportView(tree);
        this.add(pane, BorderLayout.CENTER);
    }

    protected void editTableField() {
        tree.getTable().editCellAt(tree.getTable().getSelectedRow(),
                tree.getTable().getSelectedColumn());
    }

    private void fixTable(TreeTableNode node) {
        if (node instanceof ModelsNode) {
            ((ModelsNode) node).getTreeTableModel().setTable(tree.getTable());
            ((ModelsNode) node).getTreeTableModel().setModelSupport(
                    tree.getModel().getModelSupport());
        } else
            for (TreeTableNode node2 : node.getChildren())
                fixTable(node2);
    }

    private Icon note = new ImageIcon(getClass()
            .getResource("/images/note.png"));

    private Icon function = new ImageIcon(getClass().getResource(
            "/images/function.png"));

    private Icon model = new ImageIcon(getClass().getResource(
            "/images/idef0-model.png"));

    /**
     * Метод визначає код функціонального блоку у відповідності до стандарту
     * IDEF0
     *
     * @param function Функціональний блок, для якого буде визначений його код.
     * @return Код функціонального блока у відповідності до стандарту IDEF0.
     */

    public static String getIDEF0Kod(final Row function) {
        return IDEF0Object.getIDEF0Kod(function);
    }

    public void openDiagram() {
        RowTreeTable table = tree.getTable();

        final List<OpenDiagram> models = new ArrayList<OpenDiagram>();

        Qualifier modelTree = IDEF0Plugin.getModelTree(engine);

        for (int i : table.getSelectedRows()) {
            if (i >= 0) {
                TreePath path = table.getPathForRow(i);
                if (path != null) {
                    TreeTableNode node = (TreeTableNode) path
                            .getLastPathComponent();
                    if (node != null) {
                        Row row = node.getRow();
                        if (row != null) {
                            if (row.getElement().getQualifierId() == modelTree
                                    .getId()) {
                                Long id = (Long) row
                                        .getAttribute(StandardAttributesPlugin
                                                .getAttributeQualifierId(engine));
                                if (id != null) {
                                    Qualifier model = engine.getQualifier(id);
                                    if (model != null) {
                                        OpenDiagram openDiagram = new OpenDiagram(
                                                model, -1l);
                                        models.add(openDiagram);
                                    }
                                }
                            } else {
                                if (row.getChildCount() <= 0)
                                    row = row.getParent();
                                OpenDiagram openDiagram = new OpenDiagram(row
                                        .getRowSet().getQualifier(),
                                        row.getElementId());
                                models.add(openDiagram);
                            }
                        }
                    }
                }
            }
        }
        if (models.size() > 0) {
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
                                openDiagrams(models);
                                framework.hideAnimation();
                            }
                        });
                    }
                };
                thread.start();
            } else
                openDiagrams(models);
        }
    }

    private void openDiagrams(List<OpenDiagram> diagrams) {
        for (OpenDiagram open : diagrams)
            framework.propertyChanged(IDEF0ViewPlugin.OPEN_DIAGRAM, open);

    }

    public RowTreeTableComponent getTree() {
        return tree;
    }

    public void close() {
        if (closed)
            return;
        tree.getRowSet().close();
        for (ModelsNode node : modelsNodes)
            node.close();
        closed = true;
    }

    public boolean canOpen() {
        RowTreeTable table = tree.getTable();

        final List<OpenDiagram> models = new ArrayList<OpenDiagram>();

        Qualifier modelTree = IDEF0Plugin.getModelTree(engine);

        for (int i : table.getSelectedRows()) {
            if (i >= 0) {
                TreePath path = table.getPathForRow(i);
                if (path != null) {
                    TreeTableNode node = (TreeTableNode) path
                            .getLastPathComponent();
                    if (node != null) {
                        Row row = node.getRow();
                        if (row != null) {
                            if (row.getElement().getQualifierId() == modelTree
                                    .getId()) {
                                Long id = (Long) row
                                        .getAttribute(StandardAttributesPlugin
                                                .getAttributeQualifierId(engine));
                                if (id != null) {
                                    Qualifier model = engine.getQualifier(id);
                                    if (model != null) {
                                        OpenDiagram openDiagram = new OpenDiagram(
                                                model, -1l);
                                        models.add(openDiagram);
                                    }
                                }
                            } else {
                                if (row.getChildCount() <= 0)
                                    row = row.getParent();
                                OpenDiagram openDiagram = new OpenDiagram(row
                                        .getRowSet().getQualifier(),
                                        row.getElementId());
                                models.add(openDiagram);
                            }
                        }
                    }
                }
            }
        }
        return models.size() > 0;
    }

    private class ModelRowCreator extends RowRootCreater {

        @Override
        public TreeTableNode createRoot(RowSet rowSet) {
            root = new RowNode(createRChildren(rowSet.getRoot()),
                    rowSet.getRoot());
            root.setParent();
            return root;
        }

        public Vector<TreeTableNode> createRChildren(Row r) {

            Vector<TreeTableNode> res = new Vector<TreeTableNode>();

            for (Row row : r.getChildren()) {
                if (!(row instanceof RootRow)) {
                    Long id = (Long) row.getAttribute(StandardAttributesPlugin
                            .getAttributeQualifierId(engine));

                    if (id != null) {
                        Qualifier qualifier = engine.getQualifier(id);
                        if (qualifier != null) {
                            ModelsNode node = newModelsNode(row, qualifier);
                            node.setParent();
                            res.add(node);
                            continue;
                        }
                    }
                }
                RowNode node = new RowNode(createRChildren(row), row);
                node.setParent();
                res.add(node);
            }

            return res;
        }

    }

    private ModelsNode newModelsNode(Row row, Qualifier qualifier) {
        ModelsNode node = new ModelsNode(engine, row, qualifier,
                framework.getAccessRules(), framework);
        modelsNodes.add(node);
        return node;
    }

    public void createElement(Qualifier qualifier) {
        Element element = engine.createElement(IDEF0Plugin.getModelTree(engine)
                .getId());
        engine.setAttribute(element,
                StandardAttributesPlugin.getAttributeQualifierId(engine),
                qualifier.getId());
        Row row = tree.getRowSet().createRow(null, element);
        row.setName(qualifier.getName());

    }

    public void deleteElement(Qualifier qualifier) {
        for (Row row : tree.getRowSet().getAllRows()) {
            Long id = (Long) (row.getAttribute(StandardAttributesPlugin
                    .getAttributeQualifierId(engine)));
            if (id != null && id.longValue() == qualifier.getId()) {
                tree.getRowSet().deleteRow(row);
                break;
            }
        }
        for (int i = modelsNodes.size() - 1; i >= 0; --i) {
            ModelsNode node = modelsNodes.get(i);
            if (node.getTreeTableModel().getRowSet().getQualifier()
                    .equals(qualifier)) {
                modelsNodes.remove(i);
                node.close();
            }
        }
    }

    public void createFolder() {
        tree.getRowSet().createRow(null);
    }

    @Override
    public void showSelection(final OpenDiagram diagram) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                TreePath sel = null;
                int selRow = -1;

                for (int row = 0; row < getTree().getTable().getRowCount(); row++) {
                    TreePath path = getTree().getTable().getPathForRow(row);
                    Object object = path.getLastPathComponent();
                    if (diagram.getFunctionId() == -1l) {
                        if (object instanceof ModelsNode) {
                            if (((ModelsNode) object).getQualifier().equals(
                                    diagram.getQualifier())) {
                                sel = path;
                                break;
                            }
                        }
                    } else if (object instanceof RowNode) {
                        Row row2 = ((RowNode) object).getRow();
                        if ((row2.getElement() != null)
                                && (row2.getElementId() == diagram
                                .getFunctionId())) {
                            sel = path;
                            selRow = row;
                        }
                    }
                }

                if (sel != null) {
                    if (!getTree().getTable().isExpanded(sel))
                        getTree().getTable().expandPath(sel);

                    getTree().getTable().getSelectionModel().clearSelection();

                    getTree().getTable().getSelectionModel()
                            .addSelectionInterval(selRow, selRow);
                }

            }
        });

    }

    @Override
    public void expandAll() {
        getTree().getTable().expandAll();
    }

    @Override
    public void collapseAll() {
        getTree().getTable().collapseAll();
    }

    public Object getActiveNode() {
        int selectedRow = getTree().getTable().getSelectedRow();
        if (selectedRow < 0)
            return null;
        TreePath path = getTree().getTable().getPathForRow(selectedRow);
        if (path != null)
            return path.getLastPathComponent();
        return null;
    }

    @Override
    public void deleteSelected() {
        ModelsNode node = (ModelsNode) getActiveNode();
        ((Journaled) engine).startUserTransaction();
        engine.deleteQualifier(node.getQualifier().getId());
        deleteElement(node.getQualifier());
        ((Journaled) engine).commitUserTransaction();
    }

}
