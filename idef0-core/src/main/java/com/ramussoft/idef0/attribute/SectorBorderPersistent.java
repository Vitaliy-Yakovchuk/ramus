package com.ramussoft.idef0.attribute;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Integer;
import com.ramussoft.common.persistent.Long;
import com.ramussoft.common.persistent.Table;

@Table(name = "sector_borders")
public class SectorBorderPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = -3994203521543467271L;

    private int functionType = -1;

    private int borderType = -1;

    private long function = -1;

    private long crosspoint = -1;

    private java.lang.Integer tunnelSoft = 0;

    /**
     * @param functionType the functionType to set
     */
    public void setFunctionType(int functionType) {
        this.functionType = functionType;
    }

    /**
     * @return the functionType
     */
    @Integer(id = 2)
    public int getFunctionType() {
        return functionType;
    }

    /**
     * @param borderType the borderType to set
     */
    public void setBorderType(int borderType) {
        this.borderType = borderType;
    }

    /**
     * @return the borderType
     */
    @Integer(id = 3)
    public int getBorderType() {
        return borderType;
    }

    /**
     * @param function the function to set
     */
    public void setFunction(long function) {
        this.function = function;
    }

    /**
     * @return the function
     */
    @Long(id = 4)
    public long getFunction() {
        return function;
    }

    /**
     * @param crosspoint the crosspoint to set
     */
    public void setCrosspoint(long crosspoint) {
        this.crosspoint = crosspoint;
    }

    /**
     * @return the crosspoint
     */
    @Long(id = 5)
    public long getCrosspoint() {
        return crosspoint;
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
        result = prime * result + borderType;
        result = prime * result + (int) (crosspoint ^ (crosspoint >>> 32));
        result = prime * result + (int) (function ^ (function >>> 32));
        result = prime * result + functionType;
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
        if (!(obj instanceof SectorBorderPersistent))
            return false;
        SectorBorderPersistent other = (SectorBorderPersistent) obj;
        if (borderType != other.borderType)
            return false;
        if (crosspoint != other.crosspoint)
            return false;
        if (function != other.function)
            return false;
        if (functionType != other.functionType)
            return false;
        return true;
    }

    /**
     * @param tunnelSoft the tunnelSoft to set
     */
    public void setTunnelSoft(java.lang.Integer tunnelSoft) {
        if (tunnelSoft == null)
            this.tunnelSoft = 0;
        else
            this.tunnelSoft = tunnelSoft;
    }

    /**
     * @return the tunnelSoft
     */
    @Integer(id = 6)
    public java.lang.Integer getTunnelSoft() {
        return tunnelSoft;
    }

}
