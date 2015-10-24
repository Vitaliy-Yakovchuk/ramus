package com.ramussoft.gui.common;

import java.util.List;

public interface GUIPluginProvider {

    public static final String CAJO = "cajo";

    public static final String HTTP = "http";

    void addPlugins(List<GUIPlugin> plugins);

    /**
     * cajo//http
     *
     * @return
     */

    String[] getClientsSupport();

    String[] getUserGroups();

}
