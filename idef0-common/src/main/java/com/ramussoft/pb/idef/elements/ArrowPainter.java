package com.ramussoft.pb.idef.elements;

import java.awt.BasicStroke;


import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Vector;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.pb.idef.elements.ProjectOptions;
import com.dsoft.pb.idef.elements.Readed;
import com.dsoft.pb.idef.elements.Status;
import com.dsoft.pb.types.FRectangle;
import com.dsoft.pb.types.FloatPoint;
import com.dsoft.utils.Options;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.data.SectorBorder;
import com.ramussoft.pb.data.negine.NFunction;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingFunction;
import com.ramussoft.pb.idef.visual.MovingPanel;
import com.ramussoft.pb.idef.visual.MovingText;
import com.ramussoft.pb.print.old.Line;

/**
 * Клас призначений для малювання елементів стрілок. Містить всі необхадні
 * константи.
 *
 * @author ZDD
 */

public class ArrowPainter {

    private static final String NUMBER = ResourceLoader
            .getString("IDEF0.Number");

    private static final String TOP_IDEF_NOTES = ResourceLoader
            .getString("top_idef_notes:");

    private static final String TITLE_B = ResourceLoader.getString("title:");

    private static final String NODE_B = ResourceLoader.getString("node:");

    private static final String USED_AT_T = ResourceLoader
            .getString("USED_AT:");

    private static final String PROJECT_T = ResourceLoader
            .getString("PROJECT:");

    private static final String READER_B = ResourceLoader.getString("reader");

    private static final String DATE_B = ResourceLoader.getString("date");

    private static final String TOP_T = ResourceLoader.getString("TOP");

    private static final String CONTEXT_T = ResourceLoader
            .getString("CONTEXT:");

    private static final String REV_T = ResourceLoader.getString("REV:");

    private static final String DATE_T = ResourceLoader.getString("DATE:");

    public static final BasicStroke THIN_STROKE = new BasicStroke(0.5f);

    /**
     * Ширина половини стрілки.
     */

    public static final double ARROW_WIDTH = MovingArea.getWidth(Options
            .getInteger("ARROW_WIDTH", 3));

    /**
     * Довжина половини стрілки.
     */

    public static final double ARROW_HEIGHT = MovingArea.getWidth(Options
            .getInteger("ARROW_HEIGHT", 8));

    public static final double TILDA_WIDTH = MovingArea.getWidth(Options
            .getInteger("TILDA_WIDTH", 3));

    public static final double TUNNEL_LENGTH = MovingArea.getWidth(Options
            .getInteger("TUNNEL_LENGTH", 11));

    public static final double TUNNEL_WIDTH = MovingArea.getWidth(Options
            .getInteger("TUNNEL_WIDTH", 2));

    public static final double ARROW_ROTATE_WIDTH = MovingArea.getWidth(Options
            .getInteger("ARROW_ROTATE_WIDTH", 5));

    private static final String AUTOR_T = ResourceLoader.getString("AUTHOR:")
            + " ";

    private final MovingArea movingArea;

    private double tunnelWidth;

    private double tunnelLength;

    public ArrowPainter(final MovingArea movingArea) {
        super();
        this.movingArea = movingArea;
    }

    /**
     * Малює кінець стрілки.
     *
     * @param g    Область для виводу стрілки.
     * @param x    Координата x кінця стрілки.
     * @param y    Координата y кінця стрілки.
     * @param type Напрямок направленості стрілки, MovingPanel.RIGHT,...
     */

    public void paintArrowEnd(final Graphics2D g, double x1, double y1,
                              final double xD, final double yD, int type) {
        final float x = (float) xD;
        final float y = (float) yD;
        final GeneralPath t = new GeneralPath(Path2D.WIND_EVEN_ODD, 3);
        t.moveTo(x, y);
        final float arrowWidth = (float) movingArea
                .getIDoubleOrdinate(ARROW_WIDTH);
        final float arrowHeight = (float) movingArea
                .getIDoubleOrdinate(ARROW_HEIGHT);
        type = MovingPanel.getOpposite(type);
        switch (type) {
            case MovingPanel.BOTTOM: {
                t.lineTo(x - arrowWidth, y - arrowHeight);
                t.lineTo(x + arrowWidth, y - arrowHeight);
            }
            break;
            case MovingPanel.RIGHT: {
                t.lineTo(x - arrowHeight, y - arrowWidth);
                t.lineTo(x - arrowHeight, y + arrowWidth);
            }
            break;
            case MovingPanel.TOP: {
                t.lineTo(x - arrowWidth, y + arrowHeight);
                t.lineTo(x + arrowWidth, y + arrowHeight);
            }
            break;
            case MovingPanel.LEFT: {
                t.lineTo(x + arrowHeight, y - arrowWidth);
                t.lineTo(x + arrowHeight, y + arrowWidth);
            }
            break;
        }
        t.closePath();
        g.fill(t);
    }

