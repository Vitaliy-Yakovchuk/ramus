package com.ramussoft.pb.frames.docking;

import com.dsoft.utils.Options;

public abstract class Createble {
    protected abstract ViewPanel createViewPanel();

    private ViewPanel viewPanel;

    public ViewPanel getViewPanel() {
        if (viewPanel == null) {
            viewPanel = createViewPanel();
            Options.loadOptions("v_" + viewPanel.getTitleKey(), viewPanel);
        }
        return viewPanel;
    }
}
