package com.ramussoft.ai.gui;

import java.util.List;

import com.ramussoft.gui.common.AbstractGUIPluginProvider;
import com.ramussoft.gui.common.GUIPlugin;

/**
 * Registers the AI diagram action with the GUI plugin factory so the
 * "ИИ помощь" entry appears in the Service menu.
 */
public class AiDiagramGuiPluginProvider extends AbstractGUIPluginProvider {

    @Override
    public void addPlugins(List<GUIPlugin> plugins) {
        plugins.add(new AiDiagramGuiPlugin());
    }
}
