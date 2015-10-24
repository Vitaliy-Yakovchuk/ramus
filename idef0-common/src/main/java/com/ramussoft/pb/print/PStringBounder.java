/*
 * Created on 30/8/2005
 */
package com.ramussoft.pb.print;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import com.ramussoft.pb.idef.visual.MovingArea;

/**
 * @author ZDD
 */
public class PStringBounder {
    PFont font;

    private MovingArea area;

    public static FontRenderContext FONT_CONTEXT;

    static {
        try {
            FONT_CONTEXT = new FontRenderContext(GraphicsEnvironment
                    .getLocalGraphicsEnvironment().getDefaultScreenDevice()
                    .getDefaultConfiguration().getDefaultTransform(), false,
                    false);
        } catch (final Exception e) {
            FONT_CONTEXT = new FontRenderContext(new AffineTransform(), false,
                    false);
        }
    }

    public PStringBounder(MovingArea area) {
        this.area = area;
    }

    /**
     * @param bf The font to set.
     */
    public void setFont(final Font bf) {
        font = new PFont(bf);
    }

    /**
     * @return
     */
    public Rectangle2D getStringBounds(final String text) {
        return area.textPaintCache.getTextBounds(text, font, 10000);
    }

    /**
     * @return
     */
    public PFont getFont() {
        return font;
    }

    public class Tokanizer {
        private final String string;

        private final double maxWidth;

        private int pos;

        private final int length;

        private int pos2;

        private boolean isP(final char c) {
            return c == ' ' || c == '\n' || c == '\t' || c == '-';
        }

        private boolean isP(final int c) {
            return isP(string.charAt(c));
        }

        public Tokanizer(final String string, final double maxWidth,
                         final double d) {
            this.string = string.trim();
            this.maxWidth = maxWidth - d * maxWidth / 100;
            length = this.string.length();
            pos = 0;
        }

        public boolean hasMoreData() {
            return pos < length;
        }

        private String getNextWord() {
            String res;
            pos2 = pos;
            while (pos2 < length && !isP(string.charAt(pos2)))
                pos2++;

            if (pos2 < length && string.charAt(pos2) == '-') {
                pos2++;
            }
            ;

            res = string.substring(pos, pos2);

            if (pos2 < length)
                while (isP(pos2))
                    pos2++;
            return res;
        }

        private double getWidth(final String string) {
            return getStringBounds(string).getWidth();
        }

        public String getNext() {
            String res;
            while ((pos < length) && (isP(pos)))
                pos++;
            int i = pos;

            while (i < length && !isP(string.charAt(i)))
                i++;
            if (i < length && string.charAt(i) == '-') {
                i++;
            }

            res = string.substring(pos, i);
            if (getWidth(res) > maxWidth) {
                String res2 = res.substring(0, 1);
                if (res.length() == 1) {
                    pos++;
                    return res2;
                }
                int j = 1;
                while (getWidth(res2 + res.charAt(j)) <= maxWidth) {
                    res2 += res.charAt(j);
                    j++;
                }
                pos += j;
                return res2;
            }
            pos = i;
            if (i < length)
                while (isP(pos))
                    pos++;

            String tmp;
            while (hasMoreData()) {
                tmp = getNextWord();
                if (getWidth(tmp + res) <= maxWidth) {
                    res += (res.length() > 0
                            && res.charAt(res.length() - 1) == '-' ? "" : " ")
                            + tmp;
                    pos = pos2;
                } else
                    break;
            }
            if (pos < length)
                while (isP(pos))
                    pos++;
            if (string.equals("-"))
                return "-";
            return res;
        }
    }

    ;

    public Tokanizer getTokanizer(final String string, final double maxWidth,
                                  final double d) {
        return new Tokanizer(string, maxWidth, d);
    }

    public Rectangle2D getLinesBounds(final String lines,
                                      final Rectangle2D current) {
        return area.textPaintCache.getTextBounds(lines, font,
                (float) current.getWidth());
    }
}
