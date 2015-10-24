package com.ramussoft.chart.core;

import com.ramussoft.common.attribute.Attribute;
import com.ramussoft.common.persistent.Long;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.Table;

@Table(name = "table_charts")
public class TableChartPersistent extends Persistent {

    /**
     *
     */
    private static final long serialVersionUID = 6295664792427529836L;

    private long attributeId;

    private long otherElementId;

    @Attribute(id = 0, autoset = true, primary = true)
    public long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(long attributeId) {
        this.attributeId = attributeId;
    }

    @Long(id = 1)
    public long getOtherElementId() {
        return otherElementId;
    }

    public void setOtherElementId(long otherElementId) {
        this.otherElementId = otherElementId;
    }

}
