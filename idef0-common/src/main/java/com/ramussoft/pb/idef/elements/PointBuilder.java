package com.ramussoft.pb.idef.elements;

import com.ramussoft.pb.Function;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.data.SectorBorder;

/**
 * Клас для побуддови точок сектора.
 *
 * @author ZDD
 */

public class PointBuilder {

    private static final int START = 0;

    private static final int END = 1;

    /**
     * Початкова точка, для якої необхідно пубудувати масив.
     */

    protected MPoint a;

    /**
     * Кінцева точка, для якої необхідно пубудувати масив.
     */

    protected MPoint b;

    /**
     * Посилання на функціональний блок з якого вийщла стрілка.
     */

    private Function fA;

    /**
     * Посилання на функціональний блок в який ввійшла точка.
     */

    private Function fB;

    /**
     * Вища межа функціональних блоків.
     */

    private double top;

    /**
     * Нища межа функціональних блоків.
     */

    private double bottom;

    /**
     * Права межа функціональних блоків.
     */

    private double right;

    /**
     * Ліва межа функціональних блоків.
     */

    private double left;

    private final Sector sector;

    /**
     * @author ZDD
     */

    private class MPoint {

        private final Point point;

        private final int type;

        public MPoint(final Point point, final int type) {
            this.point = point;
            this.type = type;
        }

        public Ordinate getXOrdinate() {
            return point.getXOrdinate();
        }

        public Ordinate getYOrdinate() {
            return point.getYOrdinate();
        }

        public Point getPoint() {
            return point;
        }

        public int getType() {
            return point.getType();
        }

        public double getX() {
            return point.getX();
        }

        public double getY() {
            return point.getY();
        }

        public int getFunctionType() {
            if (type == START)
                return sector.getStart().getFunctionType();
            return sector.getEnd().getFunctionType();
        }

        public int getPointType() {
            if (type == START)
                return sector.getStart().getType();
            return sector.getEnd().getType();

        }

        public void setYOrdinate(final Ordinate ordinate) {
            point.setYOrdinate(ordinate);
        }

        public void setXOrdinate(final Ordinate ordinate) {
            point.setXOrdinate(ordinate);
        }

    }

    /**
     * Конструктор, який будує точки від точки a в точку b.
     *
     * @param a Початкова точка.
     * @param b Кінцева точка.
     */

    public PointBuilder(final Point a, final Point b, final Sector sector) {
        this.a = new MPoint(a, START);
        this.b = new MPoint(b, END);
        this.sector = sector;
    }

    private boolean betv(final double a, final double b, final double c) {
        return a <= c && c <= b || a >= c && c >= b;
    }

    /**
     * Створює масив точок, які починаються з точки a і ідуть в точку b.
     *
     * @return Новостворений масив точок, які починаються з точки a і ідуть в
     * точку b (передані конструктору).
     */

    public Point[] getPoints() {
        if (a.getXOrdinate() == b.getXOrdinate()
                || a.getYOrdinate() == b.getYOrdinate()) {
            final Point[] points = new Point[2];
            points[0] = a.getPoint();
            points[1] = b.getPoint();
            return points;
        }
        fA = sector.getStart().getFunction();
        fB = sector.getEnd().getFunction();
        if (fA == null) {
            if (fB == null)
                return getCCPoints();
            else {
                return getCFPoints();
            }
        } else {
            if (fB == null) {
                return getFCPoints();
            } else
                return getFFPoints();
        }
    }

    /**
     * Сворює набір точок, для випадку, коли сектор виходить і входить з вузла.
     *
     * @return Новостворений набір точок.
     */

    private Point[] getCCPoints() {
        if (a.getType() == Ordinate.TYPE_X) {
            if (b.getType() == Ordinate.TYPE_Y)
                return getCXCYPoint();
            else
                return getCXCXPoint();
        } else {
            if (b.getType() == Ordinate.TYPE_Y)
                return getCYCYPoint();
            else
                return getCYCXPoint();
        }
    }

    private Point[] getCXCYPoint() {
        final Point[] points = new Point[3];
        points[0] = a.getPoint();
        points[2] = b.getPoint();

        points[1] = new Point(b.getXOrdinate(), a.getYOrdinate());
        return points;
    }

    private Point[] getCYCXPoint() {
        final Point[] points = new Point[3];
        points[0] = a.getPoint();
        points[2] = b.getPoint();
        points[1] = new Point(a.getXOrdinate(), b.getYOrdinate());
        return points;
    }

    private Point[] getCXCXPoint() {
        final Point[] points = new Point[4];
        points[0] = a.getPoint();
        points[3] = b.getPoint();
        points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
        points[1].setX((b.getX() + a.getX()) / 2);
        points[2] = new Point(points[1].getXOrdinate(), b.getYOrdinate());
        return points;
    }

    private Point[] getCYCYPoint() {
        final Point[] points = new Point[4];
        points[0] = a.getPoint();
        points[3] = b.getPoint();
        points[1] = createNext(a.getPoint(), Ordinate.TYPE_Y);
        points[1].setY((b.getY() + a.getY()) / 2);
        points[2] = new Point(b.getXOrdinate(), points[1].getYOrdinate());
        return points;
    }

