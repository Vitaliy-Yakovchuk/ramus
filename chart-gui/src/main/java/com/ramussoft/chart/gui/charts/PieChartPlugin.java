package com.ramussoft.chart.gui.charts;

import com.ramussoft.chart.gui.AbstractChartPlugin;
import com.ramussoft.chart.gui.ChartSetupEditor;

public class PieChartPlugin extends AbstractChartPlugin {

    @Override
    public ChartSetupEditor createChartSetupEditor() {
        return new PieChartSetupEditor(getType());
    }

    @Override
    public String getType() {
        return "Pie";
    }

}
