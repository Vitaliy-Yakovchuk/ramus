package com.ramussoft.idef0.attribute;

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;

import com.dsoft.pb.types.FRectangle;

public class RectangleVisualOptions implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8184094504536766915L;

    public FRectangle bounds;

    public Color background;

    public Color foreground;

    public Font font;

}
