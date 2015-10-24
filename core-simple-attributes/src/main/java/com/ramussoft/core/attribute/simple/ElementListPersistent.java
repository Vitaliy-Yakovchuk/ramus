package com.ramussoft.core.attribute.simple;

import com.ramussoft.common.attribute.Attribute;
import com.ramussoft.common.persistent.Element;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.Table;
import com.ramussoft.common.persistent.TableType;
import com.ramussoft.common.persistent.Text;

/**
 * Persistent object for string (text) type.
 *
 * @author zdd
 */

@Table(name = "element_lists", type = TableType.MANY_TO_MANY)
public class ElementListPersistent extends Persistent implements
        Comparable<ElementListPersistent> {

    /**
     *
     */
    private static final long serialVersionUID = -2271188469405481682L;

    private long attribute = -1;

    private long element1 = -1;

    private long element2 = -1;

    private String connectionType;

    public void setAttributeId(long attribute) {
        this.attribute = attribute;
    }

    @Attribute(id = 1, autoset = true, primary = true)
    public long getAttributeId() {
        return attribute;
    }

    public void setElement1Id(long element1) {
        this.element1 = element1;
    }

    @Element(id = 2, autoset = false, primary = true)
    public long getElement1Id() {
        return element1;
    }

    public ElementListPersistent() {
    }

    public ElementListPersistent(long element1, long element2) {
        this.element1 = element1;
        this.element2 = element2;
    }

    /**
     * @param element2 the otherElement to set
     */
    public void setElement2Id(long element2) {
        this.element2 = element2;
    }

    /**
     * @return the otherElement
     */
    @Element(id = 3, autoset = false, primary = true)
    public long getElement2Id() {
        return element2;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((connectionType == null) ? 0 : connectionType.hashCode());
        result = prime * result + (int) (element1 ^ (element1 >>> 32));
        result = prime * result + (int) (element2 ^ (element2 >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ElementListPersistent other = (ElementListPersistent) obj;
        if (connectionType == null) {
            if (other.connectionType != null)
                return false;
        } else if (!connectionType.equals(other.connectionType))
            return false;
        if (element1 != other.element1)
            return false;
        if (element2 != other.element2)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return (connectionType == null) ? "" : connectionType;
    }

    @Override
    public int compareTo(ElementListPersistent o) {
        if (element1 < o.element1)
            return -1;
        if (element1 > o.element1)
            return 1;
        if (element2 < o.element2)
            return -1;
        if (element2 > o.element2)
            return 1;
        return 0;
    }

    @Text(id = 4)
    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

}
