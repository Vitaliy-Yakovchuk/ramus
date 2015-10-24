package com.ramussoft.common.logger;

import java.util.ArrayList;
import java.util.List;

public class MemoryStorageLogExtension extends AbstractLogExtension {

    private List<Event> events = new ArrayList<Event>();

    @Override
    public List<Event> getEvents(int maxSize) {
        if (events.size() <= maxSize)
            return events;
        return events.subList(events.size() - maxSize, events.size());
    }

    @Override
    public String getType() {
        return "memory-storage";
    }

    @Override
    public void store(Event event) {
        events.add(event);
    }
}