    /**
     * Створює набір точок, для випадку, коли сектор виходить з функціонального
     * блока і приходить в функціональний блок.
     *
     * @return Новостворений набір точок.
     */

    protected Point[] getFFPoints() {

        top = fA.getBounds().getTop();
        bottom = fA.getBounds().getBottom();
        right = fA.getBounds().getRight();
        left = fA.getBounds().getLeft();

        double tmp = fB.getBounds().getTop();
        if (tmp < top)
            top = tmp;

        tmp = fB.getBounds().getBottom();
        if (tmp > bottom)
            bottom = tmp;

        tmp = fB.getBounds().getRight();
        if (tmp > right)
            right = tmp;

        tmp = fB.getBounds().getLeft();
        if (tmp < left)
            left = tmp;

        if (a.getFunctionType() == Point.RIGHT) {
            if (b.getFunctionType() == Point.LEFT) {
                if (a.getX() + PaintSector.LINE_MIN_LENGTH * 2 <= b.getX())
                    return getFLFRPoints();
                else
                    return getFRFLPoints();
            } else if (b.getFunctionType() == Point.TOP) {
                return getFFTPoints();
            } else if (b.getFunctionType() == Point.BOTTOM) {
                return getFFBPoints();
            }
        }

        Point start = null;
        Point end = null;
        if (a.getFunctionType() == Point.TOP) {
            if (a.getY() + PaintSector.LINE_MIN_LENGTH * 2 < b.getY()) {
                start = createNext(a.point, Ordinate.TYPE_Y);
                start.setY(fA.getBounds().getTop()
                        - PaintSector.LINE_MIN_LENGTH);
                start.setType(Ordinate.TYPE_X);
            }
            a.point.setType(Ordinate.TYPE_Y);
        } else if (a.getFunctionType() == Point.BOTTOM) {
            if (a.getY() + PaintSector.LINE_MIN_LENGTH * 2 > b.getY()) {
                start = createNext(a.point, Ordinate.TYPE_Y);
                start.setY(fA.getBounds().getBottom()
                        + PaintSector.LINE_MIN_LENGTH);
                start.setType(Ordinate.TYPE_X);
            }
            a.point.setType(Ordinate.TYPE_Y);
        } else if (a.getFunctionType() == Point.LEFT) {
            if (a.getX() + PaintSector.LINE_MIN_LENGTH * 2 < b.getX()) {
                start = createNext(a.point, Ordinate.TYPE_X);
                start.setX(fA.getBounds().getLeft()
                        - PaintSector.LINE_MIN_LENGTH);
                start.setType(Ordinate.TYPE_Y);
            }
            a.point.setType(Ordinate.TYPE_X);
        } else if (a.getFunctionType() == Point.RIGHT) {
            if (a.getX() + PaintSector.LINE_MIN_LENGTH * 2 > b.getX()) {
                start = createNext(a.point, Ordinate.TYPE_X);
                start.setX(fA.getBounds().getRight()
                        + PaintSector.LINE_MIN_LENGTH);
                start.setType(Ordinate.TYPE_Y);
            }
            a.point.setType(Ordinate.TYPE_X);
        }

        if (b.getFunctionType() == Point.RIGHT) {
            if (b.getX() - PaintSector.LINE_MIN_LENGTH > a.getX()) {
                end = new Point(new Ordinate(Ordinate.TYPE_X),
                        b.point.yOrdinate);
                end.setX(fB.getBounds().getRight()
                        + PaintSector.LINE_MIN_LENGTH);
                end.setType(Ordinate.TYPE_Y);
            }
            b.point.setType(Ordinate.TYPE_X);
        } else if (b.getFunctionType() == Point.LEFT) {
            if (b.getX() + PaintSector.LINE_MIN_LENGTH < a.getX()) {
                end = new Point(new Ordinate(Ordinate.TYPE_X),
                        b.point.yOrdinate);
                end
                        .setX(fB.getBounds().getLeft()
                                - PaintSector.LINE_MIN_LENGTH);
                end.setType(Ordinate.TYPE_Y);
            }
            b.point.setType(Ordinate.TYPE_X);
        } else if (b.getFunctionType() == Point.TOP) {
            if (a.getY() + PaintSector.LINE_MIN_LENGTH > b.getY()) {
                end = new Point(b.point.xOrdinate,
                        new Ordinate(Ordinate.TYPE_Y));
                end.setY(fB.getBounds().getTop() - PaintSector.LINE_MIN_LENGTH);
                end.setType(Ordinate.TYPE_X);
            }
            b.point.setType(Ordinate.TYPE_Y);
        } else if (b.getFunctionType() == Point.BOTTOM) {
            if (a.getY() - PaintSector.LINE_MIN_LENGTH < b.getY()) {
                end = new Point(b.point.xOrdinate,
                        new Ordinate(Ordinate.TYPE_Y));
                end.setY(fB.getBounds().getBottom()
                        + PaintSector.LINE_MIN_LENGTH);
                end.setType(Ordinate.TYPE_X);
            }
            b.point.setType(Ordinate.TYPE_Y);

        }
        Point oldA = a.point;
        Point oldB = b.point;
        if (start != null) {
            a = new MPoint(start, START);
        }
        if (end != null) {
            b = new MPoint(end, END);
        }
        Point[] points = getCCPoints();
        if (start != null) {
            Point[] points2 = new Point[points.length + 1];
            points2[0] = oldA;
            for (int i = 0; i < points.length; i++) {
                points2[i + 1] = points[i];
            }
            points = points2;
        }
        if (end != null) {
            Point[] points2 = new Point[points.length + 1];
            points2[points.length] = oldB;
            for (int i = 0; i < points.length; i++) {
                points2[i] = points[i];
            }
            points = points2;
        }
        return points;
    }

