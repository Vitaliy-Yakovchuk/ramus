package com.ramussoft.client.log;

import java.util.List;

import com.ramussoft.common.logger.Event;

import static com.ramussoft.client.ClientPlugin.bundle;

public class EventTableModel extends AbstractEventTableModel {

    private String[] columns = new String[]{S("EventType"),
            S("EventType.oldValue"), S("EventType.newValue"),
            S("EventType.time"), S("EventType.user")};

    public EventTableModel(List<Event> events) {
        super(events);
    }

    /**
     *
     */
    private static final long serialVersionUID = -1999501755387600916L;

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Event event = getEvent(rowIndex);
        switch (columnIndex) {
            case 0:
                return getType(event);
            case 1:
                return event.getOldValue();
            case 2:
                return event.getNewValue();
            case 3:
                return event.eventTime;
            case 4:
                return getUser(event);
            default:
                break;
        }
        return null;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    private String S(String key) {
        return bundle.getString(key);
    }
}
