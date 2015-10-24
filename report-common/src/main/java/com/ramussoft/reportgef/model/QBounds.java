package com.ramussoft.reportgef.model;

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

public class QBounds extends Bounds implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4162421123609866860L;

    private Dimension2D size;

    private Point2D location;

    private double rotation;

    private Background background;

    public QBounds() {
        this(null, null);
    }

    public QBounds(Point2D point) {
        this(point, null);
    }

    public QBounds(Point2D location, Dimension2D size) {
        this.location = location;
        this.size = size;
    }

    public void setSize(Dimension2D size) {
        this.size = size;
    }

    public Dimension2D getSize() {
        return size;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getRotation() {
        return rotation;
    }

    public void setBackground(Background background) {
        this.background = background;
    }

    public Background getBackground() {
        return background;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    public Point2D getLocation() {
        return location;
    }

    public Rectangle2D getRectangle() {
        return new Rectangle2D.Double(location.getX(), location.getY(), size
                .getWidth(), size.getHeight());
    }

    public double getLeft() {
        return location.getX();
    }

    public double getTop() {
        return location.getY();
    }

    public double getRight() {
        return location.getX() + size.getWidth();
    }

    public double getBottom() {
        return location.getY() + size.getHeight();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((background == null) ? 0 : background.hashCode());
        result = prime * result
                + ((location == null) ? 0 : location.hashCode());
        long temp;
        temp = Double.doubleToLongBits(rotation);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((size == null) ? 0 : size.hashCode());
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
        QBounds other = (QBounds) obj;
        if (background == null) {
            if (other.background != null)
                return false;
        } else if (!background.equals(other.background))
            return false;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        if (Double.doubleToLongBits(rotation) != Double
                .doubleToLongBits(other.rotation))
            return false;
        if (size == null) {
            if (other.size != null)
                return false;
        } else if (!size.equals(other.size))
            return false;
        return true;
    }


}
