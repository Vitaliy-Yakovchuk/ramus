package com.ramussoft.reportgef;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Hashtable;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.reportgef.gui.Diagram;
import com.ramussoft.reportgef.model.Background;
import com.ramussoft.reportgef.model.Bounds;
import com.ramussoft.reportgef.model.Foreground;

public abstract class Component implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6724949080153178971L;

    private static final FontRenderContext CONTEXT = new FontRenderContext(
            new AffineTransform(), false, false);

    private Hashtable<Attribute, Object> hashtable = new Hashtable<Attribute, Object>();

    protected Attribute[] attributes = new Attribute[]{};

    protected final static double MIN_WIDTH = 20;

    protected Element element;

    public Object getAttribute(Attribute attribute) {
        return hashtable.get(attribute);
    }

    public void setAttribute(Attribute attribute, Object object) {
        if (object == null)
            hashtable.remove(attribute);
        else
            hashtable.put(attribute, object);
    }

    public void updateAttribute(Attribute attribute, Object object) {
        if (object == null)
            hashtable.remove(attribute);
        else
            hashtable.put(attribute, object);
    }

    public void setAttributes(Attribute[] attributes) {
        this.attributes = attributes;
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    public abstract void paint(Graphics2D g, Bounds bounds, Diagram diagram);

    public boolean isResizeableX() {
        return true;
    }

    public boolean isResizeableY() {
        return true;
    }

    public double getMinWidth() {
        return MIN_WIDTH;
    }

    public double getMinHeight() {
        return MIN_WIDTH;
    }

    public Background getDefaultBackground() {
        return new Background();
    }

    public Foreground getDefaultForeground() {
        return new Foreground();
    }

    public Font getDefaultFont() {
        return new Font("Dialog", 0, 12);
    }

    public Color getDefaultFontColor() {
        return Color.black;
    }

    public void setBounds(Bounds bounds, Engine engine, AccessRules rules) {
    }

    public Element getElement() {
        return element;
    }

    protected Rectangle2D getStringBounds(String text, Font font) {
        return font.getStringBounds(text, CONTEXT);
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public void fillAttributes(Engine engine) {
        for (Attribute attribute : attributes) {
            setAttribute(attribute, engine.getAttribute(element, attribute));
        }
    }

    public Attribute getDefatltAttribute() {
        Attribute[] attributes = getAttributes();
        if (attributes.length > 0)
            return attributes[0];
        return null;
    }
}
