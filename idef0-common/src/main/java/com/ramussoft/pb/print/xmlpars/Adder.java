package com.ramussoft.pb.print.xmlpars;

import java.util.Vector;

import com.ramussoft.pb.MatrixProjection;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.data.projections.AbstractMatrixProjection;

public class Adder extends AbstractMatrixProjection {

    private final MatrixProjection a;

    private final MatrixProjection b;

    public Adder(final MatrixProjection a, final MatrixProjection b) {
        super();
        this.a = a;
        this.b = b;
    }

    public Vector<Row> getLeft(final Row row) {
        final Vector<Row> res = a.getLeft(row);
        final Vector<Row> t = b.getLeft(row);
        for (int i = 0; i < t.size(); i++) {
            final Row r = t.get(i);
            if (res.indexOf(r) < 0)
                res.add(r);
        }
        return res;
    }

    public Vector<Row> getRight(final Row row) {
        final Vector<Row> res = a.getRight(row);
        final Vector<Row> t = b.getRight(row);
        for (int i = 0; i < t.size(); i++) {
            final Row r = t.get(i);
            if (res.indexOf(r) < 0)
                res.add(r);
        }
        return res;
    }

    public Row getRow1() {
        return a.getRow1();
    }

    public Row getRow2() {
        return a.getRow2();
    }

    public boolean isHere(final Row row) {
        return a.isHere(row) || b.isHere(row);
    }

    @Override
    public Row[] getLeftRows() {
        Row[] rows = a.getLeftRows();
        final Vector<Row> res = new Vector();
        for (final Row r : rows)
            res.add(r);
        rows = b.getLeftRows();
        for (final Row r : rows)
            if (res.indexOf(r) < 0)
                res.add(r);
        rows = new Row[res.size()];
        for (int i = 0; i < rows.length; i++)
            rows[i] = res.get(i);
        return rows;
    }
}
