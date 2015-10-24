package com.ramussoft.chart.gui;

import static com.ramussoft.gui.qualifier.QualifierView.EDIT_NAME_CLICK_DELAY;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;

import com.ramussoft.chart.core.ChartPlugin;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.AbstractUniqueView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.qualifier.table.RowRootCreater;
import com.ramussoft.gui.qualifier.table.RowTreeTable;
import com.ramussoft.gui.qualifier.table.RowTreeTableComponent;
import com.ramussoft.gui.qualifier.table.TreeTableNode;
import com.ramussoft.gui.qualifier.table.event.SelectionEvent;
import com.ramussoft.gui.qualifier.table.event.SelectionListener;

public class ChartSetsView extends AbstractUniqueView implements UniqueView {

    private RowTreeTableComponent component;

    private CreateChartSetAction createChartSetAction = new CreateChartSetAction();

    private DeleteChartSetAction deleteChartSetAction = new DeleteChartSetAction();

    private OpenChartSetAction openChartSetAction = new OpenChartSetAction();

    private RowTreeTable table;

    protected int[] lastSelectedRows;

    protected long lastClickTime;

    public ChartSetsView(GUIFramework framework) {
        super(framework);
    }

    @Override
    public JComponent createComponent() {
        Engine engine = framework.getEngine();
        AccessRules accessRules = framework.getAccessRules();
        component = new RowTreeTableComponent(engine, ChartPlugin
                .getChartSets(engine), accessRules, new RowRootCreater(),
                new Attribute[]{StandardAttributesPlugin
                        .getAttributeNameAttribute(engine)}, framework);

        table = component.getTable();

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if ((e.getClickCount() % 2 == 0) && (e.getClickCount() > 0)) {
                        openChartSet();
                    } else {
                        if ((e.getClickCount() == 1)
                                && (System.currentTimeMillis() - lastClickTime < EDIT_NAME_CLICK_DELAY)
                                && (Arrays.equals(lastSelectedRows, table
                                .getSelectedRows()))) {
                            if (!table.isEditing()) {
                                editTableField();
                            }
                        } else {
                            lastClickTime = System.currentTimeMillis();
                            lastSelectedRows = table.getSelectedRows();
                        }
                    }
                }
            }

        });
        table.addSelectionListener(new SelectionListener() {

            @Override
            public void changeSelection(SelectionEvent event) {
                TreeTableNode selectedNode = component.getTable()
                        .getSelectedNode();
                if (selectedNode == null)
                    openChartSetAction.setEnabled(false);
                else {
                    Row row = selectedNode.getRow();
                    openChartSetAction.setEnabled(row != null);
                }
                deleteChartSetAction.setEnabled(openChartSetAction.isEnabled());
            }
        });

        table.setEditIfNullEvent(false);
        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                "EditCell");
        table.getActionMap().put("EditCell", new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = 3229634866196074563L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if ((table.getSelectedRow() >= 0)
                        && (table.getSelectedColumn() >= 0))
                    editTableField();
            }
        });

        return component;
    }

    protected void editTableField() {
        table.editCellAt(table.getSelectedRow(), table.getSelectedColumn());
    }

    protected void openChartSet() {
        TreeTableNode selectedNode = table.getSelectedNode();
        if (selectedNode == null)
            return;
        Row row = selectedNode.getRow();
        if (row != null) {
            framework.propertyChanged(ChartGUIPlugin.OPEN_CHART_SET, row
                    .getElement());
        }
    }

    @Override
    public Action[] getActions() {
        return new Action[]{createChartSetAction, openChartSetAction,
                deleteChartSetAction};
    }

    @Override
    public String getDefaultWorkspace() {
        return "Workspace.charts";
    }

    @Override
    public String getId() {
        return "View.chartSets";
    }

    public void deleteDiagram() {
        List<Row> rows = new ArrayList<Row>();
        int[] sels = table.getSelectedRows();
        for (int i : sels) {
            TreePath path = table.getPathForRow(i);
            if (path != null) {
                TreeTableNode node = (TreeTableNode) path
                        .getLastPathComponent();
                if (node != null) {
                    Row row = node.getRow();
                    if (row != null)
                        rows.add(row);
                }
            }
        }

        if (rows.size() > 0) {
            if (JOptionPane.showConfirmDialog(component, GlobalResourcesManager
                            .getString("DeleteActiveElementsDialog.Warning"),
                    GlobalResourcesManager.getString("ConfirmMessage.Title"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                return;
            Engine engine = framework.getEngine();
            ((Journaled) engine).startUserTransaction();
            for (Row row : rows)
                engine.deleteElement(row.getElementId());
            ((Journaled) engine).commitUserTransaction();
        }
    }

    private class CreateChartSetAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -6294755971488064187L;

        public CreateChartSetAction() {
            putValue(ACTION_COMMAND_KEY, "Action.CreateChartSet");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/add.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            component.getRowSet().createRow(null);
        }

    }

    ;

    private class OpenChartSetAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 4197814294321419775L;

        public OpenChartSetAction() {
            putValue(ACTION_COMMAND_KEY, "Action.OpenChartSet");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/open.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            openChartSet();
        }

    }

    ;

    private class DeleteChartSetAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -881544560976490090L;

        public DeleteChartSetAction() {
            putValue(ACTION_COMMAND_KEY, "Action.DeleteChartSet");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/delete.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            deleteChartSet();
        }

    }

    public void deleteChartSet() {
        List<Row> rows = new ArrayList<Row>();
        int[] sels = table.getSelectedRows();
        for (int i : sels) {
            TreePath path = table.getPathForRow(i);
            if (path != null) {
                TreeTableNode node = (TreeTableNode) path
                        .getLastPathComponent();
                if (node != null) {
                    Row row = node.getRow();
                    if (row != null)
                        rows.add(row);
                }
            }
        }

        if (rows.size() > 0) {
            if (JOptionPane.showConfirmDialog(component, GlobalResourcesManager
                            .getString("DeleteActiveElementsDialog.Warning"),
                    GlobalResourcesManager.getString("ConfirmMessage.Title"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                return;
            Engine engine = framework.getEngine();
            ((Journaled) engine).startUserTransaction();
            for (Row row : rows)
                ChartPlugin.deleteChartSet(engine, row.getElement());
            ((Journaled) engine).commitUserTransaction();
        }
    }

    ;
}
