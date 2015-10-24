package com.ramussoft.common.logger;

import java.util.List;

public interface ILog {

    List<Event> getEvents(int initialSize);

    List<Event> getEventsWithParams(String[] keys, Object[] values, int initialSize);

    void addLogListener(LogListener logListener);

    void removeLogListener(LogListener logListener);

    LogListener[] getLogListeners();

}
