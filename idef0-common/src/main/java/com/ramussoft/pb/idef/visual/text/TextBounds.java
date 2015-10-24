package com.ramussoft.pb.idef.visual.text;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.List;

import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.print.old.Line;

public class TextBounds {

    private List<TextLayout> layouts;

    private float height;

    private float maxWidth;

    private float width = -1;

    private List<String> texts;

    private boolean in;

    public void drawNative(Graphics2D g, int align, int pos, float fRectHeight,
                           MovingArea area) {
        float left = 0;
        float fromY = 0;

        if (pos == 1)
            fromY += fRectHeight / 2 - height / 2;
        else if (pos == 2)
            fromY += fRectHeight;

        if (fromY < 0)
            fromY = 0;

        int size = layouts.size();

        for (int i = 0; i < size; i++) {
            TextLayout textLayout = layouts.get(i);
            if (align == Line.CENTER_ALIGN)
                left = (float) maxWidth / 2f - textLayout.getAdvance() / 2f;
            else if (align == Line.RIGHT_ALIGN)
                left = (float) maxWidth - textLayout.getAdvance();

            fromY += textLayout.getAscent();

            if (fromY >= fRectHeight && i + 1 < size)
                break;

            if (fromY + textLayout.getAscent() + textLayout.getDescent()
                    // + textLayout.getLeading()
                    >= fRectHeight
                    && i + 1 < size) {
                // textLayout= textLayout.getJustifiedLayout(maxWidth-20);
                // textLayout.draw(g, left, fromY);
                g.drawString(texts.get(i), left, fromY);
                /*
                 * Color color = g.getColor(); g.setColor(new Color(120, 120,
				 * 120));
				 * 
				 * g.drawString("(.)", left + textLayout.getAdvance() + (float)
				 * area.getIDoubleOrdinate(-0.0), fromY); g.setColor(color);
				 */
                break;
            } else
                g.drawString(texts.get(i), left, fromY);
            // textLayout.draw(g, left, fromY);
            fromY += textLayout.getDescent() + textLayout.getLeading();
        }
    }

    public void draw(Graphics2D g, int align, int pos, float fRectHeight,
                     MovingArea area) {
        float left = 0;
        float fromY = 0;

        if (pos == 1)
            fromY += fRectHeight / 2 - height / 2;
        else if (pos == 2)
            fromY += fRectHeight;

        if (fromY < 0)
            fromY = 0;

        int size = layouts.size();

        for (int i = 0; i < size; i++) {
            TextLayout textLayout = layouts.get(i);
            if (align == Line.CENTER_ALIGN)
                left = (float) maxWidth / 2f - textLayout.getAdvance() / 2f;
            else if (align == Line.RIGHT_ALIGN)
                left = (float) maxWidth - textLayout.getAdvance();

            fromY += textLayout.getAscent();

            if (fromY >= fRectHeight && i + 1 < size)
                break;

            if (fromY + textLayout.getAscent() + textLayout.getDescent()
                    // + textLayout.getLeading()
                    >= fRectHeight
                    && i + 1 < size) {
                // textLayout= textLayout.getJustifiedLayout(maxWidth-20);
                textLayout.draw(g, left, fromY);
				/*
				 * Color color = g.getColor(); g.setColor(new Color(120, 120,
				 * 120));
				 * 
				 * g.drawString("(.)", left + textLayout.getAdvance() + (float)
				 * area.getIDoubleOrdinate(-0.0), fromY); g.setColor(color);
				 */
                break;
            } else
                textLayout.draw(g, left, fromY);
            fromY += textLayout.getDescent() + textLayout.getLeading();
        }
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(float maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void setLayouts(List<TextLayout> layouts) {
        this.layouts = layouts;
    }

    public List<TextLayout> getLayouts() {
        return layouts;
    }

    public float getWidth() {
        if (width < 0)
            for (TextLayout textLayout : layouts)
                if (textLayout.getAdvance() > width)
                    width = textLayout.getAdvance();
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setTexts(List<String> texts) {
        this.texts = texts;
    }

    public void drawWithSel(Graphics2D g, int align, int pos,
                            float fRectHeight, MovingArea area, int[][] found) {
        float left = 0;
        float fromY = 0;

        if (pos == 1)
            fromY += fRectHeight / 2 - height / 2;
        else if (pos == 2)
            fromY += fRectHeight;

        if (fromY < 0)
            fromY = 0;

        int size = layouts.size();
        in = false;
        int selIndex = 0;

        for (int i = 0; i < size; i++) {
            TextLayout textLayout = layouts.get(i);
            if (align == Line.CENTER_ALIGN)
                left = (float) maxWidth / 2f - textLayout.getAdvance() / 2f;
            else if (align == Line.RIGHT_ALIGN)
                left = (float) maxWidth - textLayout.getAdvance();

            fromY += textLayout.getAscent();

            if (fromY >= fRectHeight && i + 1 < size)
                break;

            if (fromY + textLayout.getAscent() + textLayout.getDescent() >= fRectHeight
                    && i + 1 < size) {
                textLayout.draw(g, left, fromY);
                selIndex = drawSels(g, found, selIndex, i, textLayout, left,
                        fromY);
                break;
            } else {
                textLayout.draw(g, left, fromY);
                selIndex = drawSels(g, found, selIndex, i, textLayout, left,
                        fromY);
            }
            fromY += textLayout.getDescent() + textLayout.getLeading();
        }
    }

    private int drawSels(Graphics2D g, int[][] found, int selIndex, int i,
                         TextLayout textLayout, float left, float fromY) {
        Shape[] start = null;
        while (selIndex < found.length) {
            if (in) {
                if (start == null)
                    start = textLayout.getCaretShapes(0);
                Shape[] shapes;
                boolean b = false;
                if (found[selIndex][0] == i) {
                    shapes = textLayout.getCaretShapes(found[selIndex][1]);
                    selIndex++;
                    in = false;
                } else {
                    shapes = textLayout.getCaretShapes(texts.get(i).length());
                    b = true;
                }
                Rectangle2D s = start[0].getBounds2D();
                Rectangle2D e = shapes[0].getBounds2D();
                Rectangle2D r = new Rectangle2D.Double(s.getMinX() + left,
                        s.getMinY() + fromY, e.getMaxX() - s.getMinX(),
                        e.getMaxY() - s.getMinY());
                g.draw(r);
                if (b)
                    break;
            } else {
                if (found[selIndex][0] == i) {
                    start = textLayout.getCaretShapes(found[selIndex][1]);
                    selIndex++;
                    in = true;
                } else
                    break;
            }
        }
        return selIndex;
    }
}
