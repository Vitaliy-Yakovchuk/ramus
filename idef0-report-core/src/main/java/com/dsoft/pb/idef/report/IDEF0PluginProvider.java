package com.dsoft.pb.idef.report;

import com.ramussoft.report.data.plugin.ConnectionPlugin;
import com.ramussoft.report.data.plugin.ConnectionPluginProvider;

public class IDEF0PluginProvider implements ConnectionPluginProvider {

    @Override
    public ConnectionPlugin[] getConnectionPlugins() {
        return new ConnectionPlugin[]{new IDEF0ConnectionPlugin()};
    }

}
