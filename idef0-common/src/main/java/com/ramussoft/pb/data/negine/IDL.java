package com.ramussoft.pb.data.negine;

import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class IDL {

    protected static final double HEIGHT = 440;

    protected static final double WIDTH = 820;

    protected static final float Y_ADD = 0.015f;

    protected static final float X_ADD = 0.01f;

    protected final NDataPlugin dataPlugin;

    protected Color[] COLORS = new Color[]{Color.red, Color.green,
            Color.blue, new Color(0, 255, 255), Color.magenta, Color.yellow,
            Color.white, Color.black, Color.pink, new Color(175, 255, 175),
            new Color(175, 175, 255), new Color(64, 175, 175),
            new Color(255, 175, 255), new Color(255, 255, 175),
            new Color(64, 64, 64), new Color(175, 175, 175)};

    protected SimpleDateFormat format = new SimpleDateFormat("d/M/yyyy");

    protected NumberFormat numberFormat = NumberFormat.getNumberInstance();

    protected Vector<Font> uniqueFonts = new Vector<Font>();

    protected final String encoding;

    public IDL(NDataPlugin dataPlugin, String encoding) {
        this.dataPlugin = dataPlugin;
        this.encoding = encoding;
    }

    protected void addFont(Font font) {
        if (font != null)
            if (uniqueFonts.indexOf(font) < 0) {
                uniqueFonts.add(font);
            }
    }
}
