package com.ramussoft.pb.data.negine;

import com.ramussoft.pb.Sector;
import com.ramussoft.pb.data.AbstractCrosspoint;

public class NCrosspoint extends AbstractCrosspoint {

    public NCrosspoint(final NDataPlugin fileDataPlugin, final long id) {
        super(fileDataPlugin);
        this.id = id;
    }

    void addXIn(final Sector sector) {
        ins = addSector(ins, sector);
        // if ((ins.length > 1) && (outs.length > 1)) {
        // ins = Arrays.copyOf(ins, ins.length - 1);
        // System.err.println("Fuching error");
        // }
    }

    void addXOut(final Sector sector) {
        outs = addSector(outs, sector);
        // if ((ins.length > 1) && (outs.length > 1)) {
        // outs = Arrays.copyOf(outs, outs.length - 1);
        // System.err.println("Fuching error");
        // }
    }

    @Override
    public void addIn(final Sector sector) {
        synchronized (dataPlugin) {
            if (!((NDataPlugin) dataPlugin).isLoading()) {
                copyVisulaToNewSector(sector);
            }
            super.addIn(sector);
        }
    }

    @Override
    public void addOut(final Sector sector) {
        synchronized (dataPlugin) {
            if (!((NDataPlugin) dataPlugin).isLoading()) {
                copyVisulaToNewSector(sector);
            }
            super.addOut(sector);
        }
    }

    public Sector getIn() {
        return ins[0];
    }

    public Sector getOut() {
        return outs[0];
    }

}
