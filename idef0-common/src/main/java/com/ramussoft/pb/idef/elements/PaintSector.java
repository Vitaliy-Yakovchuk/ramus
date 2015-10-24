package com.ramussoft.pb.idef.elements;

import static com.ramussoft.pb.data.AbstractSector.equalsStreams;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import com.dsoft.pb.types.FRectangle;
import com.dsoft.pb.types.FloatPoint;
import com.dsoft.utils.DataLoader;
import com.dsoft.utils.DataSaver;
import com.dsoft.utils.Options;
import com.dsoft.utils.DataLoader.MemoryData;
import com.ramussoft.common.Engine;
import com.ramussoft.idef0.attribute.SectorPointPersistent;
import com.ramussoft.idef0.attribute.SectorPropertiesPersistent;
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.AbstractSector;
import com.ramussoft.pb.data.negine.NCrosspoint;
import com.ramussoft.pb.data.negine.NSector;
import com.ramussoft.pb.dfds.visual.DFDSMovingLabel;
import com.ramussoft.pb.idef.visual.ArrowedStroke;
import com.ramussoft.pb.idef.visual.IDEF0Object;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingFunction;
import com.ramussoft.pb.idef.visual.MovingLabel;
import com.ramussoft.pb.idef.visual.MovingPanel;
import com.ramussoft.pb.idef.visual.MovingText;
import com.ramussoft.pb.idef.visual.WayStroke;
import com.ramussoft.pb.types.GlobalId;

/**
 * Клас сектор. Призначений для збереження набору класифікаторів, які
 * передаються з вузла в вузол. Один сектор може проходити чарез багато
 * функціональних блоків, переходячи на різні рівні функціональних блоків. Клас
 * подає списки пов’язаних класифікаторів, а також тих, які були наслідувані з
 * інших класифікаторів. Є методи для розбиття сектора на декілька, при цьому
 * створуються новий вузол, а також для об’єднання декількох секторів об’єднаних
 * вузлом.
 *
 * @author ZDD
 */
public class PaintSector {

    /**
     * Ширина, яка належить для стрілки.
     */

    public static double LINE_WIDTH = MovingArea.getWidth(2);

    /**
     * Мінімальна допустима довжина між точками сектора.
     */

    public static double LINE_MIN_LENGTH = MovingArea.getWidth(6);

    /**
     * Довжина виділеної частинки.
     */
    public static double LINE_SEL_LENGTH = MovingArea.getWidth(11);

    /**
     * Довжина шматка, до якої буде приєднання в вузолю
     */

    private static double ADDED_LENGTH = LINE_SEL_LENGTH;// MovingArea.getWidth(11);

    /**
     * Тип частини стрілки, паралельний осі x.
     */

    public static final int PIN_TYPE_X = Ordinate.TYPE_X;

    /**
     * Тип частини стрілки, паралельний осі y.
     */

    public static final int PIN_TYPE_Y = Ordinate.TYPE_Y;

    /**
     * <code>true</code>, якщо останній isOnSector був на початковій частині
     * початкової частини, <code>false</code> інакше.
     */

    private boolean selStart = false;

    /**
     * <code>true</code>, якщо останній isOnSector був на кінцевій частині
     * кігцевої частини, <code>false</code> інакше.
     */

    private boolean selEnd = false;

    /**
     * Ознака чи був пов’язаний елеменк класифікатора методом зміни секторів,
     * пов’язаних з даним, це використовується при переключенні секторів, від
     * одного до іншого. <code>true</code> - елемент бувпов’язаний медотом
     * переключення;<br>
     * <code>false</code> - інакше.
     */

    /**
     * Змінна використовується для перешкоджання рекурсивним викликам.
     */

    private boolean rec = false;

    /**
     * Шрифт тексту, який відноситься до сектора.
     */

    private Font font = Options.getFont("DEFAULT_ARROW_FONT", new Font(
            "Dialog", 0, 8));

    /**
     * Колір сектора і тексту.
     */

    private Color color = Options.getColor("DEFAULT_ARROW_COLOR", Color.black);

    /**
     * Посилання на об’єкт з методами для малювання стрілок.
     */

    private ArrowPainter arrowPainter;

    private MovingArea movingArea = null;

    /**
     * Нотатки для сектора.
     */

    // private String note = "";
    /**
     * Опис сектора.
     */

    // private String describe = "";
    /**
     * Шрифт тексту, який відноситься до сектора.
     */

    // private Font font = Options.getFont("DEFAULT_ARROW_FONT", new Font(
    // "Dialog", 0, 12));
    /**
     * Колір сектора і тексту.
     */

    // private Color color = Options.getColor("DEFAULT_ARROW_COLOR",
    // Color.black);
    /**
     * Атрибут, в якому зберігається інформація про позицію тільди.
     */

    private final TildaPos tildaPos = new TildaPos();

    /**
     * Масив, де зберігається набір точок для відображення.
     */

    protected Point[] points = new Point[0];

    /**
     * Пов’язаний з сектором потік.
     */

    // protected Stream stream = null;
    /**
     * Робота, на якій розташований сектор.
     */

    // protected Function function = null;
    /**
     * Визначає тип лінії для сектора.
     */

    private Stroke stroke = ArrowPainter.THIN_STROKE;

    /**
     * Клас для відображення назви.
     */

    private MovingLabel text = null;

    /**
     * Показувати тільду, чи ні.
     */

    private boolean showTilda = false;

    /**
     * Сектор даних, якому відповідає його візуальне відображення.
     */

    private Sector sector = null;

    /**
     * Клас призначений для зберігання позиції точки куди під’єднана тільда.
     *
     * @author ZDD
     */

    private class TildaPos {

        /**
         * Позиція точки [0,1] на секторі.
         */

        private double pos;

        /**
         * Метод, якому передаються координати точки, номер частини і позиція
         * визначаються автоматично.
         *
         * @param x Координата x.
         * @param y Координата y.
         */

        public void setPos(final double x, final double y) {
            final Pin pin = isOnMe(x, y);
            pos = 0;
            for (int i = 0; i < getPinCount(); i++)
                if (pin.getPos() > i)
                    pos += getPin(i).getLength();
            if (pin.getType() == PIN_TYPE_X)
                pos += Math.abs(pin.getStart().getX() - x);
            else
                pos += Math.abs(pin.getStart().getY() - y);
            final double l = getLength();
            if (l == 0)
                pos = 0;
            else
                pos /= getLength();
        }

        /**
         * Конструктор по замовчуванню, позиція береться по середині.
         */

        public TildaPos() {
            pos = 0.5;
        }

        /**
         * Визначає координати точки тільди.
         *
         * @return Координати точки тільди.
         */

        public FloatPoint getPoint() {
            final double sLength = getLength();
            double pos = this.pos * sLength;
            final FloatPoint res = new FloatPoint();
            Pin pin = getPin(0);
            for (int i = 0; i < getPinCount(); i++) {
                final double pLength = getPin(i).getLength();
                if (pLength >= pos) {
                    pin = getPin(i);
                    break;
                } else
                    pos -= pLength;
            }
            res.setX(pin.getStart().getX());
            res.setY(pin.getStart().getY());
            if (pin.getType() == PIN_TYPE_X) {
                if (pin.getStart().getX() < pin.getEnd().getX())
                    res.addX(pos);
                else
                    res.addX(-pos);
            } else {
                if (pin.getStart().getY() < pin.getEnd().getY())
                    res.addY(pos);
                else
                    res.addY(-pos);
            }
            return res;
        }

        /**
         * Метод визначає частину сектора, на якій знаходиться точка.
         *
         * @return Номер частини сектора, на якій знаходиться дочка.
         */

        public int getPinNumber() {
            double pos = this.pos * getLength();
            for (int i = 0; i < getPinCount(); i++) {
                final double pLength = getPin(i).getLength();
                if (pLength >= pos) {
                    return 0;
                } else
                    pos -= pLength;
            }
            return 0;
        }

        public double getPos() {
            return pos;
        }

        public void setPos(final double d) {
            pos = d;
        }

    }

    ;

    /**
     * Клас призначений для роботи з частиною стрілки, руху вверх, вниз...
     *
     * @author ZDD
     */
    public class Pin {

        /**
         * Номер початкової точки.
         */

        private final int startPoint;

        /**
         * @param startPoint Номер початкової точки.
         */

        public Pin(final int startPoint) {
            super();
            this.startPoint = startPoint;
        }

        /**
         * Повертає тип частини (напрямок по осі x або y).
         *
         * @return PIN_TYPE_X, якщо частина паралельна осі x, PIN_TYPE_Y, якщо
         * частина паралельна осі y.
         */
        public int getType() {
            if (getStart().getYOrdinate() == getEnd().getYOrdinate())
                return PIN_TYPE_X;
            else
                return PIN_TYPE_Y;

        }

        /**
         * Повертає значення початкової точки.
         *
         * @return Початкова точка.
         */

        public Point getStart() {
            return points[startPoint];
        }

        /**
         * Повертає значення кінцевої точки.
         *
         * @return Кінцева точка.
         */

        public Point getEnd() {
            return points[startPoint + 1];
        }

        /**
         * Метод перевіряє чи знаходиться координата біля початку сектора.
         *
         * @param x Координата x.
         * @param y Координата y.
         * @return <code>true</code> - координата біля початку сектора,
         * <code>false</code> - координата біля не біля початку сектора.
         */

