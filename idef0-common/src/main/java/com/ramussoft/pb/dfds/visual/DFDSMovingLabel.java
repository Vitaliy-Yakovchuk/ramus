package com.ramussoft.pb.dfds.visual;

import java.awt.Color;
import java.awt.Graphics2D;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import com.dsoft.pb.types.FRectangle;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingLabel;

public class DFDSMovingLabel extends MovingLabel {

    public DFDSMovingLabel(MovingArea movingArea) {
        super(movingArea);
        transparent = false;
    }

    @Override
    public void paint(Graphics2D g) {
        final Rectangle2D rec = movingArea.getBounds(getBounds());

        FRectangle textBounds = new FRectangle(getTextBounds());
        textBounds.setX(textBounds.getX() + 0.5d);

        double mx = movingArea.getIDoubleOrdinate(4);
        double my = movingArea.getIDoubleOrdinate(2);
        double sx = movingArea.getIDoubleOrdinate(2);
        double sy = movingArea.getIDoubleOrdinate(1);

        double maxX = rec.getMaxX() + movingArea.getIDoubleOrdinate(0.5);
        double xm = maxX - mx;
        if (rec.getX() > xm - sx)
            xm = maxX;
        double ym = rec.getMaxY() - my;
        double maxY = rec.getMaxY();

        if (rec.getY() > ym - sy)
            ym = maxY;

        int c = 1;

        if (!transparent && sector.getStream() != null
                && sector.getStream().getAdded() != null)
            c = sector.getStream().getAdded().length;

        if (!transparent || isNegative()) {
            g.setColor(fiterColor(Color.WHITE));

            GeneralPath gp = new GeneralPath(GeneralPath.WIND_NON_ZERO);
            gp.moveTo(maxX, ym);
            gp.lineTo(maxX, rec.getY());
            gp.lineTo(rec.getX(), rec.getY());
            gp.lineTo(rec.getX(), maxY);
            gp.lineTo(xm, maxY);

            gp.lineTo(maxX, ym);

            g.fill(gp);
        }

        for (int i = c - 1; i > 0; i--) {
            AffineTransform translateInstance = AffineTransform
                    .getTranslateInstance(
                            movingArea.getIDoubleOrdinate(i) * 2d,
                            movingArea.getIDoubleOrdinate(i) * 2d);

            if (!transparent || isNegative()) {
                g.setColor(fiterColor(Color.WHITE));

                GeneralPath gp = new GeneralPath(GeneralPath.WIND_NON_ZERO);
                gp.moveTo(maxX, ym);
                gp.lineTo(maxX, rec.getY());
                gp.lineTo(rec.getX(), rec.getY());
                gp.lineTo(rec.getX(), maxY);
                gp.lineTo(xm, maxY);

                gp.lineTo(maxX, ym);

                g.fill(gp.createTransformedShape(translateInstance));
            }
            g.setColor(fiterColor(getColor()));

            GeneralPath gp = new GeneralPath(GeneralPath.WIND_NON_ZERO);

            gp.moveTo(xm, maxY);
            gp.lineTo(xm, ym);

            gp.lineTo(maxX, ym);
            gp.lineTo(maxX, rec.getY());
            gp.lineTo(rec.getX(), rec.getY());
            gp.lineTo(rec.getX(), maxY);
            gp.lineTo(xm, maxY);

            gp.lineTo(maxX, ym);

            g.draw(gp.createTransformedShape(translateInstance));
        }

        if (!transparent || isNegative()) {
            g.setColor(fiterColor(Color.WHITE));

            GeneralPath gp = new GeneralPath(GeneralPath.WIND_NON_ZERO);
            gp.moveTo(maxX, ym);
            gp.lineTo(maxX, rec.getY());
            gp.lineTo(rec.getX(), rec.getY());
            gp.lineTo(rec.getX(), maxY);
            gp.lineTo(xm, maxY);

            gp.lineTo(maxX, ym);

            g.fill(gp);
        }

        GeneralPath gp = new GeneralPath(GeneralPath.WIND_NON_ZERO);

        gp.moveTo(xm, maxY);
        gp.lineTo(xm, ym);

        gp.lineTo(maxX, ym);
        gp.lineTo(maxX, rec.getY());
        gp.lineTo(rec.getX(), rec.getY());
        gp.lineTo(rec.getX(), maxY);
        gp.lineTo(xm, maxY);

        gp.lineTo(maxX, ym);
        g.setColor(fiterColor(getColor()));
        g.draw(gp);

        g.setFont(getFont());

        movingArea
                .paintText(g, getText(), textBounds, getAlign(), 0, true);
        paintBorder(g);
    }

    @Override
    public void setText(String text) {
    }

    @Override
    public void onProcessEndBoundsChange() {
        myBounds.setTransformNetBounds(MovingArea.NET_LENGTH);
        super.onProcessEndBoundsChange();
    }

    @Override
    public String getText() {
        if (labelText == null) {
            String s = sector.getAlternativeText();
            if ("".equals(s)) {
                final Stream stream = sector.getStream();
                if (stream != null) {
                    if (stream.isEmptyName()) {
                        Row[] rows = stream.getAdded();
                        StringBuilder sb = null;
                        for (Row row : rows)
                            if (row != null) {
                                if (sb == null)
                                    sb = new StringBuilder(row.getName());
                                else {
                                    sb.append(";\n");
                                    sb.append(row.getName());
                                }
                                String status = row.getAttachedStatus();
                                if (status != null) {
                                    int i = status.indexOf('|');
                                    if (i >= 0) {
                                        status = status.substring(i + 1);
                                        if (status.trim().length() == 0)
                                            status = null;
                                    }
                                }
                                if (status != null) {
                                    sb.append(" (");
                                    sb.append(status);
                                    sb.append(')');
                                }
                            }
                        if (sb != null)
                            s = sb.toString();
                    } else
                        s = stream.getName();
                }
            }
            labelText = s;
        }
        return labelText;
    }

    /*
     * Прибраємо автозменшення рамки
     */
    @Override
    public void resetBoundsX() {
        resetBounds();
    }

    @Override
    public void resetBounds() {
        final FRectangle old = getBounds();
        final Rectangle2D r = getResetsBounds(old);
        double w = r.getWidth();
        double h = r.getHeight();
        if (w < old.getWidth())
            w = old.getWidth();
        if (h < old.getHeight())
            h = old.getHeight();
        setBounds(old.getX()/*-((r.getWidth()-old.getWidth())/2)*/,
                old.getY()/*
                         * -(r.getHeight()-old.getHeight())/2
						 */, w, h);
        // myBounds.setTransformNetBoundsMax(MovingArea.NET_LENGTH);
    }

    @Override
    public double getRealHeight() {
        if (sector.getStream() != null
                && sector.getStream().getAdded().length > 1)
            return super.getRealHeight()
                    + (sector.getStream().getAdded().length - 1) * 2;
        return super.getRealHeight();
    }

    @Override
    public double getRealWidth() {
        if (sector.getStream() != null
                && sector.getStream().getAdded().length > 1)
            return super.getRealWidth()
                    + (sector.getStream().getAdded().length - 1) * 2;
        return super.getRealWidth();
    }
}
