package com.ramussoft.gui.common.event;

import java.util.EventListener;

public interface CloseMainFrameListener extends EventListener {

    boolean close();

    void closed();

    void afterClosed();
}
