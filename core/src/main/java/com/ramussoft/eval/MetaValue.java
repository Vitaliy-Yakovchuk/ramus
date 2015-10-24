package com.ramussoft.eval;

public class MetaValue {

    private long elementId;

    private long attributeId;

    public MetaValue(long elementId, long attributeId) {
        this.elementId = elementId;
        this.attributeId = attributeId;
    }

    public long getElementId() {
        return elementId;
    }

    public long getAttributeId() {
        return attributeId;
    }

    @Override
    public String toString() {
        return Util.ELEMENT_PREFIX + elementId + Util.ATTRIBUTE_PREFIX
                + attributeId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (attributeId ^ (attributeId >>> 32));
        result = prime * result + (int) (elementId ^ (elementId >>> 32));
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
        if (!(obj instanceof MetaValue))
            return false;
        MetaValue other = (MetaValue) obj;
        if (attributeId != other.attributeId)
            return false;
        if (elementId != other.elementId)
            return false;
        return true;
    }


}
