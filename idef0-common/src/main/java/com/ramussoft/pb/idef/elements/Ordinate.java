package com.ramussoft.pb.idef.elements;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;

import com.dsoft.utils.DataLoader;
import com.dsoft.utils.DataSaver;

/**
 * Клас призначений для збереження координати x або y на діаграмі. Багато точок
 * можуть використовувати одну координату, таким чином при перенесені однієї
 * точки автоматично переносяться інші точки.
 *
 * @author ZDD
 */
public class Ordinate {

    /**
     * Тип координати x (паралельно осі x).
     */

    public static final int TYPE_X = 0;

    /**
     * Тип координати y (паралельно осі y).
     */

    public static final int TYPE_Y = 1;

    /**
     * Точки які використовують дану координату.
     */

    protected Point[] points = new Point[0];

    /**
     * Позиція координати.
     */
    protected double position;

    /**
     * Тип координати (x або y).
     */

    protected int type;

    private long ordinateId = -1l;

    public Ordinate(final int type) {
        super();
        this.type = type;
    }

    /**
     * @return Позицію координати.
     */

    public double getPosition() {
        return position;
    }

    /**
     * Задає позицію координати.
     *
     * @param position Позиція координати.
     */
    public void setPosition(final double position) {
        this.position = position;
    }

    /**
     * Додає точку до координати, якщо точка вже додана, тонічого не
     * відбувається.
     *
     * @param point Точка, яка додається до координати.
     */

    public void addPoint(final Point point) {
        if (isPointPresent(point))
            return;
        final Point[] tmp = new Point[points.length + 1];
        for (int i = 0; i < points.length; i++)
            tmp[i] = points[i];
        tmp[points.length] = point;
        points = tmp;
    }

    /**
     * Водаляє точку з координати, якщо точка не була присутня, то нічого не
     * відбувається.
     *
     * @param point Точка, яка буде видалена.
     */

    public void removePoint(final Point point) {
        if (isPointPresent(point)) {
            final Point[] tmp = new Point[points.length - 1];
            for (int i = 0; i < points.length; i++) {
                if (points[i] == point) {
                    for (int j = i + 1; j < points.length; j++)
                        tmp[j - 1] = points[j];
                    break;
                } else
                    tmp[i] = points[i];
            }
            points = tmp;
        }
    }

    /**
     * Перевіряє, чи присутня точка в координаті.
     *
     * @param point Точка, наявність якої необхідно перевірити.
     * @return true, якщо точка присутня, false, якщо точка не присутня.
     */

    public boolean isPointPresent(final Point point) {
        for (final Point element : points)
            if (element == point)
                return true;
        return false;
    }

    private boolean rec = false;

    /**
     * Перевіряє, чи можливе переміщення координати.
     *
     * @param position Позиція координати.
     * @return true, якщо переміщення можливе, false, якщо переміження
     * неможливе.
     */

    public boolean isMoveable(final double position) {
        if (rec)
            return true;
        rec = true;
        boolean res = true;
        for (int i = 0; i < points.length; i++)
            if (!points[i].isMoveable(position, type))
                res = false;
        rec = false;
        return res;
    }

    /**
     * Додає точки з іншої координати, не робить перевірки на присутність такої
     * координати, заміняє значення цієї ординати в усіх точках.
     *
     * @param ordinate Координати з якої додадуться посилання на точки.
     */

    public void replacePoints(final Ordinate ordinate) {
        final Point[] points = new Point[this.points.length
                + ordinate.points.length];
        int i;
        for (i = 0; i < this.points.length; i++)
            points[i] = this.points[i];
        for (i = 0; i < ordinate.points.length; i++)
            points[i + this.points.length] = ordinate.points[i];
        for (i = 0; i < points.length; i++) {
            if (type == TYPE_X) {
                if (points[i].getXOrdinate() == this)
                    points[i].setXOrdinate(ordinate);
            } else {
                if (points[i].getYOrdinate() == this)
                    points[i].setYOrdinate(ordinate);
            }
        }
    }

    public static void saveToStream(final OutputStream stream,
                                    final Ordinate ordinate) throws IOException {
        DataSaver.saveInteger(stream, ordinate.type);
        DataSaver.saveDouble(stream, ordinate.position);
    }

    public static Ordinate loadFromStream(final InputStream stream)
            throws IOException {
        final Ordinate ordinate = new Ordinate(DataLoader.readInteger(stream));
        ordinate.setPosition(DataLoader.readDouble(stream));
        return ordinate;
    }

    public Point[] getPoints() {
        return points;
    }

    public void resetValue() {
        /*final double l = MovingArea.NET_LENGTH / 2;
        final double l1 = l / 2;
		final double t = position % l;
		if (t > l1)
			position = position + l - t;
		position = position - t;*/
    }

    public long getOrdinateId() {
        return ordinateId;
    }

    public void setOrdinateId(long ordinateId) {
        this.ordinateId = ordinateId;
    }

    @Override
    public String toString() {
        return String.valueOf(position);
    }
}
