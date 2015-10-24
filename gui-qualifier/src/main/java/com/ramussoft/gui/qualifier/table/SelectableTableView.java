package com.ramussoft.gui.qualifier.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.StringGetter;
import com.ramussoft.gui.common.prefrence.Options;

public class SelectableTableView extends TableView {

    private SelectType selectType = SelectType.CHECK;

    private boolean readOnly;

    private Action checkAll = new AbstractAction() {

        /**
         *
         */
        private static final long serialVersionUID = -6261337083603807455L;

        {
            putValue(ACTION_COMMAND_KEY, "Action.CheckAll");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/check-all.png")));
            // setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            component.getModel().checkAll();
            component.repaint();
        }
    };

    private Action uncheckAll = new AbstractAction() {

        /**
         *
         */
        private static final long serialVersionUID = 5909284575856494073L;

        {
            putValue(ACTION_COMMAND_KEY, "Action.UncheckAll");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/uncheck-all.png")));
            // setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            component.getModel().uncheckAll();
            component.repaint();
        }
    };

    public SelectableTableView(GUIFramework framework, Engine engine,
                               AccessRules accessor, Qualifier qualifier) {
        this(framework, engine, accessor, qualifier, false);
    }

    public SelectableTableView(GUIFramework framework, Engine engine,
                               AccessRules accessor, Qualifier qualifier, boolean readOnly) {
        super(framework, engine, accessor, qualifier);
        this.readOnly = readOnly;
    }

    public SelectableTableView(GUIFramework framework, Qualifier qualifier) {
        this(framework, qualifier, false);
    }

    public SelectableTableView(GUIFramework framework, Qualifier qualifier,
                               boolean readOnly) {
        this(framework, framework.getEngine(), framework.getAccessRules(),
                qualifier, readOnly);
    }

    @Override
    public JComponent createComponent() {
        JComponent res = super.createComponent();
        component.setSelectType(SelectType.CHECK);
        component.updateTableHeaderWidth();
        return res;
    }

