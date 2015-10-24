package com.ramussoft.chart.core;

import java.util.ArrayList;
import java.util.List;

import com.ramussoft.common.Plugin;
import com.ramussoft.common.PluginProvider;

public class ChartPluginProvider implements PluginProvider {

    @Override
    public List<Plugin> getPlugins() {
        List<Plugin> list = new ArrayList<Plugin>(1);
        list.add(new ChartPlugin());
        list.add(new ChartLinkPlugin());
        list.add(new TableChartPlugin());
        return list;
    }

}
