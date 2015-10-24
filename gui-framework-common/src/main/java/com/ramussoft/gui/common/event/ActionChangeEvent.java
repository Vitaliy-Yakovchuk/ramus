package com.ramussoft.gui.common.event;

import javax.swing.Action;

public class ActionChangeEvent {

    private Action[] actions;

    public ActionChangeEvent(Action[] actions) {
        this.actions = actions;
    }

    public Action[] getActions() {
        return actions;
    }

}
