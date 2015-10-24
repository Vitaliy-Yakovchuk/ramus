package com.ramussoft.pb.master.gui;

public interface PanelCreator {
    int getPanelCount();

    IPanel getPanel(int i);
}
