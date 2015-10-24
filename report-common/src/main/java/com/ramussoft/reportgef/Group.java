package com.ramussoft.reportgef;

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.List;

import com.ramussoft.reportgef.gui.Diagram;
import com.ramussoft.reportgef.gui.Side;
import com.ramussoft.reportgef.model.Bounds;
import com.ramussoft.reportgef.model.Dimension2DImpl;
import com.ramussoft.reportgef.model.QBounds;

public class Group implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 9209420633174915587L;

    protected Bounds[] bounds = new Bounds[]{};

    protected Point2D translate = new Point2D.Double();

    protected Dimension2DImpl addSize = new Dimension2DImpl(0, 0);

    protected Point2D center = null;

    protected double rotate = 0;

    public void setBounds(Bounds[] bounds) {
        this.bounds = bounds;
        clear();
    }

    protected void clear() {
        translate = new Point2D.Double(0, 0);
        center = null;
        addSize.setWidth(0);
        addSize.setHeight(0);
    }

    public Bounds[] getBounds() {
        return bounds;
    }

    public void setTranslate(Point2D translate, List<Side> dragSides,
                             Diagram diagram) {
        if (dragSides.size() == 0)
            this.translate = translate;
        else {
            Rectangle2D rect = getRectangle();

            Component component = diagram.getComponent(bounds[0]);

            double minWidth = component.getMinWidth();
            double minHieght = component.getMinHeight();

            if (dragSides.indexOf(Side.RIGHT) >= 0) {
                if (rect.getWidth() + translate.getX() > minWidth)
                    addSize.setWidth(translate.getX());
            }
            if (dragSides.indexOf(Side.BOTTOM) >= 0)
                if (rect.getHeight() + translate.getY() > minHieght)
                    addSize.setHeight(translate.getY());
            if (dragSides.indexOf(Side.TOP) >= 0) {
                if (rect.getHeight() - translate.getY() > minHieght) {
                    addSize.setHeight(-translate.getY());
                    this.translate.setLocation(this.translate.getX(), translate
                            .getY());
                }
            }
            if (dragSides.indexOf(Side.LEFT) >= 0) {
                if (rect.getWidth() - translate.getX() > minWidth) {
                    addSize.setWidth(-translate.getX());
                    this.translate.setLocation(translate.getX(), this.translate
                            .getY());
                }
            }
        }
    }

    public Point2D getTranslate() {
        return translate;
    }

    public void applyTransforms() {
        double scaleX = getScaleX();
        double scaleY = getScaleY();
        for (Bounds bounds : this.bounds) {
            if (bounds instanceof QBounds) {
                QBounds qBounds = (QBounds) bounds;
                Point2D point = qBounds.getLocation();
                qBounds.setLocation(new Point2D.Double(point.getX()
                        + translate.getX(), point.getY() + translate.getY()));
                Dimension2D d = qBounds.getSize();
                qBounds.setSize(new Dimension2DImpl(d.getWidth() * scaleX, d
                        .getHeight()
                        * scaleY));
            }
        }
        clear();
    }

    public Point2D getCenter() {
        if (center == null) {
            if (bounds.length == 0)
                center = new Point2D.Double();
            else {
                double minX = bounds[0].getLeft();
                double minY = bounds[0].getTop();
                double maxX = bounds[0].getRight();
                double maxY = bounds[0].getBottom();

                for (int i = 1; i < bounds.length; i++) {
                    Bounds bounds = this.bounds[i];
                    if (bounds.getLeft() < minX)
                        minX = bounds.getLeft();
                    if (bounds.getTop() < minY)
                        minY = bounds.getTop();
                    if (bounds.getRight() > maxX)
                        maxX = bounds.getRight();
                    if (bounds.getBottom() > maxY)
                        maxY = bounds.getBottom();
                }

                center = new Point2D.Double((minX + maxX) / 2d,
                        (minY + maxY) / 2);
            }
        }
        return center;
    }

    public Rectangle2D getRectangle() {
        double minX = bounds[0].getLeft();
        double minY = bounds[0].getTop();
        double maxX = bounds[0].getRight();
        double maxY = bounds[0].getBottom();

        for (int i = 1; i < bounds.length; i++) {
            Bounds bounds = this.bounds[i];
            if (bounds.getLeft() < minX)
                minX = bounds.getLeft();
            if (bounds.getTop() < minY)
                minY = bounds.getTop();
            if (bounds.getRight() > maxX)
                maxX = bounds.getRight();
            if (bounds.getBottom() > maxY)
                maxY = bounds.getBottom();
        }
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    public double getRotate() {
        return rotate;
    }

    public void setAddSize(Dimension2D addSize) {
        this.addSize = new Dimension2DImpl(addSize.getWidth(), addSize
                .getHeight());
    }

    public Dimension2D getAddSize() {
        return addSize;
    }

    public boolean isResizeableX(Diagram diagram) {
        return diagram.isResizeableX(this);
    }

    public boolean isResizeableY(Diagram diagram) {
        return diagram.isResizeableY(this);
    }

    public double getScaleX() {
        Rectangle2D rect = getRectangle();
        return (rect.getWidth() + getAddSize().getWidth()) / rect.getWidth();
    }

    public double getScaleY() {
        Rectangle2D rect = getRectangle();
        return (rect.getHeight() + getAddSize().getHeight()) / rect.getHeight();
    }

    public Rectangle2D translate(Rectangle2D rectangle) {
        double scaleX = getScaleX();
        double scaleY = getScaleY();
        return new Rectangle2D.Double(rectangle.getX() + translate.getX(),
                rectangle.getY() + translate.getY(), rectangle.getWidth()
                * scaleX, rectangle.getHeight() * scaleY);
    }

    public Rectangle2D getTranslateRectangle() {
        return translate(getRectangle());
    }

    public int getIndexOf(Bounds q) {
        for (int i = 0; i < this.bounds.length; i++) {
            if (q.equals(this.bounds[i]))
                return i;
        }
        return -1;
    }
}
