package com.ramussoft.common.event;

import com.ramussoft.common.Engine;

public class BranchEvent extends Event {

    /**
     *
     */
    private static final long serialVersionUID = -7613010313956944788L;

    private final long branchId;

    private final long parentBranchId;

    public BranchEvent(Engine engine, boolean journaled, long branchId, long parentBranchId) {
        super(engine, journaled);
        this.branchId = branchId;
        this.parentBranchId = parentBranchId;
    }

    public long getBranchId() {
        return branchId;
    }

    public long getParentBranchId() {
        return parentBranchId;
    }

}
