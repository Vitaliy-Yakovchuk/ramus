package com.ramussoft.chart.gui;

import com.ramussoft.chart.ChartSource;
import com.ramussoft.chart.gui.event.ChartSourceListener;

public interface ChartChangeHook {

    void addChartSourceListener(ChartSourceListener listener);

    void removeChartSourceListener(ChartSourceListener listener);

    ChartSourceListener[] getChartSourceListeners();

    void close();

    void reinit(ChartSource chartSource);

}
