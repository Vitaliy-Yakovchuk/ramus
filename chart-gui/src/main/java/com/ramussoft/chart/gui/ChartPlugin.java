package com.ramussoft.chart.gui;

import com.ramussoft.chart.ChartSource;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;

public interface ChartPlugin {

    String getType();

    String getTitle();

    ChartSetupEditor createChartSetupEditor();

    ChartChangeHook createChartChangeHook(Engine engine, Element element, ChartSource chartSource);
}
