package com.dsoft.pb.idef.report;

import java.util.Hashtable;

import com.ramussoft.common.Qualifier;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.DataException;
import com.ramussoft.report.data.Row;
import com.ramussoft.report.data.plugin.AbstractConnection;

public abstract class IDEF0Connection extends AbstractConnection {
    protected String getRecIDEF0Kod(final Row function) {
        final Row f = (Row) function.getParent();
        if (f == null || f.getParent() == null)
            return "";
        String id = Integer.toString(function.getId());
        if (id.length() > 1)
            id = "." + id + ".";
        return getRecIDEF0Kod(f) + id;
    }

    /**
     * Метод визначає код функціонального блоку у відповідності до стандарту
     * IDEF0
     *
     * @param function Функціональний блок, для якого буде визначений його код.
     * @return Код функціонального блока у відповідності до стандарту IDEF0.
     */

    public String getIDEF0Kod(final Row function) {
        final Row f = (Row) function.getParent();
        if (f == null)
            return "A-0";
        if (f.getParent() == null)
            return "A0";
        return "A" + getRecIDEF0Kod(function);
    }

    protected void addAll(Row row2, Hashtable<Long, Boolean> add) {
        for (Object row : row2.getChildren()) {
            add.put(((Row) row).getElementId(), Boolean.TRUE);
            addAll((Row) row, add);
        }
    }

    protected class FunctionId {
        Long id;

        int tunnelSoft;

        public FunctionId(long id, int tunnelSoft) {
            this.id = id;
            this.tunnelSoft = tunnelSoft;
        }

        @Override
        public String toString() {
            return "id=" + id + ", tunnelSoft=" + tunnelSoft;
        }
    }

    @Override
    public Qualifier getOpposite(Data data, Qualifier qualifier) {
        if (IDEF0Plugin.isFunction(qualifier))
            return IDEF0Plugin.getBaseStreamQualifier(data.getEngine());
        String modelName = data.getQuery().getAttribute("ReportFunction");
        if (modelName == null)
            throw new DataException("Error.noModelProperty",
                    "Set model attribute for report first!!!");
        return data.getQualifier(modelName);
    }
}
