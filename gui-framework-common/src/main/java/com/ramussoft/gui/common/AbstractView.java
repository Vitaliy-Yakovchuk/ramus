package com.ramussoft.gui.common;

import javax.swing.Action;
import javax.swing.event.EventListenerList;

import com.ramussoft.gui.common.event.ActionChangeEvent;
import com.ramussoft.gui.common.event.ActionChangeListener;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ViewTitleEvent;
import com.ramussoft.gui.common.event.ViewTitleListener;

public abstract class AbstractView implements View {

    protected GUIFramework framework;

    private EventListenerList listeners = new EventListenerList();

    @Override
    public void addViewTitleListener(ViewTitleListener listener) {
        listeners.add(ViewTitleListener.class, listener);
    }

    @Override
    public ViewTitleListener[] getViewTitleListeners() {
        return listeners.getListeners(ViewTitleListener.class);
    }

    @Override
    public void removeViewTitleListener(ViewTitleListener listener) {
        listeners.remove(ViewTitleListener.class, listener);
    }

    @Override
    public void addActionChangeListener(ActionChangeListener listener) {
        listeners.add(ActionChangeListener.class, listener);
    }

    @Override
    public void removeActionChangeListener(ActionChangeListener listener) {
        listeners.remove(ActionChangeListener.class, listener);
    }

    @Override
    public ActionChangeListener[] getActionChangeListeners() {
        return listeners.getListeners(ActionChangeListener.class);
    }

    public AbstractView(GUIFramework framework) {
        this.framework = framework;
    }

    @Override
    public void onAction(ActionEvent event) {
    }

    @Override
    public void close() {
    }

    public void titleChanged(ViewTitleEvent event) {
        for (ViewTitleListener listener : getViewTitleListeners()) {
            listener.titleChanged(event);
        }
    }

    public void actionsAdded(Action[] actions) {
        ActionChangeEvent event = new ActionChangeEvent(actions);
        for (ActionChangeListener listener : getActionChangeListeners())
            listener.actionsAdded(event);
    }

    public void actionsRemoved(Action[] actions) {
        ActionChangeEvent event = new ActionChangeEvent(actions);
        for (ActionChangeListener listener : getActionChangeListeners())
            listener.actionsRemoved(event);
    }

    @Override
    public void focusGained() {
    }

    @Override
    public void focusLost() {
    }

    @Override
    public ActionEvent getOpenAction() {
        if (this instanceof UniqueView) {
            UniqueView view = (UniqueView) this;
            return new ActionEvent(ActionEvent.OPEN_STATIC_VIEW, view.getId());
        }
        return null;
    }

    @Override
    public ActionEvent getOpenActionForSave() {
        return getOpenAction();
    }

    public String getString(String key) {
        return GlobalResourcesManager.getString(key);
    }

    @Override
    public String[] getGlobalActions() {
        return new String[]{};
    }

    public GUIFramework getFramework() {
        return framework;
    }
}
