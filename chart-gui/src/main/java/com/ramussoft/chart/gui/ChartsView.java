package com.ramussoft.chart.gui;

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
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.AbstractUniqueView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.qualifier.table.RowRootCreater;
import com.ramussoft.gui.qualifier.table.RowTreeTable;
import com.ramussoft.gui.qualifier.table.RowTreeTableComponent;
import com.ramussoft.gui.qualifier.table.TreeTableNode;
import com.ramussoft.gui.qualifier.table.event.SelectionEvent;
import com.ramussoft.gui.qualifier.table.event.SelectionListener;

import static com.ramussoft.gui.qualifier.QualifierView.EDIT_NAME_CLICK_DELAY;

public class ChartsView extends AbstractUniqueView implements UniqueView {

    private RowTreeTableComponent component;

    private CreateChartAction createChartAction = new CreateChartAction();

    private ChartPrefernecesAction chartPrefernecesAction = new ChartPrefernecesAction();

    private OpenChartAction openChartAction = new OpenChartAction();

    private DeleteChartAction deleteChartAction = new DeleteChartAction();

    private RowTreeTable table;

    protected int[] lastSelectedRows;

    protected long lastClickTime;

    public ChartsView(GUIFramework framework) {
        super(framework);
    }

    @Override
    public JComponent createComponent() {
        Engine engine = framework.getEngine();
        AccessRules accessRules = framework.getAccessRules();
        component = new RowTreeTableComponent(engine, ChartPlugin
                .getCharts(engine), accessRules, new RowRootCreater(),
                new Attribute[]{StandardAttributesPlugin
                        .getAttributeNameAttribute(engine)}, framework);

        component.getTable().addSelectionListener(new SelectionListener() {

            @Override
            public void changeSelection(SelectionEvent event) {
                TreeTableNode selectedNode = component.getTable()
                        .getSelectedNode();
                if (selectedNode == null)
                    chartPrefernecesAction.setEnabled(false);
                else {
                    Row row = selectedNode.getRow();
                    chartPrefernecesAction.setEnabled(row != null);
                }
                openChartAction.setEnabled(chartPrefernecesAction.isEnabled());
                deleteChartAction
                        .setEnabled(chartPrefernecesAction.isEnabled());
            }
        });

        table = component.getTable();

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if ((e.getClickCount() % 2 == 0) && (e.getClickCount() > 0)) {
                        openDiagram();
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

        table.setExportRows(true);

        return component;
    }

    protected void editTableField() {
        table.editCellAt(table.getSelectedRow(), table.getSelectedColumn());
    }

    @Override
    public Action[] getActions() {
        return new Action[]{createChartAction, chartPrefernecesAction,
                openChartAction, deleteChartAction};
    }

    @Override
    public String getDefaultWorkspace() {
        return "Workspace.charts";
    }

    @Override
    public String getId() {
        return "View.charts";
    }

    public Element createChartElement(String name) {
        Row row = component.getRowSet().createRow(null);
        row.setName(name);
        return row.getElement();
    }

    ;

    private class CreateChartAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 3119306865713048596L;

        public CreateChartAction() {
            putValue(ACTION_COMMAND_KEY, "Action.CreateChart");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/add.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            CreateChartDialog dialog = new CreateChartDialog(framework,
                    ChartsView.this);
            dialog.setVisible(true);
            Options.saveOptions(dialog);
            dialog.close();
        }
    }

    private class ChartPrefernecesAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -5458737578947497315L;

        public ChartPrefernecesAction() {
            putValue(ACTION_COMMAND_KEY, "Action.ChartPreferences");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/preferencies.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TreeTableNode selectedNode = component.getTable().getSelectedNode();
            if (selectedNode == null)
                return;
            Row row = selectedNode.getRow();
            if (row != null) {
                ChartPreferencesDialog dialog = new ChartPreferencesDialog(
                        framework, row);
                dialog.setVisible(true);
                Options.saveOptions(dialog);
                dialog.close();
            }
        }

    }

    ;

    private class OpenChartAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -5458737578947497315L;

        public OpenChartAction() {
            putValue(ACTION_COMMAND_KEY, "Action.OpenChart");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/open.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            openDiagram();
        }

    }

    ;

    private class DeleteChartAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -5458737578947497315L;

        public DeleteChartAction() {
            putValue(ACTION_COMMAND_KEY, "Action.DeleteChart");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/delete.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            deleteDiagram();
        }

    }

    ;

    private void openDiagram() {
        TreeTableNode selectedNode = table.getSelectedNode();
        if (selectedNode == null)
            return;
        Row row = selectedNode.getRow();
        if (row != null) {
            framework.propertyChanged(ChartGUIPlugin.OPEN_CHART, row
                    .getElement());
        }
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
                ChartPlugin.deleteChart(engine, row.getElement());
            ((Journaled) engine).commitUserTransaction();
        }
    }
}
