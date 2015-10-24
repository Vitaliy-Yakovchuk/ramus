package com.ramussoft.pb.data.negine;

import com.ramussoft.idef0.attribute.SectorBorderPersistent;
import com.ramussoft.pb.Crosspoint;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.data.AbstractCrosspoint;
import com.ramussoft.pb.data.SectorBorder;

public class NSectorBorder implements SectorBorder {

    protected NDataPlugin dataPlugin;

    protected Boolean start = null;

    protected NSector sector;

    NCrosspoint crosspoint;

    private NFunction function;

    public int getType() {
        if (getBorderType() >= 0)
            return TYPE_BORDER;
        if (getFunction() != null)
            return TYPE_FUNCTION;
        return TYPE_SPOT;
    }

    public NSectorBorder(final NDataPlugin dataPlugin, final boolean start,
                         final NSector sector) {
        this.dataPlugin = dataPlugin;
        this.start = start;
        this.sector = sector;
        updateCrospointInformation();
    }

    public void updateCrospointInformation() {
        crosspoint = null;
        NCrosspoint res = getACrosspoint();
        // if ((this.crosspoint != null)&&(res==null)) {
        // this.crosspoint.remove1(this.sector);
        // }
        if (res == null)
            return;
        if (res.isIn(sector))
            return;
        if (start)
            res.addXOut(sector);
        else
            res.addXIn(sector);
    }

    public int getBorderType() {
        return getSbp().getBorderType();
    }

    @Deprecated
    public void setBorderType(final int borderType) {
        synchronized (dataPlugin) {
            setBorderTypeA(borderType);
            commit();
        }
    }

    public void setBorderTypeA(final int borderType) {
        getSbp().setBorderType(borderType);
    }

    public void commit() {
        if (start)
            sector.setAttribute(dataPlugin.sectorBorderStart, getSbp());
        else
            sector.setAttribute(dataPlugin.sectorBorderEnd, getSbp());
    }

    public Crosspoint getCrosspoint() {
        return getACrosspoint();
    }

    public Function getFunction() {
        return getAFunction();
    }

    public int getFunctionType() {
        return getSbp().getFunctionType();
    }

    @Deprecated
    public void setCrosspoint(final Crosspoint crosspoint) {
        synchronized (dataPlugin) {
            setCrosspointA(crosspoint);

            commit();
        }
    }

    public void setCrosspointA(final Crosspoint crosspoint) {
        if (start == null)
            throw new NullPointerException("Start or end not setted");

        if (sector == null)
            throw new NullPointerException("Sector not setted");

        if (this.getACrosspoint() != null) {
            ((AbstractCrosspoint) this.getACrosspoint()).remove1(sector);
        }

        if (crosspoint != null) {
            if (start)
                ((AbstractCrosspoint) crosspoint).addOut(sector);
            else
                ((AbstractCrosspoint) crosspoint).addIn(sector);
        }

        getSbp().setCrosspoint(
                (crosspoint == null) ? -1 : crosspoint.getGlobalId());
    }

    @Deprecated
    public void setFunction(final Function function) {
        synchronized (dataPlugin) {
            setFunctionA(function);
            commit();
        }
    }

    public void setFunctionA(final Function function) {
        getSbp()
                .setFunction(
                        (function == null) ? -1 : ((NFunction) function)
                                .getElementId());
    }

    @Deprecated
    public void setFunctionType(final int functionType) {
        synchronized (dataPlugin) {
            setFunctionTypeA(functionType);
            commit();
        }
    }

    public void setFunctionTypeA(final int functionType) {
        getSbp().setFunctionType(functionType);
    }

    /**
     * @return the sbp
     */
    public SectorBorderPersistent getSbp() {
        SectorBorderPersistent p = ((start) ? sector
                .getStartP() : sector.getEndP());
        if (p == null)
            return new SectorBorderPersistent();
        return p;
    }

    /**
     * @return the crosspoint
     */
    protected NCrosspoint getACrosspoint() {
        SectorBorderPersistent p = getSbp();
        if (p.getCrosspoint() < 0l)
            return null;
        if (crosspoint == null) {
            crosspoint = (NCrosspoint) dataPlugin.findCrosspointByGlobalId(p
                    .getCrosspoint());

            if (crosspoint == null) {
                crosspoint = (NCrosspoint) dataPlugin.createCrosspoint(p
                        .getCrosspoint());
            }
            return crosspoint;
        } else if (crosspoint.getGlobalId() == p.getCrosspoint())
            return crosspoint;
        else {
            crosspoint = null;
            return getACrosspoint();
        }
    }

    /**
     * @return the function
     */
    protected Function getAFunction() {
        SectorBorderPersistent p = getSbp();
        if (p.getFunction() < 0l)
            return null;

        if (function == null) {
            function = (NFunction) dataPlugin
                    .findRowByGlobalId(p.getFunction());
            return function;
        }

        if (function.getElementId() == p.getFunction())
            return function;
        function = null;
        return getAFunction();
    }

    public int getTunnelSoft() {
        return getSbp().getTunnelSoft();
    }

    public int getTunnelType() {
        return ((NCrosspoint) getCrosspoint()).getTunnelType(getSbp());
    }

    public void setTunnelSoft(int tunnelSoft) {
        getSbp().setTunnelSoft(tunnelSoft);
        commit();
    }

}