        public boolean isNearStart(final double x, final double y) {
            final FloatPoint mS = getMinStart();
            if (getType() == PIN_TYPE_X)
                return isBetv(getStart().getY() - ADDED_LENGTH, getStart()
                        .getY() + ADDED_LENGTH, y)
                        && isBetv(getStart().getX(), mS.getX(), x);
            else
                return isBetv(getStart().getX() - ADDED_LENGTH, getStart()
                        .getX() + ADDED_LENGTH, x)
                        && isBetv(getStart().getY(), mS.getY(), y);

        }

        /**
         * Перевіряє чи координата знаходиться біля кінця сектора.
         *
         * @param x Коордиата x.
         * @param y Коордиата y.
         * @return <code>true</code> - координата біля початку сектора,
         * <code>false</code> - координата не біля початку сектора.
         */

        public boolean isNearEnd(final double x, final double y) {
            final FloatPoint mS = getMinEnd();
            if (getType() == PIN_TYPE_X)
                return isBetv(getEnd().getY() - ADDED_LENGTH, getEnd().getY()
                        + ADDED_LENGTH, y)
                        && isBetv(getEnd().getX(), mS.getX(), x);
            else
                return isBetv(getEnd().getX() - ADDED_LENGTH, getEnd().getX()
                        + ADDED_LENGTH, x)
                        && isBetv(getEnd().getY(), mS.getY(), y);
        }

        /**
         * Перевіряє, чи лежить точка на частині стрілки.
         *
         * @param x Координата x точки.
         * @param y Координата y точки.
         * @return true, якщо точка лежить на частині стрілки, false, якщо точка
         * не лежить на частині стрілки.
         */
        public boolean isOnMe(final double x, final double y) {
            boolean res = false;
            if (getType() == PIN_TYPE_X) {
                if (isBetv(getStart().getX(), getEnd().getX(), x)) {
                    res = isBetv(getStart().getY() - LINE_WIDTH, getStart()
                            .getY() + LINE_WIDTH, y);
                }
            } else {
                if (isBetv(getStart().getY(), getEnd().getY(), y))
                    res = isBetv(getStart().getX() - LINE_WIDTH, getStart()
                            .getX() + LINE_WIDTH, x);
            }
            if (res) {
                if (isFirst() && isNearStart(x, y))
                    trySetSelStart();
                else if (isNearEnd(x, y) && isEnd())
                    trySetSelEnd();
            }
            return res;
        }

        /**
         * Перевіряє, чи знаходиться точка c між точками a і b.
         *
         * @param a Точка a
         * @param b Точка b
         * @param c Точка c
         * @return true, якщо точка c знаходиться між точками a і b, false, якщо
         * точка не знаходиться між точками a і b.
         */
        private boolean isBetv(final double a, final double b, final double c) {
            return a > c && c > b || a < c && c < b;
        }

        /**
         * Малює шматок, використовуючи параметри movingArea.
         *
         * @param g          Об’єкт для малювання.
         * @param movingArea Об’єкт для завантаження параметрів.
         */

        public void paint(final Graphics2D g, final MovingArea movingArea) {
            final double x1 = movingArea.getIDoubleOrdinate(getStart().getX());
            final double x2 = movingArea.getIDoubleOrdinate(getEnd().getX());
            final double y1 = movingArea.getIDoubleOrdinate(getStart().getY());
            final double y2 = movingArea.getIDoubleOrdinate(getEnd().getY());
            if (selStart && startPoint == 0)
                drawSelStart(g, movingArea);
            else if (selEnd && startPoint == points.length - 2)
                drawSelEnd(g, movingArea);
            if (isEnd()) {
                if (sector.getEnd().getFunction() != null) {
                    arrowPainter.paintArrowEnd(g, x1, y1, x2, y2, sector
                            .getEnd().getFunctionType());
                    arrowPainter.paintPin(g, this, x1, y1, x2, y2, sector
                            .getEnd().getFunctionType());
                } else if (sector.getEnd().getBorderType() >= 0) {
                    arrowPainter.paintArrowEnd(g, x1, y1, x2, y2, MovingPanel
                            .getOpposite(sector.getEnd().getBorderType()));
                    arrowPainter.paintPin(g, this, x1, y1, x2, y2, sector
                            .getEnd().getBorderType());
                } else if (PaintSector.this.getEnd() == null) {
                    int borderType = sector.getStart().getBorderType();
                    if (borderType < 0)
                        borderType = MovingText.getOpposite(sector.getStart()
                                .getFunctionType());
                    arrowPainter.paintPin(g, this, x1, y1, x2, y2, borderType);
                    arrowPainter.paintArrowEnd(g, x1, y1, x2, y2, borderType);
                } else
                    arrowPainter.paintPin(g, this, x1, y1, x2, y2, -1);
            } else
                arrowPainter.paintPin(g, this, x1, y1, x2, y2, -1);
        }

        private FloatPoint getMinStart() {
            if (getType() == Ordinate.TYPE_X) {
                double d = getStartPoint().getX();
                if (getStart().getX() > getEnd().getX())
                    d -= LINE_SEL_LENGTH;
                else
                    d += LINE_SEL_LENGTH;
                return new FloatPoint(d, getStart().getY());
            } else {
                double d = getStartPoint().getY();
                if (getStart().getY() > getEnd().getY())
                    d -= LINE_SEL_LENGTH;
                else
                    d += LINE_SEL_LENGTH;
                return new FloatPoint(getStart().getX(), d);
            }
        }

        private FloatPoint getMinEnd() {
            if (getType() == Ordinate.TYPE_X) {
                double d = getEndPoint().getX();
                if (getStart().getX() > getEnd().getX())
                    d += LINE_SEL_LENGTH;
                else
                    d -= LINE_SEL_LENGTH;
                return new FloatPoint(d, getEnd().getY());
            } else {
                double d = getEndPoint().getY();
                if (getStart().getY() > getEnd().getY())
                    d += LINE_SEL_LENGTH;
                else
                    d -= LINE_SEL_LENGTH;
                return new FloatPoint(getEnd().getX(), d);
            }
        }

        private void drawSelEnd(final Graphics2D g, final MovingArea movingArea) {
            final Stroke tmp = g.getStroke();
            final int x1 = movingArea.getIntOrdinate(getEnd().getX());
            final FloatPoint point = getMinEnd();
            final int x2 = movingArea.getIntOrdinate(point.getX());
            final int y1 = movingArea.getIntOrdinate(getEnd().getY());
            final int y2 = movingArea.getIntOrdinate(point.getY());
            g.setStroke(getBoldStroke());
            g.drawLine(x1, y1, x2, y2);
            g.setStroke(tmp);
        }

        private void drawSelStart(final Graphics2D g,
                                  final MovingArea movingArea) {
            final Stroke tmp = g.getStroke();
            final int x1 = movingArea.getIntOrdinate(getStart().getX());
            final FloatPoint point = getMinStart();
            final int x2 = movingArea.getIntOrdinate(point.getX());
            final int y1 = movingArea.getIntOrdinate(getStart().getY());
            final int y2 = movingArea.getIntOrdinate(point.getY());
            g.setStroke(getBoldStroke());
            g.drawLine(x1, y1, x2, y2);
            g.setStroke(tmp);
        }

        /**
         * Повертає значення сектора, на якому знаходиться частина.
         *
         * @return
         */

        public PaintSector getSector() {
            return PaintSector.this;
        }

        /**
         * Метод, який переміщує частину на пердану позицію.
         *
         * @param x Значення x.
         * @param y Значення y.
         */