    public void paintTilda(final Graphics2D g, final PaintSector sector) {
        g.setStroke(ArrowPainter.THIN_STROKE);
        final MovingText movingText = sector.getText();
        final FloatPoint t = sector.getTildaPoint();
        final double x1 = movingArea.getIDoubleOrdinate(t.getX());
        final double y1 = movingArea.getIDoubleOrdinate(t.getY());
        final double x2 = movingArea.getIDoubleOrdinate(movingText.getBounds()
                .getLocation().getX());
        final double y2 = movingArea.getIDoubleOrdinate(movingText.getBounds()
                .getLocation().getY());
        final double x3 = x2
                + movingArea.getIDoubleOrdinate(movingText.getRealWidth());
        final double y3 = y2
                + movingArea.getIDoubleOrdinate(movingText.getRealHeight());
        double lw = 1;
        if (sector.getStroke() instanceof BasicStroke)
            lw = ((BasicStroke) sector.getStroke()).getLineWidth();
        double w = movingArea.getIDoubleOrdinate(lw * 1.5);
        double minW = movingArea.getIDoubleOrdinate(2);

        if (w < minW)
            w = minW;
        paintTilda(g, x1, y1, x2, y2, x3, y3, w, movingArea);
    }

    public static void paintTilda(final Graphics2D g, final double x1,
                                  final double y1, final double x2, final double y2, final double x3,
                                  final double y3, double w, MovingArea movingArea) {
        double x = x1;
        double y = y1;
        if (x3 < x)
            x = x3;
        else if (x2 > x)
            x = x2;
        if (y3 < y)
            y = y3;
        else if (y2 > y)
            y = y2;

        g.fill(new Arc2D.Double(x1 - w / 2, y1 - w / 2, w, w, 0, 360, Arc2D.PIE));
        paintTilda(g, x1, y1, x, y, movingArea);
    }

    private static void paintTilda(final Graphics2D g, final double x1,
                                   final double y1, final double x2, final double y2,
                                   MovingArea movingArea) {
        final int tWidth = movingArea.getIntOrdinate(TILDA_WIDTH);
        final double bx = (x1 + x2) / 2;
        final double by = (y1 + y2) / 2;
        double dy = y2 - y1;
        double dx = x2 - x1;
        final double len = Math.sqrt(dx * dx + dy * dy);
        if (len <= 0.2)
            return;
        dx = dx / len * tWidth;
        dy = dy / len * tWidth;

        final double nx1 = bx + dy;
        final double ny1 = by - dx;
        final double nx2 = bx - dy;
        final double ny2 = by + dx;

        g.draw(new Line2D.Double(x1, y1, nx1, ny1));
        g.draw(new Line2D.Double(nx1, ny1, nx2, ny2));
        g.draw(new Line2D.Double(nx2, ny2, x2, y2));
    }

    /**
     * Метод малює дужки тунулювання для стрілки.
     *
     * @param g     область виводу.
     * @param point Точка де закінчіється (починається сектор).
     * @param type  Тип входу/виходу сетора.
     * @param soft  true, якщо дужки маєть бути заокругленими, false, якщо дужки
     *              прямі.
     */

    public void paintTunnel(final Graphics2D g, final FloatPoint point,
                            final int type, boolean soft) {
        g.setStroke(ArrowPainter.THIN_STROKE);
        g.setStroke(new BasicStroke(0.5f));
        double x = movingArea.getIDoubleOrdinate(point.getX());
        double y = movingArea.getIDoubleOrdinate(point.getY());
        soft = !soft;
        tunnelWidth = movingArea.getIDoubleOrdinate(TUNNEL_WIDTH);
        tunnelLength = movingArea.getIDoubleOrdinate(TUNNEL_LENGTH);
        switch (type) {
            case MovingPanel.BOTTOM: {
                g.translate(x, y);
                g.translate(-tunnelLength / 2, 0);
                paintD1(g, soft);
                g.translate(tunnelLength, 0);
                paintD2(g, soft);
                g.translate(-tunnelLength / 2, 0);
                g.translate(-x, -y);
            }
            break;
            case MovingPanel.TOP: {
                g.translate(x, y - tunnelLength);
                g.translate(-tunnelLength / 2, 0);
                paintD1(g, soft);
                g.translate(tunnelLength, 0);
                paintD2(g, soft);
                g.translate(-tunnelLength / 2, 0);
                g.translate(-x, -y + tunnelLength);
            }
            break;
            case MovingPanel.RIGHT: {
                g.translate(x, y);
                g.translate(0, -tunnelLength / 2);
                paintA1(g, soft);
                g.translate(0, tunnelLength);
                paintA2(g, soft);
                g.translate(0, -tunnelLength / 2);
                g.translate(-x, -y);
            }
            break;
            case MovingPanel.LEFT: {
                g.translate(x - tunnelLength, y);
                g.translate(0, -tunnelLength / 2);
                paintA1(g, soft);
                g.translate(0, tunnelLength);
                paintA2(g, soft);
                g.translate(0, -tunnelLength / 2);
                g.translate(-x + tunnelLength, -y);
            }
            break;
        }
    }

