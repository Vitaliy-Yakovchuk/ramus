package com.ramussoft.excel;

import java.util.List;


import com.ramussoft.gui.common.AbstractGUIPluginProvider;
import com.ramussoft.gui.common.GUIPlugin;

public class ExcelPluginProvider extends AbstractGUIPluginProvider {

    @Override
    public void addPlugins(List<GUIPlugin> plugins) {
        plugins.add(new ExcelPlugin());
    }

}
