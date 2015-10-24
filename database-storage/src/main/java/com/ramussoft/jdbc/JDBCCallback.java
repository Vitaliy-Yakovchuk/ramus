package com.ramussoft.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public interface JDBCCallback {
    public Object execute(Connection connection) throws SQLException;
}
