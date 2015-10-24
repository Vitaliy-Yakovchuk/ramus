/*
 * Created on 4/9/2005
 */
package com.ramussoft.pb.data.projections;

import java.util.Vector;

import com.ramussoft.pb.Row;

/**
 * Клас представляє у вигляді матричної проекції зв’язок між дочірнім і
 * батьківським елементом.
 *
 * @author ZDD
 */
public class MatrixProjectionParent extends AbstractMatrixProjection {

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getLeft(com.jason.clasificators.elements.data.Row)
     */
    public Vector<Row> getLeft(final Row row) {
        final Vector<Row> res = new Vector<Row>();
        res.add(row.getParentRow());
        return res;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getRight(com.jason.clasificators.elements.data.Row)
     */
    public Vector<Row> getRight(final Row row) {
        final Vector<Row> res = new Vector<Row>();
        res.add(row.getParentRow());
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
        return false;
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
        return "Parent matrix projection";
    }

}
