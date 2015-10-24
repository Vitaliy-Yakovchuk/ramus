package com.ramussoft.chart.gui.event;

import java.util.EventListener;

public interface ChartSourceListener extends EventListener {

    void sourceChanged(ChartSourceEvent event);

    void chartRemoved(ChartSourceEvent event);

}
