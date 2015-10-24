package com.ramussoft.gui.common;

import javax.swing.Action;
import javax.swing.JComponent;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;

public abstract class AbstractAttributeEditor implements AttributeEditor {

    @Override
    public void close() {
    }

    @Override
    public Action[] getActions() {
        return new Action[]{};
    }

    @Override
    public boolean isAcceptable() {
        return true;
    }

    @Override
    public JComponent getLastComponent() {
        return null;
    }

    @Override
    public void apply(Engine engine, Element element, Attribute attribute,
                      Object value) {
        engine.setAttribute(element, attribute, getValue());
    }

    @Override
    public boolean canApply() {
        return true;
    }

    @Override
    public void showErrorMessage() {
    }

    @Override
    public boolean isSaveAnyway() {
        return false;
    }
}
