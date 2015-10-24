package com.ramussoft.gui.common.event;

import com.ramussoft.gui.common.TabView;

public class TabbedEvent {

    private String tabViewId;

    private TabView tabableView;

    public TabbedEvent(String tabViewId, TabView tabableView) {
        this.tabViewId = tabViewId;
        this.tabableView = tabableView;
    }

    /**
     * @return the tabViewId
     */
    public String getTabViewId() {
        return tabViewId;
    }

    /**
     * @return the tabableView
     */
    public TabView getTabableView() {
        return tabableView;
    }

}