    /**
     * Створює набір точок, для випадку, сектор виходить з функціонального блоку
     * і приходить в точку, яка знаходиться не на функціональному блоці.
     *
     * @return Новостворений набір точок.
     */

    private Point[] getFCPoints() {

        if (a.getFunctionType() == Point.LEFT) {
            a.point.setType(Ordinate.TYPE_X);
            if (b.getX() + PaintSector.LINE_MIN_LENGTH < a.getX()) {
                return getCCPoints();
            } else {

                Ordinate x = new Ordinate(Ordinate.TYPE_X);

                Point oldPoint = a.point;
                x.setPosition(oldPoint.getX() - PaintSector.LINE_MIN_LENGTH);

                Point point = new Point(x, a.point.yOrdinate);
                a = new MPoint(point, a.type);
                a.point.setType(Ordinate.TYPE_Y);
                Point[] points = getCCPoints();
                Point[] res = new Point[points.length + 1];
                res[0] = oldPoint;
                for (int i = 0; i < points.length; i++) {
                    res[i + 1] = points[i];
                }
                return res;
            }
        } else if (a.getFunctionType() == Point.TOP) {
            a.point.setType(Ordinate.TYPE_Y);
            if (b.getY() + PaintSector.LINE_MIN_LENGTH < a.getY()) {
                return getCCPoints();
            } else {

                Ordinate y = new Ordinate(Ordinate.TYPE_Y);

                Point oldPoint = a.point;
                y.setPosition(oldPoint.getY() - PaintSector.LINE_MIN_LENGTH);

                Point point = new Point(a.point.xOrdinate, y);
                a = new MPoint(point, a.type);
                a.point.setType(Ordinate.TYPE_X);
                Point[] points = getCCPoints();
                Point[] res = new Point[points.length + 1];
                res[0] = oldPoint;
                for (int i = 0; i < points.length; i++) {
                    res[i + 1] = points[i];
                }
                return res;
            }
        } else if (a.getFunctionType() == Point.BOTTOM) {
            a.point.setType(Ordinate.TYPE_Y);
            if (b.getY() - PaintSector.LINE_MIN_LENGTH > a.getY()) {
                return getCCPoints();
            } else {

                Ordinate y = new Ordinate(Ordinate.TYPE_Y);

                Point oldPoint = a.point;
                y.setPosition(oldPoint.getY() + PaintSector.LINE_MIN_LENGTH);

                Point point = new Point(a.point.xOrdinate, y);
                a = new MPoint(point, a.type);
                a.point.setType(Ordinate.TYPE_X);
                Point[] points = getCCPoints();
                Point[] res = new Point[points.length + 1];
                res[0] = oldPoint;
                for (int i = 0; i < points.length; i++) {
                    res[i + 1] = points[i];
                }
                return res;
            }
        }

        if (b.getType() == Ordinate.TYPE_X)
            return getFCXPoints();
        else
            return getFCYPoints();
    }

    /**
     * Створює набір точок, для випадку, сектор виходить з функціонального блоку
     * і приходить в точку, яка знаходиться не на функціональному блоці, при
     * чому частина, яка входить в точку має бути паралельною до осі x.
     *
     * @return Новостворений набір точок.
     */

