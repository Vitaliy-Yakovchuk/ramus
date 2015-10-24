package com.ramussoft.pb.data.projections;

import java.util.Vector;

import com.ramussoft.pb.Function;
import com.ramussoft.pb.Main;
import com.ramussoft.pb.Row;

/**
 * Клас, який представляє зв’язок між роботами і власниками у вигляді матричної
 * проекції.
 *
 * @author ZDD
 */

public class MatrixProjectionOuner extends AbstractMatrixProjection {

    public Vector<Row> getLeft(final Row row) {
        final Vector<Row> res = new Vector<Row>();
        final Function f = (Function) row;
        final Row o = f.getOwner();
        if (o != null)
            res.add(o);
        return res;
    }

    public Vector<Row> getRight(final Row row) {
        final Vector<Row> res = new Vector<Row>();
        final Vector<Row> functions = Main.dataPlugin.getRecChilds(Main.dataPlugin
                .getBaseFunction(), true);
        for (int i = 0; i < functions.size(); i++) {
            final Function f = (Function) functions.get(i);
            final Row o = f.getOwner();
            if (o.equals(row))
                res.add(f);
        }
        return res;
    }

    @Override
    public boolean isJoined(final Row left, final Row right) {
        return false;
    }

    public Row getRow1() {
        return null;
    }

    public Row getRow2() {
        return null;
    }

    public boolean isHere(final Row row) {
        return false;
    }

    @Override
    public String getName() {
        return "owner_matrix_projection";
    }

}
