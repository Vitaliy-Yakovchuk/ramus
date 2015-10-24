package com.ramussoft.common.persistent;

public class PersistentStatus {

    /**
     * Persistent must be stored into database.
     */

    public static final int CREATED = 0;

    /**
     * Persistent was load from database (no storage is needed).
     */

    public static final int LOADED = 1;

    private int status = CREATED;

    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }

}
