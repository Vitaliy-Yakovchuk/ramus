package com.ramussoft.pb.dfd.visual;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.pb.types.FRectangle;
import com.dsoft.pb.types.FloatPoint;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.idef.elements.Ordinate;
import com.ramussoft.pb.idef.elements.Point;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.IDEF0Object;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingPanel;

public class DFDObject extends IDEF0Object {

    public DFDObject(MovingArea movingArea, Function function) {
        super(movingArea, function);
    }

    public double getX(double y, boolean left, FRectangle rectangle) {
        if (left)
            return rectangle.getLeft();
        return rectangle.getRight();
    }

    public double getY(double x, boolean top, FRectangle rectangle) {
        if (top)
            return rectangle.getTop();
        return rectangle.getBottom();
    }

    public boolean contain(final FloatPoint point) {
        return point.getX() >= getBounds().getX()
                && point.getY() >= getBounds().getY()
                && point.getX() <= getBounds().getRight()
                && point.getY() <= getBounds().getBottom();
    }

    @Override
    public String getText() {
        String text = super.getText();
        if ((text == null) || (text.equals("")))
            return ResourceLoader.getString("no_dfd_object");
        return text;
    }

    @Override
    public FRectangle getTextBounds() {
        FRectangle bounds = getBounds();
        return new FRectangle(bounds.getX(), bounds.getY(), bounds.getWidth(),
                bounds.getHeight());
    }

    protected int getTriangle(final FloatPoint point) {
        int res = -1;

        FloatPoint l = getLocation();

        for (int type = MovingPanel.RIGHT; type <= MovingPanel.TOP; type++) {
            GeneralPath gp = getTrianglePath(type);
            double y = point.getY() + l.getY();
            double x = point.getX() + l.getX();
            if (gp.contains(new Point2D.Double(x, y))) {
                res = type;
                break;
            }
        }

        return res;
    }

    public void mouseClicked(final FloatPoint point) {
        if (movingArea.getChangingState() == MovingArea.ARROW_CHANGING_STATE) {
            final int type = getTriangle(point);

            final Ordinate x = new Ordinate(Ordinate.TYPE_X);
            final Ordinate y = new Ordinate(Ordinate.TYPE_Y);
            final Point p = new Point(x, y);
            final SectorRefactor.PerspectivePoint pp = new SectorRefactor.PerspectivePoint();
            pp.point = p;

            switch (type) {
                case LEFT: {
                    pp.setFunction(getFunction(), LEFT);
                    y.setPosition(getBounds().getY() + point.getY());
                    x.setPosition(getX(y.getPosition(), true, getBounds()));
                }
                break;
                case TOP: {
                    pp.setFunction(getFunction(), TOP);
                    x.setPosition(getBounds().getX() + point.getX());
                    y.setPosition(getY(x.getPosition(), true, getBounds()));
                }
                break;
                case BOTTOM: {
                    pp.setFunction(getFunction(), BOTTOM);
                    x.setPosition(getBounds().getX() + point.getX());
                    y.setPosition(getY(x.getPosition(), false, getBounds()));
                }
                break;
                case RIGHT: {
                    pp.setFunction(getFunction(), RIGHT);
                    y.setPosition(getBounds().getY() + point.getY());
                    x.setPosition(getX(y.getPosition(), false, getBounds()));
                }
                break;
                default:
                    return;
            }

            if (movingArea.getPointChangingType() == SectorRefactor.TYPE_START) {
                pp.type = SectorRefactor.TYPE_START;
                movingArea.getRefactor().setPoint(pp);
                movingArea.doSector();
            } else if (movingArea.getPointChangingType() == SectorRefactor.TYPE_END) {
                pp.type = SectorRefactor.TYPE_END;
                movingArea.getRefactor().setPoint(pp);
                movingArea.doSector();
            }
        }
    }
}