    private void paintD1(final Graphics2D g, final boolean tunelS) {
        if (tunelS) {
            g.draw(new Line2D.Double(0, 0, tunnelWidth, 0));
            g.draw(new Line2D.Double(0, 0, 0, tunnelLength));
            g.draw(new Line2D.Double(0, tunnelLength, tunnelWidth, tunnelLength));
        } else {
            g.draw(new Arc2D.Double(0, 0, tunnelWidth * 3, tunnelLength, 90,
                    180, Arc2D.OPEN));
        }
    }

    private void paintD2(final Graphics2D g, final boolean tunelS) {
        if (tunelS) {
            g.draw(new Line2D.Double(0, 0, -tunnelWidth, 0));
            g.draw(new Line2D.Double(0, 0, 0, tunnelLength));
            g.draw(new Line2D.Double(0, tunnelLength, -tunnelWidth,
                    tunnelLength));
        } else {
            g.draw(new Arc2D.Double(-tunnelWidth * 3, 0, tunnelWidth * 3,
                    tunnelLength, 90, -180, Arc2D.OPEN));

        }
    }

    private void paintA1(final Graphics2D g, final boolean tunelS) {
        if (tunelS) {
            g.draw(new Line2D.Double(0, tunnelWidth, 0, 0));
            g.draw(new Line2D.Double(0, 0, tunnelLength, 0));
            g.draw(new Line2D.Double(tunnelLength, tunnelWidth, tunnelLength, 0));
        } else {
            g.draw(new Arc2D.Double(0, 0, tunnelLength, tunnelWidth * 3, 180,
                    -180, Arc2D.OPEN));
        }
    }

    private void paintA2(final Graphics2D g, final boolean tunelS) {
        if (tunelS) {
            g.draw(new Line2D.Double(0, -tunnelWidth, 0, 0));
            g.draw(new Line2D.Double(0, 0, tunnelLength, 0));
            g.draw(new Line2D.Double(tunnelLength, -tunnelWidth, tunnelLength,
                    0));
        } else {
            g.draw(new Arc2D.Double(0, -tunnelWidth * 3, tunnelLength,
                    tunnelWidth * 3, 180, 180, Arc2D.OPEN));
        }
    }

    /**
     * Метод малює на екрані заокруглення для загибу стрлки.
     *
     * @param g     Об’єкт для виводу графіки.
     * @param vx    Координата x у внутрішніх координатах.
     * @param vy    Координата y у внутрішніх координатах.
     * @param type1 Напрямок початкової частини.
     * @param type2 Напрямок кінцевої частини.
     */

    private void paintBend2(final Graphics2D g, final double vx,
                            final double vy, final int type1, final int type2,
                            final double arrowRotateWidth) {
        if (type2 == -1)
            return;
        double x = 0;
        double y = 0;
        final double w = arrowRotateWidth * 2;
        final double h = arrowRotateWidth * 2;
        double s = 0;
        double e = 0;
        if ((type1 + 1) % 4 == type2) {
            switch (type1) {
                case MovingPanel.BOTTOM: {
                    x = vx - arrowRotateWidth * 2;
                    y = vy - arrowRotateWidth;
                    s = 270;
                    e = 90;
                }
                break;
                case MovingPanel.LEFT: {
                    x = vx - arrowRotateWidth;
                    y = vy - arrowRotateWidth * 2;
                    s = 180;
                    e = 90;
                }
                break;
                case MovingPanel.TOP: {
                    x = vx;
                    y = vy - arrowRotateWidth;
                    s = 90;
                    e = 90;
                }
                break;
                case MovingPanel.RIGHT: {
                    x = vx - arrowRotateWidth;
                    y = vy;
                    s = 0;
                    e = 90;
                }
                break;
            }
        } else {
            switch (type1) {
                case MovingPanel.TOP: {
                    x = vx - arrowRotateWidth * 2;
                    y = vy - arrowRotateWidth;
                    s = 90;
                    e = -90;
                }
                break;
                case MovingPanel.RIGHT: {
                    x = vx - arrowRotateWidth;
                    y = vy - arrowRotateWidth * 2;
                    s = 0;
                    e = -90;
                }
                break;
                case MovingPanel.BOTTOM: {
                    x = vx;
                    y = vy - arrowRotateWidth;
                    s = 270;
                    e = -90;
                }
                break;
                case MovingPanel.LEFT: {
                    x = vx - arrowRotateWidth;
                    y = vy;
                    s = 180;
                    e = -90;
                }
                break;
            }
        }

        g.draw(new Arc2D.Double(x, y, w, h, s, e, Arc2D.OPEN));
    }

