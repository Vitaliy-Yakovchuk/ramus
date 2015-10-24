package com.ramussoft.chart.gui;

import java.util.List;

import com.ramussoft.gui.common.AbstractGUIPluginProvider;
import com.ramussoft.gui.common.GUIPlugin;

public class ChartGUIProvider extends AbstractGUIPluginProvider {

    @Override
    public void addPlugins(List<GUIPlugin> plugins) {
        plugins.add(new ChartGUIPlugin());
        plugins.add(new ChartLinkAttributePlugin());
        plugins.add(new TableChartAttributePlugin());
    }

    @Override
    public String[] getUserGroups() {
        return new String[]{"admin", "chart"};
    }
}
