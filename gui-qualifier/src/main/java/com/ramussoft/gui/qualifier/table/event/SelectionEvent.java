package com.ramussoft.gui.qualifier.table.event;

import java.io.Serializable;

import com.ramussoft.database.common.Row;

public class SelectionEvent implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3084628274562207245L;

    private boolean selected;

    private Row[] rows;

    public SelectionEvent(Row[] rows, boolean selected) {
        this.rows = rows;
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public Row[] getRows() {
        return rows;
    }

}
