package com.ramussoft.common.logger;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.ramussoft.common.AbstractEngine;
import com.ramussoft.common.Engine;
import com.ramussoft.common.journal.Journaled;

public class Log implements ILog {

    /**
     * Max text message of log
     */
    public static final int DEFAULT_MAX_TEXTUAL_LENGTH = 300;

    public final Engine engine;

    public final Journaled journaled;

    private List<LogExtension> extensions = new ArrayList<LogExtension>();

    private Event openEvent;

    private long maxId = 0;

    private LogListener[] logListeners = new LogListener[]{};

    public Log(Engine engine, Journaled journaled) {
        this.engine = engine;
        this.journaled = journaled;
    }

    public void dispose() {
        applyOpen();
        for (LogExtension extension : extensions)
            extension.dispose();
        extensions.clear();
    }

    private void applyOpen() {
        if (openEvent == null)
            return;

        for (LogExtension extension : extensions)
            extension.beforeStore(openEvent);

        for (LogExtension extension : extensions)
            extension.store(openEvent);

        for (LogListener listener : logListeners)
            listener.logEvent(openEvent);

        openEvent = null;
    }

    public void applayEventAnyway() {
        applyOpen();
    }

    public void fireUpdateEvent(UpdateEventCallback callback) {
        if (openEvent != null) {
            if (!callback.canAddInfo(openEvent))
                applyOpen();
        }
        boolean init = openEvent == null;
        openEvent = getCurrentEvent(callback.getType(), callback);
        if (init)
            callback.init(openEvent);
        callback.update(openEvent);
    }

    private Event getCurrentEvent(String type, UpdateEventCallback callback) {
        if (openEvent == null)
            return createEvent(type, callback);
        return openEvent;
    }

    protected Event createEvent(String type, UpdateEventCallback callback) {
        ++maxId;
        return callback.createEvent(this, maxId,
                new Timestamp(System.currentTimeMillis()), type,
                "admin(local)", null);
    }

    /**
     * @param initialSize
     * @return
     */

    @Override
    public List<Event> getEvents(int initialSize) {
        HashMap<Long, Event> map = new HashMap<Long, Event>();
        for (LogExtension extension : extensions) {
            List<Event> events = extension.getEvents(initialSize);
            for (Event event : events) {
                Event old = map.get(event.id);
                if (old == null)
                    map.put(event.id, event);
                else
                    old.add(event);
            }
        }
        ArrayList<Event> list = new ArrayList<Event>(map.values());
        Collections.sort(list);
        return list;
    }

    @Override
    public List<Event> getEventsWithParams(String[] keys, Object[] values, int initialSize) {
        HashMap<Long, Event> map = new HashMap<Long, Event>();
        for (LogExtension extension : extensions) {
            List<Event> events = extension.getEvents(keys, values, initialSize);
            for (Event event : events) {
                Event old = map.get(event.id);
                if (old == null)
                    map.put(event.id, event);
                else
                    old.add(event);
            }
        }
        ArrayList<Event> list = new ArrayList<Event>(map.values());
        Collections.sort(list);
        return list;
    }

    public void addExtension(LogExtension extension) {
        extensions.add(extension);
        extension.init(this);
    }

    public String toString(Event event) {
        for (LogExtension extension : extensions)
            if (extension.getType().equals(event.type))
                return extension.toString(event);
        return MessageFormat.format("Old value{0} New Value {1}",
                event.getOldValue(), event.getNewValue());
    }

    public void cancelEvent(Event event) {
        if (openEvent == event)
            openEvent = null;
    }

    public LogExtension getLogExtensionByClass(Class<? extends LogExtension> l) {
        for (LogExtension extension : extensions)
            if (l.isInstance(extension))
                return extension;
        return null;
    }

    @Override
    public synchronized void addLogListener(LogListener logListener) {
        logListeners = AbstractEngine.addListener(logListeners, logListener);
    }

    @Override
    public synchronized void removeLogListener(LogListener logListener) {
        logListeners = AbstractEngine.removeListener(logListeners, logListener);
    }

    @Override
    public LogListener[] getLogListeners() {
        return Arrays.copyOf(logListeners, logListeners.length);
    }
}
