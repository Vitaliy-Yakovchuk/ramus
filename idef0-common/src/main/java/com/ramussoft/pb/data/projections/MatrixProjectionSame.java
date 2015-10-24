/*
 * Created on 4/9/2005
 */
package com.ramussoft.pb.data.projections;

import java.util.Vector;

import com.ramussoft.pb.Row;

/**
 * Матрична проекція, яка представляє елементи зв’язані між самим собою.
 *
 * @author ZDD
 */
public class MatrixProjectionSame extends AbstractMatrixProjection {

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getLeft(com.jason.clasificators.elements.data.Row)
     */
    public Vector<Row> getLeft(final Row row) {
        final Vector<Row> res = new Vector<Row>();
        res.add(row);
        return res;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getRight(com.jason.clasificators.elements.data.Row)
     */
    public Vector<Row> getRight(final Row row) {
        final Vector<Row> res = new Vector<Row>();
        res.add(row);
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
        return left.equals(right);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getRow1()
     */
    public Row getRow1() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getRow2()
     */
    public Row getRow2() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#isHere(com.jason.clasificators.elements.data.Row)
     */
    public boolean isHere(final Row row) {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getName()
     */
    @Override
    public String getName() {
        return "Same matrix projection";
    }

}