    public List<Row> getSelectedRows() {
        return component.getModel().getSelectedRows();
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    protected void tableSelectedValueChanged() {

    }

    @Override
    public void fullRefresh() {
        List<Row> list = getSelectedRows();
        super.fullRefresh();
        for (Row row : list)
            component.getModel().setSelectedRow(row, true);
    }

    public void setSelectType(SelectType selectType) {
        this.selectType = selectType;
        component.setSelectType(selectType);
        if (selectType.equals(SelectType.RADIO))
            component.getModel().setUniqueSelect(true);
        else
            component.getModel().setUniqueSelect(false);
        checkAll.setEnabled(selectType.equals(SelectType.CHECK));
        uncheckAll.setEnabled(selectType.equals(SelectType.CHECK));
    }

    @Override
    protected void componentRefresh() {
        List<Row> list = getSelectedRows();
        super.componentRefresh();
        for (Row row : list) {
            component.getModel().setSelectedRow(row, true);
        }
        setSelectType(selectType);
    }

    public void selectRows(List<Long> rows) {
        RowSet set = component.getRowSet();
        RowTreeTableModel model = component.getModel();
        for (Long id : rows) {
            Row row = set.findRow(id);
            if (row != null)
                model.setSelectedRow(row, true);
        }
    }

    public void setSelectedRow(Row row, boolean selected) {
        component.getModel().setSelectedRow(row, true);
    }

    public static List<Row> showRowSelectDialog(Component component,
                                                GUIFramework framework, Qualifier qualifier, SelectType selectType) {
        return showRowSelectDialog(component, framework, qualifier, selectType,
                false);
    }

    public static List<Row> showRowSelectDialog(Component component,
                                                GUIFramework framework, Qualifier qualifier, SelectType selectType,
                                                boolean readOnly) {
        return showRowSelectDialog(component, framework, framework.getEngine(),
                framework.getAccessRules(), qualifier, selectType, readOnly);
    }

    public static List<Row> showRowSelectDialog(Component component,
                                                GUIFramework framework, Qualifier qualifier, SelectType selectType,
                                                boolean readOnly, boolean selectAll) {
        return showRowSelectDialog(component, framework, framework.getEngine(),
                framework.getAccessRules(), qualifier, selectType, readOnly,
                selectAll);
    }

    public static List<Row> showRowSelectDialog(Component component,
                                                GUIFramework framework, Engine engine, AccessRules rules,
                                                Qualifier qualifier, SelectType selectType) {
        return showRowSelectDialog(component, framework, engine, rules,
                qualifier, selectType, false);
    }

    public static List<Row> showRowSelectDialog(Component component,
                                                GUIFramework framework, Engine engine, AccessRules rules,
                                                Qualifier qualifier, SelectType selectType, boolean readOnly) {
        return showRowSelectDialog(component, framework, framework.getEngine(),
                framework.getAccessRules(), qualifier, selectType, readOnly,
                false);
    }

    public static List<Row> showRowSelectDialog(Component component,
                                                GUIFramework framework, Engine engine, AccessRules rules,
                                                Qualifier qualifier, SelectType selectType, boolean readOnly,
                                                boolean selectAll) {
        final SelectableTableView view = new SelectableTableView(framework,
                engine, rules, qualifier, readOnly) {

        };
        JComponent createComponent = view.createComponent();

        view.setSelectType(selectType);

        Component root = SwingUtilities.getRoot(component);

        BaseDialog dialog;
        if (root instanceof JFrame)
            dialog = new BaseDialog((JFrame) root) {

                /**
                 *
                 */
                private static final long serialVersionUID = -900626699911390352L;

                @Override
                protected void onOk() {
                    if (view.getSelectedRows().size() == 0) {
                        JOptionPane
                                .showMessageDialog(
                                        this,
                                        GlobalResourcesManager
                                                .getString("SelectElementDialog.NoSelection"));
                    } else
                        super.onOk();
                }

            };
        else if (root instanceof JDialog)
            dialog = new BaseDialog((JDialog) root) {

                /**
                 *
                 */
                private static final long serialVersionUID = -900626699911390352L;

                @Override
                protected void onOk() {
                    if (view.getSelectedRows().size() == 0) {
                        JOptionPane
                                .showMessageDialog(
                                        this,
                                        GlobalResourcesManager
                                                .getString("SelectElementDialog.NoSelection"));
                    } else
                        super.onOk();
                }

            };
        dialog = new BaseDialog(framework.getMainFrame()) {

            /**
             *
             */
            private static final long serialVersionUID = -900626699911390352L;

            @Override
            protected void onOk() {
                if (view.getSelectedRows().size() == 0) {
                    JOptionPane.showMessageDialog(this, GlobalResourcesManager
                            .getString("SelectElementDialog.NoSelection"));
                } else
                    super.onOk();
            }

        };
        JPanel panel = new JPanel(new BorderLayout());
        if (!readOnly) {

            JToolBar bar = view.createToolBar();

            bar.setFloatable(false);

            panel.add(bar, BorderLayout.NORTH);
        }

        if (selectAll)
            view.getComponent().getModel().checkAll();

        panel.add(createComponent, BorderLayout.CENTER);

        dialog.setMainPane(panel);

        dialog.setLocationRelativeTo(null);
        dialog.setTitle(GlobalResourcesManager
                .getString("SelectElementDialog.Title"));
        Options.loadOptions(dialog);
        dialog.setVisible(true);
        Options.saveOptions(dialog);

        if (dialog.isOkPressed()) {
            List<Row> res = view.getSelectedRows();
            view.close();
            return res;
        }
        view.close();
        return null;
    }

    public JToolBar createToolBar() {
        JToolBar bar = new JToolBar();

        for (Action action : getActions()) {
            if (action != null) {
                String command = (String) action
                        .getValue(Action.ACTION_COMMAND_KEY);
                JButton button = bar.add(action);
                button.setFocusable(false);
                if (action.getValue(Action.SHORT_DESCRIPTION) == null) {
                    String text = null;
                    StringGetter getter = (StringGetter) action
                            .getValue(StringGetter.ACTION_STRING_GETTER);
                    if (getter != null)
                        text = getter.getString(command);
                    else
                        text = GlobalResourcesManager.getString(command);
                    if (text != null)
                        button.setToolTipText(text);
                }
            } else
                bar.addSeparator();
        }

        return bar;
    }

    public void clearSelection() {
        getComponent().getModel().clearSelection();
        getComponent().getHeader().repaint();
    }

    @Override
    public Action[] getActions() {
        if (readOnly)
            return new Action[]{checkAll, uncheckAll};
        Action[] oldActions = super.getActions();
        Action[] actions = new Action[oldActions.length + 3];
        actions[0] = checkAll;
        actions[1] = uncheckAll;
        for (int i = 0; i < oldActions.length; i++)
            actions[i + 3] = oldActions[i];
        return actions;
    }

    public List<Element> getSelectedElements() {
        List<Row> rows = getSelectedRows();
        List<Element> result = new ArrayList<Element>(rows.size());
        for (Row r : rows)
            result.add(r.getElement());
        return result;
    }

}
