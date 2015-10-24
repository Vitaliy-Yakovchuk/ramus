package com.ramussoft.pb.data.projections;

import java.util.Vector;

import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.RowFactory;

/**
 * Клас, який представляє у вигляді матричної проекції класифікатор потоків і
 * набір класифікаторів пов’язаних з класифікатором потоків.
 *
 * @author ZDD
 */

public class MatrixProjectionStreams extends AbstractMatrixProjection {

    private DataPlugin dataPlugin;

    private final Vector<Row> streams;

    public MatrixProjectionStreams(DataPlugin dataPlugin) {
        this.dataPlugin = dataPlugin;
        streams = dataPlugin.getChilds(dataPlugin.getBaseStream(), true);
    }

    /**
     * Повертає набір елементів класифікатор потоків, які пов’язані з
     * довільнимелементом класифікатора.
     */

    public Vector<Row> getRight(final Row row) {
        final Vector<Row> res = new Vector<Row>();
        for (int i = 0; i < streams.size(); i++)
            if (RowFactory.isPresent(((Stream) streams.get(i)).getAdded(), row))
                res.add(streams.get(i));
        return res;
    }

    /**
     * Повертає набір класифікаторів пов’язаних з елементом класифікатора
     * потоків, атрибут обов’язково має бети елементом класифікатора потоків.
     */

    public Vector<Row> getLeft(final Row row) {
        final Row[] rows = ((Stream) row).getAdded();
        final Vector<Row> res = new Vector<Row>();
        for (final Row row2 : rows)
            res.add(row2);
        return res;
    }

    public Row getRow1() {
        return dataPlugin.getBaseStream();
    }

    public Row getRow2() {
        return null;
    }

    public boolean isHere(final Row row) {
        return false;
    }

}
