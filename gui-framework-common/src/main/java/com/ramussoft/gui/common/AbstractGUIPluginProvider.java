package com.ramussoft.gui.common;

public abstract class AbstractGUIPluginProvider implements GUIPluginProvider {

    @Override
    public String[] getClientsSupport() {
        return null;
    }

    @Override
    public String[] getUserGroups() {
        return null;
    }
}