        public void move(final double x, final double y) {
            if (getType() == Ordinate.TYPE_Y) {
                if (getStart().isMoveable(x, Ordinate.TYPE_X)) {
                    if (text != null && !isShowTilda()
                            && tildaPos.getPinNumber() == startPoint) {
                        final double ox = getStart().getX();
                        final double dx = x - ox;
                        final FloatPoint np = text.getLocation();
                        np.add(dx, 0);
                        text.setLocation(np);
                    }
                    double dx = x - getStart().getX();
                    getStart().setX(x);
                    if (isFirst()) {
                        Function function = PaintSector.this.sector.getStart()
                                .getFunction();
                        IDEF0Object movingFunction = movingArea
                                .getIDEF0Object(function);
                        if (movingFunction != null) {
                            getStart()
                                    .setY(movingFunction
                                            .getY(x,
                                                    PaintSector.this.sector
                                                            .getStart()
                                                            .getFunctionType() == MovingFunction.TOP,
                                                    movingFunction.getBounds()));
                            if (getStart().isMoveableDFDSRow(function)) {
                                FloatPoint floatPoint = movingFunction
                                        .getLocation();
                                floatPoint.add(dx, 0);
                                movingFunction.setLocation(floatPoint);
                            }
                        }
                    }
                    if (isEnd()) {
                        Function function = PaintSector.this.sector.getEnd()
                                .getFunction();
                        IDEF0Object movingFunction = movingArea
                                .getIDEF0Object(function);
                        if (movingFunction != null) {
                            getEnd().setY(
                                    movingFunction
                                            .getY(x,
                                                    PaintSector.this.sector
                                                            .getEnd()
                                                            .getFunctionType() == MovingFunction.TOP,
                                                    movingFunction.getBounds()));
                            if (getEnd().isMoveableDFDSRow(function)) {
                                FloatPoint floatPoint = movingFunction
                                        .getLocation();
                                floatPoint.add(dx, 0);
                                movingFunction.setLocation(floatPoint);
                            }
                        }
                    }
                }
            } else if (getStart().isMoveable(y, Ordinate.TYPE_Y)) {
                if (text != null && !isShowTilda()
                        && tildaPos.getPinNumber() == startPoint) {
                    final double oy = getStart().getY();
                    final double dy = y - oy;
                    final FloatPoint np = text.getLocation();
                    np.add(0, dy);
                    text.setLocation(np);
                }
                double dy = y - getStart().getY();
                getStart().setY(y);
                if (isFirst()) {
                    Function function = PaintSector.this.sector.getStart()
                            .getFunction();
                    IDEF0Object movingFunction = movingArea
                            .getIDEF0Object(function);
                    if (movingFunction != null) {
                        getStart()
                                .setX(movingFunction
                                        .getX(y,
                                                PaintSector.this.sector
                                                        .getStart()
                                                        .getFunctionType() == MovingFunction.LEFT,
                                                movingFunction.getBounds()));
                        if (getStart().isMoveableDFDSRow(function)) {
                            FloatPoint floatPoint = movingFunction
                                    .getLocation();
                            floatPoint.add(0, dy);
                            movingFunction.setLocation(floatPoint);
                        }
                    }
                }
                if (isEnd()) {
                    Function function = PaintSector.this.sector.getEnd()
                            .getFunction();
                    IDEF0Object movingFunction = movingArea
                            .getIDEF0Object(function);
                    if (movingFunction != null) {
                        getEnd().setX(
                                movingFunction
                                        .getX(y,
                                                PaintSector.this.sector
                                                        .getEnd()
                                                        .getFunctionType() == MovingFunction.LEFT,
                                                movingFunction.getBounds()));
                        if (getEnd().isMoveableDFDSRow(function)) {
                            FloatPoint floatPoint = movingFunction
                                    .getLocation();
                            floatPoint.add(0, dy);
                            movingFunction.setLocation(floatPoint);
                        }
                    }
                }
            }
        }

        /**
         * Повертає номер початкової точки.
         *
         * @return Номер початкової точки частини.
         */

        public int getPos() {
            return startPoint;
        }

        /**
         * Медот, який визначає довжину чатини.
         *
         * @return Довжина частини.
         */

        public double getLength() {
            if (getType() == PIN_TYPE_X)
                return Math.abs(getStart().getX() - getEnd().getX());
            return Math.abs(getStart().getY() - getEnd().getY());
        }

        /**
         * Медот визначає тип напрямку частини.
         *
         * @return MovingArea.LEFT, MovingArea.TOP...
         */

        public int getWayType() {
            if (getType() == PIN_TYPE_X) {
                if (getStart().getX() < getEnd().getX())
                    return MovingPanel.RIGHT;
                else
                    return MovingPanel.LEFT;
            } else {
                if (getStart().getY() < getEnd().getY())
                    return MovingPanel.BOTTOM;
                else
                    return MovingPanel.TOP;
            }
        }

        /**
         * Метод перевіряє, чи являється частина першою для сектора.
         *
         * @return <code>true</code> - частина перша,<br>
         * <code>false</code> - частина не перша.
         */

        public boolean isFirst() {
            return startPoint == 0;
        }

        /**
         * Метод перевіряє, чи являється частина останньою.
         *
         * @return <code>true</code> - частина остання,<br>
         * <code>false</code> - частина не остання.
         */

        public boolean isEnd() {
            return startPoint == points.length - 2;
        }

        /**
         * Метод повертає наступну зацією чатстиною частину, перевірка чи є
         * наступна за цією частина не робиться.
         */

        public Pin getNext() {
            return getPin(startPoint + 1);
        }

        /**
         * Метод повертає попередню до цієї частину, перевірка, чи існує
         * попердня до цієї чистини частина не робиться.
         */

        public Pin getPrev() {
            return getPin(startPoint - 1);
        }

        /**
         * Метод перевіряє, чи є в частини частина з протилежного боку (тільки
         * для крайніх частин)
         */

        public boolean isHaveOpposite(final boolean first) {

            final int type = getWayType();
            if (first) {
                final PaintSector[] sectors = movingArea.getRefactor()
                        .getOppozite(getSector(), getSector().getStart());
                for (final PaintSector element : sectors)
                    if (element.getPin(element.getPinCount() - 1).getWayType() == type)
                        return true;
            } else {
                final PaintSector[] sectors = movingArea.getRefactor()
                        .getOppozite(getSector(), getSector().getEnd());
                for (final PaintSector element : sectors)
                    if (element.getPin(0).getWayType() == type)
                        return true;
            }
            return false;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Pin) {
                final Pin pin = (Pin) obj;
                return pin.getSector().equals(getSector())
                        && pin.startPoint == startPoint;
            }
            return false;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getSector().hashCode();
            result = prime * result + startPoint;
            return result;
        }

        public int getEnterWay(final Point point) {
            if (getStart().equals(point))
                return getWayType();
            return MovingPanel.getOpposite(getWayType());
        }

        public boolean isNearPinStart(final double x, final double y) {
            if (getType() == PIN_TYPE_Y)
                return isBetv(getStart().getY() - LINE_SEL_LENGTH, getStart()
                        .getY() + LINE_SEL_LENGTH, y);
            else
                return isBetv(getStart().getX() - LINE_SEL_LENGTH, getStart()
                        .getX() + LINE_SEL_LENGTH, x);
        }

        public boolean isNearPinEnd(final double x, final double y) {
            if (getType() == PIN_TYPE_Y)
                return isBetv(getEnd().getY() - LINE_SEL_LENGTH, getEnd()
                        .getY() + LINE_SEL_LENGTH, y);
            else
                return isBetv(getEnd().getX() - LINE_SEL_LENGTH, getEnd()
                        .getX() + LINE_SEL_LENGTH, x);
        }

        public Ordinate getOrdinate() {
            try {
                if (getType() == Ordinate.TYPE_X)
                    return getStart().getYOrdinate();
                return getStart().getXOrdinate();
            } catch (Exception exception) {
                Ordinate ordinate = new Ordinate(Ordinate.TYPE_X);
                List<Point> list = new ArrayList<Point>();
                for (Point point : points) {
                    for (Point point2 : point.getXOrdinate().getPoints())
                        if (!list.contains(point2))
                            list.add(point2);
                    for (Point point2 : point.getYOrdinate().getPoints())
                        if (!list.contains(point2))
                            list.add(point2);
                }
                ordinate.points = list.toArray(new Point[list.size()]);
                return ordinate;
            }
        }

        public Ordinate getPOrdinate() {
            if (getType() == Ordinate.TYPE_X)
                return getEnd().getXOrdinate();
            return getEnd().getYOrdinate();
        }

