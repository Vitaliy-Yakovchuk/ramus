package com.ramussoft.gui.common;

import javax.swing.JToolBar;

import com.ramussoft.gui.common.event.TabbedListener;
import com.ramussoft.gui.common.prefrence.Preferences;

public interface ViewPlugin extends GUIPlugin {

    ActionDescriptor[] getActionDescriptors();

    void addTabbedListener(TabbedListener listener);

    void removeTabbedListener(TabbedListener listener);

    TabbedListener[] getTabbedListeners();

    UniqueView[] getUniqueViews();

    TabbedView[] getTabbedViews();

    JToolBar[] getToolBars();

    Preferences[] getApplicationPreferences();

    Preferences[] getProjectPreferences();

}
