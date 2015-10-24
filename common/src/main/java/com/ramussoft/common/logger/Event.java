package com.ramussoft.common.logger;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map.Entry;

public class Event implements Serializable, Comparable<Event> {

    /**
     *
     */
    private static final long serialVersionUID = 6354538061154357236L;

    public static final int DATA_UNSET = -1;

    public static final int DATA_CREATED = 0;

    public static final int DATA_REMOVED = 1;

    public static final int DATA_UPDATED = 2;

    public static final int DATA_MIX = 3;

    public transient final Log log;

    private int changeType = DATA_UNSET;

    private String oldValue;

    private String newValue;

    public long id;

    public final Timestamp eventTime;

    public HashMap<String, Object> attributes;

    public final String type;

    public String user;

    public Object sessionObject;

    public Event(Log log, long id, Timestamp eventTime, String type) {
        this(log, id, eventTime, type, "admin (local)", null);
    }

    public Event(Log log, long id, Timestamp eventTime, String type,
                 String user, Object sessionObject) {
        this.log = log;
        this.id = id;
        this.user = user;
        if (eventTime == null)
            throw new NullPointerException("eventTime can not be null");
        this.eventTime = eventTime;
        this.type = type;
        this.sessionObject = sessionObject;
    }

    public Object getAttribute(String attribute) {
        if (attributes == null)
            return null;
        return attributes.get(attribute);
    }

    public void setAttribute(String attribute, Object value) {
        if (attributes == null)
            attributes = new HashMap<String, Object>();
        attributes.put(attribute, value);
    }

    public int getChangeType() {
        return changeType;
    }

    public void setChangeType(int changeType) {
        this.changeType = changeType;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    @Override
    public int compareTo(Event o) {
        return eventTime.compareTo(o.eventTime);
    }

    public void add(Event event) {
        if (event.attributes == null)
            return;
        if (attributes == null)
            attributes = event.attributes;
        else {
            for (Entry<String, Object> entry : event.attributes.entrySet())
                setAttribute(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public String toString() {
        if (log == null)
            return super.toString();
        return log.toString(this);
    }

    public void cancel() {
        log.cancelEvent(this);
    }
}
