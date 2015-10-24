package com.ramussoft.gui.common;

import java.util.Iterator;

import java.util.List;

import com.ramussoft.common.AdditionalPluginLoader;
import com.ramussoft.common.Metadata;

public class AdditionalGUIPluginLoader {

    @SuppressWarnings("unchecked")
    public static void loadAdditionalGUIPlugins(List<GUIPlugin> plugins,
                                                String clientType, String[] groups) {
        Iterator<GUIPluginProvider> providers = AdditionalPluginLoader
                .loadProviders(GUIPluginProvider.class);
        while (providers.hasNext()) {
            GUIPluginProvider provider = providers.next();
            if (Metadata.EDUCATIONAL) {
                if ("com.ramussoft.idef0.IDEF0GUIPluginProvider"
                        .equals(provider.getClass().getCanonicalName()))
                    provider.addPlugins(plugins);
            } else {
                if (clientType == null)
                    provider.addPlugins(plugins);
                else {
                    String[] clients = provider.getClientsSupport();
                    if (clients != null) {
                        boolean add = false;
                        for (String client : clients)
                            if (clientType.equals(client)) {
                                add = true;
                            }
                        if (!add)
                            continue;
                    }
                    String[] grps = provider.getUserGroups();
                    if (grps != null) {
                        boolean add = false;
                        for (String grp : grps) {
                            for (String group : groups) {
                                if (grp.equals(group))
                                    add = true;
                            }
                        }
                        if (!add)
                            continue;
                    }
                    provider.addPlugins(plugins);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadPrintPlugins(List<PrintPlugin> plugins) {
        Iterator<PrintPlugin> providers = AdditionalPluginLoader
                .loadProviders(PrintPlugin.class);
        while (providers.hasNext()) {
            PrintPlugin provider = providers.next();

            plugins.add(provider);
        }
    }

}
