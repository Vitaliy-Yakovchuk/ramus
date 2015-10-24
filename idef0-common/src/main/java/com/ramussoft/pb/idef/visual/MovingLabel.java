package com.ramussoft.pb.idef.visual;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

import com.dsoft.pb.types.FRectangle;
import com.dsoft.utils.DataLoader.MemoryData;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.idef.elements.PaintSector;

/**
 * Клас призначений для створення підписів стрілок.
 *
 * @author ZDD
 */

public class MovingLabel extends MovingText {

    private static Stream activeStream = null;

    protected String labelText = null;

    public static Stream getActiveStream() {
        return activeStream;
    }

    /**
     * Сектор, до якого прив’язаний напис.
     */

    protected PaintSector sector = null;

    /**
     * Конструктор по замовчуванню.
     *
     * @param movingArea Понель відображення, з якої буде зчитуватись необхідна
     *                   інформація.
     */

    public MovingLabel(final MovingArea movingArea) {
        super(movingArea);
    }

    public void setSector(final PaintSector sector) {
        this.sector = sector;

    }

    public PaintSector getSector() {
        return sector;
    }

    @Override
    public Color getColor() {
        return sector.getColor();
    }

    @Override
    public Font getFont() {
        return sector.getFont();
    }

    @Override
    public void mouseEnter() {
        super.mouseEnter();
        activeStream = sector.getStream();
    }

    @Override
    public void mouseLeave() {
        super.mouseLeave();
        activeStream = null;
    }

    @Override
    public void focusGained(boolean silent) {
        super.focusGained(silent);
        movingArea.setActiveObject(sector.getPin(0), false);
    }

    @Override
    public void focusLost() {
        super.focusLost();
        movingArea.setActiveObject(null);
    }

    @Override
    protected boolean isNegative() {
        return sector.getStream() == activeStream;
    }

    @Override
    public String getToolTipText() {
        final Stream stream = sector.getStream();
        if (stream == null)
            return super.getToolTipText();
        else
            return stream.getToolTipText(false);
    }

    @Override
    public void onProcessEndBoundsChange() {
        PaintSector.save(getSector(), new MemoryData(), movingArea
                .getDataPlugin().getEngine());
    }

    @Override
    public void onEndBoundsChange() {
        resetBounds();
        movingArea.repaint();
        movingArea.startUserTransaction();
        onProcessEndBoundsChange();
        movingArea.getRefactor().setUndoPoint();
    }

    public void resetBoundsX() {
        String s = sector.getAlternativeText();
        if ("".equals(s))
            s = sector.getSector().getName();
        setText(s);
        double t;
        boolean b = false;

        movingArea.stringBounder.setFont(getFont());

        double width = getBounds().getWidth();
        double height = getBounds().getHeight();
        if (width < (t = getMinWidth())) {
            width = t;
            b = true;
        }
        if (height < (t = getMinHeight())) {
            height = t;
            b = true;
        }
        if (b) {
            final FRectangle old = getBounds();
            final FRectangle n = new FRectangle(old);
            if (height > width) {
                width = height;
            }
            if (width > movingArea.getDoubleWidth() / 4)
                width = movingArea.getDoubleWidth() / 4;
            n.setWidth(width);
            n.setHeight(height);

            setBounds(n);
            final Rectangle2D r = getResetsBounds(n);
            setBounds(old.getX() - (r.getWidth() - old.getWidth()) / 2,
                    old.getY() - (r.getHeight() - old.getHeight()) / 2,
                    r.getWidth(), r.getHeight());
        } else
            resetBounds();
    }

    public void setText(String text) {
        labelText = text;
    }

    @Override
    public String getText() {
        if (labelText == null) {
            String s = sector.getAlternativeText();
            if ("".equals(s))
                s = sector.getSector().getName();
            setText(s);
        }
        return labelText;
    }

    public FRectangle boundsCopy;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sector == null) ? 0 : sector.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof MovingLabel))
            return false;
        MovingLabel other = (MovingLabel) obj;
        if (sector == null) {
            if (other.sector != null)
                return false;
        } else if (!sector.getSector().equals(other.sector.getSector()))
            return false;
        return true;
    }

    @Override
    public int getAlign() {
        return sector.getSector().getTextAligment();
    }

    @Override
    protected String getXText() {
        return super.getXText().replace('\n', ' ');
    }
}
