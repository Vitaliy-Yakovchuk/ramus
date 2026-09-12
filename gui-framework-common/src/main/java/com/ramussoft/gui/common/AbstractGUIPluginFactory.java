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

    /**
     * @param id ідентифікатор унікального вікна.
     * @return <code>true</code>, якщо вікно зараз показане.
     */
    public boolean isUniqueViewVisible(String id) {
        return true;
    }

    /**
     * Показує або ховає унікальне вікно.
     *
     * @param id      ідентифікатор унікального вікна.
     * @param visible <code>true</code>, щоб показати вікно.
     */
    public void setUniqueViewVisible(String id, boolean visible) {
    }

}
