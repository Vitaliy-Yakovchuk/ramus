package com.ramussoft.gui.common;

import javax.swing.Action;
import javax.swing.JComponent;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;

public interface AttributeEditor {

    JComponent getComponent();

    Object setValue(Object value);

    Object getValue();

    void close();

    Action[] getActions();

    boolean isAcceptable();

    JComponent getLastComponent();

    void apply(Engine engine, Element element, Attribute attribute, Object value);

    void showErrorMessage();

    boolean canApply();

    boolean isSaveAnyway();
}
