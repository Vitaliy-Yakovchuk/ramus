package com.ramussoft.pb.print.xml;

import com.ramussoft.pb.Function;

/**
 * Генератор функціональних блоків, що будуються на основі секторів.
 *
 * @author zdd
 */

public class IDEF0SFTreeMatrixGenerator extends AbstractTreeMatrixGenerator {

    private Function next;

    private boolean tunelSuport;

    protected boolean left;

    protected boolean top;

    protected boolean right;

    protected boolean bottom;

    //private Sector sector;

    /**
     * Конструктор по замовчуванню.
     *
     * @param type        Запис в якому записані умова відбору секторів приєднаних до
     *                    функціональних блоків, розділених сиволом |. Наприклад для
     *                    входів і управлінських факторів параметр має містити наступне
     *                    значення "left|top". Можливі варіанти: top, left, bottom,
     *                    right.
     * @param tunelSuport <code>true</code> будуть повернені всі дочірні елементи до
     *                    функціонального блоку, який був затунельований, інакше тунелі
     *                    взагалі не враховуються.
     */

    public IDEF0SFTreeMatrixGenerator(String type, boolean tunelSuport) {
        this.tunelSuport = tunelSuport;
        right = (type.indexOf("right") >= 0);
        left = (type.indexOf("left") >= 0);
        top = (type.indexOf("top") >= 0);
        bottom = (type.indexOf("bottom") >= 0);
        initNext();
    }

    public boolean isTunelSuport() {
        return tunelSuport;
    }

    private void initNext() {
    }

    @Override
    public void init(TreeMatrixNode node) {
    }

    @Override
    public boolean hasMoreElements() {
        return next != null;
    }

    @Override
    public TreeMatrixNode nextElement() {
        TreeMatrixNodeImpl res = new TreeMatrixNodeImpl(next, null);
        initNext();
        return res;
    }

}
