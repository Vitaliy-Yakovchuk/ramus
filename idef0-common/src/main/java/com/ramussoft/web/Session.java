package com.ramussoft.web;

import java.util.Hashtable;

public class Session {

    private final String id;

    private final Hashtable attrs = new Hashtable();

    private long lastUpdate = System.currentTimeMillis();

    public Session() {
        super();
        id = Long.toHexString(System.currentTimeMillis()
                + System.currentTimeMillis() % 56)
                + Long.toHexString(System.currentTimeMillis() % 4
                + System.currentTimeMillis() % 86);
    }

    public String getId() {
        return id;
    }

    public Object getAttribute(final String key) {
        return attrs.get(key);
    }

    public void setAttribute(final String key, final Object value) {
        attrs.put(key, value);
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(final long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
