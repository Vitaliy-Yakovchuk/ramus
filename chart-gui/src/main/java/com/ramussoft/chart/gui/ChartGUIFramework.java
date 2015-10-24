package com.ramussoft.chart.gui;

import java.util.Collection;

import java.util.Hashtable;

import com.ramussoft.chart.gui.charts.BarChartPlugin;
import com.ramussoft.chart.gui.charts.PieChartPlugin;
import com.ramussoft.gui.common.GUIFramework;

public class ChartGUIFramework {

    private Hashtable<String, ChartPlugin> plugins = new Hashtable<String, ChartPlugin>();

    private ChartGUIFramework() {
        addStandardPlugins();
    }

    private void addStandardPlugins() {
        addPlugin(new PieChartPlugin());
        addPlugin(new BarChartPlugin());
    }

    private void addPlugin(ChartPlugin plugin) {
        plugins.put(plugin.getType(), plugin);
    }

    public static ChartGUIFramework getFramework(GUIFramework framework) {
        ChartGUIFramework framework2 = (ChartGUIFramework) framework
                .get("ChartGUIFramework");
        if (framework2 == null) {
            framework2 = new ChartGUIFramework();
            framework.put("ChartGUIFramework", framework2);
        }
        return framework2;
    }

    public ChartPlugin getChartPlugin(String type) {
        return plugins.get(type);
    }

    public Collection<ChartPlugin> getChartPlugins() {
        return plugins.values();
    }
}
