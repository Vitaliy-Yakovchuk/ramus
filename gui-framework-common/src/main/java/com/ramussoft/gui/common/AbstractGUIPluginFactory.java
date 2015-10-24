package com.ramussoft.gui.common;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;

public abstract class AbstractGUIPluginFactory {

    public static final String POPUP_MENU = "Action.popupMenu";

    protected List<GUIPlugin> plugins;

    public AbstractGUIPluginFactory(List<GUIPlugin> plugins) {
        this.plugins = plugins;
    }

    public abstract JFrame getMainFrame();

    public abstract GUIPlugin findPluginForViewId(String id);

    public abstract void setCurrentWorkspace(String workspace);

    public abstract List<String> getWorkspaces();

    public abstract GUIPlugin getPluginForWorkspace(String workspace);

    public abstract void setNorthEastCornerComponent(JComponent component);

    public abstract GUIFramework getFramework();

}
