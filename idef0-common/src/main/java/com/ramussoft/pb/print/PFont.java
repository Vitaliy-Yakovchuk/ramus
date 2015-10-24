/*
 * Created on 30/8/2005
 */
package com.ramussoft.pb.print;

import java.awt.Font;

/**
 * @author ZDD
 */
public class PFont extends Font {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param name
     * @param style
     * @param size
     */
    public PFont(final String name, final int style, final int size) {
        super(name, style, size);
    }

    /**
     * @param font
     */
    public PFont(final Font font) {
        super(font.getName(), font.getStyle(), font.getSize());
    }

    public String getHtmlTeg() {
        return "<font face=\"" + getName() + "\" size="
                + (getSize() >= 8 ? getSize() / 8 : 1) + ">";
    }

    public String getEndHtmlTeg() {
        return "</font>\n";
    }

    @Override
    public boolean isItalic() {
        return (getStyle() & Font.ITALIC) == Font.ITALIC;
    }

    @Override
    public boolean isBold() {
        return (getStyle() & Font.BOLD) == Font.BOLD;
    }
}
