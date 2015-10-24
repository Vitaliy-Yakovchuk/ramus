package com.ramussoft.gui.common;

import javax.swing.JComponent;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;

public interface QualifierSetupEditor {

    JComponent createComponent();

    String getTitle();

    void save(Engine engine, Qualifier qualifier);

    void load(Engine engine, Qualifier qualifier);

    String[] getErrors();

}
