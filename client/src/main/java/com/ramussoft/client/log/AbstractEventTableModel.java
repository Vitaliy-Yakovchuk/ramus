package com.ramussoft.client.log;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.ramussoft.client.ClientPlugin;
import com.ramussoft.common.logger.Event;

public abstract class AbstractEventTableModel extends AbstractTableModel {

    /**
     *
     */
    private static final long serialVersionUID = 2235433265507228658L;

    protected List<Event> events;

    public AbstractEventTableModel(List<Event> events) {
        this.events = events;
    }

    @Override
    public int getRowCount() {
        return events.size();
    }

    protected Event getEvent(int rowIndex) {
        return events.get(rowIndex);
    }

    protected String getUser(Event event) {
        return event.user;
    }

    protected String getType(Event event) {
        switch (event.getChangeType()) {
            case Event.DATA_CREATED:
                return ClientPlugin.bundle.getString("EventType.created");
            case Event.DATA_REMOVED:
                return ClientPlugin.bundle.getString("EventType.removed");
            default:
                return ClientPlugin.bundle.getString("EventType.changed");
        }
    }

}
