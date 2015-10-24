package com.ramussoft.gui.core.simple;

import java.util.EventListener;

public interface FrameFocusListener extends EventListener {

    void focusGained(DFrame dockable);

    void focusLost(DFrame dockable);

}
