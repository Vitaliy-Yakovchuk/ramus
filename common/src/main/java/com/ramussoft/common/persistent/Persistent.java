package com.ramussoft.common.persistent;

import java.io.Serializable;

/**
 * Interface for object which can be used in database.
 *
 * @author zdd
 */

public class Persistent implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4694553291157744907L;

    private long valueBranchId;

    /**
     * @param valueBranchId the valueBranchId to set
     */
    public void setValueBranchId(long valueBranchId) {
        this.valueBranchId = valueBranchId;
    }

    /**
     * @return the valueBranchId
     */
    public long getValueBranchId() {
        return valueBranchId;
    }


}
