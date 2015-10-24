package com.ramussoft.pb.data;

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;

import com.dsoft.pb.types.FRectangle;

public class FunctionBean implements Serializable {

    private FRectangle bounds;

    private Font font;

    private Color background;

    private Color foreground;

    private Object name;

    private int type;

    public FRectangle getBounds() {
        return new FRectangle(bounds);
    }

    public void setBounds(FRectangle bounds) {
        this.bounds = new FRectangle(bounds);
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public Color getForeground() {
        return foreground;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    public Object getName() {
        return name;
    }

    public void setName(Object name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