    private void paintBend(final Graphics2D g, final double vx,
                           final double vy, final int type1, final int type2,
                           final double arrowRotateWidth) {
        if (type2 == -1)
            return;
        double x = 0;
        double y = 0;
        final double w = arrowRotateWidth * 2;
        final double h = arrowRotateWidth * 2;
        double s = 0;
        double e = 0;
        if ((type1 + 1) % 4 == type2) {
            switch (type1) {
                case MovingPanel.BOTTOM: {
                    x = vx - arrowRotateWidth * 2;
                    y = vy - arrowRotateWidth;
                    s = 0;
                    e = -90;
                }
                break;
                case MovingPanel.LEFT: {
                    x = vx - arrowRotateWidth;
                    y = vy - arrowRotateWidth * 2;
                    s = 270;
                    e = -90;
                }
                break;
                case MovingPanel.TOP: {
                    x = vx;
                    y = vy - arrowRotateWidth;
                    s = 180;
                    e = -90;
                }
                break;
                case MovingPanel.RIGHT: {
                    x = vx - arrowRotateWidth;
                    y = vy;
                    s = 90;
                    e = -90;
                }
                break;
            }
        } else {
            switch (type1) {
                case MovingPanel.TOP: {
                    x = vx - arrowRotateWidth * 2;
                    y = vy - arrowRotateWidth;
                    s = 0;
                    e = 90;
                }
                break;
                case MovingPanel.RIGHT: {
                    x = vx - arrowRotateWidth;
                    y = vy - arrowRotateWidth * 2;
                    s = 270;
                    e = 90;
                }
                break;
                case MovingPanel.BOTTOM: {
                    x = vx;
                    y = vy - arrowRotateWidth;
                    s = 180;
                    e = 90;
                }
                break;
                case MovingPanel.LEFT: {
                    x = vx - arrowRotateWidth;
                    y = vy;
                    s = 90;
                    e = 90;
                }
                break;
            }
        }
        g.draw(new Arc2D.Double(x, y, w, h, s, e, Arc2D.OPEN));
    }

    private Point2D getPlusPoint(final int type, double rotateWidth) {
        if (type == MovingPanel.RIGHT)
            return new Point2D.Double(rotateWidth, 0);
        else if (type == MovingPanel.LEFT)
            return new Point2D.Double(-rotateWidth, 0);
        else if (type == MovingPanel.TOP)
            return new Point2D.Double(0, -rotateWidth);
        else if (type == MovingPanel.BOTTOM)
            return new Point2D.Double(0, rotateWidth);
        else
            return new Point2D.Double(0, 0);
    }

    public void paintPin(final Graphics2D g, final PaintSector.Pin pin,
                         final double x01, final double y01, final double x02,
                         final double y02, int type) {
        final double rotateWidth = movingArea
                .getIDoubleOrdinate(ARROW_ROTATE_WIDTH);
        final int pinWay = pin.getWayType();
        final Point2D plus = getPlusPoint(pinWay, rotateWidth);
        double x1 = x01;
        double x2 = x02;
        double y1 = y01;
        double y2 = y02;
        if (type >= 0) {
            final float arrowHeight = (float) movingArea
                    .getIDoubleOrdinate(ARROW_HEIGHT);
            type = MovingPanel.getOpposite(type);
            switch (type) {
                case MovingPanel.BOTTOM: {
                    y2 -= arrowHeight;
                }
                break;
                case MovingPanel.RIGHT: {
                    x2 -= arrowHeight;
                }
                break;
                case MovingPanel.TOP: {
                    y2 += arrowHeight;
                }
                break;
                case MovingPanel.LEFT: {
                    x2 += arrowHeight;
                }
                break;
            }
        }
        boolean trank = false;
        int way2 = -1;
        if (!pin.isFirst())
            trank = true;
        else if (pin.getSector().getStart() != null
                && pin.getSector().getSector().getStart().getType() == SectorBorder.TYPE_SPOT)
            if (!pin.isHaveOpposite(true)) {
                trank = true;
                way2 = pin.getStart().getPointWayType(pin.getType());
                if (way2 != -1)
                    way2 = MovingPanel.getOpposite(way2);
            }

        if (trank) {
            x1 += plus.getX();
            y1 += plus.getY();
            paintBend2(g, x1, y1, MovingPanel.getOpposite(pinWay), way2,
                    rotateWidth);
        }
        trank = false;
        way2 = -1;
        if (!pin.isEnd()) {
            trank = true;
            way2 = pin.getNext().getWayType();
        } else if (pin.getSector().getEnd() != null
                && pin.getSector().getSector().getEnd().getType() == SectorBorder.TYPE_SPOT)
            if (!pin.isHaveOpposite(false)) {
                trank = true;
                way2 = pin.getEnd().getPointWayType(pin.getType());
            }
        if (trank) {
            x2 -= plus.getX();
            y2 -= plus.getY();
            paintBend(g, x2, y2, pinWay, way2, rotateWidth);
        }
        if (Math.abs(x01 - x02 + y01 - y02) > rotateWidth)
            g.draw(new Line2D.Double(x1, y1, x2, y2));
    }

