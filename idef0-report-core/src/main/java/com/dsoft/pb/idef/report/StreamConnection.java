package com.dsoft.pb.idef.report;

import java.util.List;

import com.ramussoft.common.Qualifier;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.attribute.AnyToAnyPersistent;
import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Row;
import com.ramussoft.report.data.Rows;
import com.ramussoft.report.data.plugin.AbstractConnection;

public class StreamConnection extends AbstractConnection {

    private final Qualifier qualifier;

    public StreamConnection(Qualifier qualifier) {
        this.qualifier = qualifier;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Rows getConnected(Data data, Row row) {
        List<AnyToAnyPersistent> list = (List) row.getAttribute(IDEF0Plugin
                .getStreamAddedAttribute(data.getEngine()));

        Rows rows = new Rows(data.getRowSet(qualifier), data, false);
        for (AnyToAnyPersistent p : list) {
            Row row2 = rows.getRowById(p.getOtherElement());
            if (row2 != null) {
                row2.setElementStatus(p.getElementStatus());
                rows.add(row2);
            }
        }

        return rows;
    }

    @Override
    public Qualifier getOpposite(Data data, Qualifier qualifier) {
        if (!qualifier.equals(IDEF0Plugin.getBaseStreamQualifier(data
                .getEngine())))
            return IDEF0Plugin.getBaseStreamQualifier(data.getEngine());
        return null;
    }
}
