package com.ramussoft.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ramussoft.common.PluginFactory;
import com.ramussoft.core.impl.IEngineImpl;
import com.ramussoft.core.persistent.BranchesPersistentFactory;
import com.ramussoft.jdbc.JDBCCallback;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.jdbc.RowMapper;
import com.ramussoft.net.common.User;

public class ServerIEngineImpl extends IEngineImpl {

    private static final String USER = "/user/";

    private ServerAccessRules serverAccessRules;

    private BranchesPersistentFactory branchesPersistentFactory;

    public ServerIEngineImpl(int id, JDBCTemplate template, String prefix,
                             PluginFactory factory) throws ClassNotFoundException {
        super(id, template, prefix, factory, true);
        branchesPersistentFactory = new BranchesPersistentFactory(template,
                prefix);
    }

    private String transformStreamName(String oldPath) {
        if (oldPath.startsWith(USER)) {
            if (serverAccessRules == null) {
                return oldPath;
            }
            String end = oldPath.substring(USER.length() - 1);
            User user = serverAccessRules.getUser();
            if ("admin".equals(user.getLogin()))
                return oldPath;
            String login = user.getLogin();
            return USER + login + end;
        }
        return oldPath;
    }

    @Override
    protected boolean deleteStreamBytes(String aName) {
        String path = transformStreamName(aName);
        if (!path.startsWith("/elements")) {
            template.update("DELETE FROM binary_data WHERE \"name\"=?",
                    new Object[]{path}, true);
            return true;
        }
        long branchId = getActiveBranchId();
        template.update(
                "UPDATE binary_data SET removed_branch=? WHERE \"name\"=? AND "
                        + branchesPersistentFactory.getSQLBranchCondition(),
                new Object[]{branchId, path, branchId, branchId}, true);
        return true;
    }

    @Override
    public byte[] getStream(String aPath) {
        String path = transformStreamName(aPath);
        long branchId = getActiveBranchId();
        if (path.startsWith("/elements")) {
            if (!canReadElement(path)) {
                throw new RuntimeException("You can not read path " + path);
            }
        }
        return (byte[]) template.queryForObjects(
                "SELECT * FROM binary_data WHERE \"name\"=? AND "
                        + branchesPersistentFactory.getSQLBranchCondition(),
                new RowMapper() {
                    @Override
                    public Object mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        return rs.getBytes("data");
                    }
                }, new Object[]{path, branchId, branchId}, true);
    }

    @Override
    public void setStream(String path, byte[] bytes) {
        super.setStream(transformStreamName(path), bytes);
    }

    @Override
    protected void writeStream(final String path, final byte[] bytes) {
        template.execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                long branchId = getActiveBranch();
                PreparedStatement ps = connection
                        .prepareStatement("SELECT \"name\" FROM binary_data WHERE \"name\"=? AND created_branch_id=?");
                ps.setString(1, path);
                ps.setLong(2, branchId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    PreparedStatement statement = connection
                            .prepareStatement("UPDATE binary_data SET data=?, removed_branch_id=? WHERE \"name\"=? AND created_branch_id=?");
                    statement.setBytes(1, bytes);
                    statement.setObject(2, null);
                    statement.setString(3, path);
                    statement.setLong(4, branchId);
                    statement.execute();
                    statement.close();
                } else {
                    PreparedStatement statement = connection
                            .prepareStatement("UPDATE binary_data SET removed_branch_id=? WHERE \"name\"=? AND "
                                    + branchesPersistentFactory
                                    .getSQLBranchCondition());
                    statement.setLong(1, branchId);
                    statement.setString(2, path);
                    statement.setLong(3, branchId);
                    statement.setLong(4, branchId);
                    statement.execute();
                    statement.close();

                    statement = connection
                            .prepareStatement("INSERT INTO binary_data(data, \"name\", created_branch_id) VALUES(?, ?, ?)");
                    statement.setBytes(1, bytes);
                    statement.setString(2, path);
                    statement.setLong(3, branchId);
                    statement.execute();
                    statement.close();
                }
                rs.close();
                ps.close();
                return null;
            }
        });
    }

    public void setServerAccessRules(ServerAccessRules serverAccessRules) {
        this.serverAccessRules = serverAccessRules;
    }

    public ServerAccessRules getServerAccessRules() {
        return serverAccessRules;
    }

}
