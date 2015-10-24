package com.ramussoft.chart;

import java.util.Hashtable;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;

public class ChartDataFramework {

    private Engine engine;

    public static ChartDataFramework getChartDataFramework(Engine engine) {
        ChartDataFramework res = (ChartDataFramework) engine.getPluginProperty(
                "Chart", "DataFramework");
        if (res == null) {
            res = new ChartDataFramework(engine);
            engine.setPluginProperty("Chart", "DataFramework", res);
        }
        return res;
    }

    public static String getPreferencesPath(Element element, Attribute attribute) {
        return "/elements/" + element.getId() + "/" + attribute.getId()
                + "/chart.xml";
    }

    private Hashtable<String, ChartDataPlugin> data = new Hashtable<String, ChartDataPlugin>();

    public ChartDataFramework(Engine engine) {
        this.engine = engine;
        addStandardPlugins();
    }

    private void addStandardPlugins() {
        addPlugin(new PieChartDataPlugin(engine));
        addPlugin(new BarChartDataPlugin(engine));
    }

    private void addPlugin(ChartDataPlugin chartDataPlugin) {
        data.put(chartDataPlugin.getType(), chartDataPlugin);
    }

    public ChartDataPlugin getChartDataPlugin(String type) {
        return data.get(type);
    }
}
