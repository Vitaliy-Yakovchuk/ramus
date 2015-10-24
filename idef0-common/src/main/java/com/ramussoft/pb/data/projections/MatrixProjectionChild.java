/*
 * Created on 3/9/2005
 */
package com.ramussoft.pb.data.projections;

import java.util.Vector;

import com.ramussoft.pb.Main;
import com.ramussoft.pb.Row;

/**
 * Клас, яки представляє дитячі елементи у зв’язані з батьківськими у вигляді
 * матричної проекції.
 *
 * @author ZDD
 */
public class MatrixProjectionChild extends AbstractMatrixProjection {

    private final Row row;

    private final boolean rec;

    public MatrixProjectionChild(final Row row, final boolean rec) {
        super();
        this.row = row;
        this.rec = rec;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getLeft(com.jason.clasificators.elements.data.Row)
     */
    public Vector<Row> getLeft(final Row row) {
        if (rec)
            return Main.dataPlugin.getRecChilds(row, true);
        else
            return Main.dataPlugin.getChilds(row, true);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getRight(com.jason.clasificators.elements.data.Row)
     */
    public Vector<Row> getRight(final Row row) {
        if (rec)
            return Main.dataPlugin.getRecChilds(row, true);
        else
            return Main.dataPlugin.getChilds(row, true);
    }

    @Override
    public boolean isJoined(final Row left, final Row right) {
        return Main.dataPlugin.isParent(right, left);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getRow1()
     */
    public Row getRow1() {
        return row;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getRow2()
     */
    public Row getRow2() {
        return row;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#isHere(com.jason.clasificators.elements.data.Row)
     */
    public boolean isHere(final Row row) {
        return Main.dataPlugin.isParent(row, row);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.MatrixProjection#getName()
     */
    @Override
    public String getName() {
        return "childs";
    }

}
