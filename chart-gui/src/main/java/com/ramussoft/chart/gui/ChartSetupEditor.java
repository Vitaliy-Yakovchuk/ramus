package com.ramussoft.chart.gui;

import javax.swing.JComponent;

import com.ramussoft.common.Element;
import com.ramussoft.gui.common.GUIFramework;

public interface ChartSetupEditor {

    void close();

    JComponent createComponent(GUIFramework framework, Element element);

    void save(Element element);

}
