/*
 * Created on 10/9/2005
 */
package com.ramussoft.pb.data.projections;

import java.util.Vector;

import com.ramussoft.pb.MatrixProjection;
import com.ramussoft.pb.Row;

/**
 * Клас призначений для додавання елементів, пов’язаних матричною проекцією, а
 * такох батьківських елементів до цих елементів.
 *
 * @author ZDD
 */
public class MatrixProjectionAdder extends AbstractMatrixProjection {

    private MatrixProjection matrixProjection = null;

    public MatrixProjectionAdder(final MatrixProjection matrixProjection) {
        super();
        this.matrixProjection = matrixProjection;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getLeft(com.jason.clasificators.elements.data.Row)
     */
    public Vector<Row> getLeft(final Row row) {
        final Vector<Row> res = new Vector<Row>();
        final Vector<Row> a = matrixProjection.getLeft(row);
        final Vector<Row[]> b = matrixProjection.getLeftParent(row);
        for (int i = 0; i < a.size(); i++)
            res.add(a.get(i));
        for (int i = 0; i < b.size(); i++)
            res.add(b.get(i)[1]);
        return res;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getRight(com.jason.clasificators.elements.data.Row)
     */
    public Vector<Row> getRight(final Row row) {
        final Vector<Row> res = new Vector<Row>();
        final Vector<Row> a = matrixProjection.getRight(row);
        final Vector<Row[]> b = matrixProjection.getRightParent(row);
        for (int i = 0; i < a.size(); i++)
            res.add(a.get(i));
        for (int i = 0; i < b.size(); i++)
            res.add(b.get(i)[1]);
        return res;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#setJoin(com.jason.clasificators.elements.data.Row,
     *      com.jason.clasificators.elements.data.Row)
     */
    @Override
    public boolean setJoin(final Row left, final Row right) {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#isJoined(com.jason.clasificators.elements.data.Row,
     *      com.jason.clasificators.elements.data.Row)
     */
    @Override
    public boolean isJoined(final Row left, final Row right) {
        return matrixProjection.isJoined(left, right);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getRow1()
     */
    public Row getRow1() {
        return matrixProjection.getRow1();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getRow2()
     */
    public Row getRow2() {
        return matrixProjection.getRow1();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#isHere(com.jason.clasificators.elements.data.Row)
     */
    public boolean isHere(final Row row) {
        return matrixProjection.isHere(row);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getName()
     */
    @Override
    public String getName() {
        return matrixProjection.getName();
    }

}
