package com.ramussoft.common.logger;

import java.util.Collections;
import java.util.List;

public abstract class AbstractLogExtension implements LogExtension {

    protected Log log;

    @Override
    public List<Event> getEvents(int maxSize) {
        return Collections.emptyList();
    }

    @Override
    public void init(Log log) {
        this.log = log;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void beforeStore(Event event) {
    }

    @Override
    public void store(Event event) {
    }

    @Override
    public String toString(Event event) {
        StringBuffer sb = new StringBuffer();
        switch (event.getChangeType()) {
            case Event.DATA_CREATED:
                sb.append("Created: ");
                sb.append(event.getNewValue());
                break;
            case Event.DATA_REMOVED:
                sb.append("Removed: ");
                sb.append(event.getOldValue());
                break;
            case Event.DATA_UPDATED:
                sb.append("Updated ");
                sb.append("old data: ");
                sb.append(event.getOldValue());
                sb.append(", new data: ");
                sb.append(event.getNewValue());
                break;

            default:
                break;
        }
        return sb.toString();
    }

    @Override
    public List<Event> getEvents(String[] keys, Object[] values, int maxSize) {
        return getEvents(maxSize);
    }
}
