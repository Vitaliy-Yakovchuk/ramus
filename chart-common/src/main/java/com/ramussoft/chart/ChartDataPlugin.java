package com.ramussoft.chart;

import org.jfree.chart.JFreeChart;

import com.ramussoft.common.Element;

public interface ChartDataPlugin {

    JFreeChart createChart(Element element);

    JFreeChart createChart(Element element, ChartSource source);

    String getType();
}
