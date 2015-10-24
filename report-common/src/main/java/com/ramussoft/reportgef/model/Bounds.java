package com.ramussoft.reportgef.model;

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;

public abstract class Bounds implements Serializable, Comparable<Bounds> {

    /**
     *
     */
    private static final long serialVersionUID = 4763143551750283622L;

    private Foreground foreground;

    private Font font;

    private Color fontColor;

    private long elementId;

    private long linkElementId;

    private String componentType;

    private int position;

    public void setForeground(Foreground foreground) {
        this.foreground = foreground;
    }

    public Foreground getForeground() {
        return foreground;
    }

    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
    }

    public Color getFontColor() {
        return fontColor;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Font getFont() {
        return font;
    }

    public void setElementId(long elementId) {
        this.elementId = elementId;
    }

    public long getElementId() {
        return elementId;
    }

    public void setLinkElementId(long linkElementId) {
        this.linkElementId = linkElementId;
    }

    public long getLinkElementId() {
        return linkElementId;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public int compareTo(Bounds o) {
        if (position < o.position)
            return -1;
        if (position > o.position)
            return 1;
        return 0;
    }

    public abstract double getLeft();

    public abstract double getTop();

    public abstract double getRight();

    public abstract double getBottom();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((componentType == null) ? 0 : componentType.hashCode());
        result = prime * result + (int) (elementId ^ (elementId >>> 32));
        result = prime * result + ((font == null) ? 0 : font.hashCode());
        result = prime * result
                + ((fontColor == null) ? 0 : fontColor.hashCode());
        result = prime * result
                + ((foreground == null) ? 0 : foreground.hashCode());
        result = prime * result
                + (int) (linkElementId ^ (linkElementId >>> 32));
        result = prime * result + position;
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
        Bounds other = (Bounds) obj;
        if (componentType == null) {
            if (other.componentType != null)
                return false;
        } else if (!componentType.equals(other.componentType))
            return false;
        if (elementId != other.elementId)
            return false;
        if (font == null) {
            if (other.font != null)
                return false;
        } else if (!font.equals(other.font))
            return false;
        if (fontColor == null) {
            if (other.fontColor != null)
                return false;
        } else if (!fontColor.equals(other.fontColor))
            return false;
        if (foreground == null) {
            if (other.foreground != null)
                return false;
        } else if (!foreground.equals(other.foreground))
            return false;
        if (linkElementId != other.linkElementId)
            return false;
        if (position != other.position)
            return false;
        return true;
    }

}
