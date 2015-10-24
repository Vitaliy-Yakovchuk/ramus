package com.ramussoft.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ramussoft.jdbc.JDBCCallback;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.jdbc.RowMapper;
import com.ramussoft.net.common.Group;
import com.ramussoft.net.common.User;
import com.ramussoft.net.common.UserFactory;

public class UserFactoryImpl implements UserFactory {

    private final JDBCTemplate template;

    public UserFactoryImpl(JDBCTemplate template) {
        this.template = template;
    }

    @Override
    public void createGroup(final Group group) {
        template.execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                PreparedStatement ps = connection
                        .prepareStatement("INSERT INTO GROUPS(group_name) VALUES(?)");
                ps.setString(1, group.getName());
                ps.execute();
                ps.close();
                insertGroupAttributes(group, connection);
                return null;
            }
        });
    }

    protected void insertGroupAttributes(Group group, Connection connection)
            throws SQLException {
        PreparedStatement ps = connection
                .prepareStatement("INSERT INTO group_qualifier_access(group_name, qualifier_id) VALUES(?, ?)");
        for (Long l : group.getQualifierIds()) {
            ps.setString(1, group.getName());
            ps.setLong(2, l);
            ps.execute();
        }
        ps.close();
    }

    @Override
    public void createUser(final User user) {
        template.execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {

                PreparedStatement us = connection
                        .prepareStatement("INSERT INTO USERS(\"login\", \"name\", \"password\") VALUES(?, ?, ?)");

                us.setString(1, user.getLogin());

                us.setString(2, user.getName());

                us.setString(3, user.getPassword());

                us.execute();
                us.close();

                PreparedStatement ps = connection
                        .prepareStatement("INSERT INTO users_groups(\"login\", group_name) VALUES(?, ?)");

                for (Group group : user.getGroups()) {
                    ps.setString(1, user.getLogin());
                    ps.setString(2, group.getName());
                    ps.execute();
                }
                ps.close();

                return null;
            }
        });

    }

    @Override
    public void deleteGroup(final String groupName) {
        if (!groupName.equals("admin"))
            template.execute(new JDBCCallback() {
                @Override
                public Object execute(Connection connection)
                        throws SQLException {
                    PreparedStatement d1 = connection
                            .prepareStatement("DELETE FROM USERS_GROUPS WHERE GROUP_NAME=?");
                    d1.setString(1, groupName);
                    d1.execute();
                    d1.close();

                    d1 = connection
                            .prepareStatement("DELETE FROM group_qualifier_access WHERE GROUP_NAME=?");

                    d1.setString(1, groupName);
                    d1.execute();
                    d1.close();

                    d1 = connection
                            .prepareStatement("DELETE FROM GROUPS WHERE GROUP_NAME=?");

                    d1.setString(1, groupName);
                    d1.execute();
                    d1.close();

                    return null;
                }
            });
    }

    @Override
    public void deleteUser(final String login) {
        if (!login.equals("admin"))
            template.execute(new JDBCCallback() {
                @Override
                public Object execute(Connection connection)
                        throws SQLException {
                    PreparedStatement d1 = connection
                            .prepareStatement("DELETE FROM USERS_GROUPS WHERE \"login\"=?");
                    d1.setString(1, login);
                    d1.execute();
                    d1.close();

                    d1 = connection
                            .prepareStatement("DELETE FROM USERS WHERE \"login\"=?");

                    d1.setString(1, login);
                    d1.execute();
                    d1.close();

                    return null;
                }
            });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Group> getGroups() {
        return template.query("SELECT * FROM GROUPS ORDER BY group_name",
                new RowMapper() {
                    @Override
                    public Object mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        Group group = new Group();
                        group.setName(rs.getString("group_name").trim());
                        PreparedStatement ps = template
                                .getConnection()
                                .prepareStatement(
                                        "SELECT qualifier_id FROM group_qualifier_access WHERE group_name=? ORDER BY qualifier_id");
                        fillGroup(group, ps);
                        ps.close();
                        return group;
                    }

                });
    }

    private void fillGroup(Group group, PreparedStatement ps)
            throws SQLException {
        ps.setString(1, group.getName());
        ResultSet rs1 = ps.executeQuery();
        while (rs1.next()) {
            group.getQualifierIds().add(rs1.getLong(1));
        }
        rs1.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<User> getUsers() {
        return (List<User>) template.execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                List<User> result = new ArrayList<User>();
                Statement st = connection.createStatement();
                PreparedStatement groups = connection
                        .prepareStatement("SELECT * FROM users_groups WHERE \"login\"=? ORDER BY group_name");
                ResultSet rs = st
                        .executeQuery("SELECT * FROM users ORDER BY \"login\"");

                PreparedStatement groupQualifiers = template
                        .getConnection()
                        .prepareStatement(
                                "SELECT qualifier_id FROM group_qualifier_access WHERE group_name=? ORDER BY qualifier_id");

                while (rs.next()) {
                    User user = createUser(groups, rs, groupQualifiers);
                    result.add(user);
                }
                groupQualifiers.close();
                groups.close();
                rs.close();
                st.close();
                return result;
            }

        });
    }

    private User createUser(PreparedStatement groups, ResultSet rs,
                            PreparedStatement groupQualifiers) throws SQLException {
        User user = new User();
        user.setName(rs.getString("name").trim());
        String login = rs.getString("login").trim();
        user.setLogin(login);
        user.setPassword(rs.getString("password").trim());
        groups.setString(1, login);
        ResultSet grs = groups.executeQuery();
        while (grs.next()) {
            Group group = new Group();
            group.setName(grs.getString("group_name").trim());
            fillGroup(group, groupQualifiers);
            user.getGroups().add(group);
        }
        grs.close();
        return user;
    }

    @Override
    public void updateUser(final User user) {
        template.execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {

                if (user.getName().equals("admin")) {
                    boolean adm = false;
                    for (Group group : user.getGroups()) {
                        if (group.getName().equals("admin"))
                            adm = true;
                    }
                    if (!adm)
                        user.getGroups().add(new Group("admin"));
                }

                PreparedStatement ps = connection
                        .prepareStatement("UPDATE USERS SET \"name\"=?, \"password\"=? WHERE \"login\"=?");
                ps.setString(1, user.getName());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getLogin());
                ps.execute();
                ps.close();
                ps = connection
                        .prepareStatement("DELETE FROM users_groups WHERE \"login\"=?");
                ps.setString(1, user.getLogin());
                ps.execute();
                ps.close();
                ps = connection
                        .prepareStatement("INSERT INTO users_groups(\"login\", group_name) VALUES(?, ?)");
                for (Group group : user.getGroups()) {
                    ps.setString(1, user.getLogin());
                    ps.setString(2, group.getName());
                    ps.execute();
                }
                ps.close();
                return null;
            }
        });
    }

    @Override
    public User getUser(final String login) {
        return (User) template.execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                PreparedStatement selUser = connection
                        .prepareStatement("SELECT * FROM USERS WHERE \"login\"=?");
                selUser.setString(1, login);
                ResultSet rs = selUser.executeQuery();

                PreparedStatement groupQualifiers = template
                        .getConnection()
                        .prepareStatement(
                                "SELECT qualifier_id FROM group_qualifier_access WHERE group_name=? ORDER BY qualifier_id");

                if (rs.next()) {
                    PreparedStatement groups = connection
                            .prepareStatement("SELECT * FROM users_groups WHERE \"login\"=? ORDER BY group_name");
                    User user = createUser(groups, rs, groupQualifiers);
                    groups.close();
                    rs.close();
                    selUser.close();
                    return user;
                }
                rs.close();
                groupQualifiers.close();
                selUser.close();
                return null;
            }
        });
    }

    @Override
    public void updateGroup(final Group group) {
        template.execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                PreparedStatement ps = connection
                        .prepareStatement("DELETE FROM group_qualifier_access WHERE group_name=?");
                ps.setString(1, group.getName());
                ps.execute();
                ps.close();
                insertGroupAttributes(group, connection);
                return null;
            }
        });

    }

    @Override
    public Group getGroup(String groupName) {
        Group group = new Group();
        group.setName(groupName);
        return group;
    }
}