    private Point[] getFCXPoints() {
        Point[] points;
        if (b.getPointType() == SectorBorder.TYPE_BORDER
                && betv(fA.getBounds().getBottom(), fA.getBounds().getTop(), b
                .getY())) {
            points = new Point[2];
            a.setYOrdinate(b.getYOrdinate());
            points[0] = a.getPoint();
            points[1] = b.getPoint();
            return points;
        }
        if (a.getX() + 2 * PaintSector.LINE_MIN_LENGTH < b.getX()) {
            points = new Point[4];
            points[0] = a.getPoint();
            points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
            points[1].setX(a.getX() + PaintSector.LINE_MIN_LENGTH);
            points[2] = new Point(points[1].getXOrdinate(), b.getYOrdinate());
            points[3] = b.getPoint();
            if (fA.getBounds().getTop() < b.point.yOrdinate.getPosition()
                    && fA.getBounds().getBottom() > b.point.yOrdinate
                    .getPosition())
                a.point.yOrdinate.setPosition(b.getY());
            return points;
        } else {
            if (b.getY() > fA.getBounds().getTop()
                    - PaintSector.LINE_MIN_LENGTH
                    && b.getY() < fA.getBounds().getBottom()
                    + PaintSector.LINE_MIN_LENGTH) {
                points = new Point[6];
                points[0] = a.getPoint();
                points[5] = b.getPoint();
                points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
                points[1].setX(a.getX() + PaintSector.LINE_MIN_LENGTH);
                points[2] = createNext(points[1], Ordinate.TYPE_Y);
                if (a.getY() < b.getY()) {
                    points[2].setY(fA.getBounds().getTop()
                            - PaintSector.LINE_MIN_LENGTH);
                } else {
                    points[2].setY(fA.getBounds().getBottom()
                            + PaintSector.LINE_MIN_LENGTH);
                }
                points[3] = createNext(points[2], Ordinate.TYPE_X);
                if (fA.getBounds().getLeft() - PaintSector.LINE_MIN_LENGTH * 2 > b
                        .getX()) {
                    points[3].setX(b.getX() + PaintSector.LINE_MIN_LENGTH);
                } else {
                    points[3].setX(b.getX() - PaintSector.LINE_MIN_LENGTH);
                }
                points[4] = new Point(points[3].getXOrdinate(), b
                        .getYOrdinate());

            } else {
                points = new Point[4];
                points[0] = a.getPoint();
                points[3] = b.getPoint();
                points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
                points[1].setX(a.getX() + PaintSector.LINE_MIN_LENGTH);
                points[2] = new Point(points[1].getXOrdinate(), b
                        .getYOrdinate());
            }
        }
        return points;
    }

    /**
     * Створює набір точок, для випадку, сектор виходить з функціонального блоку
     * і приходить в точку, яка знаходиться не на функціональному блоці, при
     * чому частина, яка входить в точку має бути паралельною до осі y.
     *
     * @return Новостворений набір точок.
     */

    private Point[] getFCYPoints() {
        Point[] points;
        if (a.getX() + 2 * PaintSector.LINE_MIN_LENGTH < b.getX()) {
            points = new Point[3];
            points[0] = a.getPoint();
            points[1] = new Point(b.getXOrdinate(), a.getYOrdinate());
            points[2] = b.getPoint();
        } else {
            points = new Point[5];
            points[0] = a.getPoint();
            points[4] = b.getPoint();
            points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
            points[1].setX(a.getX() + PaintSector.LINE_MIN_LENGTH);
            points[2] = createNext(points[1], Ordinate.TYPE_Y);
            if (b.getY() < a.getY())
                points[2].setY(fA.getBounds().getTop()
                        - PaintSector.LINE_MIN_LENGTH);
            else
                points[2].setY(fA.getBounds().getBottom()
                        + PaintSector.LINE_MIN_LENGTH);
            points[3] = new Point(b.getXOrdinate(), points[2].getYOrdinate());
        }
        return points;
    }

    /**
     * Створює набір точок, для випадку, коли сектор виходить з точки, що не
     * пов’язана з функціональнальним блоком і приходить в функціональний блок.
     *
     * @return Новостворений набір точок.
     */

    private Point[] getCFPoints() {
        if (a.getType() == Ordinate.TYPE_X) {
            if (b.getFunctionType() == Point.TOP)
                return getCXFTPoints();
            else if (b.getFunctionType() == Point.LEFT)
                return getCXFLPoints();
            else if (b.getFunctionType() == Point.BOTTOM)
                return getCXFBPoints();
            else
                return getCXFRPoints();
        } else {
            if (b.getFunctionType() == Point.TOP)
                return getCYFTPoints();
            else if (b.getFunctionType() == Point.LEFT)
                return getCYFLPoints();
            else if (b.getFunctionType() == Point.BOTTOM)
                return getCYFBPoints();
            else
                return getCYFRPoints();
        }
    }

    private Point[] getCYFRPoints() {

        Point[] points;
        if (a.getX() - 2 * PaintSector.LINE_MIN_LENGTH > b.getX()) {
            points = new Point[3];
            points[0] = a.getPoint();
            points[1] = new Point(a.getXOrdinate(), b.getYOrdinate());
            points[2] = b.getPoint();
        } else {
            points = new Point[5];
            points[0] = a.getPoint();
            points[4] = b.getPoint();
            points[1] = createNext(a.getPoint(), Ordinate.TYPE_Y);
            if (b.getY() > a.getY())
                points[1].setY(fB.getBounds().getTop()
                        - PaintSector.LINE_MIN_LENGTH);
            else
                points[1].setY(fB.getBounds().getBottom()
                        + PaintSector.LINE_MIN_LENGTH);
            points[2] = createNext(points[1], Ordinate.TYPE_X);
            points[2].setX(b.getX() + PaintSector.LINE_MIN_LENGTH);
            points[3] = new Point(points[2].getXOrdinate(), b.getYOrdinate());
        }
        return points;
    }

