package com.ramussoft.common;

import java.io.Serializable;

public class CalculateInfo implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = 1783385321058186538L;

    private long elementId;

    private long attributeId;

    private String formula;

    private boolean autoRecalculate = true;

    public CalculateInfo() {
    }

    public CalculateInfo(long elementId, long attributeId, String formula) {
        this.elementId = elementId;
        this.attributeId = attributeId;
        this.formula = formula;
    }

    public CalculateInfo(long elementId, long attributeId, String formula,
                         boolean autoRecalculate) {
        this(elementId, attributeId, formula);
        this.autoRecalculate = autoRecalculate;
    }

    /**
     * @param elementId the elementId to set
     */
    public void setElementId(long elementId) {
        this.elementId = elementId;
    }

    /**
     * @return the elementId
     */
    public long getElementId() {
        return elementId;
    }

    /**
     * @param attributeId the attributeId to set
     */
    public void setAttributeId(long attributeId) {
        this.attributeId = attributeId;
    }

    /**
     * @return the attributeId
     */
    public long getAttributeId() {
        return attributeId;
    }

    /**
     * @param formula the formula to set
     */
    public void setFormula(String formula) {
        this.formula = formula;
    }

    /**
     * @return the formula
     */
    public String getFormula() {
        return formula;
    }

    /**
     * @param autoRecalculate the autoRecalculate to set
     */
    public void setAutoRecalculate(boolean autoRecalculate) {
        this.autoRecalculate = autoRecalculate;
    }

    /**
     * @return the autoRecalculate
     */
    public boolean isAutoRecalculate() {
        return autoRecalculate;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (attributeId ^ (attributeId >>> 32));
        result = prime * result + (autoRecalculate ? 1231 : 1237);
        result = prime * result + (int) (elementId ^ (elementId >>> 32));
        result = prime * result + ((formula == null) ? 0 : formula.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof CalculateInfo))
            return false;
        CalculateInfo other = (CalculateInfo) obj;
        if (attributeId != other.attributeId)
            return false;
        if (autoRecalculate != other.autoRecalculate)
            return false;
        if (elementId != other.elementId)
            return false;
        if (formula == null) {
            if (other.formula != null)
                return false;
        } else if (!formula.equals(other.formula))
            return false;
        return true;
    }
}
