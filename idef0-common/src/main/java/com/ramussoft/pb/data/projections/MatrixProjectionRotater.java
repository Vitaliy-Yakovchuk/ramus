/*
 * Created on 22/10/2004
 */
package com.ramussoft.pb.data.projections;

import java.util.Vector;

import com.ramussoft.pb.MatrixProjection;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.types.GlobalId;

/**
 * Клас призначений для зміни місцями правого і лівого рядка інших матричних
 * проекцій.
 *
 * @author ZDD
 */
public class MatrixProjectionRotater implements MatrixProjection {

    private final MatrixProjection matrix;

    public MatrixProjectionRotater(final MatrixProjection matrix) {
        super();
        this.matrix = matrix;
    }

    public Vector<Row[]> getLeftParent(final Row row) {
        return matrix.getRightParent(row);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getRightParent(com.jason.clasificators.elements.data.Row)
     */
    public Vector<Row[]> getRightParent(final Row row) {
        return matrix.getLeftParent(row);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getLeft(com.jason.clasificators.elements.data.Row)
     */
    public Vector<Row> getLeft(final Row row) {
        return matrix.getRight(row);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getRight(com.jason.clasificators.elements.data.Row)
     */
    public Vector<Row> getRight(final Row row) {
        return matrix.getLeft(row);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#setJoin(com.jason.clasificators.elements.data.Row,
     *      com.jason.clasificators.elements.data.Row)
     */
    public boolean setJoin(final Row left, final Row right) {
        return matrix.setJoin(right, left);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#isJoined(com.jason.clasificators.elements.data.Row,
     *      com.jason.clasificators.elements.data.Row)
     */
    public boolean isJoined(final Row left, final Row right) {
        return matrix.isJoined(right, left);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getRow1()
     */
    public Row getRow1() {
        return matrix.getRow2();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getRow2()
     */
    public Row getRow2() {
        return matrix.getRow1();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#isHere(com.jason.clasificators.elements.data.Row)
     */
    public boolean isHere(final Row row) {
        return matrix.isHere(row);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getName()
     */
    public String getName() {
        return matrix.getName();
    }

    /**
     * @return Returns the matrix.
     */
    public MatrixProjection getMatrix() {
        return matrix;
    }

    public void setName(final String text) {
    }

    public GlobalId getGlobalId() {
        return null;
    }

    public Row[] getLeftRows() {
        return matrix.getLeftRows();
    }

    public boolean isStatic() {
        return matrix.isStatic();
    }
}
