package com.ramussoft.pb.idef.elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import com.dsoft.pb.types.FloatPoint;
import com.dsoft.utils.DataLoader;
import com.dsoft.utils.DataSaver;
import com.ramussoft.common.Engine;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.attribute.SectorPointPersistent;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.data.SectorBorder;
import com.ramussoft.pb.idef.elements.PaintSector.Pin;
import com.ramussoft.pb.idef.visual.IDEF0Object;
import com.ramussoft.pb.idef.visual.MovingPanel;
import com.ramussoft.pb.types.GlobalId;

/**
 * Клас точка призначений для обробки точок на яких відображається стрілка.
 *
 * @author ZDD
 */
public class Point {

    public static final int LEFT = MovingPanel.LEFT;

    public static final int RIGHT = MovingPanel.RIGHT;

    public static final int TOP = MovingPanel.TOP;

    public static final int BOTTOM = MovingPanel.BOTTOM;

    /**
     * Змінна призначена для захисту від рекурсивного виклику методів.
     */

    private boolean rec = false;

    /**
     * Посилання на сектор, на якому розташована точка.
     */

    private PaintSector sector = null;

    /**
     * Тип точки, пряма виходить паралельно x або y.
     */

    private int type = -1;

    /**
     * Координата точки x
     */

    protected Ordinate xOrdinate = null;

    /**
     * Координата точки y
     */

    protected Ordinate yOrdinate = null;

    /**
     * Простий конструктор.
     */

    public Point() {
        super();
    }

    /**
     * Заносить значення сектора на якому розташована точка.
     *
     * @param sector Сектор, на якому розташована точка (не може бути null).
     */

    public void setSector(final PaintSector sector) {
        if (sector == null)
            System.out.println("Erorr");
        this.sector = sector;
    }