    private Point[] getCXFRPoints() {
        Point[] points;
        if (a.getPointType() == SectorBorder.TYPE_BORDER) {
            points = new Point[4];
            points[0] = a.getPoint();
            points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
            points[1].setX(fB.getBounds().getRight()
                    + PaintSector.LINE_MIN_LENGTH);
            points[2] = new Point(points[1].getXOrdinate(), b.getYOrdinate());
            points[3] = b.getPoint();
            return points;
        }
        if (a.getX() - 2 * PaintSector.LINE_MIN_LENGTH < b.getX()) {
            // a.
            points = new Point[4];
            points[0] = a.getPoint();
            points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
            points[1].setX(fB.getBounds().getRight()
                    + PaintSector.LINE_MIN_LENGTH);
            points[2] = new Point(points[1].getXOrdinate(), b.getYOrdinate());
            points[3] = b.getPoint();
            if (a.point.yOrdinate.isMoveable(b.getY()))
                a.point.yOrdinate.setPosition(b.getY());
            return points;
        } else {
            points = new Point[4];
            points[0] = a.getPoint();
            points[3] = b.getPoint();
            points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
            points[1].setX(b.getX() + PaintSector.LINE_MIN_LENGTH);
            points[2] = new Point(points[1].getXOrdinate(), b.getYOrdinate());
        }
        return points;
    }

    /**
     * Створює набір точок, для випадку, коли сектор виходить з точки, що не
     * пов’язана з функціональнальним блоком, при чому частина, яка пов’язана з
     * точкою виходу паралельна осі x, сектор входить в функціональний блок
     * зверху.
     *
     * @return Новостворений набір точок.
     */

    private Point[] getCXFTPoints() {
        Point[] points;
        if (a.getY() <= b.getY() - PaintSector.LINE_MIN_LENGTH) {
            points = new Point[3];
            points[0] = a.getPoint();
            points[1] = new Point(b.getXOrdinate(), a.getYOrdinate());
            points[2] = b.getPoint();
        } else {
            points = new Point[5];
            points[0] = a.getPoint();
            points[4] = b.getPoint();
            points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
            if (a.getY() > fB.getBounds().getTop()
                    - PaintSector.LINE_MIN_LENGTH
                    && a.getY() < fB.getBounds().getBottom()
                    + PaintSector.LINE_MIN_LENGTH)
                points[1].setX(fB.getBounds().getLeft()
                        - PaintSector.LINE_MIN_LENGTH);
            else
                points[1].setX(fB.getBounds().getRight()
                        + PaintSector.LINE_MIN_LENGTH);
            points[2] = createNext(points[1], Ordinate.TYPE_Y);
            points[2].setY(fB.getBounds().getTop()
                    - PaintSector.LINE_MIN_LENGTH);
            points[3] = new Point(b.getXOrdinate(), points[2].getYOrdinate());
        }
        return points;
    }

    /**
     * Створює набір точок, для випадку, коли сектор виходить з точки, що не
     * пов’язана з функціональнальним блоком, при чому частина, яка пов’язана з
     * точкою виходу паралельна осі x, сектор входить в функціональний блок
     * зліва.
     *
     * @return Новостворений набір точок.
     */

    private Point[] getCXFLPoints() {
        Point[] points;
        if (a.getPointType() == SectorBorder.TYPE_BORDER
                && betv(fB.getBounds().getBottom(), fB.getBounds().getTop(), a
                .getY())) {
            points = new Point[2];
            a.setYOrdinate(b.getYOrdinate());
            points[0] = a.getPoint();
            points[1] = b.getPoint();
            return points;
        }
        if (a.getX() + 2 * PaintSector.LINE_MIN_LENGTH < b.getX()) {
            // a.
            points = new Point[4];
            points[0] = a.getPoint();
            points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
            points[1].setX(b.getX() - PaintSector.LINE_MIN_LENGTH);
            points[2] = new Point(points[1].getXOrdinate(), b.getYOrdinate());
            points[3] = b.getPoint();
            if (a.point.yOrdinate.isMoveable(b.getY()))
                a.point.yOrdinate.setPosition(b.getY());
            return points;
        } else {
            points = new Point[4];
            points[0] = a.getPoint();
            points[3] = b.getPoint();
            points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
            points[1].setX(b.getX() - PaintSector.LINE_MIN_LENGTH);
            points[2] = new Point(points[1].getXOrdinate(), b.getYOrdinate());
        }
        return points;
    }

    /**
     * Створює набір точок, для випадку, коли сектор виходить з точки, що не
     * пов’язана з функціональнальним блоком, при чому частина, яка пов’язана з
     * точкою виходу паралельна осі x, сектор входить в функціональний блок
     * знизу.
     *
     * @return Новостворений набір точок.
     */

