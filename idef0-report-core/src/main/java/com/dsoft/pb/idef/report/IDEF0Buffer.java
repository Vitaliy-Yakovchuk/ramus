package com.dsoft.pb.idef.report;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Row;
import com.ramussoft.report.data.RowSet;
import com.ramussoft.report.data.Rows;

public class IDEF0Buffer {

    private Hashtable<Row, Hashtable<Row, Boolean>> functionStreamsBuff = new Hashtable<Row, Hashtable<Row, Boolean>>();

    private Hashtable<Row, Hashtable<Row, Boolean>> rowFunctionsBuff = new Hashtable<Row, Hashtable<Row, Boolean>>();

    private Hashtable<Row, Rows> functionStreams = new Hashtable<Row, Rows>();

    private Hashtable<Row, Rows> rowFunctions = new Hashtable<Row, Rows>();

    private Data data;

    private RowSet streams;

    private RowSet functions;

    private Row row;

    private Row stream;

    public IDEF0Buffer(final Data aData, Qualifier model) {
        this.data = aData;
        this.streams = getStreamsRowSet(aData);
        this.functions = aData.getRowSet(model, false);
    }

    public Row addFunctionStream(long functionId, long streamId,
                                 com.ramussoft.database.common.Row parent) {
        Row function = (Row) functions.findRow(functionId);
        stream = (Row) streams.findRow(streamId);
        if (function == null || stream == null)
            return null;
        if (parent != null
                && (function.getParent() == null || !function.getParent()
                .equals(parent)))
            return null;
        addFunctionStream(function, stream);
        return function;
    }

    private void addFunctionStream(Row function, Row stream) {
        Hashtable<Row, Boolean> rows = getStreamsBuff(function);
        rows.put(stream, Boolean.TRUE);
    }

    public void addRowFunction(long rowId, long functionId, String status,
                               com.ramussoft.database.common.Row parent) {
        Row r = data.findRow(rowId);
        if (r == null)
            return;
        row = r.createCopy();

        row.setElementStatus(status);
        Row function = (Row) functions.findRow(functionId);
        if (function == null || row == null)
            return;
        if (parent != null
                && (function.getParent() == null || !function.getParent()
                .equals(parent)))
            return;

        addRowFunction(row, function);
    }

    public void addRowFunction(Row function) {
        addRowFunction(row, function);
    }

    public void addFunctionStream(Row function) {
        addFunctionStream(function, stream);
    }

    private void addRowFunction(Row row, Row function) {
        if (row == null)
            return;
        Hashtable<Row, Boolean> rows = getFunctionsBuff(row);
        rows.put(function, Boolean.TRUE);
    }

    private Hashtable<Row, Boolean> getStreamsBuff(Row function) {
        Hashtable<Row, Boolean> stream = functionStreamsBuff.get(function);
        if (stream == null) {
            stream = new Hashtable<Row, Boolean>();
            functionStreamsBuff.put(function, stream);
        }
        return stream;
    }

    private Hashtable<Row, Boolean> getFunctionsBuff(Row row) {
        Hashtable<Row, Boolean> function = rowFunctionsBuff.get(row);
        if (function == null) {
            function = new Hashtable<Row, Boolean>();
            rowFunctionsBuff.put(row, function);
        }
        return function;
    }

    public void commit(boolean all) {
        Enumeration<Row> keys = functionStreamsBuff.keys();
        while (keys.hasMoreElements()) {
            Row key = keys.nextElement();
            Hashtable<Row, Boolean> fs = functionStreamsBuff.get(key);
            Rows rows = new Rows(streams, data, false);
            functionStreams.put(key, rows);
            Enumeration<Row> streams = fs.keys();
            while (streams.hasMoreElements())
                rows.add(streams.nextElement());
        }

        List<com.ramussoft.database.common.Row> allRows = this.functions
                .getAllRows();
        Row[] functions = allRows.toArray(new Row[allRows.size()]);

        keys = rowFunctionsBuff.keys();
        while (keys.hasMoreElements()) {
            Row key = keys.nextElement();
            Hashtable<Row, Boolean> rf = rowFunctionsBuff.get(key);
            Rows fns = new Rows(this.functions, data, false);
            // fns.addAll(rf.keySet());
            rowFunctions.put(key, fns);
            for (Row f : functions)
                if (all || getChildCount(f) == 0)
                    if (rf.get(f) != null)
                        fns.add(f);
        }
        functionStreamsBuff = null;
        rowFunctionsBuff = null;
    }

    private int getChildCount(Row f) {
        int c = 0;
        for (com.ramussoft.database.common.Row row : f.getChildren()) {
            Integer type = (Integer) row.getAttribute(IDEF0Plugin
                    .getFunctionTypeAttribute(f.getEngine()));
            if (type != null && type.intValue() <= 1001)
                c++;
        }
        return c;
    }

    public Rows getFunctions(Row row) {
        Rows rows = rowFunctions.get(row);
        if (rows == null)
            return new Rows(functions, data, false);
        Rows clone = (Rows) rows.clone();
        return clone;
    }

    public Rows getStreams(Row function) {
        Rows rows = functionStreams.get(function);
        if (rows == null)
            return new Rows(streams, data, false);
        Rows clone = (Rows) rows.clone();
        return clone;
    }

    public static RowSet getStreamsRowSet(final Data data) {
        RowSet rowSet = (RowSet) data.get("Streams");
        if (rowSet != null)
            return rowSet;
        com.ramussoft.database.common.RowSet.RowCreater rowCreater = new com.ramussoft.database.common.RowSet.RowCreater() {

            @Override
            public com.ramussoft.database.common.Row createRow(
                    Element aElement,
                    com.ramussoft.database.common.RowSet rowSet,
                    Attribute[] attributes, Object[] objects) {
                return new Stream(aElement, (RowSet) rowSet, attributes,
                        objects, data);
            }

        };

        Engine engine = data.getEngine();

        rowSet = new RowSet(engine, IDEF0Plugin.getBaseStreamQualifier(engine),
                data, rowCreater);
        data.put("Streams", rowSet);
        return rowSet;
    }

    public Row getRow() {
        return row;
    }
}
