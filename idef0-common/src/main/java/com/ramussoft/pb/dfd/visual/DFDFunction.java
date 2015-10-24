package com.ramussoft.pb.dfd.visual;

import java.awt.BasicStroke;

import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import com.dsoft.pb.types.FRectangle;
import com.dsoft.pb.types.FloatPoint;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.idef.elements.Ordinate;
import com.ramussoft.pb.idef.elements.Point;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingFunction;
import com.ramussoft.pb.idef.visual.MovingPanel;

public class DFDFunction extends MovingFunction {

    public DFDFunction(MovingArea movingArea, Function function) {
        super(movingArea, function);
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

    @Override
    public void paint(Graphics2D g) {
        g.setColor(function.getBackground());
        final Rectangle2D rect = movingArea.getBounds(getBounds());
        RoundRectangle2D rectangle2d = new RoundRectangle2D.Double(rect.getX(),
                rect.getY(), rect.getWidth(), rect.getHeight(),
                movingArea.getIDoubleOrdinate(20),
                movingArea.getIDoubleOrdinate(20));
        g.fill(rectangle2d);
        g.setFont(function.getFont());
        paintText(g);
        paintBorder(g);

        final Stroke tmp = g.getStroke();
        g.draw(rectangle2d);
        if (!function.isHaveChilds()) {
            g.draw(new Line2D.Double(rect.getX()
                    + Math.round(movingArea.getIDoubleOrdinate(15)), rect
                    .getY(), rect.getX(), rect.getY()
                    + Math.round(movingArea.getIDoubleOrdinate(15))));
        }
        g.setStroke(new BasicStroke(2));

        final String string = Integer.toString(function.getId());
        g.setFont(function.getFont());
        double h = MovingArea.getWidth((int) function.getFont()
                .getStringBounds(string, g.getFontRenderContext()).getHeight());
        paintTringle(g);
        g.setStroke(tmp);

        movingArea.paintText(g, string, new FRectangle(getBounds().getX(),
                        getBounds().getBottom() - h, getBounds().getWidth() - 3, h),
                com.ramussoft.pb.print.old.Line.RIGHT_ALIGN, 1, true);
    }

    @Override
    public void paintTringle(Graphics2D g) {
        if (paintTriangle >= 0) {
            final Rectangle2D rect = movingArea.getBounds(getBounds());
            RoundRectangle2D rectangle2d = new RoundRectangle2D.Double(rect.getX(),
                    rect.getY(), rect.getWidth(), rect.getHeight(),
                    movingArea.getIDoubleOrdinate(20),
                    movingArea.getIDoubleOrdinate(20));
            g.setClip(rectangle2d);
            g.fill(getTrianglePath(paintTriangle).createTransformedShape(
                    AffineTransform.getScaleInstance(movingArea.zoom,
                            movingArea.zoom)));
            g.setClip(null);
        }
    }

    @Override
    public FRectangle getTextBounds() {
        FRectangle bounds = getBounds();
        return new FRectangle(bounds.getX() + 2, bounds.getY(),
                bounds.getWidth() - 4, bounds.getHeight());
    }
}
