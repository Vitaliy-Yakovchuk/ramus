package com.dsoft.pb.idef.report;

import java.util.List;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.attribute.AnyToAnyPersistent;
import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Row;
import com.ramussoft.report.data.RowSet;
import com.ramussoft.report.data.Rows;
import com.ramussoft.report.data.plugin.AbstractConnection;

public class QualifiersConnection extends AbstractConnection {

    @SuppressWarnings("unchecked")
    @Override
    public Rows getConnected(Data data, Row row) {
        if (!(row.getQualifier().equals(IDEF0Plugin.getBaseStreamQualifier(data
                .getEngine()))))
            return new Rows(null, data, false);
        List<AnyToAnyPersistent> list = (List) row.getAttribute(IDEF0Plugin
                .getStreamAddedAttribute(data.getEngine()));

        final Engine engine = data.getEngine();

        Rows rows = new Rows(null, data, false) {
            /**
             *
             */
            private static final long serialVersionUID = -284240974188065028L;

            @Override
            public RowSet getRowSet() {
                Row current = getCurrent();
                if (current == null)
                    return null;
                return current.getRowSet();
            }

            @Override
            public Row addRow(long elementId) {
                long qId = engine.getQualifierIdForElement(elementId);
                if (qId >= 0l) {
                    Qualifier qualifier = this.data.getQualifier(qId);
                    if (qualifier != null) {
                        RowSet rowSet = this.data.getRowSet(qualifier);
                        Row row = (Row) rowSet.findRow(elementId);
                        if (row != null) {
                            add(row);
                            return row;
                        }
                    }
                }
                return null;
            }

        };
        for (AnyToAnyPersistent p : list) {
            Row row2 = rows.addRow(p.getOtherElement());
            if (row2 != null)
                row2.setElementStatus(p.getElementStatus());
        }

        return rows;
    }

    @Override
    public Qualifier getOpposite(Data data, Qualifier qualifier) {
        if (qualifier.equals(IDEF0Plugin.getBaseStreamQualifier(data
                .getEngine())))
            return null;
        return IDEF0Plugin.getBaseStreamQualifier(data.getEngine());
    }

}
