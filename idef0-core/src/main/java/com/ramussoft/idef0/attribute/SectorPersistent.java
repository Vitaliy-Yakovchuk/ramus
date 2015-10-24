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
     */
    @Binary(id = 4)
    public byte[] getVisualAttributes() {
        return visualAttributes;
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