        public void onEndMove() {
            try {
                getOrdinate().setPosition(
                        FRectangle.transform(getOrdinate().getPosition(),
                                MovingArea.NET_LENGTH));
                FRectangle.disableTransform = true;
                if (getType() == Ordinate.TYPE_Y) {
                    if (isFirst()) {
                        Function function = PaintSector.this.sector.getStart()
                                .getFunction();
                        IDEF0Object movingFunction = movingArea
                                .getIDEF0Object(function);
                        if (movingFunction != null) {
                            if (getStart().isMoveableDFDSRow(function))
                                movingFunction.onProcessEndBoundsChange(Arrays
                                        .asList(getSector()));
                        }
                    }
                    if (isEnd()) {
                        Function function = PaintSector.this.sector.getEnd()
                                .getFunction();
                        IDEF0Object movingFunction = movingArea
                                .getIDEF0Object(function);
                        if (movingFunction != null) {
                            if (movingFunction != null) {
                                if (getEnd().isMoveableDFDSRow(function))
                                    movingFunction
                                            .onProcessEndBoundsChange(Arrays
                                                    .asList(getSector()));
                            }
                        }
                    }
                } else {
                    if (isFirst()) {
                        Function function = PaintSector.this.sector.getStart()
                                .getFunction();
                        IDEF0Object movingFunction = movingArea
                                .getIDEF0Object(function);
                        if (movingFunction != null) {
                            if (getStart().isMoveableDFDSRow(function))
                                movingFunction.onProcessEndBoundsChange(Arrays
                                        .asList(getSector()));
                        }
                    }
                    if (isEnd()) {
                        Function function = PaintSector.this.sector.getEnd()
                                .getFunction();
                        IDEF0Object movingFunction = movingArea
                                .getIDEF0Object(function);
                        if (movingFunction != null) {
                            if (getEnd().isMoveableDFDSRow(function))
                                movingFunction.onProcessEndBoundsChange(Arrays
                                        .asList(getSector()));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            FRectangle.disableTransform = false;
        }

    }

    ;

    public void setMovingArea(final MovingArea movingArea) {
        this.movingArea = movingArea;
    }

    public static int getOppositePinType(final int pinType) {
        if (pinType == PIN_TYPE_X)
            return PIN_TYPE_Y;
        else
            return PIN_TYPE_X;
    }

    public static PaintSector loadFromStream(final InputStream stream,
                                             final int version, final MovingArea area,
                                             final DataLoader.MemoryData memoryData, final DataPlugin dataPlugin)
            throws IOException {
        return loadFromStream(stream, version, area, memoryData, dataPlugin,
                null);
    }

    public static PaintSector loadFromSector(final int version,
                                             final MovingArea area, final DataLoader.MemoryData memoryData,
                                             final DataPlugin dataPlugin, Sector llsector) throws IOException {
        final PaintSector sector = new PaintSector(area);
        sector.sector = llsector;

        SectorPropertiesPersistent spp = sector.sector.getSectorProperties();

        sector.showTilda = spp.getShowTilda() == 1;

        sector.sector.reload();

        loadPoints(memoryData, dataPlugin, sector);

        sector.loadVisuals();

        sector.setSectorPoints();

        if (spp.getShowText() == 0)
            sector.text = null;
        else {
            sector.createText();
            if (sector.text != null) {
                FRectangle bounds = new FRectangle();
                bounds.setBounds(spp.getTextX(), spp.getTextY(),
                        spp.getTextWidth(), spp.getTextHieght());
                sector.text.setBounds(bounds);
                sector.text.setTransparent(spp.getTransparent() == 1);

                if (sector.showTilda)
                    sector.tildaPos.setPos(spp.getTildaPos());
            }

        }
        return sector;
    }

    public static PaintSector loadFromStream(final InputStream stream,
                                             final int version, final MovingArea area,
                                             final DataLoader.MemoryData memoryData,
                                             final DataPlugin dataPlugin, final GlobalId newId)
            throws IOException {
        return loadFromStreamOld(stream, version, area, memoryData, dataPlugin,
                newId);
    }

    private static void loadPoints(final DataLoader.MemoryData memoryData,
                                   final DataPlugin dataPlugin, final PaintSector sector)
            throws IOException {

        List<SectorPointPersistent> spps = sector.sector
                .getSectorPointPersistents();

        sector.points = new Point[spps.size()];

        int l = spps.size();

        for (int i = 0; i < l; i++) {
            final Point p = new Point();
            p.load(memoryData, spps.get(i));
            sector.points[i] = p;
        }
    }

    public static PaintSector loadFromStreamOld(final InputStream stream,
                                                final int version, final MovingArea area,
                                                final DataLoader.MemoryData memoryData,
                                                final DataPlugin dataPlugin, final GlobalId newId)
            throws IOException {

        final PaintSector sector = new PaintSector(area);
        DataLoader.readString(stream);
        DataLoader.readString(stream);
        sector.showTilda = DataLoader.readBoolean(stream);
        final int l = DataLoader.readInteger(stream);
        sector.points = new Point[l];
        for (int i = 0; i < l; i++) {
            final Point p = new Point();
            // p.setSector(sector);
            p.loadFromStreamOld(stream, memoryData, dataPlugin);
            sector.points[i] = p;
        }

        GlobalId id = new GlobalId();
        id.loadFromStream(stream);
        if (newId != null)
            id = newId;
        sector.sector = dataPlugin.findSectorByGlobalId(id);
        if (sector.sector == null) {
            if (!DataLoader.readBoolean(stream)) {
                DataLoader.readFRectangle(stream);
                DataLoader.readBoolean(stream);
                if (sector.showTilda)
                    DataLoader.readDouble(stream);

            }

            return null;
        }
        sector.sector.reload();

        sector.loadVisuals();

        sector.setSectorPoints();
        if (DataLoader.readBoolean(stream))
            sector.text = null;
        else {
            sector.createText();
            if (sector.text == null) {
                DataLoader.readFRectangle(stream);
                DataLoader.readBoolean(stream);
                if (sector.showTilda)
                    DataLoader.readDouble(stream);
            } else {
                sector.text.setBounds(DataLoader.readFRectangle(stream));
                sector.text.setTransparent(DataLoader.readBoolean(stream));
                if (sector.showTilda)
                    sector.tildaPos.setPos(DataLoader.readDouble(stream));
            }

        }
        return sector;
    }

    private void loadVisuals() {
        final byte[] bs = sector.getVisualAttributes();
        if (bs.length == 0) {
            stroke = Options.getStroke("DEFAULT_ARROW_STROKE", stroke);
            return;
        }
        final DataLoader.MemoryData memoryData = new DataLoader.MemoryData();
        final ByteArrayInputStream is = new ByteArrayInputStream(bs);
        try {
            stroke = DataLoader.readStroke(is, memoryData);
            font = DataLoader.readFont(is, memoryData);
            color = DataLoader.readColor(is, memoryData);
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void saveVisual() {
        try {
            final DataLoader.MemoryData memoryData = new DataLoader.MemoryData();
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            DataSaver.saveStroke(os, stroke, memoryData);
            DataSaver.saveFont(os, font, memoryData);
            DataSaver.saveColor(os, color, memoryData);
            sector.setVisualAttributes(os.toByteArray());
            os.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Зберігає сектор в потік.
     *
     * @param stream Потік даних.
     * @param sector Сектор, який буде збережений.
     * @throws IOException Виникає, якщо виникла помилка запису.
     */

    public static void save(final PaintSector sector,
                            final DataLoader.MemoryData memoryData, Engine engine) {
        // sector.saveVisual();
        SectorPropertiesPersistent spp = new SectorPropertiesPersistent();
        spp.setShowTilda((sector.showTilda) ? 1 : 0);

        savePoints(sector, engine);

        if (sector.text == null)
            spp.setShowText(0);
        else {
            spp.setShowText(1);
            FRectangle bounds = sector.text.getBounds();
            spp.setTextX(bounds.getX());
            spp.setTextY(bounds.getY());
            spp.setTextWidth(bounds.getWidth());
            spp.setTextHieght(bounds.getHeight());
            spp.setTransparent(sector.text.isTransparent() ? 1 : 0);
            spp.setShowTilda(sector.showTilda ? 1 : 0);
            if (sector.showTilda)
                spp.setTildaPos(sector.tildaPos.getPos());
        }
        if (sector.sector.getCreateState() >= 0)
            sector.sector.setCreateState(-1, 0);
        sector.sector.setSectorProperties(spp);
    }

    private static void savePoints(final PaintSector sector, Engine engine) {
        List<SectorPointPersistent> list = new ArrayList<SectorPointPersistent>();
        for (final Point element : sector.points) {
            // element.saveToStreamOld(stream, memoryData);
            try {
                list.add(element.save(engine));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        sector.sector.setSectorPointPersistents(list);
    }

    /**
     * Повертає значення кольору сектора.
     *
     * @return Колір сектора.
     */

    public Color getColor() {
        return color;
    }

    /**
     * Повертає шрифт сектора.
     *
     * @return Шрифт сектора.
     */

    public Font getFont() {
        return font;
    }

    /**
     * Задає колір сектора.
     *
     * @param color Колір сектора.
     */

    public void setColor(final Color color) {
        this.color = color;
    }

    /**
     * Задає шрифт сектора.
     *
     * @param font Шрифт сектора.
     */

    public void setFont(final Font font) {
        this.font = font;
    }

    /**
     * Метод, яки намагається зробити виділиним початок, якщо це можливо.
     */

    private void trySetSelStart() {
        if (getStart() == null || !getStart().isOne(sector)
                || sector.getStart().getFunction() != null
                || sector.getStart().getBorderType() >= 0 && getEnd() != null)
            selStart = true;
    }

    /**
     * Метод, який намагається зробити виділеним кінець, якщо це можливо.
     */

    private void trySetSelEnd() {
        if (getEnd() == null || !getEnd().isOne(sector)
                || sector.getEnd().getFunction() != null
                || sector.getEnd().getBorderType() >= 0 && getStart() != null)
            selEnd = true;
    }

    /**
     * Перевіряє, чи являється диний сектор активний.
     *
     * @param area Область відображення.
     * @return true, якщо активний даний сектор або той, що наслідується, false,
     * якщо сектор не активний.
     */
    private boolean isSelected(final MovingArea area) {
        if (MovingLabel.getActiveStream() != null
                && equalsStreams(MovingLabel.getActiveStream(), getStream()))
            return true;
        if (rec)
            return false;
        final PaintSector as = area.getActiveSector();
        if (as == this)
            return true;
        rec = true;
        boolean res = false;
        if (as != null) {
            if (getStart() != null && !getStart().isOne(sector)) {
                if (isSelected(area, getStart()))
                    res = true;
            } else if (getEnd() != null && !getEnd().isOne(sector)) {
                if (isSelected(area, getEnd()))
                    res = true;
            }
        }
        if (!res && area.getActiveObject() instanceof IDEF0Object) {
            final Function active = ((IDEF0Object) area.getActiveObject())
                    .getFunction();
            if (active == sector.getStart().getFunction()
                    || active == sector.getEnd().getFunction())
                res = true;
        }
        rec = false;
        return res;
    }

    private boolean isSelected(final MovingArea area, final Crosspoint point) {
        PaintSector sector = null;
        final PaintSector[] sectors = area.getRefactor().getOppozite(this,
                point);
        if (sectors != null && sectors.length > 0)
            sector = sectors[0];
        if (sector != null)
            if (sector.getFunction().equals(getFunction()))
                return sector.isSelected(area);
        return false;
    }

    private static Stroke getBoldStroke() {
        return new BasicStroke(4);
    }

    /**
     * Перевіряє, чи знаходився курсор останній раз на початку сектора.
     *
     * @return true, якщо останній isOnSector був на початковій частині
     * початкової частини, false інакше.
     */

    public boolean isSelStart() {
        return selStart;
    }

    /**
     * Перевіряє, чи знаходився курсор останній раз в кінці сектора.
     *
     * @return true, якщо останній isOnSector був на кінцевій частині кігцевої
     * частини, false інакше.
     */

    public boolean isSelEnd() {
        return selEnd;
    }

    /**
     * Перевіряє, чи знаходиться точка на стрілці.
     *
     * @param x Координата x.
     * @param y Координата y.
     * @return Посилання на частину на якій знаходиться точка або null, якщо
     * точка не знаходиться на секторі.
     */
    public Pin isOnMe(final double x, final double y) {
        selStart = false;
        selEnd = false;
        for (int i = 0; i < getPinCount(); i++) {
            final Pin pin = getPin(i);
            if (pin.isOnMe(x, y))
                return pin;
        }
        return null;
    }

    /**
     * @return Кількість частин, з яких складається стрілка.
     */
    public int getPinCount() {
        return points.length - 1;
    }

    /**
     * Повертає чатину з номером pinPos.
     *
     * @param pinPos - позиція частини стрілки.
     * @return Частина стрілки, з позицією pinPos.
     */

    public Pin getPin(final int pinPos) {
        return new Pin(pinPos);
    }

    /**
     * Повертає кількість точок, з яких складається сектор.
     *
     * @return Кількість точок сектора.
     */
    public int getPointCount() {
        return points.length;
    }

    /**
     * Повертає точку з номером i.
     *
     * @param i Номер точки.
     * @return Точка з номером i.
     */

    public Point getPoint(final int i) {
        return points[i];
    }

    /**
     * Таблиця з відображеннями ArrowModel для різний функцій. Ключами тут
     * являються функціональні блоки на яких відображаються конкретні стрілки.
     */

    /**
     * @return Початковий вузол.
     */
    public Crosspoint getStart() {
        return sector.getStart().getCrosspoint();
    }

    /**
     * @return Кінцевий вузол.
     */

    public Crosspoint getEnd() {
        return sector.getEnd().getCrosspoint();
    }

    /**
     * Під’єднує до сектора інший сектор, переданий сектор має бути продовженням
     * першого сектора.
     *
     * @param sector Сектор, який буде під’єднаний до активного.
     */

    public void joinSector(final PaintSector sector) {
        int less;
        if (getPin(getPinCount() - 1).getType() == sector.getPin(0).getType()) {
            less = 2;
        } else {
            less = 1;
        }
        // sector.getStartPoint().remove();

        final Point[] points = new Point[this.points.length
                + sector.points.length - less];
        int i, j = 0;
        for (i = 0; i < this.points.length - less + 1; i++) {
            points[j] = this.points[i];
            j++;
        }
        for (i = 1; i < sector.points.length; i++) {
            points[j] = sector.points[i];
            j++;
        }
        this.points = points;
        this.sector.joinSector(sector.sector);

        if (less == 2) {
            getEndPoint().remove();
            sector.getStartPoint().remove();
        } else {
            sector.getStartPoint().remove();
        }

        movingArea.getRefactor().removeSector(sector);
        // sector.removeX();

    }

    /**
     * Повертає елемент класифікатора робіт, на якому розташований сектор.
     *
     * @return Елемент класифікатора робіт.
     */

    public Function getFunction() {
        return sector.getFunction();
    }

    /**
     * Метод перестворює координати точки, яка були для неї задані.
     * Використовується для розриву склеїних секторів несуміжних секторів, при
     * переключені сектора
     *
     * @param point кінцева точка, протилежна до тієї, яка переключається.
     */

    private void resetArrowPoints(final Point point) {
    }

    /**
     * Метод, який викликається після зміни початку чи кінця сектора.
     *
     * @param point Точка, яка була змінена.
     */

    private void setChangeOptions(final Point point) {
        setSectorPoints();
        createTexts();
        PaintSector[] sectors;
        Stream stream = null;
        if (getStart() != null) {
            sectors = movingArea.getRefactor().getOppozite(this, getStart());
            for (final PaintSector element : sectors)
                if (stream == null && element.getStream() != null) {
                    stream = element.getStream();
                    break;
                }
        }

        if (stream == null && getEnd() != null) {
            sectors = movingArea.getRefactor().getOppozite(this, getStart());
            for (final PaintSector element : sectors)
                if (stream == null && element.getStream() != null) {
                    stream = element.getStream();
                    break;
                }
        }

        if (stream != null && getSector().getStream() == null)
            setStream(stream, ReplaceStreamType.CHILDREN);
    }

    /**
     * Змінює початкове значення сектора, забирає сектор з необхідної точки і
     * додає сектор до всіх необхідних точок.
     *
     * @param crosspoint Початкова точка.
     * @param point      Початкова точка відображення.
     */

    public void setStart(final Crosspoint crosspoint, final Point point,
                         boolean rem) {
        Crosspoint c = getStart();
        if (rem) {
            sector.getStart().setCrosspointA(crosspoint);
            removeAther(c);
        }
        for (int i = 0; i < points.length - 1; i++)
            points[i].remove();
        points = new PointBuilder(point, getEndPoint(), sector).getPoints();
        // sector.getStart().setCrosspoint(crosspoint);
        setChangeOptions(point);
        setSectorPoints();
        resetArrowPoints(getEndPoint());
        createTexts();

        // sector.reload();
        loadVisuals();

    }

    /**
     * Змінює кінцеве значення сектора, забирає сектор з необхідної точки і
     * додає сектор до всіх необхідних точок.
     *
     * @param crosspoint Кінцева точка.
     * @param point      Кінцева точка відображення.
     */

    public void setEnd(final Crosspoint crosspoint, final Point point,
                       boolean rem) {
        Crosspoint c = getEnd();
        if (rem) {
            sector.getEnd().setCrosspointA(crosspoint);
            removeAther(c);
        }
        for (int i = 1; i < points.length; i++)
            points[i].remove();
        points = new PointBuilder(getStartPoint(), point, sector).getPoints();
        // sector.getEnd().setCrosspoint(crosspoint);
        setChangeOptions(point);
        setSectorPoints();
        resetArrowPoints(getStartPoint());
        createTexts();

        sector.reload();
        loadVisuals();
    }

    /**
     * Перевіряє і видаляє під’єднані до сектора сектори.
     */

    private void removeAther(final Crosspoint point) {
        // System.out.println("Remove other "+point);
        if (point != null) {
            Row[] del;
            if (getStream() == null)
                del = new Row[]{};
            else
                del = getStream().getAdded();
            if (!point.isDLevel()) {
                final boolean start = !point.isIn(sector);
                ((AbstractSector) sector).removeFromParent(del, start);
            }
            if (point.isRemoveable()) {
                PaintSector s = null;
                for (int i = 0; i < movingArea.getRefactor().getSectorsCount(); i++) {
                    s = movingArea.getRefactor().getSector(i);
                    if (point.isIn(s.getSector()))
                        break;
                }
                if (s == null)
                    return;
                try {
                    s.joinSector(movingArea.getRefactor().getOppozite(s, point)[0]);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                s.createTexts();
            }
        }
    }

    /**
     * Метод який видаляє сектор.
     */

    public void remove() {
        List<Sector> sectors = new ArrayList<Sector>();
        Crosspoint start = getStart();
        if (start != null) {
            for (Sector s : start.getIns())
                if (s != getSector())
                    sectors.add(s);
            for (Sector s : start.getOuts())
                if (s != getSector())
                    sectors.add(s);
        }

        Crosspoint end = getEnd();
        if (end != null) {
            for (Sector s : end.getIns())
                if (s != getSector())
                    sectors.add(s);
            for (Sector s : end.getOuts())
                if (s != getSector())
                    sectors.add(s);
        }

        final Vector<PaintSector> resRemove = new Vector<PaintSector>();
        getRemoved(getSector(), resRemove);
        if (!resRemove.contains(this))
            resRemove.add(this);
        for (int i = 0; i < resRemove.size(); i++)
            resRemove.get(i).removeX();

        for (Sector sector : sectors)
            SectorRefactor.fixOwners(sector, movingArea.dataPlugin);
    }

    public void savePointOrdinates() {
        List<Ordinate> ordinates = new ArrayList<Ordinate>();
        for (int i = 0; i < getPointCount(); i++) {
            Point point = getPoint(i);
            addOrdinate(ordinates, point.getXOrdinate());
            addOrdinate(ordinates, point.getYOrdinate());
        }
        savePointOrdinates(ordinates);
    }

    private void savePointOrdinates(List<Ordinate> ordinates) {
        List<PaintSector> paintSectors = new ArrayList<PaintSector>();
        for (Ordinate ordinate : ordinates) {
            for (Point point : ordinate.getPoints())
                if (!paintSectors.contains(point.getSector()))
                    paintSectors.add(point.getSector());
        }

        MemoryData memoryData = new MemoryData();

        Engine engine = getMovingArea().getDataPlugin().getEngine();
        for (PaintSector paintSector : paintSectors)
            save(paintSector, memoryData, engine);
    }

    private void addOrdinate(List<Ordinate> ordinates, Ordinate ordinate) {
        if (!ordinates.contains(ordinate))
            ordinates.add(ordinate);
    }

    private void getRemoved(final Sector sector,
                            final Vector<PaintSector> resRemove) {
        final PaintSector ps = movingArea.getRefactor().getPaintSector(sector);
        if (ps == null || resRemove.indexOf(ps) >= 0)
            return;
        resRemove.add(ps);
        Crosspoint c;
        c = sector.getStart().getCrosspoint();
        if (c != null && c.isOne(sector)) {
            final Sector[] sectors = c.getIns();
            for (final Sector s : sectors)
                getRemoved(s, resRemove);
        }
        c = sector.getEnd().getCrosspoint();
        if (c != null && c.isOne(sector)) {
            final Sector[] sectors = c.getOuts();
            for (final Sector s : sectors)
                getRemoved(s, resRemove);
        }
    }

    private void removeX() {
        if (rec)
            return;
        rec = true;
        for (final Point element : points)
            element.remove();
        movingArea.getRefactor().removeSector(this);
        Crosspoint start = getStart();
        Crosspoint end = getEnd();
        sector.remove();

        synchronized (movingArea.getDataPlugin()) {
            ((NSector) sector).clearMe();
        }

        removeAther(start);
        removeAther(end);
    }

    /**
     * Повертає тип тунеля для початку сектора.
     *
     * @return Crosspoint.TUNNEL_NONE - тунель відсутній;<br>
     * Crosspoint.TUNNEL_HARD - тунель показаний прямими дужками;<br>
     * Crosspoint.TUNNEL_SOFT - тунель показаний заокругленими дужками.
     */

    public int getStartTunnelType() {
        if (getStart() == null)
            return Crosspoint.TUNNEL_NONE;
        int res = sector.getStart().getTunnelType();
        Function function = sector.getFunction();
        if ((function.getParentRow() == null)
                || (function.getParentRow().toString()
                .equals("F_BASE_FUNCTIONS"))) {
            if (sector.getStart().getBorderType() >= 0)
                res = Crosspoint.TUNNEL_NONE;
        }
        if (res != Crosspoint.TUNNEL_NONE)
            if (sector.getStart().getFunction() != null
                    && (sector.getStart().getFunction().getChildCount() == 0 || sector
                    .getStart().getFunction().getType() == Function.TYPE_DFDS_ROLE))
                res = Crosspoint.TUNNEL_NONE;
        return res;
    }

    /**
     * Повертає тип тунеля для кінця сектора.
     *
     * @return Crosspoint.TUNNEL_NONE - тунель відсутній;<br>
     * Crosspoint.TUNNEL_HARD - тунель показаний прямими дужками;<br>
     * Crosspoint.TUNNEL_SOFT - тунель показаний заокругленими дужками.
     */

    public int getEndTunnelType() {
        if (getEnd() == null)
            return Crosspoint.TUNNEL_NONE;
        int res = sector.getEnd().getTunnelType();
        if ((sector.getFunction().getParentRow() == null)
                || (sector.getFunction().getParentRow().toString()
                .equals("F_BASE_FUNCTIONS"))) {
            if (sector.getEnd().getBorderType() >= 0)
                res = Crosspoint.TUNNEL_NONE;
        }
        if (res != Crosspoint.TUNNEL_NONE)
            if (sector.getEnd().getFunction() != null
                    && (sector.getEnd().getFunction().getChildCount() == 0 || sector
                    .getEnd().getFunction().getType() == Function.TYPE_DFDS_ROLE))
                res = Crosspoint.TUNNEL_NONE;

        return res;
    }

    /**
     * Малює сектор, використовуючи параметри movingArea.
     *
     * @param g          Об’єкт для малювання.
     * @param movingArea Об’єкт для завантаження параметрів.
     */

    public void paint(final Graphics2D g, final MovingArea movingArea) {
        g.setColor(getColor());
        g.setFont(getFont());
        arrowPainter = new ArrowPainter(movingArea);
        if (movingArea.getMousePin() != null
                && movingArea.getMousePin().getSector() == this
                && !selStart && !selEnd || isSelected(movingArea))
            g.setStroke(getBoldStroke());
        else {
            Stroke stroke = getStroke();

            if (stroke instanceof WayStroke) {
                final float arrowWidth = (float) movingArea
                        .getIDoubleOrdinate(ArrowPainter.ARROW_WIDTH);
                WayStroke wayStroke = (WayStroke) stroke;
                if (wayStroke.getType() == 0)
                    wayStroke.setArrowWidth(arrowWidth);
                else if (wayStroke.getType() == 1)
                    wayStroke.setArrowWidth(arrowWidth / 2);
                else
                    wayStroke.setArrowWidth(arrowWidth / 3);
            } else if (stroke instanceof ArrowedStroke) {
                final float arrowWidth = (float) movingArea
                        .getIDoubleOrdinate(ArrowPainter.ARROW_WIDTH);
                final float arrowHeight = (float) movingArea
                        .getIDoubleOrdinate(ArrowPainter.ARROW_HEIGHT);
                g.setStroke(new ArrowedStroke(arrowWidth, arrowHeight));
                ArrowedStroke arrowedStroke = (ArrowedStroke) stroke;
                if (arrowedStroke.getType() == 0) {
                    arrowedStroke.setArrowWidth(arrowWidth);
                    arrowedStroke.setArrowHeight(arrowHeight);
                } else if (arrowedStroke.getType() == 1) {
                    arrowedStroke.setArrowWidth(arrowWidth / 2);
                    arrowedStroke.setArrowHeight(arrowHeight / 2);
                } else {
                    arrowedStroke.setArrowWidth(arrowWidth / 3);
                    arrowedStroke.setArrowHeight(arrowHeight / 3);
                }
            }
            g.setStroke(stroke);
        }

        for (int i = 0; i < getPinCount(); i++)
            getPin(i).paint(g, movingArea);
        if (showTilda && text != null)
            arrowPainter.paintTilda(g, this);

        switch (getStartTunnelType()) {
            case Crosspoint.TUNNEL_SIMPLE_SOFT:
            case Crosspoint.TUNNEL_SOFT: {
                arrowPainter.paintTunnel(g, getStartPoint().getPoint(),
                        getVectorType(SectorRefactor.TYPE_START), true);
            }
            break;
            case Crosspoint.TUNNEL_HARD: {
                arrowPainter.paintTunnel(g, getStartPoint().getPoint(),
                        getVectorType(SectorRefactor.TYPE_START), false);
            }
            break;
            case Crosspoint.TUNNEL_NONE: {
                int borderType = getSector().getStart().getBorderType();
                NCrosspoint crosspoint = (NCrosspoint) getStart();
                if ((borderType >= 0) && (crosspoint != null)
                        && (crosspoint.isOneInOut())) {
                    NSector in = (NSector) crosspoint.getIn();
                    Function outer = in.getFunction();
                    NSector out = (NSector) crosspoint.getOut();
                    Function inner = out.getFunction();

                    if ((inner != null) && (inner.getParent() != null)
                            && (!inner.getParent().equals(outer))) {
                        arrowPainter.paintLink(g, getStartPoint().getPoint(),
                                getVectorType(SectorRefactor.TYPE_START),
                                inner, outer);
                    }
                }
            }
            break;
        }

        switch (getEndTunnelType()) {
            case Crosspoint.TUNNEL_SIMPLE_SOFT:
            case Crosspoint.TUNNEL_SOFT: {
                arrowPainter.paintTunnel(g, getEndPoint().getPoint(),
                        getVectorType(SectorRefactor.TYPE_END), true);
            }
            break;
            case Crosspoint.TUNNEL_HARD: {
                arrowPainter.paintTunnel(g, getEndPoint().getPoint(),
                        getVectorType(SectorRefactor.TYPE_END), false);
            }
            break;
            case Crosspoint.TUNNEL_NONE: {
                int borderType = getSector().getEnd().getBorderType();
                NCrosspoint crosspoint = (NCrosspoint) getEnd();
                if ((borderType >= 0) && (crosspoint != null)
                        && (crosspoint.isOneInOut())) {
                    Function outer = crosspoint.getOut().getFunction();
                    Function inner = crosspoint.getIn().getFunction();
                    if ((inner != null) && (inner.getParent() != null)
                            && (!inner.getParent().equals(outer))) {
                        arrowPainter.paintLink(g, getEndPoint().getPoint(),
                                getVectorType(SectorRefactor.TYPE_END), inner,
                                outer);
                    }
                }
            }
            break;
        }

    }

    public FloatPoint getTildaOPoint() {
        final MovingText movingText = getText();

        final double x2 = movingArea.getIDoubleOrdinate(movingText.getBounds()
                .getLocation().getX());
        final double y2 = movingArea.getIDoubleOrdinate(movingText.getBounds()
                .getLocation().getY());
        final double x3 = x2
                + movingArea.getIDoubleOrdinate(movingText.getBounds()
                .getWidth());
        final double y3 = y2
                + movingArea.getIDoubleOrdinate(movingText.getBounds()
                .getHeight());
        double x = getTildaPoint().getX();
        double y = getTildaPoint().getY();
        if (x3 < x)
            x = x3;
        else if (x2 > x)
            x = x2;
        if (y3 < y)
            y = y3;
        else if (y2 > y)
            y = y2;

        final FloatPoint fp = new FloatPoint(x, y);
        return fp;
    }

    private int getVectorType(final int type) {
        if (type == SectorRefactor.TYPE_START) {
            if (getSector().getStart().getFunction() != null)
                return getSector().getStart().getFunctionType();
            else
                return MovingPanel.getOpposite(getSector().getStart()
                        .getBorderType());
        } else {
            if (getSector().getEnd().getFunction() != null)
                return getSector().getEnd().getFunctionType();
            else
                return MovingPanel.getOpposite(getSector().getEnd()
                        .getBorderType());
        }
    }

    /**
     * Метод видаляє точки, які співпадають.
     *
     * @param movingArea Область відображення, з якої зчитуються налаштування.
     * @return true, якщо точки були видалені, false, якщо точки видалені не
     * були.
     */
    public boolean tryRemovePin(final MovingArea movingArea) {
        boolean res = false;
        for (int i = 2; i < points.length - 1; i++) {
            if (movingArea.isSame(points[i - 1], points[i])) {
                if (getPin(i - 2).getType() == Ordinate.TYPE_Y)
                    points[i - 2].getXOrdinate().replacePoints(
                            points[i + 1].getXOrdinate());
                else
                    points[i - 2].getYOrdinate().replacePoints(
                            points[i + 1].getYOrdinate());
                points[i - 1].remove();
                points[i].remove();
                final Point[] tmp = new Point[points.length - 2];
                for (int j = 0; j < i - 1; j++)
                    tmp[j] = points[j];
                for (int j = i + 1; j < points.length; j++)
                    tmp[j - 2] = points[j];
                points = tmp;
                res = true;
                break;
            }
        }
        /*
         * for(int i=0;i<points.length;i++){ Point point = points[i];
		 * point.xOrdinate.resetValue(); point.yOrdinate.resetValue(); }
		 */

        return res;
    }

    /**
     * Повертає значення початкової точки.
     *
     * @return Початкова точка.
     */

    public Point getStartPoint() {
        return points[0];
    }

    /**
     * Повертає кінцеву точку.
     *
     * @return Кінцева точка.
     */

    public Point getEndPoint() {
        return points[points.length - 1];
    }

    /**
     * Метод перебудовує всі точки.
     */

    public void regeneratePoints() {
        for (int i = 1; i < points.length - 1; i++)
            points[i].remove();
        points = new PointBuilder(getStartPoint(), getEndPoint(), sector)
                .getPoints();
        setSectorPoints();
    }

    public void setShowText(final boolean showText) {
        sector.setShowText(showText);
    }

    /**
     * Передає всім точкам, що вони знаходяться на цьому секторі.
     */

    private void setSectorPoints() {
        for (final Point element : points) {
            element.setSector(this);
        }
    }

    public class SplitSectorType {
        public PaintSector paintSector;

        public int type;

        public SplitSectorType(final PaintSector paintSector, final int type) {
            this.paintSector = paintSector;
            this.type = type;
        }
    }

    ;

    /**
     * Відщеплює сектор від поточного, поточний сектор урізається, а створений
     * сектор буде іти відразу після поточного, додає його на то й же
     * функціональний блок.
     *
     * @param pin        Частина, яка буде розбита на дві.
     * @param point      Точка на частині, яка буде точкою розбиття.
     * @param oX         Координата x точки секотра, який є відщеплювачем.
     * @param oY         Координата y точки секотра, який є відщеплювачем.
     * @param startPoint <code>true</code>, якщо під’єднується початок
     *                   <code>false</code>, якщо під’єднується кінець.
     * @return Новостворений сектор зверху.
     */

    public SplitSectorType splitSector(final Pin pin, final FloatPoint point,
                                       final double oX, final double oY, final double opX,
                                       final double opY, final boolean startPoint) {

        final int start = pin.getPos();
        boolean nstart = false;
        final int pinType = pin.getType();
        // Point ep = getEndPoint();
        final PaintSector sector = new PaintSector(movingArea);

        saveVisual();
        sector.setSector(this.sector.splitSector());

        final Point p = new Point();// новий кінець цього сектора
        final Point p2 = new Point();// початок новоствореного сектора
        p.setSector(this);
        p2.setSector(sector);

        // Частина створення точок.

        Point cross = null;

        if (pin.isNearPinStart(point.getX(), point.getY()) && start > 0) {
            nstart = true;
            cross = pin.getStart();
        } else if (pin.isNearPinEnd(point.getX(), point.getY())
                && start < getPinCount() - 1) {
            cross = pin.getEnd();
            nstart = false;
        }

        int moreLengthM = 2;
        int moreLengthN = 2;

        /**
         * New Point Type = напрямок нової точки.
         */

        int ct = -1;

        if ((ct = isCrossed(cross, opX, opY, startPoint)) != -1) {
            int st = start;
            if (nstart) {
                moreLengthM = 1;
                moreLengthN = 2;
            } else {
                moreLengthM = 2;
                moreLengthN = 1;
                st = start + 1;
            }

            p.setXOrdinate(points[st].getXOrdinate());
            p.setYOrdinate(points[st].getYOrdinate());
            p.setType(points[st].getType());
            p2.setXOrdinate(p.getXOrdinate());
            p2.setYOrdinate(p.getYOrdinate());
            p2.setType(getPin(st).getType());
            points[st].remove();
        } else {

            if (pinType == Ordinate.TYPE_Y) {
                p.setXOrdinate(points[start].getXOrdinate());
                p.setYOrdinate(new Ordinate(Ordinate.TYPE_Y));
                p.setY(point.getY());
                p.setType(Ordinate.TYPE_Y);
                p2.setType(Ordinate.TYPE_Y);
            } else {
                p.setYOrdinate(points[start].getYOrdinate());
                p.setXOrdinate(new Ordinate(Ordinate.TYPE_X));
                p.setX(point.getX());
                p.setType(Ordinate.TYPE_X);
                p2.setType(Ordinate.TYPE_X);
            }
        }
        p2.setXOrdinate(p.getXOrdinate());
        p2.setYOrdinate(p.getYOrdinate());
		/* поточні точки */
        final Point[] tmp = new Point[start + moreLengthM];
        for (int i = 0; i <= start; i++)
            tmp[i] = points[i];
        tmp[tmp.length - 1] = p;
		/* точки новоствореного сектора */
        final Point[] tmp2 = new Point[points.length - start - 2 + moreLengthN];
        for (int i = points.length - 1; i - start - 2 + moreLengthN > 0; i--)
            tmp2[i - start - 2 + moreLengthN] = points[i];
        tmp2[0] = p2;

        // кінець частини створення точок.

        points = tmp;
        sector.points = tmp2;
        sector.setSectorPoints();
        setSectorPoints();
        if (ct == -1) {
            if (sector.getStartPoint().getType() == Ordinate.TYPE_X)
                ct = Ordinate.TYPE_Y;
            else
                ct = Ordinate.TYPE_X;
        }

        return new SplitSectorType(sector, ct);
    }

    public void setSector(final Sector sector) {
        this.sector = sector;
        loadVisuals();
    }

    /**
     * Метод перевіряє чи може бути створений розрив між секторами на кутовій
     * точці.
     *
     * @param cross      Кутова точка
     * @param x          Координата x сектора, який буде доданий до новоствореного кута
     * @param y          Координата y сектора, який буде доданий до новоствореного кута
     * @param startPoint <code>true</code>, якщо під’єднується початок новоствореного
     *                   сектора <code>false</code>, якщо під’єднується кінець
     *                   новоствореного сектора.
     * @return PIN_TYPE_Y - сектор можу бути під’єднаний, при чому частина буде
     * паралельна осі y.<br>
     * Sector.PIN_TYPE_X - сектор можу бути під’єднаний, при чому
     * частина буде паралельна осі x.<br>
     * -1 Сектор не може бути під’єднаний.
     */

    private int isCrossed(final Point cross, final double x, final double y,
                          final boolean startPoint) {
        if (cross == null)
            return -1;
        return cross.isCanConnected(x, y, startPoint);
    }

    /**
     * Перевіряє, чи являється сектор обрубком.
     *
     * @return <b>true</b> - сектор являється обрубком, <br>
     * <b>false</b> - сектор не являється обрубком.
     */

    public boolean isPart() {
        return getStart() == null || getEnd() == null;
    }

    /**
     * Метод, який визначає довжину сектора.
     */

    protected double getLength() {
        double r = 0;
        for (int i = 0; i < getPinCount(); i++)
            r += getPin(i).getLength();
        return r;
    }

    /**
     * Медод заносить значення координати точки тільди. Координати мають
     * обов’язково лежати на секторі, інаше винекне помилка NullPointerExeption.
     *
     * @param x Координата x;
     * @param y Координата y;
     */

    public List<PaintSector> setTildaPos(final double x, final double y) {
        tildaPos.setPos(x, y);
        List<PaintSector> toSave = new ArrayList<PaintSector>();
        toSave.add(this);
        setShowTilda(true);
        final HashSet v = new HashSet();
        getConnectedSector(v);
        final PaintSector[] sectors = toArray(v);
        for (int i = 0; i < sectors.length; i++)
            if (sectors[i].text != null && sectors[i] != this) {
                sectors[i].text.setSector(this);
                text = sectors[i].text;
                sectors[i].text = null;
                sectors[i].showTilda = false;
                if (!toSave.contains(sectors[i]))
                    toSave.add(sectors[i]);
            }
        return toSave;
    }

    /**
     * Повертає тип малювання стрілки.
     *
     * @return Клас для задання типе ліній.
     */

    public Stroke getStroke() {
        return stroke;
    }

    /**
     * Задає тип для малювання ліній сектора.
     *
     * @param stroke Клас для задання типу малювання сектора.
     */

    public void setStroke(final Stroke stroke) {
        this.stroke = stroke;
    }

    public PaintSector(final Sector sector, final Point a, final Point b,
                       final MovingArea movingArea) {
        this.sector = sector;
        this.movingArea = movingArea;
        points = new PointBuilder(a, b, sector).getPoints();
        setSectorPoints();
        createTexts();

        sector.reload();
        loadVisuals();
    }

    public PaintSector(final MovingArea area, final PaintSector sector,
                       final Sector dataSector) {
        super();
        movingArea = area;
        this.sector = dataSector;
        if (sector != null) {
            points = sector.points;
            tildaPos.pos = sector.tildaPos.pos;
            showTilda = sector.showTilda;
        }
        loadVisuals();
    }

    public PaintSector() {
    }

    /**
     * Копіює візуальні параметри в інші сектори.
     *
     * @param sectors Список секторів, в які будуть скопійовані візуальні параметри.
     */

	/*
	 * private void copyVisual(Vector v) { int n = v.size(); for (int i = 0; i <
	 * n; i++) { ((PaintSector) v.get(i)).copyVisual(this); ((PaintSector)
	 * v.get(i)).copyVisual(); } }
	 * 
	 * private void copyVisual(PaintSector sector2) { basikStroke =
	 * sector2.basikStroke; }
	 */
    private void createText() {
        if (!isShowText())
            return;
        if (sector.getStream() == null && "".equals(getAlternativeText()))
            return;
		/*
		 * MovingArea movingArea = Main.getMainFrame().getIdf0Editor()
		 * .getMovingArea();
		 */
        if (getFunction().getDecompositionType() == MovingArea.DIAGRAM_TYPE_DFDS)
            text = new DFDSMovingLabel(movingArea);
        else
            text = new MovingLabel(movingArea);
        text.setSector(this);
        try {
            text.setLocation(tildaPos.getPoint());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        // text.resetBoundsX();
    }

    public MovingLabel getText() {
        return text;
    }

    public boolean isShowTilda() {
        return showTilda;
    }

    public void setShowTilda(final boolean showTilda) {
        this.showTilda = showTilda;
        if (sector != null)
            sector.setShowText(true);
    }

    public FloatPoint getTildaPoint() {
        return tildaPos.getPoint();
    }

    public boolean isTransparent() {
        if (text == null)
            return false;
        return text.isTransparent();
    }

    public void setTransparent(final boolean b) {
        if (text != null)
            text.setTransparent(b);
    }

    public PaintSector(final MovingArea area) {
        super();
        movingArea = area;
    }

    public static PaintSector[] toArray(final HashSet v) {
        return ((HashSet<PaintSector>) v).toArray(new PaintSector[v.size()]);
    }

    private static boolean isIn(final PaintSector[] sectors,
                                final PaintSector sector) {
        for (final PaintSector element : sectors)
            if (element == sector)
                return true;
        return false;
    }

    /**
     * Медот додає два масиви секторів, якщо в першому масиві присутні елементи
     * другого масиву, то сектори не додаються.
     *
     * @param a
     * @param b
     * @return
     */

    public static PaintSector[] addSectors(final PaintSector[] a,
                                           final PaintSector[] b) {
        if (b.length == 0)
            return a;
        if (a.length == 0)
            return b;
        int n = a.length + b.length;
        int i, j = 0;
        for (i = 0; i < b.length; i++)
            if (isIn(a, b[i]))
                n--;
        final PaintSector[] res = new PaintSector[n];
        for (i = 0; i < a.length; i++)
            res[i] = a[i];
        j = a.length;
        for (i = 0; i < b.length; i++)
            if (!isIn(a, b[i])) {
                res[j] = b[i];
                j++;
            }
        return res;
    }

    /**
     * Перевіряє чи присутний в масиві хоч один сетор в пов’язаний з елементом
     * відображення напису.
     */

    private boolean isHaveText(final HashSet v) {
        if (isShowText() && text != null)
            return true;
        for (PaintSector ps : ((HashSet<PaintSector>) v))
            if (ps.text != null)
                return true;
        return false;
    }

    /**
     * Метод, який створює (знищує) усі необхідні поля відображення для стрілки
     * на усіх рівнях.
     */

    public void createTexts() {
        createTexts(new HashSet<PaintSector>());
    }

    public void createTexts(HashSet<PaintSector> processed) {
        if (processed.contains(this))
            return;
        final HashSet v = new HashSet();
        getConnectedSector(v);
        processed.addAll(v);
        if (!isHaveText(v)) {
            createText();
        } else {
            final PaintSector[] strs = toArray(v);
            for (int j = strs.length - 1; j >= 0; j--)
                if (strs[j].text != null) {
                    strs[j].text.resetBoundsX();
                    for (int k = j - 1; k >= 0; k--)
                        strs[k].text = null;
                }
        }
    }

    public void getConnectedSector(final HashSet v) {
        if (sector.isShowText() && !"".equals(sector.getAlternativeText()))
            v.add(this);
        else {
            final HashSet<PaintSector> z = new HashSet<PaintSector>();
            movingArea.getRefactor().getStreamedSectors(this, z);
            for (final PaintSector s : z) {
                if ("".equals(s.getAlternativeText()))
                    v.add(s);
            }
        }
    }

    /**
     * Повертає пов’язаний з сектором елемент класифікатора потоків.
     *
     * @return Пов’язаний з сектором елемент класифікатора потоків.
     */

    public Stream getStream() {
        return sector.getStream();
    }

    /**
     * Пов’язує з сектором набір класифікаторів, класифікатори
     * додаються/віднімаються до всіх необхідних секторів.
     *
     * @param rows Набір класифікаторів, які будуть пов’язані з сектором.
     */

    public void setRows(final Row[] rows) {
        sector.setRows(rows);
    }

    /**
     * Задає об’єкт відображення назви стрілки для сектора.
     *
     * @param label Об’єкт відображення назви сектора.
     */

    public void setText(final MovingLabel label) {
        text = label;
    }

    /**
     * Задає параметри відображення для дочірніх секторів.
     */

    public void setLookForChildrens() {
        copyVisual(Sector.VISUAL_COPY_ADDED);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public void setStream(final Stream stream, ReplaceStreamType type) {
        sector.setStream(SectorRefactor.cloneStream(stream,
                movingArea.dataPlugin, sector), type);
    }

    public Sector getSector() {
        return sector;
    }

    public void copyVisual(final int type) {
        saveVisual();
        sector.copyVisual(type);
        final SectorRefactor refactor = movingArea.getRefactor();
        final int l = refactor.getSectorsCount();
        for (int i = 0; i < l; i++)
            refactor.getSector(i).loadVisuals();
    }

    public void setCorrectRows() {
        final Stream s = sector.getStream();
        if (s != null) {
            final Row[] rows = s.getAdded();
            if (rows != null) {
                setRows(rows);
            }
        } else {
            Sector[] sectors = getStart().getOppozite(sector);
            boolean start = true;
            if (sectors.length == 0) {
                sectors = getEnd().getOppozite(sector);
                start = false;
            }
            if (sectors.length > 0 && sectors[0].getStream() != null) {
                sector.setStream(SectorRefactor.cloneStream(
                        sectors[0].getStream(), movingArea.dataPlugin, sector),
                        ReplaceStreamType.CHILDREN);
                sector.loadRowAttributes(sectors[0], start);
            }
        }
    }

    public boolean isShowText() {
        return sector.isShowText();
    }

    public void setAlternativeText(String alternativeText) {
        if ("".equals(alternativeText))
            alternativeText = null;
        sector.setAlternativeText(alternativeText);
    }

    public String getAlternativeText() {
        return sector.getAlternativeText();
    }

    public void setPoints(Point[] points) {
        this.points = points;
        setSectorPoints();
    }

    public Pin getLastPin() {
        return getPin(getPinCount() - 1);
    }

    public Pin getFirstPin() {
        return getPin(0);
    }

    public MovingArea getMovingArea() {
        return movingArea;
    }

    public List<PaintSector> getOrdinateConnectedSectors() {
        List<Ordinate> ordinates = new ArrayList<Ordinate>();
        for (int i = 0; i < getPointCount(); i++) {
            Point point = getPoint(i);
            addOrdinate(ordinates, point.getXOrdinate());
            addOrdinate(ordinates, point.getYOrdinate());
        }
        List<PaintSector> paintSectors = new ArrayList<PaintSector>();
        for (Ordinate ordinate : ordinates) {
            for (Point point : ordinate.getPoints())
                if (!paintSectors.contains(point.getSector()))
                    paintSectors.add(point.getSector());
        }

        return paintSectors;
    }

    public Point[] getPoints() {
        return points;
    }
}
