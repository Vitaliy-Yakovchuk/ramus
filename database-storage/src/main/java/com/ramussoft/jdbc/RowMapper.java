package com.ramussoft.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper {
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException;
}
