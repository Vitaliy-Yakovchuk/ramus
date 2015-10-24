package com.ramussoft.idef0.attribute;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Double;
import com.ramussoft.common.persistent.Table;

@Table(name = "rectangles")
public class FRectanglePersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = -3322780072541981227L;

    private double x;

    private double y;

    private double width;

    private double height;

    /**
     * @param x the x to set
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return the x
     */
    @Double(id = 2)
    public double getX() {
        return x;
    }

    /**
     * @param y the y to set
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * @return the y
     */
    @Double(id = 3)
    public double getY() {
        return y;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * @return the width
     */
    @Double(id = 4)
    public double getWidth() {
        return width;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * @return the height
     */
    @Double(id = 5)
    public double getHeight() {
        return height;
    }

}
