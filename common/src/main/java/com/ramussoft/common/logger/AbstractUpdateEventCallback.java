package com.ramussoft.common.logger;

import java.sql.Timestamp;

public abstract class AbstractUpdateEventCallback implements
        UpdateEventCallback {

    @Override
    public void init(Event event) {
    }

    protected boolean hasAttribute(Event event, String attribute, Object value) {
        Object o;
        if ((o = event.getAttribute(attribute)) == null)
            return value == null;
        return o.equals(value);
    }

    @Override
    public Event createEvent(Log log, long id, Timestamp timestamp,
                             String type, String userName, Object sessionObject) {
        return new Event(log, id, new Timestamp(System.currentTimeMillis()),
                type, userName, sessionObject);
    }
}
