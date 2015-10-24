/*
 * Created on 31/7/2005
 */
package com.dsoft.pb.types;

/**
 * @author ZDD
 */
public class FloatPoint {
    private double x;

    private double y;

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
     *
     */
    public FloatPoint() {
    }

    public FloatPoint(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @param a
     */
    public FloatPoint(final FloatPoint a) {
        x = a.getX();
        y = a.getY();
    }

    public void add(final FloatPoint point) {
        add(point.getX(), point.getY());
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
        if (!(obj instanceof FloatPoint))
            return false;
        FloatPoint other = (FloatPoint) obj;
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
            return false;
        return true;
    }

    public FloatPoint minus(final FloatPoint m) {
        final FloatPoint res = new FloatPoint(this);
        res.x -= m.x;
        res.y -= m.y;
        return res;
    }

    /**
     * @param dx
     * @param dy
     */
    public void add(final double dx, final double dy) {
        x += dx;
        y += dy;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "X = " + x + "; Y = " + y;
    }

    public void addX(final double pos) {
        x += pos;
    }

    public void addY(final double pos) {
        y += pos;
    }

    public FloatPoint transform(int netLength) {
        return new FloatPoint(FRectangle.transform(x, netLength),
                FRectangle.transform(y, netLength));
    }
}
