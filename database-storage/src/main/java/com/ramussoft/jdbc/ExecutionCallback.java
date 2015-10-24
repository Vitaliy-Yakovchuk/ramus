package com.ramussoft.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ExecutionCallback {
    Object execute(PreparedStatement statement) throws SQLException;
}
