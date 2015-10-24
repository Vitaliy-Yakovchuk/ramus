package com.ramussoft.pb.print.xml;

import java.util.Vector;

import com.ramussoft.pb.Function;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.idef.visual.MovingPanel;

/**
 * Генератор секторів, що будуються на основі функціональних блоків.
 *
 * @author zdd
 */

public abstract class IDEF0FSTreeMatrixGenerator extends
        AbstractTreeMatrixGenerator {

    private Sector next = null;

    protected Function f;

    private Vector<Sector> sectors;

    private int pos;

    protected boolean left;

    protected boolean top;

    protected boolean right;

    protected boolean bottom;

    /**
     * Конструктор по замовчуванню.
     *
     * @param type Запис в якому записані умова відбору секторів приєднаних до
     *             функціональних блоків, розділених сиволом |. Наприклад для
     *             входів і управлінських факторів параметр має містити наступне
     *             значення "left|top". Можливі варіанти: top, left, bottom,
     *             right.
     */

    public IDEF0FSTreeMatrixGenerator(String type) {
        right = (type.indexOf("right") >= 0);
        left = (type.indexOf("left") >= 0);
        top = (type.indexOf("top") >= 0);
        bottom = (type.indexOf("bottom") >= 0);
        initNext();
    }

    @Override
    public void init(TreeMatrixNode node) {
        f = (Function) node.getRow();
        sectors = ((Function) f.getParentRow()).getSectors();
        pos = 0;
    }

    @Override
    public boolean hasMoreElements() {
        return next != null;
    }

    @Override
    public TreeMatrixNode nextElement() {
        TreeMatrixNodeImpl res = new TreeMatrixNodeImpl(null, next);
        initNext();
        return res;
    }

    private void initNext() {
        this.next = null;
        while (pos < sectors.size()) {
            if (isOk(sectors.get(pos))) {
                next = sectors.get(pos);
                pos++;
            }
            pos++;
        }
    }

    protected boolean isOk(Sector sector) {
        if (left) {
            if ((sector.getEnd().getType() == MovingPanel.LEFT)
                    && (f.equals(sector.getEnd().getFunction())))
                return true;
        }
        if (top) {
            if ((sector.getEnd().getType() == MovingPanel.TOP)
                    && (f.equals(sector.getEnd().getFunction())))
                return true;
        }
        if (bottom) {
            if ((sector.getEnd().getType() == MovingPanel.BOTTOM)
                    && (f.equals(sector.getEnd().getFunction())))
                return true;
        }
        if (right) {
            if ((sector.getStart().getType() == MovingPanel.RIGHT)
                    && (f.equals(sector.getStart().getFunction())))
                return true;
        }
        return false;
    }
}