    private boolean isStart() {
        try {
            return sector.getPoint(0).equals(this);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        // В цьому місці був (може є ) глюк, якщо метод викликається ще до
        // задання сектора.
    }

    private boolean isEnd() {
        return sector.getPoint(sector.getPointCount() - 1).equals(this);
    }

    /**
     * Конструктор, який ініціалузує значення x та y координати.
     *
     * @param xOrdinate
     * @param yOrdinate
     */

    public Point(final Ordinate xOrdinate, final Ordinate yOrdinate) {
        this();
        setXOrdinate(xOrdinate);
        setYOrdinate(yOrdinate);
    }

    /**
     * Повертає значення типу точки (Ordinate.TYPE_X - пряма, що виходить з
     * точки йде паралельно осі x, Ordinate.TYPE_Y - пряма, що виходить з точки
     * йде паралельно осі y).
     *
     * @return Тип точки.
     */

    public int getType() {
        if (type >= 0)
            return type;
        final int type = getBorderType();
        if (type >= 0) {
            if (type == LEFT || type == RIGHT)
                return Ordinate.TYPE_X;
            else
                return Ordinate.TYPE_Y;
        }
        return this.type;
    }

    private int getBorderType() {
        if (isStart())
            return sector.getSector().getStart().getBorderType();
        else if (isEnd())
            return sector.getSector().getEnd().getBorderType();
        return -1;
    }

    /**
     * Заносить значення типу точки(TYPE_X - пряма, що виходить з точки йде
     * паралельно осі x, TYPE_Y - пряма, що виходить з точки йде паралельно осі
     * y).
     *
     * @param type Тип точки.
     */

    public void setType(final int type) {
        this.type = type;
    }

    /**
     * @return Позиції x;
     */

    public double getX() {
        return xOrdinate.getPosition();
    }

    /**
     * @return Позицію y.
     */

    public double getY() {
        return yOrdinate.getPosition();
    }

    /**
     * @return Координату x.
     */

    public Ordinate getXOrdinate() {
        return xOrdinate;
    }

    /**
     * @return Координату y.
     */

    public Ordinate getYOrdinate() {
        return yOrdinate;
    }

    /**
     * Заностить координату y.
     *
     * @param ordinate Координата y.
     */

    public void setYOrdinate(final Ordinate ordinate) {
        if (yOrdinate != null)
            yOrdinate.removePoint(this);
        yOrdinate = ordinate;
        yOrdinate.addPoint(this);
    }

    /**
     * Заностить координату y.
     *
     * @param ordinate Координата x.
     */

    public void setXOrdinate(final Ordinate ordinate) {
        if (xOrdinate != null)
            xOrdinate.removePoint(this);
        xOrdinate = ordinate;
        xOrdinate.addPoint(this);
    }

    /**
     * Перевіряє, чи можливе переміщення координати.
     *
     * @param position Позиція координати.
     * @param type     Тип координати (TYPE_X або TYPE_Y).
     * @return true, якщо переміщення можливе, false, якщо переміження
     * неможливе.
     */
    public boolean isMoveable(final double position, final int type) {
        if (rec)
            return true;
        boolean res = isMoveablePoint(position, type);
        if (!res)
            return false;
        rec = true;
        if (type == Ordinate.TYPE_X)
            res = xOrdinate.isMoveable(position);
        else
            res = yOrdinate.isMoveable(position);
        rec = false;
        return res;
    }

    /**
     * Метод перевіряє, чи можливе переміщення саме цієї точки.
     *
     * @param position Нова позиція координати.
     * @param type     Тип координати, переміщення якої має відбуватись.
     * @return true, якщо перміщення можливе, false, якщо переміщення не
     * можливе.
     */

    private boolean isMoveablePoint(final double position, final int type) {
        if (sector == null)
            return true;
        switch (getPointType()) {
            case SectorBorder.TYPE_BORDER:
                return isMoveableBorderPoint(position, type);
            case SectorBorder.TYPE_FUNCTION:
                return isMoveableFunctionPoint(position, type);
            case SectorBorder.TYPE_SPOT:
                return isMoveableSpotPoint(position, type);
        }
        return false;
    }

    /**
     * Метод визначає тип точки, чи знаходиться вона на краю, чи висить в
     * повітрі...
     *
     * @return SectorBorder.TYPE_BORDER - точка знаходиться на краю контексту
     * функції,<br>
     * SectorBorder.TYPE_FUNCTION - точка знаходиться на краю
     * функціонального блоку<br>
     * SectorBorder.TYPE_SPOT - точка знаходиться на перетині секторів.
     */

    private int getPointType() {
        if (isStart())
            return sector.getSector().getStart().getType();
        else if (isEnd())
            return sector.getSector().getEnd().getType();
        return SectorBorder.TYPE_SPOT;
    }

    /**
     * Метод перевіряє, чи можливе переміщення саме цієї точки, якщо точка
     * розташовна на границі.
     *
     * @param position Нова позиція координати.
     * @param type     Тип координати, переміщення якої має відбуватись.
     * @return true, якщо перміщення можливе, false, якщо переміщення не
     * можливе.
     */

    private boolean isMoveableBorderPoint(final double position, final int type) {
        return true;
    }

    /**
     * Метод перевіряє, чи можливе переміщення саме цієї точки, якщо точка
     * розташована на краю функціонального блоку.
     *
     * @param position Нова позиція координати.
     * @param type     Тип координати, переміщення якої має відбуватись.
     * @return true, якщо перміщення можливе, false, якщо переміщення не
     * можливе.
     */

    private boolean isMoveableFunctionPoint(final double position,
                                            final int type) {
        int funType;
        Function fun;
        if (isStart()) {
            funType = sector.getSector().getStart().getFunctionType();
            fun = sector.getSector().getStart().getFunction();
        } else {
            funType = sector.getSector().getEnd().getFunctionType();
            fun = sector.getSector().getEnd().getFunction();
        }
        if (isMoveableDFDSRow(fun))
            return true;

        IDEF0Object function = sector.getMovingArea().getIDEF0Object(fun);

        if (funType == MovingPanel.LEFT || funType == MovingPanel.RIGHT) {
            if (type == Ordinate.TYPE_Y)
                return position >= function.getBounds().getTop()
                        && position <= function.getBounds().getBottom();
            else {
                if (funType == MovingPanel.LEFT)
                    return position > sector.getPoint(
                            sector.getPointCount() - 2).getX();
                else
                    return position < sector.getPoint(1).getX();
            }
        } else {
            if (type == Ordinate.TYPE_X)
                return position >= function.getBounds().getLeft()
                        && position <= function.getBounds().getRight();
            else {
                if (funType == MovingPanel.TOP)
                    return position > sector.getPoint(
                            sector.getPointCount() - 2).getY();
                else
                    return position < sector.getPoint(
                            sector.getPointCount() - 2).getY();
            }
        }
    }

    public boolean isMoveableDFDSRow(Function function) {
        if (!getSector().getMovingArea().isMousePinWidthCtrl())
            return false;
        return function.getType() == Function.TYPE_DFDS_ROLE
                && function.getOwner() == null;
    }

    /**
     * Метод перевіряє, чи можливе переміщення саме цієї точки, якщо точка
     * висить в повітрі.
     *
     * @param position Нова позиція координати.
     * @param type     Тип координати, переміщення якої має відбуватись.
     * @return true, якщо перміщення можливе, false, якщо переміщення не
     * можливе.
     */

    private boolean isMoveableSpotPoint(final double position, final int type) {

        int i;
        for (i = 1; i < sector.getPointCount() - 1; i++)
            if (sector.getPoint(i) == this)
                break;
        if (i == 1
                && type == Ordinate.TYPE_X
                && sector.getSector().getStart().getFunctionType() == MovingPanel.RIGHT
                && sector.getSector().getStart().getFunction() != null)
            return position > sector.getStartPoint().getX();
        if (i == 1
                && type == Ordinate.TYPE_X
                && sector.getSector().getStart().getFunctionType() == MovingPanel.LEFT
                && sector.getSector().getStart().getFunction() != null)
            return position < sector.getStartPoint().getX();
        else if (i == sector.getPointCount() - 2
                && sector.getSector().getEnd().getFunction() != null) {
            if (sector.getSector().getEnd().getFunctionType() == MovingPanel.LEFT
                    && type == Ordinate.TYPE_X) {
                return position < sector.getEndPoint().getX();
            } else if (sector.getSector().getEnd().getFunctionType() == MovingPanel.TOP
                    && type == Ordinate.TYPE_Y) {
                return position < sector.getEndPoint().getY();
            } else if (sector.getSector().getEnd().getFunctionType() == MovingPanel.BOTTOM
                    && type == Ordinate.TYPE_Y) {
                return position > sector.getEndPoint().getY();
            }
        }

        if (type != this.type)
            return true;
        if (sector.isPart())
            return true;
        if (sector.getStartPoint() == this) {
            final Point p = sector.getPoint(1);
            if (type == Ordinate.TYPE_X) {
                return (p.getX() - position) * (p.getX() - getX()) > 0;
            } else {
                return (p.getY() - position) * (p.getY() - getY()) > 0;
            }
        } else if (sector.getEndPoint() == this) {
            final Point p = sector.getPoint(sector.getPointCount() - 2);
            if (type == Ordinate.TYPE_X) {
                return (p.getX() - position) * (p.getX() - getX()) > 0;
            } else {
                return (p.getY() - position) * (p.getY() - getY()) > 0;
            }
        }
        return true;
    }

    /**
     * Задає координату x точки (в відповідну координату).
     *
     * @param x Координата x.
     */

    public void setX(final double x) {
        xOrdinate.setPosition(x);
    }

    /**
     * Задає координату x точки (в відповідну координату).
     *
     * @param y Координата y.
     */

    public void setY(final double y) {
        yOrdinate.setPosition(y);
    }

    /**
     * Метод має викликатись, кожного разу для видалення точки.
     */

    public void remove() {
        xOrdinate.removePoint(this);
        yOrdinate.removePoint(this);
    }

    public void load(final DataLoader.MemoryData memoryData,
                     SectorPointPersistent persistent) throws IOException {
        xOrdinate = memoryData.getXOrdinate(persistent);
        yOrdinate = memoryData.getYOrdinate(persistent);
        yOrdinate.addPoint(this);
        xOrdinate.addPoint(this);
        type = persistent.getPointType();
    }

    public void loadFromStreamOld(final InputStream stream,
                                  final DataLoader.MemoryData memoryData, DataPlugin dataPlugin)
            throws IOException {
        xOrdinate = DataLoader.readOrdinate(stream, memoryData);
        yOrdinate = DataLoader.readOrdinate(stream, memoryData);
        yOrdinate.addPoint(this);
        xOrdinate.addPoint(this);
        type = DataLoader.readInteger(stream);
        // borderTypeO =
        DataLoader.readInteger(stream);// old
        final GlobalId id = new GlobalId();
        id.loadFromStream(stream);//
        if (id.getLocalId() >= 0) {// old
            // functionO = (Function)
            dataPlugin.findRowByGlobalId(id);
            // functionTypeO =
            DataLoader.readInteger(stream);
        }
    }

    public void saveToStreamOld(final OutputStream stream,
                                final DataLoader.MemoryData memoryData) throws IOException {
        DataSaver.saveOrdinate(stream, xOrdinate, memoryData);
        DataSaver.saveOrdinate(stream, yOrdinate, memoryData);
        DataSaver.saveInteger(stream, type);
        DataSaver.saveInteger(stream, -1);// old
        DataSaver.saveInteger(stream, -1);// old
    }

    public SectorPointPersistent save(Engine engine) {
        SectorPointPersistent persistent = new SectorPointPersistent();

        if (xOrdinate.getOrdinateId() < 0l)
            xOrdinate.setOrdinateId(IDEF0Plugin.getNextOrdinateId(engine));

        if (yOrdinate.getOrdinateId() < 0l)
            yOrdinate.setOrdinateId(IDEF0Plugin.getNextOrdinateId(engine));

        persistent.setXOrdinateId(xOrdinate.getOrdinateId());
        persistent.setYOrdinateId(yOrdinate.getOrdinateId());

        persistent.setXPosition(xOrdinate.getPosition());
        persistent.setYPosition(yOrdinate.getPosition());

        persistent.setPointType(type);

        return persistent;
    }

    /**
     * Метод повертає точку розташування координат.
     */

    public FloatPoint getPoint() {
        return new FloatPoint(getX(), getY());
    }

    /**
     * Медот повертає набір частин сектора, тільки на секторі, на якаму
     * розташована точка.
     *
     * @return Набір частин сектора на якому розташована точка.
     */

    private PaintSector.Pin[] getPins() {
        PaintSector.Pin[] pins;
        int pos = 0;
        if (sector == null) {
            System.out.println("error paintg");
            return new PaintSector.Pin[0];
        }
        while (sector.getPoint(pos) != this)
            pos++;
        if (pos == 0) {
            pins = new PaintSector.Pin[1];
            pins[0] = sector.getPin(0);
        } else if (pos == sector.getPinCount()) {
            pins = new PaintSector.Pin[1];
            pins[0] = sector.getPin(pos - 1);
        } else {
            pins = new PaintSector.Pin[2];
            pins[0] = sector.getPin(pos - 1);
            pins[1] = sector.getPin(pos);
        }
        return pins;
    }

    /**
     * Визначає та повертає набір всіх частин всіх секторів, які стикаються в
     * даній точці.
     *
     * @return Набір частин усіх секторів, на яких розташована точка.
     */

    public PaintSector.Pin[] getAllPins() {
        final Vector r = new Vector();
        final Point[] points = xOrdinate.getPoints();
        int i, j;
        for (i = 0; i < points.length; i++)
            if (points[i].equals(this)) {
                final Point p = points[i];
                final PaintSector.Pin[] pins = p.getPins();
                for (j = 0; j < pins.length; j++)
                    r.add(pins[j]);
            }
        final PaintSector.Pin[] pins = new PaintSector.Pin[r.size()];
        for (i = 0; i < pins.length; i++)
            pins[i] = (PaintSector.Pin) r.get(i);
        return pins;
    }

    /**
     * Повертає масив частин секторів, які використовують дану точку.
     *
     * @param pinsType Тип частин, масив яких буде знайдений. Sector.PIN_TYPE_X -
     *                 буде повернутий масив частин, які паралельні осі x, <br>
     *                 Sector.PIN_TYPE_Y - буде повернутий масив частин, які
     *                 паралельні осі y.
     * @return Масив частин секторів, які паралельні відповідній осі координат.
     */

    private PaintSector.Pin[] getPins(final int pinsType) {
        final Vector r = new Vector();
        final Point[] points = xOrdinate.getPoints();
        int i, j;
        for (i = 0; i < points.length; i++)
            if (points[i].equals(this)) {
                final Point p = points[i];
                final PaintSector.Pin[] pins = p.getPins();
                for (j = 0; j < pins.length; j++)
                    if (pins[j].getType() == pinsType)
                        r.add(pins[j]);
            }
        final PaintSector.Pin[] pins = new PaintSector.Pin[r.size()];
        for (i = 0; i < pins.length; i++)
            pins[i] = (PaintSector.Pin) r.get(i);
        return pins;
    }

    /**
     * Метод повертає напрямок частин, які перпендикулярні до напрямку pinType.
     *
     * @param pinType Sector.PIN_TYPE_X - буде повернутий напрямок частин, які
     *                паралельні осі y, <br>
     *                Sector.PIN_TYPE_Y - буде повернутий напрямок частин, які
     *                паралельні осі x.
     * @return MovingArea.LEFT, MovingArea.TOP..., ящо всі частини повернуті в
     * одну сторону. <br>
     * -1 - якщо є частини повернуті в різні сторони.
     */

    public int getPointWayType(final int pinType) {
        int res = -1;
        if (pinType == PaintSector.PIN_TYPE_Y) {
            final PaintSector.Pin[] pins = getPins(PaintSector.PIN_TYPE_X);
            if (pins.length > 0) {
                res = pins[0].getWayType();
                for (int i = 1; i < pins.length; i++)
                    if (res != pins[i].getWayType())
                        return -1;
            }
        } else {
            final PaintSector.Pin[] pins = getPins(PaintSector.PIN_TYPE_Y);
            if (pins.length > 0)
                res = pins[0].getWayType();
            for (int i = 1; i < pins.length; i++)
                if (res != pins[i].getWayType())
                    return -1;
        }
        return res;
    }

    private int isCanConnected(final double x, final double y,
                               final int pinType, boolean startPoint) {
        final PaintSector.Pin[] pins = getPins(pinType);
        int res = isCanConnected(x, y, pinType);
        if (res != -1) {
            if (!startPoint)
                tmpType = MovingPanel.getOpposite(tmpType);
            if (pins.length > 0) {
                final PaintSector.Pin pin = pins[0];
                if (tmpType != pin.getWayType())
                    res = -1;
            }
        }
        return res;
    }

    public int isCanConnected(final double x, final double y,
                              final boolean startPoint) {
        int res = -1;
        if (Math.abs(y - getY()) > Math.abs(x - getX())) {
            res = isCanConnected(x, y, PaintSector.PIN_TYPE_Y, startPoint);
            if (res == -1)
                res = isCanConnected(x, y, PaintSector.PIN_TYPE_X, startPoint);
        } else {
            res = isCanConnected(x, y, PaintSector.PIN_TYPE_X, startPoint);
            if (res == -1)
                res = isCanConnected(x, y, PaintSector.PIN_TYPE_Y, startPoint);
        }
        return res;
    }

    /**
     * Метод перевіряє, чи може бути доданий до вузлової точки ще один
     * вихід/вхід.
     *
     * @param ox Координата x
     * @param oy Координата y
     * @return Sector.PIN_TYPE_Y - сектор можу бути під’єднаний, при чому
     * частина буде паралельна осі y.<br>
     * Sector.PIN_TYPE_X - сектор можу бути під’єднаний, при чому
     * частина буде паралельна осі x.<br>
     * -1 Сектор не може бути під’єднаний.
     */

    public int isCanConnected(final double ox, final double oy) {

        int res = isCanConnected(ox, oy, PaintSector.PIN_TYPE_X);
        if (res == -1)
            res = isCanConnected(ox, oy, PaintSector.PIN_TYPE_Y);
        return res;
    }

    int tmpType;

    private int isCanConnected(final double x, final double y, final int pinType) {
        final PaintSector.Pin[] pins = getPins(pinType);// Набір паралельних
        // частин.
        int type;
        if (pinType == PaintSector.PIN_TYPE_Y) {
            if (y < getY())
                type = MovingPanel.TOP;
            else
                type = MovingPanel.BOTTOM;
            for (final Pin element : pins)
                if (element.getEnterWay(this) == type)
                    return -1;
        } else {
            if (x < getX())
                type = MovingPanel.LEFT;
            else
                type = MovingPanel.RIGHT;
            for (final Pin element : pins)
                if (element.getEnterWay(this) == type)
                    return -1;
        }
        tmpType = type;
        return pinType;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((xOrdinate == null) ? 0 : xOrdinate.hashCode());
        result = prime * result
                + ((yOrdinate == null) ? 0 : yOrdinate.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        final Point point = (Point) obj;
        return point.xOrdinate.equals(xOrdinate)
                && point.yOrdinate.equals(yOrdinate);
    }

    public PaintSector getSector() {
        return sector;
    }

    @Override
    public String toString() {
        return "[" + xOrdinate + ", " + yOrdinate + "]";
    }
}
