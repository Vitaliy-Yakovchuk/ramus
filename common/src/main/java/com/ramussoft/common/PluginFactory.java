package com.ramussoft.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import com.ramussoft.common.attribute.AttributeConverter;
import com.ramussoft.common.attribute.AttributePlugin;

/**
 * Class for plugin using and manipulation.
 *
 * @author zdd
 */

public class PluginFactory {

    private Hashtable<String, Hashtable<String, AttributeConverter>> attributeConverters = new Hashtable<String, Hashtable<String, AttributeConverter>>();

    private Hashtable<String, Hashtable<String, AttributePlugin>> aPlugins = new Hashtable<String, Hashtable<String, AttributePlugin>>();

    private List<Plugin> plugins;

    private AttributeType[] types;
    private AttributeType[] systemTypes;

    private List<AttributePlugin> attributePlugins = new ArrayList<AttributePlugin>();

    public PluginFactory(List<Plugin> plugins) {

        this.plugins = plugins;

        List<AttributeType> list = new ArrayList<AttributeType>();

        List<AttributeType> systemList = new ArrayList<AttributeType>();

        for (Plugin plugin : plugins) {
            if (plugin instanceof AttributePlugin) {
                AttributePlugin attributePlugin = (AttributePlugin) plugin;
                attributePlugins.add(attributePlugin);

                Hashtable<String, AttributePlugin> h = aPlugins
                        .get(attributePlugin.getName());
                if (h == null) {
                    h = new Hashtable<String, AttributePlugin>();
                    aPlugins.put(attributePlugin.getName(), h);
                }

                h.put(attributePlugin.getTypeName(), attributePlugin);

                AttributeConverter converter = attributePlugin
                        .getAttributeConverter();
                Hashtable<String, AttributeConverter> c = attributeConverters
                        .get(attributePlugin.getName());
                if (c == null) {
                    c = new Hashtable<String, AttributeConverter>();
                    attributeConverters.put(attributePlugin.getName(), c);
                }
                c.put(attributePlugin.getTypeName(), converter);
                if (attributePlugin.isSystem()) {
                    systemList.add(new AttributeType(attributePlugin.getName(),
                            attributePlugin.getTypeName(), attributePlugin
                            .isComparable(), attributePlugin.isLight(),
                            attributePlugin.isHistorySupport()));
                } else

                    list.add(new AttributeType(attributePlugin.getName(),
                            attributePlugin.getTypeName(), attributePlugin
                            .isComparable(), attributePlugin.isLight(),
                            attributePlugin.isHistorySupport()));
            }
        }
        types = list.toArray(new AttributeType[list.size()]);
        systemTypes = systemList.toArray(new AttributeType[systemList.size()]);
    }

    public AttributeConverter getAttributeConverter(AttributeType attributeType) {
        return attributeConverters.get(attributeType.getPluginName()).get(
                attributeType.getTypeName());
    }

    public AttributeType[] getAttributeTypes() {
        return Arrays.copyOf(types, types.length);
    }

    public List<AttributePlugin> getAttributePlugins() {
        return attributePlugins.subList(0, attributePlugins.size());
    }

    public AttributePlugin getAttributePlugin(AttributeType attributeType) {
        return aPlugins.get(attributeType.getPluginName()).get(
                attributeType.getTypeName());
    }

    public AttributeType[] getSystemAttributeTypes() {
        return Arrays.copyOf(systemTypes, systemTypes.length);
    }

    public List<Plugin> getPlugins() {
        return plugins.subList(0, plugins.size());
    }

    public void clear() {
        aPlugins = null;
        attributeConverters = null;
        attributePlugins = null;
        plugins = null;
        systemTypes = null;
        types = null;
    }

    public static void loadAdditionalSuits(String suitNames,
                                           List<PluginProvider> suits) {
        StringTokenizer st = new StringTokenizer(suitNames, ", ");
        while (st.hasMoreTokens()) {
            try {
                String className = st.nextToken();
                Class<?> clazz = Class.forName(className);
                PluginProvider suit = (PluginProvider) clazz.newInstance();
                suits.add(suit);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
