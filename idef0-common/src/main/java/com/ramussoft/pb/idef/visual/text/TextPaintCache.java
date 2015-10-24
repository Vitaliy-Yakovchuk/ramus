package com.ramussoft.pb.idef.visual.text;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.dsoft.pb.types.FRectangle;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.print.PStringBounder;

public class TextPaintCache {

    private HashMap<TextKey, TextBounds> complexTexts = new HashMap<TextKey, TextBounds>();

    private boolean nativePaint;

    private String searchText;

    public FRectangle paintText(final Graphics2D g, final String text,
                                final FRectangle frect, final int align, final int pos,
                                final boolean zoom, MovingArea area, boolean cached) {
        if (text.trim().length() == 0)
            return frect;

        TextKey key = new TextKey(cached);

        float width = (float) area.getDoubleOrdinate(frect.getWidth());
        if (zoom)
            width *= (float) area.zoom;

        key.width = width;
        key.text = text;
        key.font = g.getFont();

        TextBounds textBounds = getTextBounds(text, key);

        if (zoom) {
            g.scale(area.zoom, area.zoom);
            g.translate(frect.getX(), frect.getY());
        }
        if (nativePaint)
            textBounds.drawNative(g, align, pos, (float) frect.getHeight(),
                    area);
        else if (searchText == null)
            textBounds.draw(g, align, pos, (float) frect.getHeight(), area);
        else {
            if (text.toLowerCase().contains(searchText)) {
                drawSearch(text, g, align, pos, (float) frect.getHeight(),
                        area, key);
            } else
                textBounds.draw(g, align, pos, (float) frect.getHeight(), area);

        }

        if (zoom) {
            g.translate(-frect.getX(), -frect.getY());
            g.scale(1.0 / area.zoom, 1.0 / area.zoom);
            return new FRectangle(frect.getX(), frect.getY(),
                    textBounds.getWidth(), area.getDoubleOrdinate(textBounds
                    .getHeight()) * area.zoom);
        }
        return new FRectangle(frect.getX(), frect.getY(),
                textBounds.getWidth(), area.getDoubleOrdinate(textBounds
                .getHeight()));
    }

    private void drawSearch(String text, Graphics2D g, int align, int pos,
                            float fRectHeight, MovingArea area, TextKey key) {
        if (text.length() == 0) {
            return;
        }

        AttributedString attributedString = new AttributedString(text);
        attributedString.addAttribute(TextAttribute.FONT, key.font);

        AttributedCharacterIterator characterIterator = attributedString
                .getIterator();
        FontRenderContext fontRenderContext = PStringBounder.FONT_CONTEXT;// g.getFontRenderContext();
        LineBreakMeasurer measurer = new LineBreakMeasurer(characterIterator,
                BreakIterator.getLineInstance(), fontRenderContext);
        int position = measurer.getPosition();
        int end = characterIterator.getEndIndex();

        List<TextLayout> layouts = new ArrayList<TextLayout>();
        List<String> texts = new ArrayList<String>();

        float lHeight = 0;
        List<Integer> founds = new ArrayList<Integer>();
        int from = 0;
        String t = text.toLowerCase();
        while (true) {
            int index = t.indexOf(searchText, from);
            if (index >= 0) {
                founds.add(index);
                founds.add(index + searchText.length());
                from = index + searchText.length();
            } else
                break;
        }

        int[][] found = new int[founds.size()][2];

        int j = 0;

        while (position < characterIterator.getEndIndex()) {
            int index = text.indexOf((int) '\n', position + 1);
            if (index < 0)
                index = end;
            TextLayout textLayout = measurer
                    .nextLayout(key.width, index, false);
            layouts.add(textLayout);
            lHeight += textLayout.getAscent();
            lHeight += textLayout.getDescent();// + textLayout.getLeading();
            texts.add(text.substring(position, measurer.getPosition()));
            while (founds.size() > j && founds.get(j).intValue() >= position
                    && founds.get(j).intValue() <= measurer.getPosition()) {
                found[j][0] = texts.size() - 1;
                found[j][1] = founds.get(j).intValue() - position;
                j++;
            }
            position = measurer.getPosition();
        }
        TextBounds textBounds = new TextBounds();
        textBounds.setHeight(lHeight);
        textBounds.setMaxWidth(key.width);
        textBounds.setLayouts(layouts);
        textBounds.setTexts(texts);
        textBounds.drawWithSel(g, align, pos, fRectHeight, area, found);

    }

    private TextBounds getTextBounds(final String text, TextKey key) {
        if (text.length() == 0) {
            TextBounds textBounds = new TextBounds();
            textBounds.setWidth(1);
            return textBounds;
        }
        key.width = Math.round(key.width);
        TextBounds textBounds = complexTexts.get(key);

        if (textBounds == null) {

            AttributedString attributedString = new AttributedString(text);
            attributedString.addAttribute(TextAttribute.FONT, key.font);

            AttributedCharacterIterator characterIterator = attributedString
                    .getIterator();
            FontRenderContext fontRenderContext = PStringBounder.FONT_CONTEXT;// g.getFontRenderContext();
            LineBreakMeasurer measurer = new LineBreakMeasurer(
                    characterIterator, BreakIterator.getLineInstance(),
                    fontRenderContext);
            int position = measurer.getPosition();
            int end = characterIterator.getEndIndex();

            List<TextLayout> layouts = new ArrayList<TextLayout>();
            List<String> texts = new ArrayList<String>();

            float height = 0;

            while (position < characterIterator.getEndIndex()) {
                int index = text.indexOf((int) '\n', position + 1);
                if (index < 0)
                    index = end;
                TextLayout textLayout = measurer.nextLayout(key.width, index,
                        false);
                layouts.add(textLayout);
                height += textLayout.getAscent();
                height += textLayout.getDescent();// + textLayout.getLeading();
                texts.add(text.substring(position, measurer.getPosition()));
                position = measurer.getPosition();
            }
            textBounds = new TextBounds();
            textBounds.setHeight(height);
            textBounds.setMaxWidth(key.width);
            textBounds.setLayouts(layouts);
            textBounds.setTexts(texts);
            if (key.cached)
                complexTexts.put(key, textBounds);
        }
        return textBounds;
    }

    public void clear() {
        complexTexts.clear();
    }

    public Rectangle2D getTextBounds(String text, Font font, float width) {
        TextKey key = new TextKey(false);
        key.width = width;
        key.text = text;
        key.font = font;

        TextBounds textBounds = getTextBounds(text, key);

        return new Rectangle2D.Float(0, 0, textBounds.getWidth(),
                textBounds.getHeight());
    }

    public boolean isNativePaint() {
        return nativePaint;
    }

    public void setNativePaint(boolean nativePaint) {
        this.nativePaint = nativePaint;
    }

    public void setSearchText(String text) {
        if (text != null && text.trim().length() == 0)
            this.searchText = null;
        else
            this.searchText = text.toLowerCase();
    }
}
