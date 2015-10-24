package com.ramussoft.gui.common.event;

import java.util.EventListener;

public interface TabbedListener extends EventListener {

    void tabCreated(TabbedEvent event);

    void tabRemoved(TabbedEvent event);
}
