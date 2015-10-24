package com.ramussoft.common.attribute;

import java.util.ArrayList;
import java.util.List;

import com.ramussoft.common.PluginProvider;
import com.ramussoft.common.Plugin;

/**
 * This class uses for attribute plug-in suit.
 *
 * @author zdd
 */

public abstract class AttributePluginProvider implements PluginProvider {

    public abstract AttributePlugin[] getAttributePlugins();

    public abstract AttributePlugin[] getSystemAttributePlugins();

    @Override
    public List<Plugin> getPlugins() {
        List<Plugin> list = new ArrayList<Plugin>();

        for (AttributePlugin plugin : getSystemAttributePlugins())
            list.add(plugin);

        for (AttributePlugin plugin : getAttributePlugins())
            list.add(plugin);

        return list;
    }

}
