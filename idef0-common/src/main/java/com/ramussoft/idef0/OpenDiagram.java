package com.ramussoft.idef0;

import java.io.Serializable;

import com.ramussoft.common.Qualifier;

public class OpenDiagram implements Serializable, PreserveObfuscating {

    private Qualifier qualifier;

    private long functionId;

    public OpenDiagram(Qualifier qualifier, long functionId) {
        this.qualifier = qualifier;
        this.functionId = functionId;
    }

    public Qualifier getQualifier() {
        return qualifier;
    }

    public long getFunctionId() {
        return functionId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (functionId ^ (functionId >>> 32));
        result = prime * result
                + ((qualifier == null) ? 0 : qualifier.hashCode());
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
        if (!(obj instanceof OpenDiagram))
            return false;
        OpenDiagram other = (OpenDiagram) obj;
        if (functionId != other.functionId)
            return false;
        if (qualifier == null) {
            if (other.qualifier != null)
                return false;
        } else if (!qualifier.equals(other.qualifier))
            return false;
        return true;
    }

}
