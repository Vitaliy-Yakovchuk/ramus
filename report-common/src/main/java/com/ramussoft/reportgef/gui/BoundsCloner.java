package com.ramussoft.reportgef.gui;

import java.awt.geom.Point2D;

import com.ramussoft.reportgef.model.ArrowBounds;
import com.ramussoft.reportgef.model.Background;
import com.ramussoft.reportgef.model.Bounds;
import com.ramussoft.reportgef.model.Foreground;
import com.ramussoft.reportgef.model.QBounds;

public class BoundsCloner {

    public static Bounds copy(Bounds bounds) {

        if (bounds instanceof QBounds) {
            QBounds qBounds = (QBounds) bounds;
            QBounds bounds2 = new QBounds();
            bounds2.setBackground(copy(qBounds.getBackground()));
            bounds2.setComponentType(qBounds.getComponentType());
            bounds2.setElementId(qBounds.getElementId());
            bounds2.setFont(qBounds.getFont());
            bounds2.setFontColor(qBounds.getFontColor());
            bounds2.setForeground(copy(qBounds.getForeground()));

            bounds2.setLinkElementId(qBounds.getLinkElementId());
            bounds2.setLocation(qBounds.getLocation());
            bounds2.setPosition(qBounds.getPosition());
            bounds2.setRotation(qBounds.getRotation());
            bounds2.setSize(qBounds.getSize());
            return bounds2;
        } else {
            ArrowBounds arrowBounds = (ArrowBounds) bounds;
            ArrowBounds bounds2 = new ArrowBounds();
            bounds2.setComponentType(arrowBounds.getComponentType());
            bounds2.setElementId(arrowBounds.getElementId());
            bounds2.setFont(arrowBounds.getFont());
            bounds2.setFontColor(arrowBounds.getFontColor());
            bounds2.setForeground(copy(arrowBounds.getForeground()));

            bounds2.setLinkElementId(arrowBounds.getLinkElementId());
            bounds2.setPosition(arrowBounds.getPosition());

            bounds2.setFromBounds(arrowBounds.getFromBounds());
            bounds2.setPoints(copy(arrowBounds.getPoints()));
            bounds2.setToBounds(arrowBounds.getToBounds());
            bounds2.setFromPreferences(arrowBounds.getFromPreferences());
            bounds2.setToPreferences(arrowBounds.getToPreferences());

            return bounds2;
        }
    }

    private static Point2D[] copy(Point2D[] points) {
        Point2D[] point2ds = new Point2D[points.length];
        for (int i = 0; i < point2ds.length; i++)
            point2ds[i] = new Point2D.Double(points[i].getX(), points[i].getY());
        return point2ds;
    }

    private static Foreground copy(Foreground foreground) {
        Foreground foreground2 = new Foreground();
        foreground2.setColor(foreground.getColor());
        foreground2.setStroke(foreground.getStroke());
        return foreground2;
    }

    private static Background copy(Background background) {
        Background background2 = new Background();
        background2.setColor(background.getColor());
        return background2;
    }

    public static Bounds[] copy(Bounds[] bounds) {
        Bounds[] bounds2 = new Bounds[bounds.length];
        for (int i = 0; i < bounds.length; i++) {
            bounds2[i] = copy(bounds[i]);
        }
        return bounds2;
    }

}
