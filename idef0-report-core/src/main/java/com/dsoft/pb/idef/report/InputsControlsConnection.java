package com.dsoft.pb.idef.report;

import com.ramussoft.common.Qualifier;
import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Row;
import com.ramussoft.report.data.Rows;
import com.ramussoft.report.data.plugin.AbstractConnection;
import com.ramussoft.report.data.plugin.Connection;

public class InputsControlsConnection extends AbstractConnection {

    private Connection inputs;

    private Connection controls;

    public InputsControlsConnection(boolean all) {
        inputs = new FastIdef0Connection(2, all);
        controls = new FastIdef0Connection(3, all);
    }

    @Override
    public Rows getConnected(Data data, Row row) {
        Rows rows = inputs.getConnected(data, row);
        rows.addAll(controls.getConnected(data, row));
        return rows;
    }

    @Override
    public Qualifier getOpposite(Data data, Qualifier qualifier) {
        return inputs.getOpposite(data, qualifier);
    }

}
