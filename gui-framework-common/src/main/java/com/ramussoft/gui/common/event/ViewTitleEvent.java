package com.ramussoft.gui.common.event;

import com.ramussoft.gui.common.View;

public class ViewTitleEvent {

    private View tabbedView;

    private String newTitle;

    public ViewTitleEvent(View tabbedView, String newTitle) {
        this.newTitle = newTitle;
        this.tabbedView = tabbedView;
    }

    /**
     * @return the newTitle
     */
    public String getNewTitle() {
        return newTitle;
    }

    /**
     * @return the tabbedView
     */
    public View getTabbedView() {
        return tabbedView;
    }

}
