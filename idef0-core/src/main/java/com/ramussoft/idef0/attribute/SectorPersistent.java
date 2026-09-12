package com.ramussoft.idef0.attribute;

import java.util.Arrays;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Binary;
import com.ramussoft.common.persistent.Integer;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.Text;

@Table(name = "sectors")
public class SectorPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = -7625534079801884428L;

    private int createState;

    private double createPos = -1;

    private byte[] visualAttributes = new byte[]{};

    private int showText = 1;

    private String alternativeText = "";

    private int textAligment;

    private String strokeKind;

    /**
     * Обгорнуті типи, а не примітиви: у наявних файлах цих стовпчиків немає,
     * і читання віддає {@code null}. Примітивний сеттер на такому падає.
     */
    private java.lang.Double strokeWidth;

    private java.lang.Integer strokeEndCap;

    private java.lang.Integer strokeLineJoin;

    private java.lang.Double strokeDashPhase;

    private java.lang.Double strokeMiterLimit;

    private String strokeDash;

    private java.lang.Integer strokeType;

    private String fontName;

    private java.lang.Integer fontStyle;

    private java.lang.Integer fontSize;

    private java.lang.Integer color;

    /**
     * @param createState the createState to set
     */
    public void setCreateState(int createState) {
        this.createState = createState;
    }

    /**
     * @return the createState
     */
    @Integer(id = 2)
    public int getCreateState() {
        return createState;
    }

    /**
     * @param createPos the createPos to set
     */
    public void setCreatePos(double createPos) {
        this.createPos = createPos;
    }

    /**
     * @return the createPos
     */
    @com.ramussoft.common.persistent.Double(id = 3)
    public double getCreatePos() {
        return createPos;
    }

    /**
     * @param visualAttributes the visualAttributes to set
     */
    public void setVisualAttributes(byte[] visualAttributes) {
        this.visualAttributes = visualAttributes;
    }

    /**
     * @return the visualAttributes
     * @deprecated Обведення, шрифт і колір стрілки тепер зберігаються
     * окремими полями нижче. Поле лишається, щоб читати наявні файли; при
     * першому збереженні воно очищується.
     */
    @Deprecated
    @Binary(id = 4)
    public byte[] getVisualAttributes() {
        return visualAttributes;
    }

    /**
     * Різновид обведення: {@code basic}, {@code way} чи {@code arrowed}.
     * {@code null} означає, що вигляд ще не переносився зі старого
     * двійкового поля.
     */
    @Text(id = 8)
    public String getStrokeKind() {
        return strokeKind;
    }

    public void setStrokeKind(String strokeKind) {
        this.strokeKind = strokeKind;
    }

    @com.ramussoft.common.persistent.Double(id = 9)
    public java.lang.Double getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(java.lang.Double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    @Integer(id = 10)
    public java.lang.Integer getStrokeEndCap() {
        return strokeEndCap;
    }

    public void setStrokeEndCap(java.lang.Integer strokeEndCap) {
        this.strokeEndCap = strokeEndCap;
    }

    @Integer(id = 11)
    public java.lang.Integer getStrokeLineJoin() {
        return strokeLineJoin;
    }

    public void setStrokeLineJoin(java.lang.Integer strokeLineJoin) {
        this.strokeLineJoin = strokeLineJoin;
    }

    @com.ramussoft.common.persistent.Double(id = 12)
    public java.lang.Double getStrokeDashPhase() {
        return strokeDashPhase;
    }

    public void setStrokeDashPhase(java.lang.Double strokeDashPhase) {
        this.strokeDashPhase = strokeDashPhase;
    }

    @com.ramussoft.common.persistent.Double(id = 13)
    public java.lang.Double getStrokeMiterLimit() {
        return strokeMiterLimit;
    }

    public void setStrokeMiterLimit(java.lang.Double strokeMiterLimit) {
        this.strokeMiterLimit = strokeMiterLimit;
    }

    /**
     * Штрихування як перелік довжин через кому, наприклад {@code "3.0,3.0"}.
     * Один рядок замість масиву — щоб не заводити ще одну таблицю заради
     * двох чисел.
     */
    @Text(id = 14)
    public String getStrokeDash() {
        return strokeDash;
    }

    public void setStrokeDash(String strokeDash) {
        this.strokeDash = strokeDash;
    }

    /**
     * Різновид для {@code way} та {@code arrowed}.
     */
    @Integer(id = 15)
    public java.lang.Integer getStrokeType() {
        return strokeType;
    }

    public void setStrokeType(java.lang.Integer strokeType) {
        this.strokeType = strokeType;
    }

    @Text(id = 16)
    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    @Integer(id = 17)
    public java.lang.Integer getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(java.lang.Integer fontStyle) {
        this.fontStyle = fontStyle;
    }

    @Integer(id = 18)
    public java.lang.Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(java.lang.Integer fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * Колір лінії в ARGB; {@code null} — типовий.
     */
    @Integer(id = 19)
    public java.lang.Integer getColor() {
        return color;
    }

    public void setColor(java.lang.Integer color) {
        this.color = color;
    }

    /**
     * @param showText the showText to set
     */
    public void setShowText(int showText) {
        this.showText = showText;
    }

    /**
     * @return the showText
     */
    @Integer(id = 5)
    public int getShowText() {
        return showText;
    }

    /**
     * @param alternativeText the alternativeText to set
     */
    public void setAlternativeText(String alternativeText) {
        this.alternativeText = alternativeText;
    }

    /**
     * @return the alternativeText
     */
    @Text(id = 6)
    public String getAlternativeText() {
        return alternativeText;
    }

    @Integer(id = 7)
    public java.lang.Integer getTextAligment() {
        return textAligment;
    }

    public void setTextAligment(java.lang.Integer textAligment) {
        if (textAligment == null)
            textAligment = 0;
        this.textAligment = textAligment;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((alternativeText == null) ? 0 : alternativeText.hashCode());
        long temp;
        temp = Double.doubleToLongBits(createPos);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + createState;
        result = prime * result + showText;
        result = prime * result + Arrays.hashCode(visualAttributes);
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof SectorPersistent))
            return false;
        SectorPersistent other = (SectorPersistent) obj;
        if (alternativeText == null) {
            if (other.alternativeText != null)
                return false;
        } else if (!alternativeText.equals(other.alternativeText))
            return false;
        if (Double.doubleToLongBits(createPos) != Double
                .doubleToLongBits(other.createPos))
            return false;
        if (createState != other.createState)
            return false;
        if (showText != other.showText)
            return false;
        if (!Arrays.equals(visualAttributes, other.visualAttributes))
            return false;
        return true;
    }
}
