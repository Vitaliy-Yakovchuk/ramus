package com.ramussoft.core.attribute.simple;

import java.text.MessageFormat;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.TableType;

/**
 * Persistent object for string (text) type.
 *
 * @author zdd
 */

@Table(name = "hierarchicals", type = TableType.ONE_TO_MANY)
public class HierarchicalPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = -2271188469405481682L;

    private long previousElementId;

    private long parentElementId;

    private long iconId = -1l;

    public HierarchicalPersistent() {
    }

    public HierarchicalPersistent(HierarchicalPersistent old) {
        this.previousElementId = old.previousElementId;
        this.parentElementId = old.parentElementId;
        this.iconId = old.iconId;
    }

    /**
     * Setter for previous element in table, can be -1 if this is top element on
     * its level.
     */

    public void setPreviousElementId(long previousElementId) {
        this.previousElementId = previousElementId;
    }

    /**
     * Getter for previous element in table, can be -1 if this is top element on
     * its level.
     */

    @com.ramussoft.common.persistent.Long(id = 2)
    public long getPreviousElementId() {
        return previousElementId;
    }

    /**
     * Setter for parent element id, -1, if element has 0 level.
     */

    public void setParentElementId(long parentElementId) {
        this.parentElementId = parentElementId;
    }

    /**
     * Getter for parent element id, -1, if element has 0 level.
     */

    @com.ramussoft.common.persistent.Long(id = 3)
    public long getParentElementId() {
        return parentElementId;
    }

    @Override
    public String toString() {
        return MessageFormat.format("Parent: {0}, Prev: {1}", parentElementId,
                previousElementId);
    }

    /**
     * @param iconId the iconId to set
     */
    public void setIconId(Long iconId) {
        if (iconId == null)
            this.iconId = -1l;
        else
            this.iconId = iconId;
    }

    /**
     * @return the iconId
     */
    @com.ramussoft.common.persistent.Long(id = 4)
    public Long getIconId() {
        return iconId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (iconId ^ (iconId >>> 32));
        result = prime * result
                + (int) (parentElementId ^ (parentElementId >>> 32));
        result = prime * result
                + (int) (previousElementId ^ (previousElementId >>> 32));
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
        if (!(obj instanceof HierarchicalPersistent))
            return false;
        HierarchicalPersistent other = (HierarchicalPersistent) obj;
        if (iconId != other.iconId)
            return false;
        if (parentElementId != other.parentElementId)
            return false;
        if (previousElementId != other.previousElementId)
            return false;
        return true;
    }


}