    private Point[] getCXFBPoints() {
        Point[] points;
        if (a.getY() >= b.getY() + PaintSector.LINE_MIN_LENGTH) {
            points = new Point[3];
            points[0] = a.getPoint();
            points[1] = new Point(b.getXOrdinate(), a.getYOrdinate());
            points[2] = b.getPoint();
        } else {
            points = new Point[5];
            points[0] = a.getPoint();
            points[4] = b.getPoint();
            points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
            if (a.getY() > fB.getBounds().getTop()
                    - PaintSector.LINE_MIN_LENGTH
                    && a.getY() < fB.getBounds().getBottom()
                    + PaintSector.LINE_MIN_LENGTH)
                points[1].setX(fB.getBounds().getLeft()
                        - PaintSector.LINE_MIN_LENGTH);
            else
                points[1].setX(fB.getBounds().getRight()
                        + PaintSector.LINE_MIN_LENGTH);
            points[2] = createNext(points[1], Ordinate.TYPE_Y);
            points[2].setY(fB.getBounds().getBottom()
                    + PaintSector.LINE_MIN_LENGTH);
            points[3] = new Point(b.getXOrdinate(), points[2].getYOrdinate());
        }
        return points;
    }

    /**
     * Створює набір точок, для випадку, коли сектор виходить з точки, що не
     * пов’язана з функціональнальним блоком, при чому частина, яка пов’язана з
     * точкою виходу паралельна осі y, сектор входить в функціональний блок
     * зверху.
     *
     * @return Новостворений набір точок.
     */

    private Point[] getCYFTPoints() {
        Point[] points;
        if (a.getPointType() == SectorBorder.TYPE_BORDER
                && betv(fB.getBounds().getLeft(), fB.getBounds().getRight(), a
                .getX())) {
            points = new Point[2];
            a.setXOrdinate(b.getXOrdinate());
            points[0] = a.getPoint();
            points[1] = b.getPoint();
            return points;
        }
        if (a.getY() < fB.getBounds().getTop() - PaintSector.LINE_MIN_LENGTH) {
            points = new Point[4];
            points[0] = a.getPoint();
            points[3] = b.getPoint();
            points[1] = createNext(points[0], Ordinate.TYPE_Y);
            points[1].setY(fB.getBounds().getTop()
                    - PaintSector.LINE_MIN_LENGTH);
            points[2] = new Point(b.getXOrdinate(), points[1].getYOrdinate());
            if (a.point.xOrdinate.isMoveable(b.getX()))
                a.point.xOrdinate.setPosition(b.getX());
        } else {
            if (a.getX() < fB.getBounds().getRight()
                    + PaintSector.LINE_MIN_LENGTH
                    && a.getX() > fB.getBounds().getLeft()
                    - PaintSector.LINE_MIN_LENGTH) {
                points = new Point[6];
                points[0] = a.getPoint();
                points[5] = b.getPoint();
                points[1] = createNext(points[0], Ordinate.TYPE_Y);
                points[1].setY(fB.getBounds().getBottom()
                        + PaintSector.LINE_MIN_LENGTH);
                points[2] = createNext(points[1], Ordinate.TYPE_X);
                points[2].setX(fB.getBounds().getRight()
                        + PaintSector.LINE_MIN_LENGTH);
                points[3] = createNext(points[2], Ordinate.TYPE_Y);
                points[3].setY(fB.getBounds().getTop()
                        - PaintSector.LINE_MIN_LENGTH);
                points[4] = new Point(b.getXOrdinate(), points[3]
                        .getYOrdinate());
            } else {
                points = new Point[4];
                points[0] = a.getPoint();
                points[3] = b.getPoint();
                points[1] = createNext(points[0], Ordinate.TYPE_Y);
                points[1].setY(fB.getBounds().getTop()
                        - PaintSector.LINE_MIN_LENGTH);
                points[2] = new Point(b.getXOrdinate(), points[1]
                        .getYOrdinate());
            }
        }
        return points;
    }

    /**
     * Створює набір точок, для випадку, коли сектор виходить з точки, що не
     * пов’язана з функціональнальним блоком, при чому частина, яка пов’язана з
     * точкою виходу паралельна осі y, сектор входить в функціональний блок
     * зліва.
     *
     * @return Новостворений набір точок.
     */

    private Point[] getCYFLPoints() {
        Point[] points;
        if (a.getX() + 2 * PaintSector.LINE_MIN_LENGTH < b.getX()) {
            points = new Point[3];
            points[0] = a.getPoint();
            points[1] = new Point(a.getXOrdinate(), b.getYOrdinate());
            points[2] = b.getPoint();
        } else {
            points = new Point[5];
            points[0] = a.getPoint();
            points[4] = b.getPoint();
            points[1] = createNext(a.getPoint(), Ordinate.TYPE_Y);
            if (b.getY() > a.getY())
                points[1].setY(fB.getBounds().getTop()
                        - PaintSector.LINE_MIN_LENGTH);
            else
                points[1].setY(fB.getBounds().getBottom()
                        + PaintSector.LINE_MIN_LENGTH);
            points[2] = createNext(points[1], Ordinate.TYPE_X);
            points[2].setX(b.getX() - PaintSector.LINE_MIN_LENGTH);
            points[3] = new Point(points[2].getXOrdinate(), b.getYOrdinate());
        }
        return points;
    }

