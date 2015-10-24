package com.ramussoft.gui.common.event;

public abstract class CloseMainFrameAdapter implements CloseMainFrameListener {

    @Override
    public boolean close() {
        return true;
    }

    @Override
    public void closed() {
    }

    @Override
    public void afterClosed() {
    }

}
