package com.ramussoft.database;

import java.util.ArrayList;
import java.util.List;

import com.ramussoft.common.Plugin;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.PluginProvider;

public abstract class AbstractDatabase implements Database {

    protected PluginFactory createPluginFactory(List<PluginProvider> list) {
        ArrayList<Plugin> plugins = new ArrayList<Plugin>();

        for (PluginProvider suit : list) {
            plugins.addAll(suit.getPlugins());
        }

        PluginFactory factory = new PluginFactory(plugins);
        return factory;
    }
}
