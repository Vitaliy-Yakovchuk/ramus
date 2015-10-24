package com.ramussoft.report.data.plugin;

import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Row;
import com.ramussoft.report.data.Rows;

public abstract class AbstractConnectionPlugin implements ConnectionPlugin {

    @Override
    public Rows getVirtualQualifier(Data data, String name) {
        return null;
    }

    @Override
    public Object getAttribute(Data data, Row row, String name) {
        return null;
    }

}
