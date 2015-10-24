package com.ramussoft.report;

import java.util.ArrayList;
import java.util.List;

import com.ramussoft.common.Plugin;
import com.ramussoft.common.PluginProvider;

public class ReportPluginProvider implements PluginProvider {

    @Override
    public List<Plugin> getPlugins() {
        List<Plugin> list = new ArrayList<Plugin>(1);
        list.add(new ReportPlugin());
        return list;
    }

}
