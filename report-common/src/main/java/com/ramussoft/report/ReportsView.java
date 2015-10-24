package com.ramussoft.report;

import static com.ramussoft.report.ReportViewPlugin.OPEN_SCRIPT_REPORT;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowChildAdapter;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.qualifier.QualifierView;
import com.ramussoft.gui.qualifier.table.TableView;
import com.ramussoft.gui.qualifier.table.TreeTableNode;

public class ReportsView extends TableView implements UniqueView {

    protected int[] lastSelectedRows;

    protected long lastClickTime;

    private OpenReportAction openReportAction = new OpenReportAction();

    private String createType;

    private String name;

    private class OpenReportAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 1328041697585385438L;

        public OpenReportAction() {
            putValue(ACTION_COMMAND_KEY, "Action.OpenReport");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/open.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            openReport();
        }

    }

    ;

    public ReportsView(GUIFramework framework) {
        super(framework, framework.getEngine(), framework.getAccessRules(),
                ReportPlugin.getReportsQualifier(framework.getEngine()));
    }

    @Override
    protected boolean beforeRowCreate() {
        boolean create = super.beforeRowCreate();
        if (create) {
            new CreateReportDialog().setVisible(true);
            return createType != null;
        }
        return create;
    }

    @Override
    public String getDefaultWorkspace() {
        return "workspace.reportEditor";
    }

    @Override
    public String getId() {
        return "reports";
    }

    @Override
    public JComponent createComponent() {
        JComponent createComponent = super.createComponent();
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if ((e.getClickCount() % 2 == 0) && (e.getClickCount() > 0)) {
                        openReport();
                    } else {
                        if ((e.getClickCount() == 1)
                                && (System.currentTimeMillis() - lastClickTime < QualifierView.EDIT_NAME_CLICK_DELAY)
                                && (Arrays.equals(lastSelectedRows,
                                table.getSelectedRows()))) {
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

        component.getRowSet().addRowChildListener(new RowChildAdapter() {
            @Override
            public void addedByThisRowSet(final Row row) {
                row.setAttribute(ReportPlugin.getReportTypeAttribute(engine),
                        createType);
                row.setAttribute(ReportPlugin.getReportNameAttribute(engine),
                        name);
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        openReport(row);
                    }
                });
            }
        });

        return createComponent;
    }

    @Override
    protected void createInnerComponent() {
        super.createInnerComponent();
        ((AbstractTableModel) component.getTable().getModel())
                .fireTableStructureChanged();
    }

    @Override
    protected void refreshActions() {
        super.refreshActions();
        boolean anySelected = table.getTreeSelectionModel().getSelectionPath() != null;
        openReportAction.setEnabled(anySelected);
    }

    protected void openReport(Row row) {
        framework.propertyChanged(OPEN_SCRIPT_REPORT, row.getElement());
    }

    protected void openReport() {
        table.setEditable(false);
        TreeTableNode node = table.getSelectedNode();
        if (node != null) {
            Row row = node.getRow();
            if (row != null) {
                openReport(row);
            }
        }
        table.setEditable(true);
    }

    protected void editTableField() {
        table.editCellAt(table.getSelectedRow(), table.getSelectedColumn());
    }

    @Override
    public Action[] getActions() {
        Action[] actions = super.getActions();
        return new Action[]{actions[0], actions[1], actions[2], actions[6],
                null, openReportAction, null, actions[8], actions[9]};
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getString(String key) {
        String string = super.getString(key);
        if (string == null)
            return "key " + key + " not found!!!";
        return string;
    }

    private class CreateReportDialog extends BaseDialog {
        /**
         *
         */
        private static final long serialVersionUID = 6590832274777011593L;

        private JRadioButton xml = new JRadioButton(
                ReportResourceManager.getString("ReportType.XML"));

        private JRadioButton jssp = new JRadioButton(
                ReportResourceManager.getString("ReportType.JSSP"));

        private JRadioButton jsspDocBook = new JRadioButton(
                ReportResourceManager.getString("ReportType.JSSPDocBook"));

        private JTextField name = new JTextField();

        public CreateReportDialog() {
            super(framework.getMainFrame(), true);
            createType = null;
            this.name.setPreferredSize(new Dimension(190, this.name
                    .getPreferredSize().height));
            setTitle(ReportResourceManager
                    .getString("CreateReportDialog.title"));
            xml.setSelected(true);
            ButtonGroup group = new ButtonGroup();
            group.add(xml);
            group.add(jssp);
            group.add(jsspDocBook);
            JPanel panel = new JPanel(new BorderLayout());

            JPanel name = new JPanel(new BorderLayout());

            JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            jPanel.add(new JLabel(GlobalResourcesManager
                    .getString("AttributeName")));
            jPanel.add(this.name);
            name.add(jPanel, BorderLayout.NORTH);
            JPanel jPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            jPanel2.add(xml);
            jPanel2.add(jssp);
            jPanel2.add(jsspDocBook);
            name.add(jPanel2, BorderLayout.CENTER);
            panel.add(name, BorderLayout.NORTH);
            this.setMainPane(panel);
            this.pack();
            this.setMinimumSize(getSize());
            this.setLocationRelativeTo(null);
            Options.loadOptions(this);
        }

        @Override
        protected void onOk() {
            Options.saveOptions(this);
            ReportsView.this.name = name.getText();
            if (xml.isSelected())
                createType = ReportPlugin.TYPE_XML;
            else if (jssp.isSelected())
                createType = ReportPlugin.TYPE_JSSP;
            else
                createType = ReportPlugin.TYPE_JSSP_DOC_BOOK;
            super.onOk();
        }
    }

    @Override
    public String getDefaultPosition() {
        return BorderLayout.WEST;
    }

    ;
}
