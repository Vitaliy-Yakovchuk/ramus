package com.ramussoft.chart.gui.charts;

import com.ramussoft.chart.gui.AbstractChartPlugin;
import com.ramussoft.chart.gui.ChartSetupEditor;

public class BarChartPlugin extends AbstractChartPlugin {

    @Override
    public ChartSetupEditor createChartSetupEditor() {
        return new BarChartSetupEditor(getType());
    }

    @Override
    public String getType() {
        return "Bar";
    }

}
