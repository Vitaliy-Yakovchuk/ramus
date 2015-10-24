package com.ramussoft.common;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public class AdditionalPluginLoader {

    public static void loadAdditionalSuits(List<PluginProvider> providers) {

        ServiceLoader<PluginProvider> serviceLoader = ServiceLoader
                .load(PluginProvider.class);

        Iterator<PluginProvider> iterator = serviceLoader.iterator();

        while (iterator.hasNext()) {
            PluginProvider provider = iterator.next();
            providers.add(provider);
        }
    }

    @SuppressWarnings("unchecked")
    public static Iterator loadProviders(Class clazz) {
        ServiceLoader sl = ServiceLoader.load(clazz);
        return sl.iterator();
    }

}
