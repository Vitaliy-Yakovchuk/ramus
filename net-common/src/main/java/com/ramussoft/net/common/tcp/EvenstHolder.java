package com.ramussoft.net.common.tcp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EvenstHolder implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3372395378048177203L;

    private List<EventHolder> events = new ArrayList<EventHolder>();

    /**
     * @return the events
     */
    public List<EventHolder> getEvents() {
        return events;
    }

    public void addEventHolder(EventHolder holder) {
        this.events.add(holder);
    }
}