    /**
     * Метод малює шапку моделі.
     *
     * @param g      Об’єкт для виводу.
     * @param height Висота області малювання.
     */

    public void paintTop(final Graphics2D g, final int height, MovingArea area,
                         int partNumber, int hPageCount) {
        paintTop(g, height, area, new Font(g.getFont().getName(), 0, 10),
                partNumber, hPageCount);
    }

    public void paintTop(final Graphics2D g, final int height, MovingArea area) {
        paintTop(g, height, area, new Font(g.getFont().getName(), 0, 10), 0, 1);
    }

    public void paintTop(final Graphics2D g, final int height, MovingArea area,
                         Font font2) {
        paintTop(g, height, area, font2, 0, 1);
    }

    public void paintTop(final Graphics2D g, final int height, MovingArea area,
                         Font font2, int partNumber, int hPageCount) {
        if (!area.isPrinting()) {
            if (!MovingArea.DISABLE_RENDERING_HINTS) {
                g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                        RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                        RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                        RenderingHints.VALUE_STROKE_NORMALIZE);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
        }

        int width = movingArea.getIntOrdinate(movingArea.MOVING_AREA_WIDTH);
        width /= hPageCount;
        int left = 0;
        if (partNumber > 0) {
            left = width * partNumber;
            width += 1;
        }

        g.drawLine(left, 0, left, height);
        g.drawLine(left + width - 1, 0, left + width - 1, height);
        g.drawRect(left, 0, width - 1, height - 1);
        final int div = (int) ((width / height) * 0.65d);
        final int x = left + width - width / div;
        g.drawLine(x, 0, x, height - 1);

        g.setFont(movingArea.getFont(font2));
        final int height2 = height
                - 8
                - (int) g.getFont()
                .getStringBounds(CONTEXT_T, g.getFontRenderContext())
                .getHeight();
        g.drawString(
                CONTEXT_T,
                x + 5,
                5 + (int) g.getFont()
                        .getStringBounds(CONTEXT_T, g.getFontRenderContext())
                        .getHeight());
        final Function activeFunction = movingArea.getActiveFunction();
        if (activeFunction == movingArea.dataPlugin.getBaseFunction()) {
            g.setFont(movingArea.getFont(font2));

            final int textWidth = (int) g.getFont()
                    .getStringBounds(TOP_T, g.getFontRenderContext())
                    .getWidth();
            final int textHeight = (int) g.getFont()
                    .getStringBounds(TOP_T, g.getFontRenderContext())
                    .getHeight();
            g.drawString(TOP_T, x + (width / div - textWidth) / 2,
                    (height - textHeight) / 2 + textHeight);
        } else {
            final Vector v = movingArea.dataPlugin.getChilds(
                    activeFunction.getParentRow(), true);
            final int width2 = width - x + left;
            for (int i = 0; i < v.size(); i++) {
                final Function f = (Function) v.get(i);
                final int x1 = (int) (f.getBounds().getX() * width2 / movingArea
                        .getDoubleWidth());
                final int y1 = (int) (f.getBounds().getY() * height2 / movingArea
                        .getDoubleHeight());
                final int width1 = (int) (f.getBounds().getWidth() * width2 / movingArea
                        .getDoubleWidth());
                final int height1 = (int) (f.getBounds().getHeight() * height2 / movingArea
                        .getDoubleHeight());
                if (f.equals(activeFunction))
                    g.fillRect(x + x1, y1 + height - height2, width1, height1);
                else
                    g.drawRect(x + x1, y1 + height - height2, width1, height1);
            }
        }
        g.setFont(movingArea.getFont(font2));
        final int h2 = height - 1;
        final int x2 = left + (int) (width / 2.0);
        g.drawLine(x2, 0, x2, h2);
        final int w = height / 4;
        final int w2 = (x - x2 + w) / 2;
        g.drawLine(x2 + w, 0, x2 + w, h2);
        g.drawLine(x2 + w2, 0, x2 + w2, h2);
        String s;
        int h;

        final ProjectOptions projectOptions = activeFunction
                .getProjectOptions();

        final Readed[] rs = projectOptions.getReadedModel().getSortReaded(3);
        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                s = READER_B;
                h = (int) g.getFont()
                        .getStringBounds(s, g.getFontRenderContext())
                        .getHeight();
                g.drawString(s, x2 + w2 + 2, (i + 1) * w - (w - h) / 2);
                s = DATE_B;
                h = (int) g.getFont()
                        .getStringBounds(s, g.getFontRenderContext())
                        .getHeight();
                final int ww = (int) g.getFont()
                        .getStringBounds(s, g.getFontRenderContext())
                        .getWidth();
                g.drawString(s, x - 2 - ww, (i + 1) * w - (w - h) / 2);
            } else if (i <= rs.length) {
                s = rs[i - 1].getReader();
                if (s.length() > 13) {
                    s = s.substring(0, 10) + "...";
                }
                h = (int) g.getFont()
                        .getStringBounds(s, g.getFontRenderContext())
                        .getHeight();
                g.drawString(s, x2 + w2 + 2, (i + 1) * w - (w - h) / 2);
                s = rs[i - 1].getDate();
                h = (int) g.getFont()
                        .getStringBounds(s, g.getFontRenderContext())
                        .getHeight();
                final int ww = (int) g.getFont()
                        .getStringBounds(s, g.getFontRenderContext())
                        .getWidth();
                g.drawString(s, x - 2 - ww, (i + 1) * w - (w - h) / 2);
            }
            g.drawLine(x2, i * w, x, i * w);
            s = ResourceLoader.getString(Status.STATUS_NAMES[i]);
            h = (int) g.getFont().getStringBounds(s, g.getFontRenderContext())
                    .getHeight();
            g.drawString(s, x2 + w + 2, (i + 1) * w - (w - h) / 2);
            if (activeFunction.getStatus().getType() == i)
                g.fillRect(x2, i * w, w, w);
        }

        final int x3 = left + width / 6;
        g.drawLine(x3, 0, x3, h2);

        s = AUTOR_T + activeFunction.getAuthor();
        h = (int) g.getFont().getStringBounds(s, g.getFontRenderContext())
                .getHeight();
        g.drawString(s, x3 + 2, w - (w - h) / 2);
        s = PROJECT_T + " ";
        final int projectTitileWidth = (int) g.getFont()
                .getStringBounds(s, g.getFontRenderContext()).getWidth();
        h = (int) g.getFont().getStringBounds(s, g.getFontRenderContext())
                .getHeight();
        g.drawString(s, x3 + 2, 2 * w - (w - h) / 2);
        s = TOP_IDEF_NOTES;
        final int t1 = 4 * w - (w - h) / 2;
        g.drawString(s, x3 + 2, t1);
        int wi1 = (int) g.getFont()
                .getStringBounds(s, g.getFontRenderContext()).getWidth()
                + 8 + x3;
        for (int i = 1; i <= 10; i++) {
            s = Integer.toString(i);
            g.drawString(s, wi1, t1);
            wi1 += (int) g.getFont()
                    .getStringBounds(s, g.getFontRenderContext()).getWidth() + 5;

        }
        final String d1 = DATE_T + " ";
        final String d2 = REV_T + " ";

        wi1 = (int) g.getFont().getStringBounds(d1, g.getFontRenderContext())
                .getWidth();
        final int wi2 = (int) g.getFont()
                .getStringBounds(d2, g.getFontRenderContext()).getWidth();
        final int wi = wi1 > wi2 ? wi1 : wi2;
        String createDate = Readed.dateFormat.format(activeFunction
                .getCreateDate());
        final int dw = (int) g.getFont()
                .getStringBounds(createDate, g.getFontRenderContext())
                .getWidth();
        final String pn = projectOptions.getProjectName();
        g.setFont(font2);
        movingArea.paintText(g, pn, movingArea.getFBounds(new Rectangle(x3 + 2
                        + projectTitileWidth, (int) (1.45 * w - (w - h) / 2), x2 - 4
                        - wi - dw - (x3 + 2 + projectTitileWidth), height - w)),
                Line.CENTER_ALIGN, 0, true);
        g.setFont(movingArea.getFont(font2));
        g.drawString(d1, x2 - 2 - wi - dw, w - (w - h) / 2);
        g.drawString(d2, x2 - 2 - wi - dw, 2 * w - (w - h) / 2);

        g.drawString(createDate, x2 - 2 - dw, w - (w - h) / 2);
        String revDate = Readed.dateFormat.format(activeFunction.getRevDate());
        g.drawString(revDate, x2 - 2 - dw, 2 * w - (w - h) / 2);
        s = USED_AT_T;
        h = (int) g.getFont().getStringBounds(s, g.getFontRenderContext())
                .getHeight();
        g.drawString(s, left + 5, 5 + h);
        s = projectOptions.getUsedAt();
        g.setFont(font2);
        movingArea.paintText(
                g,
                s,
                movingArea.getFBounds(new Rectangle(left + 5, h + 3, x3 - 11
                        - left, height - h - 8)), Line.CENTER_ALIGN, 1, true);
    }

    /**
     * Метод малює нижню частину моделі.
     *
     * @param g      Об’єкт для виводу графіки.
     * @param height Висота області виводу.
     */

    public void paintBottom(final Graphics2D g, final int height,
                            MovingArea area) {
        paintBottom(g, height, area, new Font(g.getFont().getName(), 0, 10));
    }

    public void paintBottom(final Graphics2D g, final int height,
                            MovingArea area, int partNumber, int hPageCount) {
        paintBottom(g, height, area, new Font(g.getFont().getName(), 0, 10),
                partNumber, hPageCount);
    }

    public void paintBottom(final Graphics2D g, final int height,
                            MovingArea area, Font bottomFont) {
        paintBottom(g, height, area, bottomFont, 0, 1);
    }

    public void paintBottom(final Graphics2D g, final int height,
                            MovingArea area, Font bottomFont, int partNumber, int hPageCount) {
        if (!area.isPrinting()) {
            if (!MovingArea.DISABLE_RENDERING_HINTS) {
                g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                        RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                        RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                        RenderingHints.VALUE_STROKE_NORMALIZE);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
        }

        g.setFont(bottomFont);
        final Font font = bottomFont;
        final Function activeFunction = movingArea.getActiveFunction();
        int width = movingArea.getIntOrdinate(movingArea.MOVING_AREA_WIDTH);
        width /= hPageCount;

        int left = 0;
        if (partNumber > 0) {
            left = width * partNumber;
            width += 1;
        }

        final int x = left + width / 5;
        g.drawLine(left, 0, left, height - 1);
        g.drawLine(width - 1, 0, width - 1, height - 1);
        g.drawRect(left, 0, width - 1, height - 1);
        g.drawLine(x, 0, x, height - 1);
        g.drawLine(left + width / 5 * 4, 0, left + width / 5 * 4, height - 1);
        g.setFont(movingArea.getFont(g.getFont()));
        String number = NUMBER + " " + movingArea.getFunctionNumber();
        if (hPageCount > 1)
            number += "." + (partNumber + 1);

        String node = NODE_B;
        final double nodeWidth = g.getFont()
                .getStringBounds(node, g.getFontRenderContext()).getWidth();
        final int nT = 5 + 4 + (int) g.getFont()
                .getStringBounds(node, g.getFontRenderContext()).getHeight();
        g.drawString(node, left + 5, nT);
        g.drawString(number, left + width / 5 * 4 + 5, nT);

        node = TITLE_B;
        final int titleWidth = (int) g.getFont()
                .getStringBounds(node, g.getFontRenderContext()).getWidth();

        g.drawString(node, x + 5, nT);
        if (activeFunction.getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFDS) {
            node = MovingFunction.getDFDSKod(
                    (com.ramussoft.database.common.Row) activeFunction,
                    movingArea.dataPlugin);
        } else
            node = MovingFunction
                    .getIDEF0Kod((com.ramussoft.database.common.Row) activeFunction);

        g.drawString(node, left + (int) nodeWidth + 10, nT);
        node = activeFunction.getName();
        if (activeFunction.equals(movingArea.dataPlugin.getBaseFunction())
                && activeFunction.getDecompositionType() != MovingArea.DIAGRAM_TYPE_DFDS) {
            final Vector v = movingArea.dataPlugin.getChilds(activeFunction,
                    true);
            if (v.size() > 0)
                node = ((Row) v.get(0)).getName();
        }
        g.setFont(font);
        movingArea.paintText(
                g,
                node,
                movingArea.getFBounds(new Rectangle(x + 10 + titleWidth, 2, x
                        * 3 - 10 - titleWidth - 3 * left, height)),
                Line.CENTER_ALIGN, 1, true);
    }

    public void paintLink(Graphics2D g, FloatPoint point, int type,
                          Function inner, Function outer) {
        long innerQ = ((NFunction) inner).getQualifierId();
        long outerQ = ((NFunction) outer).getQualifierId();

        String text = MovingFunction
                .getIDEF0Kod((com.ramussoft.database.common.Row) outer);
        String letter = outer.getProjectOptions().getDeligate()
                .getModelLetter();
        if (outer.getParentRow().getName().equals("F_BASE_FUNCTIONS")) {
            if (letter == null)
                text = "A-0";
            else
                text = letter + "-0";
        }
        if (innerQ != outerQ) {
            if (letter == null || "A".equals(letter))
                text = movingArea.getDataPlugin().getEngine()
                        .getQualifier(outerQ).getName()
                        + "/" + text;
        }

        tunnelLength = movingArea.getIDoubleOrdinate(TUNNEL_LENGTH) * 1;
        g.setStroke(ArrowPainter.THIN_STROKE);
        g.setStroke(new BasicStroke(0.5f));

        double x = movingArea.getIDoubleOrdinate(point.getX());
        double y = movingArea.getIDoubleOrdinate(point.getY());
        double helf = tunnelLength / 2;
        switch (type) {
            case MovingPanel.BOTTOM: {
                g.draw(new Ellipse2D.Double(x - helf, y, tunnelLength, tunnelLength));
                boolean left = point.getX() < movingArea.CLIENT_WIDTH / 2;
                if (!left) {
                    movingArea.paintText(g, text,
                            new FRectangle(point.getX() + TUNNEL_LENGTH, 0,
                                    movingArea.CLIENT_WIDTH - point.getX()
                                            - TUNNEL_WIDTH * 10,
                                    movingArea.CLIENT_HEIGHT), Line.LEFT_ALIGN,
                            true);
                } else {
                    movingArea.paintText(g, text, new FRectangle(0, 0, point.getX()
                                    - TUNNEL_LENGTH, movingArea.CLIENT_HEIGHT),
                            Line.RIGHT_ALIGN, true);
                }
            }
            break;
            case MovingPanel.TOP: {
                g.draw(new Ellipse2D.Double(x - helf, y - tunnelLength,
                        tunnelLength, tunnelLength));

                boolean left = point.getX() < movingArea.CLIENT_WIDTH / 2;
                if (!left) {
                    movingArea.paintText(g, text,
                            new FRectangle(point.getX() + TUNNEL_LENGTH, 0,
                                    movingArea.CLIENT_WIDTH - point.getX()
                                            - TUNNEL_WIDTH * 10,
                                    movingArea.CLIENT_HEIGHT), Line.LEFT_ALIGN, 2,
                            true);
                } else {
                    movingArea.paintText(g, text, new FRectangle(0, 0, point.getX()
                                    - TUNNEL_LENGTH, movingArea.CLIENT_HEIGHT),
                            Line.RIGHT_ALIGN, 2, true);
                }
            }
            break;
            case MovingPanel.RIGHT: {
                g.draw(new Ellipse2D.Double(x, y - helf, tunnelLength, tunnelLength));
                boolean top = point.getY() < movingArea.CLIENT_HEIGHT / 2;
                if (!top) {
                    movingArea.paintText(g, text, new FRectangle(2, point.getY()
                            + tunnelLength / 2, movingArea.CLIENT_WIDTH * 0.20,
                            movingArea.CLIENT_HEIGHT - point.getY() - tunnelLength
                                    / 2), Line.LEFT_ALIGN, 0, true);
                } else {
                    movingArea.paintText(g, text, new FRectangle(2, 0,
                            movingArea.CLIENT_WIDTH * 0.20, point.getY()
                            - tunnelLength / 2), Line.LEFT_ALIGN, 2, true);
                }
            }
            break;
            case MovingPanel.LEFT: {
                g.draw(new Ellipse2D.Double(x - tunnelLength, y - helf,
                        tunnelLength, tunnelLength));
                boolean top = point.getY() < movingArea.CLIENT_HEIGHT / 2;
                if (!top) {
                    movingArea.paintText(g, text, new FRectangle(
                            movingArea.CLIENT_WIDTH * 0.80, point.getY()
                            + tunnelLength / 2,
                            movingArea.CLIENT_WIDTH * 0.20 - 2,
                            movingArea.CLIENT_HEIGHT - point.getY() - tunnelLength
                                    / 2), Line.RIGHT_ALIGN, 0, true);
                } else {
                    movingArea.paintText(g, text, new FRectangle(
                            movingArea.CLIENT_WIDTH * 0.80, 0,
                            movingArea.CLIENT_WIDTH * 0.20 - 2, point.getY()
                            - tunnelLength / 2), Line.RIGHT_ALIGN, 2, true);
                }
            }
            break;
        }

    }
}
