package com.ramussoft.pb.data.projections;

import java.util.Vector;

import com.ramussoft.pb.MatrixProjection;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.types.GlobalId;

/**
 * Абстрактнй клас, який реалізує методи getLeftParent і getRightParent.
 *
 * @author ZDD
 */

public abstract class AbstractMatrixProjection implements MatrixProjection {

    private void getLeftParent(final Row row, final Vector<Row[]> add) {
        if (row.isElement()) {
            final Vector v = getLeft(row);
            Row[] v1;
            for (int i = 0; i < v.size(); i++) {
                v1 = new Row[2];
                v1[0] = row;
                v1[1] = (Row) v.get(i);
                add.add(v1);
            }
            getLeftParent((Row) row.getParent(), add);
        }
    }

    private void getRightParent(final Row row, final Vector<Row[]> add) {
        if (row.isElement()) {
            final Vector v = getRight(row);
            Row[] v1;
            for (int i = 0; i < v.size(); i++) {
                v1 = new Row[2];
                v1[0] = row;
                v1[1] = (Row) v.get(i);
                add.add(v1);
            }
            getRightParent((Row) row.getParent(), add);
        }
    }

    /**
     * Метод повертає вектор з даними в який зберігається інформація про всі
     * зв’язки батьківських елементів. Медод повертає вектор масивів з двох
     * елементів. Перший елемент - елемент класифікатора, з яким саме пов’язаний
     * елемен, другий елемент сам елемент класифікатора.
     */

    public Vector<Row[]> getLeftParent(final Row row) {
        if (row == null)
            return new Vector<Row[]>();
        final Vector<Row[]> res = new Vector<Row[]>();
        if (row != null)
            getLeftParent(row.getParentRow(), res);
        return res;
    }

    /**
     * Метод повертає вектор з даними в який зберігається інформація про всі
     * зв’язки батьківських елементів. Медод повертає вектор масивів з двох
     * елементів. Перший елемент - елемент класифікатора, з яким саме пов’язаний
     * елемен, другий елемент сам елемент класифікатора.
     */

    public Vector<Row[]> getRightParent(final Row row) {
        if (row == null)
            return new Vector<Row[]>();
        final Vector<Row[]> res = new Vector<Row[]>();
        if (row != null)
            getRightParent(row.getParentRow(), res);
        return res;
    }

    public boolean setJoin(final Row left, final Row right) {
        return false;
    }

    public String getName() {
        return getClass().getName();
    }

    public boolean isJoined(final Row left, final Row right) {
        final Vector j = getLeft(left);
        return j.indexOf(right) >= 0;
    }

    public void setName(final String text) {
    }

    public GlobalId getGlobalId() {
        return null;
    }

    public Row[] getLeftRows() {
        return new Row[0];
    }

    public boolean isStatic() {
        return false;
    }
}
