package com.ramussoft.pb.dfd.visual;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import com.dsoft.pb.types.FRectangle;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.data.negine.NFunction;
import com.ramussoft.pb.idef.elements.Point;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.print.old.Line;

public class DataStore extends DFDObject {

    private static final int EMPTY_LEFT = 20;
    private double left;

    public DataStore(MovingArea movingArea, Function function) {
        super(movingArea, function);
    }

    @Override
    public void paint(Graphics2D g) {
        final Stroke tmp = g.getStroke();
        try {

            DataPlugin dp = ((NFunction) function).getDataPlugin();

            Row row = dp.findRowByGlobalId(function.getLink());
            final class PolygonD extends Polygon {
                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                public void addPoint(final double x, final double y) {
                    super.addPoint((int) x, (int) y);
                }
            }
            ;

            g.setColor(function.getBackground());
            final Rectangle2D rect = movingArea.getBounds(getBounds());
            Rectangle2D r2d = null;
            if (row != null) {
                String string = row.getKod();
                r2d = function.getFont().getStringBounds(string,
                        g.getFontRenderContext());
                left = movingArea.getIDoubleOrdinate(r2d.getWidth()) + 1;

            } else {
                left = EMPTY_LEFT;
            }
            g.fill(rect);
            g.setFont(function.getFont());
            paintText(g);

            if (row != null) {
                String string = row.getKod();
                movingArea.paintText(g, string, new FRectangle(
                        myBounds.getX() + 0.5, myBounds.getY(), left - 0.5,
                        myBounds.getHeight()), Line.LEFT_ALIGN, 1, true);
            }
            g.draw(new Line2D.Double(rect.getX() + left, rect.getMaxY(), rect
                    .getX()
                    + left, rect.getY()));
            paintBorder(g);

            g.draw(new Line2D.Double(rect.getX(), rect.getY(), rect.getMaxX(),
                    rect.getY()));
            g.draw(new Line2D.Double(rect.getX(), rect.getMaxY(), rect.getX(),
                    rect.getY()));
            g.draw(new Line2D.Double(rect.getMaxX(), rect.getMaxY(), rect
                    .getX(), rect.getMaxY()));
            g.setStroke(new BasicStroke(2));
            /*
             * g.draw(new Line2D.Double(rect.getX() + rect.getWidth() - 1,
			 * rect.getY() + 1, rect.getX() + rect.getWidth() - 1, rect.getY() +
			 * rect.getHeight() - 1)); g.draw(new Line2D.Double(rect.getX() + 1,
			 * rect.getY() + rect.getHeight() - 1, rect.getX() + rect.getWidth()
			 * - 1, rect .getY() + rect.getHeight() - 1));
			 */
            final String string = getIDEF0Kod();
            g.setFont(function.getFont());
            double h = MovingArea.getWidth(0)
                    + MovingArea.getWidth((int) function.getFont()
                    .getStringBounds(string, g.getFontRenderContext())
                    .getHeight());
            h = h * 0.7;

            if (paintTriangle >= 0) {
                final PolygonD p = new PolygonD();
                p.addPoint(rect.getX() + rect.getWidth() / 2, rect.getY()
                        + rect.getHeight() / 2);
                int standoff2 = -1;
                switch (paintTriangle) {
                    case Point.TOP: {
                        p
                                .addPoint(rect.getX() + standoff2, rect.getY()
                                        + standoff2);
                        p.addPoint(rect.getX() + rect.getWidth() - standoff2, rect
                                .getY()
                                + standoff2);
                    }
                    break;
                    case Point.LEFT: {
                        p
                                .addPoint(rect.getX() + standoff2, rect.getY()
                                        + standoff2);
                        p.addPoint(rect.getX() + standoff2, rect.getY()
                                + rect.getHeight() - standoff2);
                    }
                    break;
                    case Point.RIGHT: {
                        p.addPoint(rect.getX() + rect.getWidth() - standoff2, rect
                                .getY()
                                + standoff2);
                        p.addPoint(rect.getX() + rect.getWidth() - standoff2, rect
                                .getY()
                                + rect.getHeight() - standoff2);
                    }
                    break;
                    case Point.BOTTOM: {
                        p.addPoint(rect.getX() + standoff2, rect.getY()
                                + rect.getHeight() - standoff2);
                        p.addPoint(rect.getX() + rect.getWidth() - standoff2, rect
                                .getY()
                                + rect.getHeight() - standoff2);
                    }
                    break;
                }
                if (p.npoints > 1)
                    g.fillPolygon(p);
            }
        } catch (NullPointerException e) {
        }
        g.setStroke(tmp);

    }

    @Override
    public FRectangle getTextBounds() {
        FRectangle bounds = getBounds();
        return new FRectangle(bounds.getX() + left, bounds.getY(), bounds
                .getWidth()
                - left, bounds.getHeight());
    }
}
