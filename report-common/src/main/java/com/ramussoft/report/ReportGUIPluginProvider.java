package com.ramussoft.report;

import java.util.List;

import com.ramussoft.gui.common.AbstractGUIPluginProvider;
import com.ramussoft.gui.common.GUIPlugin;

public class ReportGUIPluginProvider extends AbstractGUIPluginProvider {

    @Override
    public void addPlugins(List<GUIPlugin> plugins) {
        plugins.add(new ReportViewPlugin());
    }

    @Override
    public String[] getClientsSupport() {
        return new String[]{CAJO};
    }

    @Override
    public String[] getUserGroups() {
        return new String[]{"admin", "report"};
    }
}
