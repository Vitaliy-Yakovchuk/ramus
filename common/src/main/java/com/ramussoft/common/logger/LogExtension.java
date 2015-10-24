package com.ramussoft.common.logger;

import java.util.List;

public interface LogExtension {

    List<Event> getEvents(int maxSize);

    List<Event> getEvents(String[] keys, Object[] values, int maxSize);

    void init(Log log);

    void dispose();

    void beforeStore(Event event);

    void store(Event event);

    String getType();

    String toString(Event event);
}
