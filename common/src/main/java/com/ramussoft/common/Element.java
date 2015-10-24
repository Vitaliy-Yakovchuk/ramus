package com.ramussoft.common;

import java.io.Serializable;

/**
 * Element of qualifier
 *
 * @author zdd
 */

public class Element extends Unique implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8953990752205029710L;

    private long qualifierId;

    private String name;

    public Element(long elementId, long qualifierId, String name) {
        this.id = elementId;
        this.qualifierId = qualifierId;
        this.name = (name == null) ? "" : name;
    }

    /**
     * Return name of the element, can not be null.
     */

    public String toString() {
        return getName();
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = (name == null) ? "" : name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param qualifierId the qualifierId to set
     */
    public void setQualifierId(long qualifierId) {
        this.qualifierId = qualifierId;
    }

    /**
     * @return the qualifierId
     */
    public long getQualifierId() {
        return qualifierId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (qualifierId ^ (qualifierId >>> 32));
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof Element))
            return false;
        Element other = (Element) obj;
        if (qualifierId != other.qualifierId)
            return false;
        return true;
    }


}
