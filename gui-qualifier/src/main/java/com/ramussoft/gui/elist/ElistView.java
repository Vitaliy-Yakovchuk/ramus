package com.ramussoft.gui.elist;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.gui.common.AbstractUniqueView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.qualifier.Commands;
import com.ramussoft.gui.qualifier.QualifierView;

import static com.ramussoft.gui.qualifier.QualifierView.EDIT_NAME_CLICK_DELAY;

public class ElistView extends AbstractUniqueView implements UniqueView,
        Commands {

    private ElistComponent component;

    private CreateElementListAction create = new CreateElementListAction();

    private DeleteElementListAction delete = new DeleteElementListAction();

    private OpenElementListInTableAction open = new OpenElementListInTableAction();

    private OpenElementListAction openInList = new OpenElementListAction();

    private EListPropertiesAction propertiesAction = new EListPropertiesAction();

    private AccessRules rules;

    private JPanel root;

    public ElistView(GUIFramework framework) {
        super(framework);
        this.rules = framework.getAccessRules();
        create.setEnabled(rules.canCreateAttribute());
    }

    @Override
    public JComponent createComponent() {
        framework.addActionListener(FULL_REFRESH, new ActionListener() {

            @Override
            public void onAction(
                    com.ramussoft.gui.common.event.ActionEvent event) {
                component.close();
                root.removeAll();
                createInnerComponent();
            }
        });
        root = new JPanel(new BorderLayout());
        createInnerComponent();
        return root;
    }

    protected void createInnerComponent() {
        component = new ElistComponent(framework);
        component.getTable().getSelectionModel()
                .addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        delete.setEnabled(component.canDeleteAttributes());
                        open.setEnabled(component.getTable()
                                .getSelectedRowCount() > 0);
                        openInList.setEnabled(open.isEnabled());
                        propertiesAction.setEnabled(open.isEnabled());
                    }
                });

        final JTable table = component.getTable();

        table.addMouseListener(new MouseAdapter() {

            private int[] lastSelectedRows;

            private long lastClickTime;

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if ((e.getClickCount() % 2 == 0) && (e.getClickCount() > 0)) {
                        component.openElementListsInTables();
                    } else {
                        if ((e.getClickCount() == 1)
                                && (System.currentTimeMillis() - lastClickTime < EDIT_NAME_CLICK_DELAY)
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

        table.setComponentPopupMenu(QualifierView.createPopupMenu(getActions()));

        root.add(component, BorderLayout.CENTER);
        root.revalidate();
        root.repaint();
    }

    protected void editTableField() {
        component.getTable().editCellAt(component.getTable().getSelectedRow(),
                component.getTable().getSelectedColumn());
    }

    @Override
    public Action[] getActions() {
        return new Action[]{create, delete, null, openInList, open, null,
                propertiesAction};
    }

    @Override
    public String getDefaultWorkspace() {
        return "Workspace.ElementList";
    }

    @Override
    public String getId() {
        return "ElementListView";
    }

    @Override
    public void close() {
        super.close();
        component.close();
    }

    private class EListPropertiesAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -6888995431053986108L;

        public EListPropertiesAction() {
            putValue(ACTION_COMMAND_KEY, "Action.EListProperties");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/preferencies.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            CreateOrEditElementListDialog dialog = new CreateOrEditElementListDialog(framework) {
                /**
                 *
                 */
                private static final long serialVersionUID = -8259590172280276367L;

                @Override
                protected Attribute getAttribute() {
                    return component.getSelectedAttribute();
                }
            };
            dialog.setVisible(true);

        }

    }

    private class CreateElementListAction extends AbstractAction {
        /**
         *
         */
        private static final long serialVersionUID = -2501991160616384841L;

        public CreateElementListAction() {
            putValue(ACTION_COMMAND_KEY, "Action.CreateElementList");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/add.png")));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS,
                    KeyEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            CreateOrEditElementListDialog dialog = new CreateOrEditElementListDialog(framework) {
                /**
                 *
                 */
                private static final long serialVersionUID = -8259590172280276367L;

                @Override
                protected Attribute getAttribute() {
                    return null;
                }
            };
            dialog.setVisible(true);
        }
    }

    ;

    private class DeleteElementListAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -5277912444115141862L;

        public DeleteElementListAction() {
            putValue(ACTION_COMMAND_KEY, "Action.DeleteElementList");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/delete.png")));
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (JOptionPane.showConfirmDialog(null, GlobalResourcesManager
                            .getString("DeleteActiveElementsDialog.Warning"),
                    GlobalResourcesManager.getString("ConfirmMessage.Title"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                return;
            component.deleteElements();
        }
    }

    ;

    private class OpenElementListInTableAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -5277912444115141862L;

        public OpenElementListInTableAction() {
            putValue(ACTION_COMMAND_KEY, "Action.OpenElementListInTable");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/open.png")));
            putValue(
                    ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK
                            | KeyEvent.SHIFT_MASK));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            component.openElementListsInTables();
        }
    }

    ;

    private class OpenElementListAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -5277912444115141862L;

        public OpenElementListAction() {
            putValue(ACTION_COMMAND_KEY, "Action.OpenElementList");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/open-element-list.png")));
            putValue(
                    ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_MASK
                            | KeyEvent.SHIFT_MASK));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            component.openElementLists();
        }
    }

    ;

}
