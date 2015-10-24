package com.ramussoft.gui.qualifier.table.event;

import java.util.EventListener;

public interface CloseListener extends EventListener {

    void closed(CloseEvent event);

}
