package com.ramussoft.gui.common.prefrence;

import javax.swing.JComponent;
import javax.swing.JDialog;

public interface Preferences {

    JComponent createComponent();

    boolean save(JDialog dialog);

    String getTitle();

    void close();
}
