package com.ramussoft.common.logger;

import java.sql.Timestamp;

public interface UpdateEventCallback {

    void init(Event event);

    void update(Event event);

    String getType();

    boolean canAddInfo(Event event);

    Event createEvent(Log log, long id, Timestamp timestamp, String type,
                      String userName, Object sessionObject);
}
