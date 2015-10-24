package com.ramussoft.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.ramussoft.common.logger.AbstractLogExtension;
import com.ramussoft.common.logger.EngineLogExtension;
import com.ramussoft.common.logger.Event;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.jdbc.RowMapper;

public class StorageLogExtension extends AbstractLogExtension {

    private JDBCTemplate template;
    private String prefix;
    private final String[] attrs = {EngineLogExtension.ATTRIBUTE_ID,
            EngineLogExtension.ELEMENT_ID, EngineLogExtension.QUALIFIER_ID};

    public StorageLogExtension(JDBCTemplate template, String prefix) {
        this.template = template;
        this.prefix = prefix;
    }

    @Override
    public String getType() {
        return "engine-log-storage";
    }

    @Override
    public List<Event> getEvents(String[] keys, Object[] values, int maxSize) {
        List<String> aList = Arrays.asList(attrs);
        List<String> sKeys = new ArrayList<String>();
        List<Object> sValues = new ArrayList<Object>();
        for (int i = 0; i < values.length; i++)
            if (aList.contains(keys[i])) {
                sKeys.add(keys[i]);
                sValues.add(values[i]);
            }
        if (sKeys.size() == 0)
            return Collections.emptyList();
        StringBuffer sb = new StringBuffer(
                "SELECT * FROM ramus_qualifiers_log WHERE ");
        boolean first = true;
        for (int i = 0; i < sKeys.size(); i++) {
            if (first)
                first = false;
            else
                sb.append(" END ");
            sb.append(sKeys.get(i));
            sb.append("=?");
        }

        sb.append(" ORDER BY when_done DESC LIMIT ?");

        sValues.add(maxSize);

        return template.query(sb.toString(), new EventMapper(),
                sValues.toArray(), false);
    }

    @Override
    public List<Event> getEvents(int maxSize) {
        return template
                .query("SELECT * FROM ramus_qualifiers_log  ORDER BY when_done DESC LIMIT ?",
                        new EventMapper(), new Object[]{maxSize}, false);
    }

    private class EventMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            Event event = new Event(log, rs.getLong("log_id"),
                    rs.getTimestamp("when_done"), EngineLogExtension.NAME,
                    rs.getString("user_login"), null);
            event.setOldValue(rs.getString("old_value"));
            event.setNewValue(rs.getString("new_value"));
            event.setChangeType(rs.getInt("change_type"));

            event.attributes = new HashMap<String, Object>();
            put(event.attributes, rs, EngineLogExtension.ATTRIBUTE_ID);
            put(event.attributes, rs, EngineLogExtension.ELEMENT_ID);
            put(event.attributes, rs, EngineLogExtension.QUALIFIER_ID);

            return event;
        }

    }

    @Override
    public void store(Event event) {
        if (event.getOldValue() == null && event.getNewValue() == null)
            return;
        template.update(
                "INSERT INTO "
                        + prefix
                        + "qualifiers_log(log_id, attribute_id, "
                        + "element_id, qualifier_id, change_type, old_value, new_value, user_login) VALUES(?, "
                        + "?, ?, ?, ?, ?, ?, ?)",
                new Object[]{event.id, event.getAttribute("attribute_id"),
                        event.getAttribute("element_id"),
                        event.getAttribute("qualifier_id"),
                        event.getChangeType(), event.getOldValue(),
                        event.getNewValue(), event.user}, true);

    }

    public void put(HashMap<String, Object> attributes, ResultSet rs,
                    String name) throws SQLException {
        Object object = rs.getObject(name);
        if (object != null)
            attributes.put(name, rs.getLong(name));
    }

}
