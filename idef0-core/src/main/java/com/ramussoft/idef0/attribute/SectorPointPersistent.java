package com.ramussoft.idef0.attribute;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Double;
import com.ramussoft.common.persistent.Integer;
import com.ramussoft.common.persistent.Long;
import com.ramussoft.common.persistent.Table;

@Table(name = "sector_points")
public class SectorPointPersistent extends AbstractPersistent implements
        Comparable<SectorPointPersistent> {

    /**
     *
     */
    private static final long serialVersionUID = -2255465224992926364L;

    private long xOrdinateId;

    private long yOrdinateId;

    private double xPosition;

    private double yPosition;

    private int type;

    private int position;

    @Long(primary = true, id = 2)
    public long getXOrdinateId() {
        return xOrdinateId;
    }

    public void setXOrdinateId(long xOrdinateId) {
        this.xOrdinateId = xOrdinateId;
    }

    @Long(primary = true, id = 3)
    public long getYOrdinateId() {
        return yOrdinateId;
    }

    public void setYOrdinateId(long yOrdinateId) {
        this.yOrdinateId = yOrdinateId;
    }

    @Double(id = 4)
    public double getXPosition() {
        return xPosition;
    }

    public void setXPosition(double xPosition) {
        this.xPosition = xPosition;
    }

    @Double(id = 5)
    public double getYPosition() {
        return yPosition;
    }

    public void setYPosition(double yPosition) {
        this.yPosition = yPosition;
    }

    @Integer(id = 6)
    public int getPointType() {
        return type;
    }

    public void setPointType(int type) {
        this.type = type;
    }

    @Integer(id = 7)
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public int compareTo(SectorPointPersistent p) {
        if (position == p.position)
            return 0;
        if (position < p.position)
            return -1;
        return 1;
    }
}
