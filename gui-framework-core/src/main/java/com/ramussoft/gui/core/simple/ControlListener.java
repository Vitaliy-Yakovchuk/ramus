package com.ramussoft.gui.core.simple;

import java.util.EventListener;

public interface ControlListener extends EventListener {

    void closed(Control control, DFrame dockable);

}
