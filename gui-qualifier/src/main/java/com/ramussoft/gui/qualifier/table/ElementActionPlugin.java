package com.ramussoft.gui.qualifier.table;

import javax.swing.Action;

import com.ramussoft.gui.common.GUIPlugin;

public interface ElementActionPlugin extends GUIPlugin {

    public Action[] getActions(TableTabView tableView);

}
