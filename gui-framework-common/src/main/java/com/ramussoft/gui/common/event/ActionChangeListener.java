package com.ramussoft.gui.common.event;

import java.util.EventListener;

public interface ActionChangeListener extends EventListener {

    void actionsAdded(ActionChangeEvent event);

    void actionsRemoved(ActionChangeEvent event);

}
