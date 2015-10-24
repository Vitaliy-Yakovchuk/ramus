package com.ramussoft.client.log;

import java.util.Date;
import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.logger.Event;

import static com.ramussoft.client.ClientPlugin.bundle;

public abstract class ElementEventTableModel extends AbstractEventTableModel {

    private String[] columns = new String[]{S("EventType"),
            S("EventType.attribute"), S("EventType.oldValue"),
            S("EventType.newValue"), S("EventType.time"), S("EventType.user")};

    public ElementEventTableModel(List<Event> events) {
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
                Long id = (Long) event.getAttribute("attribute_id");
                if (id == null)
                    return null;
                Engine engine = getEngine();
                Attribute attribute = engine.getAttribute(id);
                if (attribute == null)
                    return null;
                return attribute.getName();
            case 2:
                return event.getOldValue();
            case 3:
                return event.getNewValue();
            case 4:
                return event.eventTime;
            case 5:
                return getUser(event);
            default:
                break;
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 4)
            return Date.class;
        return super.getColumnClass(columnIndex);
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    private String S(String key) {
        return bundle.getString(key);
    }

    protected abstract Engine getEngine();
}
