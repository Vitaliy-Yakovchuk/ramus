package com.ramussoft.idef0.attribute;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Double;
import com.ramussoft.common.persistent.Integer;
import com.ramussoft.common.persistent.Table;

@Table(name = "sector_properties")
public class SectorPropertiesPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = -5928934431465492616L;

    private int showTilda;

    private int showText;

    private int transparent;

    private double tildaPos;

    private double textX;

    private double textY;

    private double textWidth;

    private double textHieght;

    @Integer(id = 2)
    public int getShowTilda() {
        return showTilda;
    }

    public void setShowTilda(int showTilda) {
        this.showTilda = showTilda;
    }

    @Integer(id = 3)
    public int getTransparent() {
        return transparent;
    }

    public void setTransparent(int transparent) {
        this.transparent = transparent;
    }

    @Double(id = 4)
    public double getTildaPos() {
        return tildaPos;
    }

    public void setTildaPos(double tildaPos) {
        this.tildaPos = tildaPos;
    }

    @Double(id = 5)
    public double getTextX() {
        return textX;
    }

    public void setTextX(double textX) {
        this.textX = textX;
    }

    @Double(id = 6)
    public double getTextY() {
        return textY;
    }

    public void setTextY(double textY) {
        this.textY = textY;
    }

    @Double(id = 7)
    public double getTextWidth() {
        return textWidth;
    }

    public void setTextWidth(double textWidth) {
        this.textWidth = textWidth;
    }

    @Double(id = 8)
    public double getTextHieght() {
        return textHieght;
    }

    public void setTextHieght(double textHieght) {
        this.textHieght = textHieght;
    }

    @Integer(id = 9)
    public int getShowText() {
        return showText;
    }

    public void setShowText(int showText) {
        this.showText = showText;
    }
}
