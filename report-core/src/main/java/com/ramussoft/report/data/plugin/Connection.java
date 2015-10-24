package com.ramussoft.report.data.plugin;

import com.ramussoft.common.Qualifier;
import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Row;
import com.ramussoft.report.data.Rows;

public interface Connection {

    Rows getConnected(Data data, Row row);

    Qualifier getOpposite(Data data, Qualifier qualifier);

}
