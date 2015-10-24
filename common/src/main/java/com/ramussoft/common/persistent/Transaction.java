package com.ramussoft.common.persistent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Transaction implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7315286277031416961L;

    private List<Persistent> save = new ArrayList<Persistent>();

    private List<Persistent> update = new ArrayList<Persistent>();

    private List<Persistent> delete = new ArrayList<Persistent>();

    private transient List<Persistent> oldUpdate = new ArrayList<Persistent>();

    private boolean removeBranchInfo;

    /**
     * @return the save
     */
    public List<Persistent> getSave() {
        return save;
    }

    /**
     * @return the update
     */
    public List<Persistent> getUpdate() {
        return update;
    }

    /**
     * @return the delete
     */
    public List<Persistent> getDelete() {
        return delete;
    }

    public List<Persistent> getOldUpdate() {
        return oldUpdate;
    }

    /**
     * @return the updateBranchInfoIfNeed
     */
    public boolean isRemoveBranchInfo() {
        return removeBranchInfo;
    }

    /**
     * @param updateBranchInfoIfNeed the updateBranchInfoIfNeed to set
     */
    public void setRemoveBranchInfo(boolean updateBranchInfoIfNeed) {
        this.removeBranchInfo = updateBranchInfoIfNeed;
    }
}
