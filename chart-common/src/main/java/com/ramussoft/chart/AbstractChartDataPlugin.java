package com.ramussoft.chart;

import org.jfree.chart.JFreeChart;

import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.eval.EObject;

import static com.ramussoft.chart.ChartDataFramework.getPreferencesPath;

public abstract class AbstractChartDataPlugin implements ChartDataPlugin {

    protected Engine engine;

    public AbstractChartDataPlugin(Engine engine) {
        this.engine = engine;
    }

    @Override
    public JFreeChart createChart(Element element) {
        String path = getPreferencesPath(element, StandardAttributesPlugin
                .getAttributeNameAttribute(engine));
        ChartSource source = new ChartSource(engine);
        source.load(engine.getInputStream(path));
        return createChart(element, source);
    }

    protected String toString(Object value) {
        return new EObject(value).stringValue();
    }

    protected double toDouble(Object value) {
        return new EObject(value).doubleValue();
    }

}
