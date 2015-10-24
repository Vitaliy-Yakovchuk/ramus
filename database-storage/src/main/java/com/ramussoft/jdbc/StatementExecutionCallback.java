package com.ramussoft.jdbc;

import java.sql.SQLException;
import java.sql.Statement;

public interface StatementExecutionCallback {
    Object execute(Statement statement) throws SQLException;
}
