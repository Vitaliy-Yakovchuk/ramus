package com.ramussoft.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.AbstractTableModel;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.net.common.Group;
import com.ramussoft.net.common.User;
import com.ramussoft.net.common.UserFactory;

public class EditUsersDialog extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = 3412973397511252254L;

    private UserFactory factory;

    private AdminPanelPlugin plugin;

    private ArrayList<User> usersToUpdate = new ArrayList<User>();

    private ArrayList<Group> groupsToUpdate = new ArrayList<Group>();

    private ArrayList<String> usersToDelete = new ArrayList<String>();

    private ArrayList<String> groupsToDelete = new ArrayList<String>();

    private List<User> users = new ArrayList<User>();

    private List<Group> groups = new ArrayList<Group>();

    private List<Qualifier> qualifiers = new ArrayList<Qualifier>();

    private JTable qualifiersTable;

    private JTable groupTable;

    private JTable userTable;

    private AbstractAction createUser;

    private AbstractAction createGroup;

    private AbstractAction deleteUser;

    private AbstractAction deleteGroup;

    private AbstractAction editUser;

    public EditUsersDialog(JFrame frame, UserFactory userFactory,
                           AdminPanelPlugin plugin, Engine engine) {
        super(frame, true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.factory = userFactory;
        this.plugin = plugin;

        this.setTitle(plugin.getString("Action.EditUsers"));

        users = factory.getUsers();
        groups = factory.getGroups();
        qualifiers = engine.getQualifiers();

        createModels();

        createActions();

        JComponent userPanel = createUserPanel();
        JComponent groupPanel = createGroupPanel();

        JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        pane.setLeftComponent(userPanel);
        pane.setRightComponent(groupPanel);
        JToolBar panel = new JToolBar();
        panel.add(createUser).setFocusable(false);
        panel.add(editUser).setFocusable(false);
        panel.add(deleteUser).setFocusable(false);
        panel.add(createGroup).setFocusable(false);
        panel.add(deleteGroup).setFocusable(false);

        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(panel, BorderLayout.NORTH);
        panel2.add(pane, BorderLayout.CENTER);
        pane.setDividerLocation(300);
        setMainPane(panel2);
        this.setMinimumSize(new Dimension(800, 600));
        this.setLocationRelativeTo(null);
        Options.loadOptions(this);
    }

    private AbstractTableModel qualifierModel;

    private AbstractTableModel userModel;

    private AbstractTableModel groupModel;

    private void createModels() {
        userModel = new AbstractTableModel() {

            /**
             *
             */
            private static final long serialVersionUID = -5788741544620802246L;

            private String[] columnNames = new String[]{
                    plugin.getString("Column.User.Login"),
                    plugin.getString("Column.User.Name")};


            public int getColumnCount() {
                return 2;
            }


            public int getRowCount() {
                return users.size();
            }


            public Object getValueAt(int rowIndex, int columnIndex) {
                User user = users.get(rowIndex);
                return (columnIndex == 0) ? user.getLogin() : user.getName();
            }


            public String getColumnName(int column) {
                return columnNames[column];
            }
        };

        groupModel = new AbstractTableModel() {

            /**
             *
             */
            private static final long serialVersionUID = 2848439255843446217L;

            private String[] columnNames = new String[]{
                    plugin.getString("Column.Selected"),
                    plugin.getString("Column.Group.Name")};


            public int getColumnCount() {
                return 2;
            }


            public int getRowCount() {
                return groups.size();
            }


            public Object getValueAt(int rowIndex, int columnIndex) {
                if (columnIndex == 0) {
                    if (getActiveUser() != null) {
                        return getActiveUser().getGroups().indexOf(
                                groups.get(rowIndex)) >= 0;
                    } else
                        return false;
                }
                return groups.get(rowIndex).getName();
            }


            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0)
                    return Boolean.class;
                return super.getColumnClass(columnIndex);
            }


            public String getColumnName(int column) {
                return columnNames[column];
            }


            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return (columnIndex == 0) && (getActiveUser() != null);
            }


            public void setValueAt(Object value, int rowIndex, int columnIndex) {
                User user = getActiveUser();
                if (user != null) {
                    updateUser(user);
                    Group group = groups.get(rowIndex);
                    if ((Boolean) value)
                        user.getGroups().add(group);
                    else
                        user.getGroups().remove(group);
                }
            }
        };

        qualifierModel = new AbstractTableModel() {

            /**
             *
             */
            private static final long serialVersionUID = -8072350565904373494L;

            private String[] columnNames = new String[]{
                    plugin.getString("Column.Selected"),
                    plugin.getString("Column.Qualifier.Name")};


            public int getColumnCount() {
                return 2;
            }


            public int getRowCount() {
                return qualifiers.size();
            }


            public Object getValueAt(int rowIndex, int columnIndex) {
                if (columnIndex == 0) {
                    if (getActiveGroup() != null) {
                        return getActiveGroup().getQualifierIds().indexOf(
                                qualifiers.get(rowIndex).getId()) >= 0;
                    } else
                        return Boolean.FALSE;
                }
                return qualifiers.get(rowIndex).getName();
            }


            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0)
                    return Boolean.class;
                return super.getColumnClass(columnIndex);
            }


            public String getColumnName(int column) {
                return columnNames[column];
            }


            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return (columnIndex == 0) && (getActiveGroup() != null);
            }


            public void setValueAt(Object value, int rowIndex, int columnIndex) {
                Group group = getActiveGroup();
                if (group != null) {
                    updateGroup(group);
                    long qualifierId = qualifiers.get(rowIndex).getId();
                    if ((Boolean) value)
                        group.getQualifierIds().add(qualifierId);
                    else
                        group.getQualifierIds().remove(qualifierId);
                }
            }
        };

    }

    private void updateUser(User user) {
        User u = findUser(user.getLogin(), usersToUpdate);
        if (u != null)
            usersToUpdate.remove(u);
        usersToUpdate.add(user);
    }

    private void updateGroup(Group group) {
        Group g = findGroup(group.getName(), groupsToUpdate);
        if (g != null)
            groupsToUpdate.remove(g);
        groupsToUpdate.add(group);
    }

    private void createActions() {
        editUser = new AbstractAction() {
            {
                putValue(SHORT_DESCRIPTION, plugin.getString("Action.EditUser"));
                putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                        "/com/ramussoft/client/edit_user.png")));
            }

            /**
             *
             */
            private static final long serialVersionUID = 3903478872742330272L;


            public void actionPerformed(ActionEvent e) {
                User user = getActiveUser();
                if (user == null)
                    return;
                EditUserDialog dialog = new EditUserDialog(
                        EditUsersDialog.this, user, plugin);
                dialog.setVisible(true);
                updateUser(user);
                userModel.fireTableDataChanged();
            }
        };

        createUser = new AbstractAction() {

            {
                putValue(SHORT_DESCRIPTION, plugin
                        .getString("Action.CreateUser"));
                putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                        "/com/ramussoft/client/add_user.png")));
            }

            /**
             *
             */
            private static final long serialVersionUID = -1876218590535351065L;


            public void actionPerformed(ActionEvent e) {

                EditUserDialog dialog = new EditUserDialog(
                        EditUsersDialog.this, null, plugin);
                dialog.setVisible(true);
                User user = dialog.getUser();
                if (user != null) {

                    User u = findUser(user.getLogin());
                    if (u != null) {
                        JOptionPane
                                .showMessageDialog(EditUsersDialog.this,
                                        MessageFormat.format(plugin
                                                .getString("User.Exists"), u
                                                .getLogin()));
                        return;
                    }

                    usersToUpdate.add(user);
                    Group group = findGroup(user.getLogin());
                    if (group == null) {
                        group = new Group(user.getLogin());
                        groups.add(group);
                        groupsToUpdate.add(group);
                        user.getGroups().add(group);
                        groupModel.fireTableDataChanged();
                    }
                    users.add(user);
                    userModel.fireTableDataChanged();

                    u = findUser(user.getLogin());
                    if (u != null) {
                        usersToDelete.remove(u);
                    }
                }
            }

        };

        createGroup = new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = 7889274557650997117L;

            {
                putValue(SHORT_DESCRIPTION, plugin
                        .getString("Action.CreateGroup"));
                putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                        "/com/ramussoft/client/add_group.png")));
            }


            public void actionPerformed(ActionEvent e) {
                String string = JOptionPane.showInputDialog(plugin
                        .getString("Action.CreateGroup"));
                if (string != null) {
                    if (findGroup(string) != null) {
                        JOptionPane.showMessageDialog(EditUsersDialog.this,
                                MessageFormat.format(plugin
                                        .getString("Group.Exists"), string));
                        return;
                    }

                    Group g = findGroup(string);
                    if (g != null)
                        groupsToDelete.remove(g);

                    Group group = new Group(string);
                    groups.add(group);
                    groupsToUpdate.add(group);
                    groupModel.fireTableDataChanged();
                }

            }
        };

        deleteGroup = new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = 7889274557650997117L;

            {
                putValue(SHORT_DESCRIPTION, plugin
                        .getString("Action.DeleteGroup"));
                putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                        "/com/ramussoft/client/delete_group.png")));
            }


            public void actionPerformed(ActionEvent e) {
                Group group = getActiveGroup();
                if (group != null) {
                    if ((groupsToDelete.indexOf(group.getName())) < 0)
                        groupsToDelete.add(group.getName());
                    groups.remove(group);
                    groupModel.fireTableDataChanged();
                    groupsToUpdate.remove(group);
                }
            }
        };
        deleteUser = new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = 7889274557650997117L;

            {
                putValue(SHORT_DESCRIPTION, plugin
                        .getString("Action.DeleteUser"));
                putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                        "/com/ramussoft/client/delete_user.png")));
            }


            public void actionPerformed(ActionEvent e) {
                User user = getActiveUser();
                if (user != null) {
                    if ((usersToDelete.indexOf(user.getLogin())) < 0)
                        usersToDelete.add(user.getLogin());
                    users.remove(user);
                    usersToUpdate.remove(user);
                    userModel.fireTableDataChanged();
                }
            }
        };

    }

    protected User findUser(String login) {
        return findUser(login, users);
    }

    protected User findUser(String login, List<User> users) {
        for (User user : users) {
            if (user.getLogin().equals(login))
                return user;
        }
        return null;
    }

    protected Group findGroup(String login, List<Group> groups) {
        for (Group group : groups) {
            if (group.getName().equals(login))
                return group;
        }
        return null;
    }

    protected Group findGroup(String login) {
        return findGroup(login, groups);
    }

    private JComponent createUserPanel() {
        JScrollPane pane = new JScrollPane();
        userTable = new JTable(userModel) {
            /**
             *
             */
            private static final long serialVersionUID = 2660594023797090389L;


            public void changeSelection(int rowIndex, int columnIndex,
                                        boolean toggle, boolean extend) {
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
                groupModel.fireTableDataChanged();
                qualifierModel.fireTableDataChanged();
                deleteUser.setEnabled(rowIndex >= 0);
                editUser.setEnabled(rowIndex >= 0);
            }
        };
        pane.setViewportView(userTable);
        return pane;
    }

    private JComponent createGroupPanel() {
        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        groupTable = new JTable(groupModel) {
            /**
             *
             */
            private static final long serialVersionUID = 5000667220319722662L;


            public void changeSelection(int rowIndex, int columnIndex,
                                        boolean toggle, boolean extend) {
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
                qualifierModel.fireTableDataChanged();
                deleteGroup.setEnabled(rowIndex >= 0);
            }
        };
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(groupTable);

        pane.setLeftComponent(scrollPane);

        JScrollPane pane2 = new JScrollPane();
        qualifiersTable = new JTable(qualifierModel);
        pane2.setViewportView(qualifiersTable);
        pane.setRightComponent(pane2);
        pane.setDividerLocation(400);
        return pane;
    }

    /**
     * @return the activeGroup
     */
    public synchronized Group getActiveGroup() {
        int row = groupTable.getSelectedRow();
        if (row < 0)
            return null;
        return groups.get(row);
    }

    /**
     * @return the activeUser
     */
    public synchronized User getActiveUser() {
        int row = userTable.getSelectedRow();
        if (row < 0)
            return null;
        return users.get(row);
    }


    protected void onOk() {
        List<Group> grps = factory.getGroups();
        for (Group group : groupsToUpdate) {
            if (findGroup(group.getName(), grps) != null) {
                factory.updateGroup(group);
            } else {
                factory.createGroup(group);
            }
        }

        List<User> urs = factory.getUsers();
        for (User user : usersToUpdate) {
            if (findUser(user.getLogin(), urs) != null) {
                factory.updateUser(user);
            } else {
                factory.createUser(user);
            }
        }

        for (String group : groupsToDelete) {
            factory.deleteGroup(group);
        }

        for (String user : usersToDelete) {
            factory.deleteUser(user);
        }
        super.onOk();
    }
}
