package com.ramussoft.idef0;

import java.util.List;

import com.ramussoft.gui.common.AbstractGUIPluginProvider;
import com.ramussoft.gui.common.GUIPlugin;
import com.ramussoft.idef0.attribute.ColorAttributePlugin;
import com.ramussoft.idef0.attribute.DFDSNameAttributePlugin;
import com.ramussoft.idef0.attribute.FontAttributePlugin;
import com.ramussoft.idef0.attribute.FunctionOwnerAttributePlugin;
import com.ramussoft.idef0.attribute.FunctionTypeAttributePlugin;

public class IDEF0GUIPluginProvider extends AbstractGUIPluginProvider {

    @Override
    public void addPlugins(List<GUIPlugin> plugins) {
        plugins.add(new IDEF0ViewPlugin());
        plugins.add(new ColorAttributePlugin());
        plugins.add(new FontAttributePlugin());
        plugins.add(new FunctionTypeAttributePlugin());
        plugins.add(new FunctionOwnerAttributePlugin());
        plugins.add(new DFDSNameAttributePlugin());
    }

    @Override
    public String[] getClientsSupport() {
        return new String[]{CAJO};
    }

    @Override
    public String[] getUserGroups() {
        return new String[]{"admin", "idef0"};
    }
}
