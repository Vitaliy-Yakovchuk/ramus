package com.ramussoft.reportgef.model;

import java.awt.geom.Point2D;
import java.util.Arrays;

public class ArrowBounds extends Bounds {

    /**
     *
     */
    private static final long serialVersionUID = -2893572918641993205L;

    private Point2D[] points = new Point2D[]{};

    private long fromBounds;

    private long toBounds;

    private String fromPreferences;

    private String toPreferences;

    public void setPoints(Point2D[] points) {
        this.points = points;
    }

    public Point2D[] getPoints() {
        return points;
    }

    public void setFromBounds(long fromBounds) {
        this.fromBounds = fromBounds;
    }

    public long getFromBounds() {
        return fromBounds;
    }

    public void setToBounds(long toBounds) {
        this.toBounds = toBounds;
    }

    public long getToBounds() {
        return toBounds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (fromBounds ^ (fromBounds >>> 32));
        result = prime * result + Arrays.hashCode(points);
        result = prime * result + (int) (toBounds ^ (toBounds >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ArrowBounds other = (ArrowBounds) obj;
        if (fromBounds != other.fromBounds)
            return false;
        if (!Arrays.equals(points, other.points))
            return false;
        if (toBounds != other.toBounds)
            return false;
        return true;
    }

    @Override
    public double getBottom() {
        if (points.length <= 0)
            return 0;
        double bottom = points[0].getY();
        for (int i = 1; i < points.length; i++) {
            if (bottom < points[i].getY())
                bottom = points[i].getY();
        }
        return bottom;
    }

    @Override
    public double getLeft() {
        if (points.length <= 0)
            return 0;
        double left = points[0].getX();
        for (int i = 1; i < points.length; i++) {
            if (left > points[i].getX())
                left = points[i].getX();
        }
        return left;
    }

    @Override
    public double getRight() {
        if (points.length <= 0)
            return 0;
        double right = points[0].getY();
        for (int i = 1; i < points.length; i++) {
            if (right < points[i].getY())
                right = points[i].getY();
        }
        return right;
    }

    @Override
    public double getTop() {
        if (points.length <= 0)
            return 0;
        double top = points[0].getY();
        for (int i = 1; i < points.length; i++) {
            if (top > points[i].getY())
                top = points[i].getY();
        }
        return top;
    }

    public void setFromPreferences(String fromPreferences) {
        this.fromPreferences = fromPreferences;
    }

    public String getFromPreferences() {
        return fromPreferences;
    }

    public void setToPreferences(String toPreferences) {
        this.toPreferences = toPreferences;
    }

    public String getToPreferences() {
        return toPreferences;
    }

}
