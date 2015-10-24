package com.ramussoft.pb.master.gui;

import com.ramussoft.pb.master.model.MasterModel;

public class DefaultPanelCreator implements PanelCreator {

    private final MasterModel masterModel;

    private final DefaultPanel[] panels;

    public DefaultPanelCreator(final MasterModel masterModel) {
        this.masterModel = masterModel;
        panels = new DefaultPanel[masterModel.getPanelCount()];
    }

    public IPanel getPanel(final int i) {
        if (panels[i] == null) {
            panels[i] = new DefaultPanel(masterModel.getPanel(i));
        }
        return panels[i];
    }

    public int getPanelCount() {
        return masterModel.getPanelCount();
    }

}
