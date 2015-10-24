package com.ramussoft.pb.dfd.visual;

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

public class External extends DFDObject {

    public External(MovingArea movingArea, Function function) {
        super(movingArea, function);
    }

    @Override
    public void paint(Graphics2D g) {
        DataPlugin dp = ((NFunction) function).getDataPlugin();

        Row row = dp.findRowByGlobalId(function.getLink());
        final class PolygonD extends Polygon {
            /**
             *
             */
            private static final long serialVersionUID = 113232131232134523L;

            public void addPoint(final double x, final double y) {
                super.addPoint((int) x, (int) y);
            }
        }

        g.setColor(function.getBackground());
        final Rectangle2D rect = movingArea.getBounds(getBounds());
        g.fill(rect);
        g.setFont(function.getFont());
        paintText(g);

        if (row != null) {
            String string = row.getKod();

            movingArea.paintText(g, string,
                    new FRectangle(myBounds.getX() + 3, myBounds.getY() + 3,
                            myBounds.getWidth() - 3, myBounds.getHeight() - 3),
                    Line.LEFT_ALIGN, 0, true);
        }

        paintBorder(g);

        final Stroke tmp = g.getStroke();
        g.draw(rect);

        double zm = movingArea.getIDoubleOrdinate(2);

        g.draw(new Line2D.Double(rect.getX() + zm, rect.getY() + zm, rect
                .getX() + zm, rect.getY() + rect.getHeight()));
        g.draw(new Line2D.Double(rect.getX() + zm, rect.getY() + zm, rect
                .getX() + rect.getWidth(), rect.getY() + zm));

        if (paintTriangle >= 0) {
            final PolygonD p = new PolygonD();
            p.addPoint(rect.getX() + rect.getWidth() / 2,
                    rect.getY() + rect.getHeight() / 2);
            int standoff2 = -1;
            switch (paintTriangle) {
                case Point.TOP: {
                    p.addPoint(rect.getX() + standoff2, rect.getY() + standoff2);
                    p.addPoint(rect.getX() + rect.getWidth() - standoff2,
                            rect.getY() + standoff2);
                }
                break;
                case Point.LEFT: {
                    p.addPoint(rect.getX() + standoff2, rect.getY() + standoff2);
                    p.addPoint(rect.getX() + standoff2,
                            rect.getY() + rect.getHeight() - standoff2);
                }
                break;
                case Point.RIGHT: {
                    p.addPoint(rect.getX() + rect.getWidth() - standoff2,
                            rect.getY() + standoff2);
                    p.addPoint(rect.getX() + rect.getWidth() - standoff2,
                            rect.getY() + rect.getHeight() - standoff2);
                }
                break;
                case Point.BOTTOM: {
                    p.addPoint(rect.getX() + standoff2,
                            rect.getY() + rect.getHeight() - standoff2);
                    p.addPoint(rect.getX() + rect.getWidth() - standoff2,
                            rect.getY() + rect.getHeight() - standoff2);
                }
                break;
            }
            if (p.npoints > 1)
                g.fillPolygon(p);
        }
        g.setStroke(tmp);
    }

}
