package com.ramussoft.database.common;

import java.util.EventListener;

public interface RowChildListener extends EventListener {

    void added(Row parent, Row row, int index);

    void addedByThisRowSet(Row row);
}
