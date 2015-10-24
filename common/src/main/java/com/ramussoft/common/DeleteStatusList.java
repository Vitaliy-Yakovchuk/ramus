package com.ramussoft.common;

import java.util.ArrayList;

public class DeleteStatusList extends ArrayList<DeleteStatus> {

    /**
     *
     */
    private static final long serialVersionUID = -213794649070706008L;

    @Override
    public boolean add(DeleteStatus e) {
        if (e != null)
            return super.add(e);
        return false;
    }

    public boolean canDelete() {
        for (DeleteStatus status : this) {
            if (status.getDelete().equals(Delete.CAN_NOT))
                return false;
        }
        return true;
    }
}
