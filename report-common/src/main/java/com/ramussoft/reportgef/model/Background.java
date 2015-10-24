package com.ramussoft.reportgef.model;

import java.awt.Color;
import java.io.Serializable;

public class Background implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4145394430662490770L;

    private Color color = Color.white;

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((color == null) ? 0 : color.hashCode());
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
        Background other = (Background) obj;
        if (color == null) {
            if (other.color != null)
                return false;
        } else if (!color.equals(other.color))
            return false;
        return true;
    }


}
