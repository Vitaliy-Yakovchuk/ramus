package com.ramussoft.pb.idef.visual;

import java.awt.Color;
import java.awt.Font;

import com.dsoft.pb.types.FRectangle;

public class VisualPanelImpl implements VisualPanel {

    private Font font;
    private Color foreground;
    private Color background;
    private FRectangle bounds;

    public VisualPanelImpl(VisualPanel source) {
        font = source.getFontA();
        foreground = source.getForegroundA();
        background = source.getBackgroundA();
        bounds = source.getBoundsA();
    }

    public void copyTo(VisualPanel dest) {
        copyTo(dest, true, true, true, true);
    }

    public void copyTo(VisualPanel dest, boolean copyBackground,
                       boolean copyForeground, boolean copyFont, boolean copySize) {
        if (copyBackground)
            dest.setBackgroundA(background);
        if (copyFont)
            dest.setFontA(font);
        if (copyForeground)
            dest.setForegroundA(foreground);
        if (copySize) {
            FRectangle bds = new FRectangle();
            bds.setX(dest.getBoundsA().getX());
            bds.setY(dest.getBoundsA().getY());
            bds.setWidth(bounds.getWidth());
            bds.setHeight(bounds.getHeight());
            dest.setBoundsA(bds);
        }
    }

    @Override
    public Color getBackgroundA() {
        return background;
    }

    @Override
    public Color getForegroundA() {
        return foreground;
    }

    @Override
    public Font getFontA() {
        return font;
    }

    @Override
    public void setBackgroundA(Color background) {
        this.background = background;
    }

    @Override
    public void setForegroundA(Color foreground) {
        this.foreground = foreground;
    }

    @Override
    public void setFontA(Font font) {
        this.font = font;
    }

    @Override
    public FRectangle getBoundsA() {
        return bounds;
    }

    @Override
    public void setBoundsA(FRectangle bounds) {
        this.bounds = bounds;
    }

}
