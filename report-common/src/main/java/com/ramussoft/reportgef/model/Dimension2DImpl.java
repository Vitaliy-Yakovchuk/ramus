package com.ramussoft.reportgef.model;

import java.awt.geom.Dimension2D;
import java.io.Serializable;

public class Dimension2DImpl extends Dimension2D implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2592888018354658763L;

    private double height;

    private double width;

    public Dimension2DImpl() {
    }

    public Dimension2DImpl(double width, double height) {
        this();
        setSize(width, height);
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(height);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(width);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Dimension2DImpl other = (Dimension2DImpl) obj;
        if (Double.doubleToLongBits(height) != Double
                .doubleToLongBits(other.height))
            return false;
        if (Double.doubleToLongBits(width) != Double
                .doubleToLongBits(other.width))
            return false;
        return true;
    }


}
