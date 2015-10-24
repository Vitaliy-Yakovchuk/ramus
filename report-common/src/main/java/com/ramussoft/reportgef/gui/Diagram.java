package com.ramussoft.reportgef.gui;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.io.Serializable;

import com.ramussoft.reportgef.Component;
import com.ramussoft.reportgef.Group;
import com.ramussoft.reportgef.model.Bounds;
import com.ramussoft.reportgef.model.Dimension2DImpl;
import com.ramussoft.reportgef.model.QBounds;

public class Diagram implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 9132092058437335726L;

    protected Component[] components = new Component[]{};

    protected Bounds[] bounds = new Bounds[]{};

    protected Dimension2D size = new Dimension2DImpl(600, 900);

    protected Diagram() {
    }

    public Component[] getComponents() {
        return components;
    }

    public Bounds[] getBounds() {
        return bounds;
    }

    public void paint(Graphics2D g) {
        paint(g, bounds, 1, 1);
    }

    private void paint(Graphics2D g, Bounds[] filter, double scaleX,
                       double scaleY) {
        int k = 0;
        for (int i = 0; i < components.length; i++)
            if (components[i] != null) {
                if (filter.length <= k)
                    return;
                Component component = components[i];
                Bounds bounds = this.bounds[i];
                if (bounds.equals(filter[k])) {
                    if (bounds instanceof QBounds) {
                        QBounds qBounds = (QBounds) bounds;
                        k++;
                        Point2D point = qBounds.getLocation();
                        g.translate(point.getX(), point.getY());
                        if (qBounds.getRotation() != 0)
                            g.rotate(qBounds.getRotation());
                        g.scale(scaleX, scaleY);
                        component.paint(g, bounds, this);
                        g.scale(1 / scaleX, 1 / scaleY);
                        if (qBounds.getRotation() != 0)
                            g.rotate(-qBounds.getRotation());
                        g.translate(-point.getX(), -point.getY());
                    }
                }
            }
    }

    public Dimension2D getSize() {
        return size;
    }

    public Dimension2D zoom(Dimension2D size, double zoom) {
        return new Dimension2DImpl(size.getWidth() * zoom, size.getHeight()
                * zoom);
    }

    public void paintGroup(Graphics2D g, Group selection) {
    }

    public Component getComponent(Bounds bounds2) {
        for (int i = 0; i < bounds.length; i++)
            if (bounds[i].equals(bounds2))
                return components[i];
        return null;
    }

    public String getType() {
        return null;
    }

    public void fill(Graphics2D g, Shape shape, QBounds bounds) {
        g.setColor(bounds.getBackground().getColor());
        /*
         * double max = Math.max(bounds.getSize().getWidth(), bounds.getSize()
		 * .getHeight()); g.setPaint(new GradientPaint(new Point2D.Double(0, 0),
		 * Color.white, new Point2D.Double(max, max), Color.lightGray));
		 */
        g.fill(shape);
    }

    public void draw(Graphics2D g, Shape shape, Bounds bounds) {
        g.setColor(bounds.getForeground().getColor());
        g.draw(shape);
    }

    public void draw(Graphics2D g, String string, double x, double y,
                     Bounds bounds) {
        g.setFont(bounds.getFont());
        g.setColor(bounds.getFontColor());
        g.drawString(string, (float) x, (float) y);
    }

    public int getIndexOfBounds(Bounds b) {
        for (int i = 0; i < bounds.length; i++)
            if (b.getElementId() == bounds[i].getElementId())
                return i;
        return -1;
    }

    public boolean isResizeableX(Group group) {
        for (Bounds b : group.getBounds()) {
            int index = getIndexOfBounds(b);
            if (index >= 0)
                if (!components[index].isResizeableX())
                    return false;
        }
        return true;
    }

    public boolean isResizeableY(Group group) {
        for (Bounds b : group.getBounds()) {
            int index = getIndexOfBounds(b);
            if (index >= 0)
                if (!components[index].isResizeableY())
                    return false;
        }
        return true;
    }

    public Component[] getComponents(Bounds[] bounds) {
        Component[] result = new Component[bounds.length];
        for (int i = 0; i < bounds.length; i++) {
            int index = getIndexOfBounds(bounds[i]);
            result[i] = components[index];
        }
        return result;
    }

    public Bounds getBounds(Component component) {
        for (int i = 0; i < bounds.length; i++) {
            if (components[i].equals(component))
                return bounds[i];
        }
        return null;
    }

    public Integer getIndexOfComponent(Component component) {
        for (int i = 0; i < bounds.length; i++) {
            if (components[i].equals(component))
                return i;
        }
        return null;
    }
}
