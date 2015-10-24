package com.ramussoft.reportgef.model;

import java.awt.Color;
import java.io.Serializable;

public class Foreground implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2523621634057140625L;

    private Color color = Color.black;

    private String stroke;

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setStroke(String stroke) {
        this.stroke = stroke;
    }

    public String getStroke() {
        return stroke;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        result = prime * result + ((stroke == null) ? 0 : stroke.hashCode());
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
        Foreground other = (Foreground) obj;
        if (color == null) {
            if (other.color != null)
                return false;
        } else if (!color.equals(other.color))
            return false;
        if (stroke == null) {
            if (other.stroke != null)
                return false;
        } else if (!stroke.equals(other.stroke))
            return false;
        return true;
    }

}
