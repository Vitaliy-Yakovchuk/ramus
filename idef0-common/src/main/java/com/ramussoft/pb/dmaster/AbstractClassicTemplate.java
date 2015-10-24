package com.ramussoft.pb.dmaster;

import static com.ramussoft.pb.idef.visual.MovingPanel.LEFT;
import static com.ramussoft.pb.idef.visual.MovingPanel.RIGHT;
import static com.ramussoft.pb.idef.visual.MovingPanel.TOP;

import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.idef.elements.Ordinate;
import com.ramussoft.pb.idef.elements.Point;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;

public class AbstractClassicTemplate extends SimpleTemplate {

    public void createChilds(final Function function,
                             final DataPlugin dataPlugin, final MovingArea movingArea) {
        super.createChilds(function, dataPlugin);
        createInsOuts(function, movingArea);
    }

    protected void createSimpleArrow(final Function function,
                                     final MovingArea movingArea, final int num, final int type) {
        final Function f = (Function) function.getChildAt(num);
        createStartxBorderPoint(movingArea, getX(f), 10, type);
        createInPoint(movingArea, type, f);
    }

    protected void createInPoint(final MovingArea movingArea, final int type,
                                 final Function f) {
        createInPoint(movingArea, f, type, getX(f), type == TOP ? f.getBounds()
                .getTop() : f.getBounds().getBottom());
    }

    private void createInsOuts(final Function function,
                               final MovingArea movingArea) {
        Function f = (Function) function.getChildAt(0);
        createStartxBorderPoint(movingArea, 10, getY(f), LEFT);
        createInPoint(movingArea, f, LEFT, f.getBounds().getLeft(), getY(f));

        for (int i = 1; i < function.getChildCount(); i++) {
            final Function sF = (Function) function.getChildAt(i);
            createOutPoint(movingArea, f);
            createInPoint(movingArea, sF, LEFT, sF.getBounds().getLeft(),
                    getY(sF));
            f = sF;
        }
        createOutPoint(movingArea, f);
        createRightBorderPoint(movingArea, getY(f));
    }

    protected double getY(final Function f) {
        return f.getBounds().getTop() + f.getBounds().getHeight() / 2;
    }

    protected double getX(final Function f) {
        return f.getBounds().getLeft() + f.getBounds().getWidth() / 2;
    }

    private void createStartxBorderPoint(final MovingArea movingArea,
                                         final double x, final double y, final int type) {
        final SectorRefactor refactor = movingArea.getRefactor();
        final SectorRefactor.PerspectivePoint pp = new SectorRefactor.PerspectivePoint();
        pp.x = x;
        pp.y = y;
        pp.borderType = type;
        pp.type = SectorRefactor.TYPE_START;

        refactor.setPoint(pp);
        movingArea.doSector();
    }

    private void createRightBorderPoint(final MovingArea movingArea,
                                        final double y) {
        final SectorRefactor refactor = movingArea.getRefactor();
        final SectorRefactor.PerspectivePoint pp = new SectorRefactor.PerspectivePoint();
        pp.x = 10;
        pp.y = y;
        pp.borderType = RIGHT;
        pp.type = SectorRefactor.TYPE_END;

        refactor.setPoint(pp);
        movingArea.doSector();
    }

    private void createInPoint(final MovingArea movingArea, final Function f,
                               final int type, final double xp, final double yp) {
        SectorRefactor.PerspectivePoint pp;
        final Ordinate x = new Ordinate(Ordinate.TYPE_X);
        final Ordinate y = new Ordinate(Ordinate.TYPE_Y);
        final Point p = new Point(x, y);
        pp = new SectorRefactor.PerspectivePoint();
        pp.point = p;
        pp.setFunction(f, type);
        pp.type = SectorRefactor.TYPE_END;
        x.setPosition(xp);
        y.setPosition(yp);
        movingArea.getRefactor().setPoint(pp);
        movingArea.doSector();
    }

    private void createOutPoint(final MovingArea movingArea, final Function f) {
        SectorRefactor.PerspectivePoint pp;
        final Ordinate x = new Ordinate(Ordinate.TYPE_X);
        final Ordinate y = new Ordinate(Ordinate.TYPE_Y);
        final Point p = new Point(x, y);
        pp = new SectorRefactor.PerspectivePoint();
        pp.point = p;
        pp.setFunction(f, RIGHT);
        pp.type = SectorRefactor.TYPE_START;
        x.setPosition(f.getBounds().getRight());
        y.setPosition(getY(f));
        movingArea.getRefactor().setPoint(pp);
        movingArea.doSector();
    }
}
