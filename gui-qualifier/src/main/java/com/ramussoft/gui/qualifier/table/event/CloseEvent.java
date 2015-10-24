package com.ramussoft.gui.qualifier.table.event;

public class CloseEvent {

    private Object source;

    public CloseEvent(Object source) {
        this.source = source;
    }

    public Object getSource() {
        return source;
    }
}
