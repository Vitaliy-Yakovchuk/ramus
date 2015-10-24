package com.ramussoft.gui.common;

import javax.swing.JComponent;

public interface AttributePreferences {
    JComponent getComponent();

    Object getValue();

    void setValue(Object value);
}
