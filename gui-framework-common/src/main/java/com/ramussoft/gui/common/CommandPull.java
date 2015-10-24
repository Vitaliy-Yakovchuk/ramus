package com.ramussoft.gui.common;

import java.util.Hashtable;

public class CommandPull extends Hashtable<String, Object> {

    /**
     *
     */
    private static final long serialVersionUID = -6505305786861441338L;

    private Hashtable<String, Long> ids = new Hashtable<String, Long>();

    public long getActionId(String action) {
        Long res = ids.get(action);
        if (res == null)
            return -1;
        return res.longValue();
    }

    @Override
    public synchronized Object put(String key, Object value) {
        Long long1 = ids.get(key);
        if (long1 == null) {
            ids.put(key, 0l);
        } else {
            ids.put(key, long1.longValue() + 1l);
        }
        return super.put(key, value);
    }
}
