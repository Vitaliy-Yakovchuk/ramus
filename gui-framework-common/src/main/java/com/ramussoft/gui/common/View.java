package com.ramussoft.gui.common;

import javax.swing.Action;
import javax.swing.JComponent;

import com.ramussoft.gui.common.event.ActionChangeListener;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ViewTitleListener;

public interface View {

    Action[] getActions();

    JComponent createComponent();

    void onAction(ActionEvent event);

    void addViewTitleListener(ViewTitleListener listener);

    void removeViewTitleListener(ViewTitleListener listener);

    ViewTitleListener[] getViewTitleListeners();

    void addActionChangeListener(ActionChangeListener listener);

    void removeActionChangeListener(ActionChangeListener listener);

    ActionChangeListener[] getActionChangeListeners();

    void close();

    void focusGained();

    void focusLost();

    ActionEvent getOpenAction();

    ActionEvent getOpenActionForSave();

    String[] getGlobalActions();
}
