package com.ramussoft.report.data.plugin;

import com.ramussoft.common.Qualifier;
import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Row;
import com.ramussoft.report.data.Rows;

public interface ConnectionPlugin {

    Connection getConnection(Data data, Qualifier qualifier, String name);

    Rows getVirtualQualifier(Data data, String name);

    Object getAttribute(Data data, Row row, String name);

}