    /**
     * Створює набір точок, для випадку, коли сектор виходить з точки, що не
     * пов’язана з функціональнальним блоком, при чому частина, яка пов’язана з
     * точкою виходу паралельна осі y, сектор входить в функціональний блок
     * знизу.
     *
     * @return Новостворений набір точок.
     */

    private Point[] getCYFBPoints() {
        Point[] points;
        if (a.getPointType() == SectorBorder.TYPE_BORDER
                && betv(fB.getBounds().getLeft(), fB.getBounds().getRight(), a
                .getX())) {
            points = new Point[2];
            a.setXOrdinate(b.getXOrdinate());
            points[0] = a.getPoint();
            points[1] = b.getPoint();
            return points;
        }
        if (a.getY() > fB.getBounds().getBottom() + PaintSector.LINE_MIN_LENGTH) {
            points = new Point[4];
            points[0] = a.getPoint();
            points[3] = b.getPoint();
            points[1] = createNext(points[0], Ordinate.TYPE_Y);
            points[1].setY(fB.getBounds().getBottom()
                    + PaintSector.LINE_MIN_LENGTH);
            if (a.point.xOrdinate.isMoveable(b.getX()))
                a.point.xOrdinate.setPosition(b.getX());
            points[2] = new Point(b.getXOrdinate(), points[1].getYOrdinate());
        } else {
            if (a.getX() < fB.getBounds().getRight()
                    + PaintSector.LINE_MIN_LENGTH
                    && a.getX() > fB.getBounds().getLeft()
                    - PaintSector.LINE_MIN_LENGTH) {
                points = new Point[6];
                points[0] = a.getPoint();
                points[5] = b.getPoint();
                points[1] = createNext(points[0], Ordinate.TYPE_Y);
                points[1].setY(fB.getBounds().getTop()
                        - PaintSector.LINE_MIN_LENGTH);
                points[2] = createNext(points[1], Ordinate.TYPE_X);
                points[2].setX(fB.getBounds().getRight()
                        + PaintSector.LINE_MIN_LENGTH);
                points[3] = createNext(points[2], Ordinate.TYPE_Y);
                points[3].setY(fB.getBounds().getBottom()
                        + PaintSector.LINE_MIN_LENGTH);
                points[4] = new Point(b.getXOrdinate(), points[3]
                        .getYOrdinate());
            } else {
                points = new Point[4];
                points[0] = a.getPoint();
                points[3] = b.getPoint();
                points[1] = createNext(points[0], Ordinate.TYPE_Y);
                points[1].setY(fB.getBounds().getBottom()
                        + PaintSector.LINE_MIN_LENGTH);
                points[2] = new Point(b.getXOrdinate(), points[1]
                        .getYOrdinate());
            }
        }
        return points;
    }

    /**
     * Генерує набір точок для між функціональними блоками, функціональний блок
     * з якого виходить сектор знаходиться правіше, ніж функціональний блок, в
     * який входить сектор. Сектор виходить з правого края, приходить в лівий
     * край.
     *
     * @return Новостворений набір точок.
     */

    private Point[] getFRFLPoints() {
        final Point[] points = new Point[6];
        points[0] = a.getPoint();
        points[5] = b.getPoint();
        points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
        points[1].setX(right + PaintSector.LINE_MIN_LENGTH);
        points[2] = createNext(points[1], Ordinate.TYPE_Y);
        /*
         * if (a.getY() > b.getY()) points[2].setY(top -
		 * Sector.LINE_MIN_LENGTH); else
		 */
        points[2].setY(bottom + PaintSector.LINE_MIN_LENGTH);

        points[3] = createNext(points[2], Ordinate.TYPE_X);
        points[3].setX(b.getX() - PaintSector.LINE_MIN_LENGTH);
        points[4] = new Point(points[3].getXOrdinate(), b.getYOrdinate());
        return points;
    }

    /**
     * Генерує набір точок для між функціональними блоками, функціональний блок
     * з якого виходить сектор знаходиться лівіше, ніж функціональний блок, в
     * який входить сектор. Сектор виходить з правого края, приходить в лівий
     * край.
     *
     * @return Новостворений набір точок.
     */

    private Point[] getFLFRPoints() {
        Point[] points;
        if (betv(fA.getBounds().getTop(), fA.getBounds().getBottom(), b.getY())) {
            points = new Point[2];
            points[0] = a.getPoint();
            points[1] = b.getPoint();
            a.setYOrdinate(b.getYOrdinate());
            return points;
        } else if (betv(fB.getBounds().getTop(), fB.getBounds().getBottom(), a
                .getY())) {
            points = new Point[2];
            points[0] = a.getPoint();
            points[1] = b.getPoint();
            b.setYOrdinate(a.getYOrdinate());
            return points;
        }
        points = new Point[4];
        points[0] = a.getPoint();
        points[3] = b.getPoint();
        final double center = (a.getX() + b.getX()) / 2;
        points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
        points[1].setX(center);
        points[2] = new Point(points[1].getXOrdinate(), b.getYOrdinate());
        return points;
    }

    /**
     * Створює набір точок, між двома функціональними блоками. При чому точка
     * входить зверху в функціональний блок.
     *
     * @return
     */

