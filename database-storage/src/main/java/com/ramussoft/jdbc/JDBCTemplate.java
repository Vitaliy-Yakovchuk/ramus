package com.ramussoft.jdbc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class JDBCTemplate {

    private Connection connection;

    private Hashtable<String, PreparedStatement> statements = new Hashtable<String, PreparedStatement>();

    public JDBCTemplate(Connection connection) {
        this.connection = connection;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List query(final String sql, final RowMapper mapper) {
        return (List<Object>) query(new StatementExecutionCallback() {
            @Override
            public Object execute(Statement statement) throws SQLException {
                ResultSet rs = statement.executeQuery(sql);
                int rowNum = 0;
                ArrayList<Object> res = new ArrayList<Object>();
                while (rs.next()) {
                    res.add(mapper.mapRow(rs, rowNum));
                    rowNum++;
                }
                rs.close();
                return res;
            }
        });
    }

    public Object update(String sql, final long id, boolean cached) {
        return execute(sql, new ExecutionCallback() {

            @Override
            public Object execute(PreparedStatement statement)
                    throws SQLException {
                statement.setLong(1, id);
                return statement.execute();
            }

        }, cached);
    }

    public Object update(String sql, final Object[] objects, boolean cached) {
        return execute(sql, new ExecutionCallback() {

            @Override
            public Object execute(PreparedStatement statement)
                    throws SQLException {
                setParams(statement, objects);
                return statement.execute();
            }

        }, cached);
    }

    public Object update(String sql, final Object[] objects, boolean cached,
                         JDBCCallback callback) {
        return execute(sql, new ExecutionCallback() {

            @Override
            public Object execute(PreparedStatement statement)
                    throws SQLException {
                setParams(statement, objects);
                return statement.execute();
            }

        }, cached, callback);
    }

    private Object execute(final String sql,
                           final ExecutionCallback executionCallback, final boolean cached) {
        return execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                PreparedStatement ps = getPreparedStatement(sql, cached);
                synchronized (ps) {
                    Object res = executionCallback.execute(ps);
                    if (!cached)
                        ps.close();
                    return res;
                }
            }
        });
    }

    private Object execute(final String sql,
                           final ExecutionCallback executionCallback, final boolean cached,
                           final JDBCCallback callback) {
        return execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                PreparedStatement ps = getPreparedStatement(sql, cached);
                synchronized (ps) {
                    executionCallback.execute(ps);
                    if (!cached)
                        ps.close();
                    return callback.execute(connection);
                }
            }
        });
    }

    public PreparedStatement getPreparedStatement(String sql, boolean cached)
            throws SQLException {
        if (cached) {
            PreparedStatement ps = statements.get(sql);
            if (ps == null) {
                ps = connection.prepareStatement(sql);
                statements.put(sql, ps);
            }
            return ps;
        } else {
            return connection.prepareStatement(sql);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List query(final String sql, final RowMapper mapper,
                      final Object[] objects, boolean cached) {
        return (List<Object>) query(sql, new ExecutionCallback() {
            @Override
            public Object execute(PreparedStatement statement)
                    throws SQLException {
                setParams(statement, objects);
                ResultSet rs = statement.executeQuery();
                int rowNum = 0;
                ArrayList<Object> res = new ArrayList<Object>();
                while (rs.next()) {
                    Object row = mapper.mapRow(rs, rowNum);
                    if (row != null)
                        res.add(row);
                    rowNum++;
                }
                rs.close();
                return res;
            }
        }, cached);
    }

    public void queryWithoutResults(final String sql, final RowMapper mapper,
                                    boolean cached) {
        query(sql, new ExecutionCallback() {
            @Override
            public Object execute(PreparedStatement statement)
                    throws SQLException {
                ResultSet rs = statement.executeQuery();
                int rowNum = 0;
                while (rs.next()) {
                    mapper.mapRow(rs, rowNum);
                    rowNum++;
                }
                rs.close();
                return null;
            }
        }, cached);
    }

    public void setParams(PreparedStatement ps, Object[] params)
            throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object object = params[i];
            setParam(ps, i + 1, object);
        }
    }

    public void setParam(PreparedStatement ps, int parameterIndex,
                         Object object) throws SQLException {
        if (object instanceof Timestamp) {
            ps.setTimestamp(parameterIndex, (Timestamp) object);
        } else if (object instanceof Date) {
            ps.setDate(parameterIndex, (Date) object);
        } else if (object instanceof String) {
            ps.setString(parameterIndex, (String) object);
        } else if (object instanceof Integer) {
            ps.setInt(parameterIndex, ((Integer) object).intValue());
        } else if (object instanceof Long) {
            ps.setLong(parameterIndex, ((Long) object).longValue());
        } else if (object instanceof Boolean) {
            ps.setBoolean(parameterIndex, ((Boolean) object).booleanValue());
        } else {
            ps.setObject(parameterIndex, object);
        }
    }

    public Object query(String sql, ExecutionCallback executionCallback,
                        boolean cached) {
        try {
            PreparedStatement ps = getPreparedStatement(sql, cached);
            synchronized (ps) {
                Object res = executionCallback.execute(ps);
                if (!cached)
                    ps.close();
                return res;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Object execute(
            final StatementExecutionCallback statementExecutionCallback)
            throws SQLException {
        return execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                Statement st = connection.createStatement();

                Object res = statementExecutionCallback.execute(st);
                st.close();
                return res;
            }
        });
    }

    public Object query(StatementExecutionCallback statementExecutionCallback) {
        Statement st;
        try {
            st = connection.createStatement();
            Object res = statementExecutionCallback.execute(st);

            st.close();
            return res;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Object executeResource(String resourceName) throws SQLException,
            IOException {
        String sql = extractTextFromResource(resourceName);
        return execute(sql);
    }

    public Object executeResource(String resourceName, String prefix)
            throws SQLException, IOException {
        String sql = extractTextFromResource(resourceName);
        return execute(MessageFormat.format(sql, prefix));
    }

    private String extractTextFromResource(String resourceName)
            throws IOException {
        InputStream is = getClass().getResourceAsStream(resourceName);
        byte[] buff = new byte[1024];
        ByteArrayOutputStream sb = new ByteArrayOutputStream();
        int r;
        while ((r = is.read(buff)) > 0) {
            sb.write(buff, 0, r);
        }
        is.close();
        return new String(sb.toByteArray(), "UTF-8");
    }

    public Object execute(final String sql) throws SQLException {
        return execute(new StatementExecutionCallback() {
            @Override
            public Object execute(Statement statement) throws SQLException {
                return statement.execute(sql);
            }
        });
    }

    public void close() throws SQLException {
        for (PreparedStatement ps : statements.values())
            ps.close();
        connection.close();
    }

    public Connection getConnection() {
        return connection;
    }

    public void execute(String sql, String prefix) throws SQLException {
        execute(MessageFormat.format(sql, prefix));
    }

    public Object execute(JDBCCallback callback) {
        try {
            Object res = callback.execute(connection);
            connection.commit();
            return res;
        } catch (Exception e) {
            try {
                connection.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                } else if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            } catch (SQLException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    public long queryForLong(final String sql) {
        return (Long) execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql);
                Long res = null;
                if (rs.next()) {
                    res = rs.getLong(1);
                }
                rs.close();
                st.close();
                return res;
            }
        });
    }

    public long nextVal(String sequence) {
        return queryForLong("SELECT nextval(\'" + sequence + "\')");
    }

    public Object queryForObject(final String sql, final RowMapper rowMapper,
                                 final long id, final boolean cached) {
        return execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                PreparedStatement ps = getPreparedStatement(sql, cached);
                synchronized (ps) {
                    ps.setLong(1, id);
                    ResultSet rs = ps.executeQuery();
                    Object mapRow = null;
                    if (rs.next()) {
                        mapRow = rowMapper.mapRow(rs, 0);
                    }
                    if (!cached)
                        ps.close();
                    return mapRow;
                }
            }
        });
    }

    public Object queryForObjects(final String sql, final RowMapper rowMapper,
                                  final Object[] params, final boolean cached) {
        return execute(new JDBCCallback() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                PreparedStatement ps = getPreparedStatement(sql, cached);
                synchronized (ps) {
                    setParams(ps, params);
                    ResultSet rs = ps.executeQuery();
                    Object mapRow = null;
                    if (rs.next()) {
                        mapRow = rowMapper.mapRow(rs, 0);
                    }
                    if (!cached)
                        ps.close();
                    return mapRow;
                }
            }
        });
    }

    public static StringBuffer toSqlArray(long[] elementIds) {
        StringBuffer sb = new StringBuffer();
        if (elementIds.length > 0)
            sb.append("" + elementIds[0]);
        for (int i = 1; i < elementIds.length; i++)
            sb.append(", " + elementIds[i]);
        return sb;
    }
}
