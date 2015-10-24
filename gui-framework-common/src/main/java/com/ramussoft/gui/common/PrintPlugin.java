package com.ramussoft.gui.common;

import javax.swing.Action;

import com.ramussoft.gui.common.print.RamusPrintable;

public interface PrintPlugin {
    Action getPrintAction(GUIFramework framework, RamusPrintable printable);
}
