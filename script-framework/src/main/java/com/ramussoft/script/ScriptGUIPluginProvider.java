package com.ramussoft.script;

import java.util.List;

import com.ramussoft.gui.common.AbstractGUIPluginProvider;
import com.ramussoft.gui.common.GUIPlugin;

public class ScriptGUIPluginProvider extends AbstractGUIPluginProvider {

    @Override
    public void addPlugins(List<GUIPlugin> plugins) {
        plugins.add(new ScriptPlugin());
    }

    @Override
    public String[] getUserGroups() {
        return new String[]{"admin", "scripts"};
    }
}
