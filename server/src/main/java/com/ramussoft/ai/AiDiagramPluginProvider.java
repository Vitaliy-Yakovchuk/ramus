package com.ramussoft.ai;

import java.util.ArrayList;
import java.util.List;

import com.ramussoft.common.Plugin;
import com.ramussoft.common.PluginProvider;

/**
 * Plugin provider that makes the AI diagram plugin available to the engine.
 */
public class AiDiagramPluginProvider implements PluginProvider {

    @Override
    public List<Plugin> getPlugins() {
        List<Plugin> plugins = new ArrayList<Plugin>(1);
        plugins.add(new AiDiagramPlugin());
        return plugins;
    }
}
