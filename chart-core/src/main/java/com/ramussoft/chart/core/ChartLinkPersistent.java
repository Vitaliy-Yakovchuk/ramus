package com.ramussoft.chart.core;

import com.ramussoft.common.persistent.AbstractPersistent;
import com.ramussoft.common.persistent.Long;
import com.ramussoft.common.persistent.Table;

@Table(name = "chart_links")
public class ChartLinkPersistent extends AbstractPersistent {

    /**
     *
     */
    private static final long serialVersionUID = 6897550683441149179L;

    private long otherElementId;

    /**
     * @param otherElementId the otherElementId to set
     */
    public void setOtherElementId(long otherElementId) {
        this.otherElementId = otherElementId;
    }

    /**
     * @return the otherElementId
     */
    @Long(id = 2)
    public long getOtherElementId() {
        return otherElementId;
    }

}
