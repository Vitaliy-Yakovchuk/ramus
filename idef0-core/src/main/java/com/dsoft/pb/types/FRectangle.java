/*
 * Created on 27/7/2005
 */
package com.dsoft.pb.types;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

/**
 * @author ZDD
 */
public class FRectangle implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -173840812061065186L;

    double x = 0;

    double y = 0;

    double width = 0;

    double height = 0;

    public static volatile boolean disableTransform;

    /**
     * @return Returns the x.
     */
    public double getX() {
        return x;
    }

    /**
     * @return Returns the y.
     */
    public double getY() {
        return y;
    }

    /**
     * @return Returns the width.
     */
    public double getWidth() {
        return width;
    }

    /**
     * @return Returns the height.
     */
    public double getHeight() {
        return height;
    }

    /**
     * @param x The x to set.
     */
    public void setX(final double x) {
        this.x = x;
    }

    /**
     * @param y The y to set.
     */
    public void setY(final double y) {
        this.y = y;
    }

    /**
     * @param width The width to set.
     */
    public void setWidth(final double width) {
        this.width = width;
    }

    /**
     * @param height The height to set.
     */
    public void setHeight(final double height) {
        this.height = height;
    }

    public void setBounds(final double x, final double y, final double width,
                          final double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public FRectangle(final double x, final double y, final double width,
                      final double height) {
        setBounds(x, y, width, height);
    }

    public FRectangle() {
    }

    /**
     * @param bounds
     */
    public FRectangle(final FRectangle bounds) {
        x = bounds.x;
        y = bounds.y;
        width = bounds.width;
        height = bounds.height;
    }

    public FRectangle(final Rectangle2D rect) {
        x = rect.getX();
        y = rect.getY();
        width = rect.getWidth();
        height = rect.getHeight();
    }

    public FloatPoint getCenter() {
        return new FloatPoint(x + width / 2, y + height / 2);
    }

    public FloatPoint getLocation() {
        return new FloatPoint(x, y);
    }

    public double getRight() {
        return x + width;
    }

    public double getBottom() {
        return y + height;
    }

    public double getTop() {
        return getY();
    }

    public double getLeft() {
        return getX();
    }

    /**
     * @return
     */
    public Rectangle getRectangle() {
        return new Rectangle((int) x, (int) y, (int) width, (int) height);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + width + ", " + height + "]";
    }

    public void setTransformNetBounds(final int netLength) {
        //if (disableTransform)
        //	return;
        x = transform(x, netLength);
        y = transform(y, netLength);
        width = transform(width, netLength);
        height = transform(height, netLength);
        /*
         * x = x - x%netLength; y = y - y%netLength; width = width -
		 * width%netLength; height = height - height%netLength;
		 */
    }

    public static double transform(final double a, final int netLength) {
        //if (disableTransform)
        //	return a;
        final int d = netLength / 2;
        final double t = a % netLength;
        if (t > d)
            return a + netLength - t;
        return a - t;
    }

    public void setTransformNetBoundsMax(final int netLength) {
        //if (disableTransform)
        //	return;
        x = transform(x, netLength);
        y = transform(y, netLength);
        width = transformM(width, netLength);
        height = transformM(height, netLength);
    }

    private double transformM(final double a, final int netLength) {
        //if (disableTransform)
        //	return a;
        final double t = a % netLength;
        if (t > 0)
            return a + netLength - t;
        return a;
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
        long temp;
        temp = Double.doubleToLongBits(height);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(width);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof FRectangle))
            return false;
        FRectangle other = (FRectangle) obj;
        if (Double.doubleToLongBits(height) != Double
                .doubleToLongBits(other.height))
            return false;
        if (Double.doubleToLongBits(width) != Double
                .doubleToLongBits(other.width))
            return false;
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
            return false;
        return true;
    }

    public FloatPoint getLeftBottom() {
        return new FloatPoint(getLeft(), getBottom());
    }

    public FRectangle zoom(double percent) {
        FRectangle rectangle = new FRectangle(x * percent, y * percent, width
                * percent, height * percent);
        return rectangle;
    }

    public boolean contains(FRectangle rectangle) {
        return contains(rectangle.x, rectangle.y, rectangle.width,
                rectangle.height);
    }

    public boolean contains(double x, double y, double w, double h) {
        double x0 = this.x;
        double y0 = this.y;
        return (x >= x0 && y >= y0 && (x + w) <= x0 + this.width && (y + h) <= y0
                + this.height);
    }

    public boolean intersects(FRectangle rectangle) {
        return intersects(rectangle.x, rectangle.y, rectangle.width,
                rectangle.height);
    }

    public boolean intersects(double x, double y, double w, double h) {
        double x0 = getX();
        double y0 = getY();
        return (x + w > x0 && y + h > y0 && x < x0 + getWidth() && y < y0
                + getHeight());
    }
}
