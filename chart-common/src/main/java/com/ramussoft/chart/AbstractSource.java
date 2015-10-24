package com.ramussoft.chart;

public abstract class AbstractSource implements XMLElement {

    protected static final String ATTRIBUTE_ID = "attribute-id";

    protected static final String SOURCE = "source";

    protected ChartSource chartSource;

    AbstractSource(ChartSource source) {
        this.chartSource = source;
    }

    public ChartSource getChartSource() {
        return chartSource;
    }
}
