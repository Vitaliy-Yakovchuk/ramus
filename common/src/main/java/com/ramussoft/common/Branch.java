package com.ramussoft.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Branch implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2595756272593639861L;

    private long branchId;

    private List<Branch> children = new ArrayList<Branch>();

    private String reason;

    private String user;

    private int type;

    private long parentBranchId;

    private String module;

    private Date creationTime = new Date();

    /**
     * @param branchId the branchId to set
     */
    public void setBranchId(long branchId) {
        this.branchId = branchId;
    }

    /**
     * @return the branchId
     */
    public long getBranchId() {
        return branchId;
    }

    public List<Branch> getChildren() {
        return children;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param parentBranchId the parentBranchId to set
     */
    public void setParentBranchId(long parentBranchId) {
        this.parentBranchId = parentBranchId;
    }

    /**
     * @return the parentBranchId
     */
    public long getParentBranchId() {
        return parentBranchId;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Branch [branchId=" + branchId + ", reason=" + reason
                + ", user=" + user + ", type=" + type + ", parentBranchId="
                + parentBranchId + ", module=" + module + "]";
    }

    /**
     * @param module the module to set
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * @return the module
     */
    public String getModule() {
        return module;
    }

    /**
     * @param creationTime the creationDate to set
     */
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * @return the creationDate
     */
    public Date getCreationTime() {
        return creationTime;
    }

}
