package com.ramussoft.client;

import java.util.List;

import com.ramussoft.common.PluginProvider;
import com.ramussoft.gui.common.GUIPlugin;
import com.ramussoft.script.ScriptGUIPluginProvider;

/**
 * Звичайний TcpLightClient, але з "випиляними" зайвими модулями (механізм
 * підключення модулів вимкнений).
 */
public class MinimalisticTcpClient extends TcpLightClient {

    public static void main(String[] args) {
        try {
            new MinimalisticTcpClient().start(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MinimalisticTcpClient() {
        loadPlugins = false;
    }

    @Override
    protected void initAdditionalPluginSuits(List<PluginProvider> suits) {
    }

    @Override
    protected void initAdditionalGuiPlugins(List<GUIPlugin> list) {
        super.initAdditionalGuiPlugins(list);
        new ScriptGUIPluginProvider().addPlugins(list);
    }

}
