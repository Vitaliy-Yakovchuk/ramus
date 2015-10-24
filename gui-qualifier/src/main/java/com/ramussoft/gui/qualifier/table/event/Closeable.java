package com.ramussoft.gui.qualifier.table.event;

public interface Closeable {
    public void addCloseListener(CloseListener listener);

    public void removeCloseListener(CloseListener listener);

    public CloseListener[] getCloseListeners();
}
