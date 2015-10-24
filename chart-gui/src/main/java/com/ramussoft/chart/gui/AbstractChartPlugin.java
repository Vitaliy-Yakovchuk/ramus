package com.ramussoft.chart.gui;

import com.ramussoft.chart.ChartSource;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;

public abstract class AbstractChartPlugin implements ChartPlugin {

    @Override
    public String getTitle() {
        return ChartResourceManager.getString("ChartType." + getType());
    }

    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChartPlugin) {
            ChartPlugin chartPlugin = (ChartPlugin) obj;
            return getType().equals(chartPlugin.getType());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getType().hashCode();
    }

    @Override
    public ChartChangeHook createChartChangeHook(Engine engine,
                                                 Element element, ChartSource chartSource) {
        return new DefaultChartChangeHook(engine, element, chartSource);
    }

}
