package com.ramussoft.gui.common;

import javax.swing.Action;

/**
 * Action descriptor.
 *
 * @author zdd
 */

public class ActionDescriptor {

    private Action action = null;

    private String menu = null;

    private String toolBar = null;

    private String buttonGroup = null;

    private boolean selective = false;

    private ActionLevel actionLevel = ActionLevel.GLOBAL;

    /**
     * @param action the action to set.
     */
    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * @return the action. If action == <code>null</code>, application will
     * create separator.
     */
    public Action getAction() {
        return action;
    }

    /**
     * @param menu the menu to set
     */
    public void setMenu(String menu) {
        this.menu = menu;
    }

    /**
     * @return the menu in like folder style (can be <code>null</code>).
     */
    public String getMenu() {
        return menu;
    }

    /**
     * @param toolBar the toolBar to set
     */
    public void setToolBar(String toolBar) {
        this.toolBar = toolBar;
    }

    /**
     * @return the toolBar name (can be <code>null</code>).
     */
    public String getToolBar() {
        return toolBar;
    }

    /**
     * @param buttonGroup the buttonGroup to set
     */
    public void setButtonGroup(String buttonGroup) {
        this.buttonGroup = buttonGroup;
    }

    /**
     * @return the buttonGroup name, if <code>null</code> this is just an action
     * or select, but not an radio group.
     */
    public String getButtonGroup() {
        return buttonGroup;
    }

    /**
     * @param actionLevel the actionLevel to set
     */
    public void setActionLevel(ActionLevel actionLevel) {
        this.actionLevel = actionLevel;
    }

    /**
     * @return the actionLevel
     */
    public ActionLevel getActionLevel() {
        return actionLevel;
    }

    /**
     * @param selective the selective to set
     */
    public void setSelective(boolean selective) {
        this.selective = selective;
    }

    /**
     * @return the selective
     */
    public boolean isSelective() {
        return selective;
    }
}
