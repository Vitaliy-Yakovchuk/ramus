package com.ramussoft.gui.common;

import javax.swing.*;
import javax.swing.event.EventListenerList;

import com.ramussoft.gui.common.event.TabbedEvent;
import com.ramussoft.gui.common.event.TabbedListener;
import com.ramussoft.gui.common.prefrence.Preferences;

public abstract class AbstractViewPlugin implements ViewPlugin {

    protected EventListenerList tabbedListeners = new EventListenerList();

    protected GUIFramework framework;

    @Override
    public void setFramework(GUIFramework framework) {
        this.framework = framework;
    }

    @Override
    public void addTabbedListener(TabbedListener listener) {
        tabbedListeners.add(TabbedListener.class, listener);
    }

    @Override
    public TabbedListener[] getTabbedListeners() {
        return tabbedListeners.getListeners(TabbedListener.class);
    }

    @Override
    public void removeTabbedListener(TabbedListener listener) {
        tabbedListeners.remove(TabbedListener.class, listener);
    }

    @Override
    public ActionDescriptor[] getActionDescriptors() {
        return new ActionDescriptor[]{};
    }

    @Override
    public String getString(String key) {
        return GlobalResourcesManager.getString(key);
    }

    @Override
    public TabbedView[] getTabbedViews() {
        return new TabbedView[]{};
    }

    @Override
    public UniqueView[] getUniqueViews() {
        return new UniqueView[]{};
    }

    public void tabCreated(TabbedEvent event) {
        for (TabbedListener listener : tabbedListeners
                .getListeners(TabbedListener.class)) {
            listener.tabCreated(event);
        }
    }

    public void tabRemoved(TabbedEvent event) {
        for (TabbedListener listener : tabbedListeners
                .getListeners(TabbedListener.class)) {
            listener.tabRemoved(event);
        }
    }

    @Override
    public JToolBar[] getToolBars() {
        return new JToolBar[]{};
    }

    public GUIFramework getFramework() {
        return framework;
    }

    @Override
    public Preferences[] getProjectPreferences() {
        return null;
    }

    @Override
    public Preferences[] getApplicationPreferences() {
        return null;
    }
}
