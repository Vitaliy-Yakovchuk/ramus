package com.ramussoft.report.data.plugin.impl;

import com.ramussoft.report.data.plugin.ConnectionPlugin;
import com.ramussoft.report.data.plugin.ConnectionPluginProvider;

public class StantardPluginProvider implements ConnectionPluginProvider {

    @Override
    public ConnectionPlugin[] getConnectionPlugins() {
        return new ConnectionPlugin[]{new ProjectionConnectionPlugin()};
    }

}
