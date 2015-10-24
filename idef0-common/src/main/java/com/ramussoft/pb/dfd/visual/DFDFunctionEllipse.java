package com.ramussoft.pb.dfd.visual;

import java.awt.BasicStroke;

import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.dsoft.pb.types.FRectangle;
import com.dsoft.pb.types.FloatPoint;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.idef.elements.Ordinate;
import com.ramussoft.pb.idef.elements.Point;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingFunction;
import com.ramussoft.pb.idef.visual.MovingPanel;

public class DFDFunctionEllipse extends MovingFunction {

    public DFDFunctionEllipse(MovingArea movingArea, Function function) {
        super(movingArea, function);
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

    @Override
    public void paint(Graphics2D g) {
        g.setColor(function.getBackground());
        final Rectangle2D rect = movingArea.getBounds(getBounds());
        Ellipse2D ellipse = new Ellipse2D.Double(rect.getX(), rect.getY(),
                rect.getWidth(), rect.getHeight());
        g.fill(ellipse);
        g.setFont(function.getFont());
        paintText(g);
        paintBorder(g);

        final Stroke tmp = g.getStroke();
        g.draw(ellipse);
        if (!function.isHaveChilds()) {
            FRectangle fr = new FRectangle(rect);
            double y1 = rect.getY() + rect.getHeight() / 2.5;
            double x1 = getX(rect.getY() + rect.getHeight() / 2.5, true, fr);

            double x2 = rect.getX() + rect.getWidth() / 2.5;
            double y2 = getY(x2, true, fr);

            g.draw(new Line2D.Double(x1, y1, x2, y2));
        }
        g.setStroke(new BasicStroke(2));

        final String string = Integer.toString(function.getId());
        g.setFont(function.getFont());
        double h = MovingArea.getWidth(0)
                + MovingArea.getWidth((int) function.getFont()
                .getStringBounds(string, g.getFontRenderContext())
                .getHeight());
        h = h * 0.7;

        paintTringle(g);
        g.setStroke(tmp);

        movingArea.paintText(g, string, new FRectangle(getBounds().getX(),
                        getBounds().getBottom() - h, getBounds().getWidth(), h),
                com.ramussoft.pb.print.old.Line.RIGHT_ALIGN, 1, true);
    }

    @Override
    public void paintTringle(Graphics2D g) {
        if (paintTriangle >= 0) {

            final Rectangle2D rect = movingArea.getBounds(getBounds());
            Ellipse2D ellipse = new Ellipse2D.Double(rect.getX(), rect.getY(),
                    rect.getWidth(), rect.getHeight());
            g.setClip(ellipse);

            g.fill(getTrianglePath(paintTriangle).createTransformedShape(
                    AffineTransform.getScaleInstance(movingArea.zoom,
                            movingArea.zoom)));
            g.setClip(null);
        }
    }

    @Override
    public double getX(double y, boolean left, FRectangle rectangle) {
        double y0 = rectangle.getY();
        double x0 = rectangle.getX();
        double w = rectangle.getWidth();
        double h = rectangle.getHeight();
        double ny = (y - y0) / h - 0.5;
        double q = Math.sqrt(0.25 - ny * ny);
        if (left)
            q = -q;

        return w * (q + x0 / w + 0.5);
    }

    @Override
    public double getY(double x, boolean top, FRectangle rectangle) {
        double y0 = rectangle.getY();
        double x0 = rectangle.getX();
        double w = rectangle.getWidth();
        double h = rectangle.getHeight();
        double nx = (x - x0) / w - 0.5;
        double q = Math.sqrt(0.25 - nx * nx);
        if (top)
            q = -q;

        return h * (q + y0 / h + 0.5);
    }

	/*
     * @Override public boolean contain(FloatPoint point) { double ellw =
	 * getBounds().getWidth(); if (ellw <= 0.0) { return false; } double normx =
	 * (point.getX() - getBounds().getX()) / ellw - 0.5; double ellh =
	 * getBounds().getHeight(); if (ellh <= 0.0) { return false; } double normy
	 * = (point.getY() - getBounds().getY()) / ellh - 0.5; return (normx * normx
	 * + normy * normy) < 0.25; }
	 */

    @Override
    public FRectangle getTextBounds() {
        FRectangle bounds = getBounds();
        return new FRectangle(bounds.getX(), bounds.getY() + 1,
                bounds.getWidth(), bounds.getHeight());
    }
}