    private Point[] getFFTPoints() {
        if (a.getY() > b.getY() - PaintSector.LINE_MIN_LENGTH
                || b.getX() < fA.getBounds().getLeft()) {
            final Point[] points = new Point[5];
            points[0] = a.getPoint();
            points[4] = b.getPoint();
            points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
            if (a.getY() > b.getY() - PaintSector.LINE_MIN_LENGTH
                    && a.getY() < fB.getBounds().getBottom()
                    && b.getX() > fA.getBounds().getRight())
                points[1].setX(fB.getBounds().getLeft()
                        - PaintSector.LINE_MIN_LENGTH);
            else
                points[1].setX(right + PaintSector.LINE_MIN_LENGTH);
            points[2] = createNext(points[1], Ordinate.TYPE_Y);
            if (b.getX() < fA.getBounds().getLeft())
                points[2].setY(top - PaintSector.LINE_MIN_LENGTH);
            else
                points[2].setY(b.getY() - PaintSector.LINE_MIN_LENGTH);
            points[3] = new Point(b.getXOrdinate(), points[2].getYOrdinate());
            return points;
        } else if (a.getX() + PaintSector.LINE_MIN_LENGTH < b.getX()) {
            final Point[] points = new Point[3];
            points[0] = a.getPoint();
            points[2] = b.getPoint();
            points[1] = new Point(b.getXOrdinate(), a.getYOrdinate());
            return points;
        } else {
            final Point[] points = new Point[5];
            points[0] = a.getPoint();
            points[4] = b.getPoint();
            points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
            points[1].setX(right + PaintSector.LINE_MIN_LENGTH);
            points[2] = createNext(points[1], Ordinate.TYPE_Y);
            points[2].setY(fA.getBounds().getBottom()
                    + PaintSector.LINE_MIN_LENGTH);
            points[3] = new Point(b.getXOrdinate(), points[2].getYOrdinate());
            return points;
        }
    }

    /**
     * Створює набір точок, між двома функціональними блоками. При чому точка
     * входить знизу в функціональний блок.
     *
     * @return
     */

    private Point[] getFFBPoints() {
        if (a.getY() < b.getY() + PaintSector.LINE_MIN_LENGTH
                || a.getX() > b.getX()
                && fA.getBounds().getTop() - PaintSector.LINE_MIN_LENGTH * 2 < b
                .getY()) {
            final Point[] points = new Point[5];
            points[0] = a.getPoint();
            points[4] = b.getPoint();
            points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
            if (a.getY() < b.getY() + PaintSector.LINE_MIN_LENGTH
                    && a.getY() < fB.getBounds().getBottom()
                    && a.getY() > fB.getBounds().getTop()
                    && a.getX() < b.getX())
                points[1].setX(fB.getBounds().getLeft()
                        - PaintSector.LINE_MIN_LENGTH);
            else
                points[1].setX(right + PaintSector.LINE_MIN_LENGTH);
            points[2] = createNext(points[1], Ordinate.TYPE_Y);
            // if(b.getX()<fA.getBounds().getLeft())
            points[2].setY(bottom + PaintSector.LINE_MIN_LENGTH);
			/*
			 * else points[2].setY(b.getY() + Sector.LINE_MIN_LENGTH);
			 */
            points[3] = new Point(b.getXOrdinate(), points[2].getYOrdinate());
            return points;
        } else {
            if (a.getX() + PaintSector.LINE_MIN_LENGTH > b.getX()) {
                final Point[] points = new Point[5];
                points[0] = a.getPoint();
                points[4] = b.getPoint();
                points[1] = createNext(a.getPoint(), Ordinate.TYPE_X);
                points[1].setX(right + PaintSector.LINE_MIN_LENGTH);
                points[2] = createNext(points[1], Ordinate.TYPE_Y);
                points[2].setY(bottom + PaintSector.LINE_MIN_LENGTH);
                points[3] = new Point(b.getXOrdinate(), points[2]
                        .getYOrdinate());
                return points;
            } else {
                final Point[] points = new Point[3];
                points[0] = a.getPoint();
                points[2] = b.getPoint();
                points[1] = new Point(b.getXOrdinate(), a.getYOrdinate());
                return points;
            }
        }
    }

    /**
     * Метод створює нову точку, після поточної, і відповідне значення ординати
     * x або y в залежності від типу ординати.
     *
     * @param point        Поточна точка.
     * @param ordinateType Ordinate.TYPE_X - якщо лінія між поточної і новоствореною
     *                     паралельна осі x, Ordinate.TYPE_Y - якщо лінія між поточної і
     *                     новоствореною паралельна осі y
     * @return Новостворена наступна точка.
     */

    private Point createNext(final Point point, final int ordinateType) {
        final Point res = new Point();
        if (ordinateType == Ordinate.TYPE_Y) {
            res.setXOrdinate(point.getXOrdinate());
            res.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
        } else {
            res.setYOrdinate(point.getYOrdinate());
            res.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
        }
        return res;
    }
}
