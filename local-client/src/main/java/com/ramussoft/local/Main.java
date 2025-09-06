package com.ramussoft.local;

import java.awt.Desktop;
import java.util.List;

import javax.swing.JDialog;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Plugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GUIPlugin;
import com.ramussoft.gui.core.AboutDialog;

public class Main extends Runner {

    public static void main(String[] args) {
        // Set macOS-specific system properties
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", "Ramus");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Ramus MacOS");
        }

        new Main().load(args);
    }

    @Override
    protected void postShowVisibaleMainFrame(Engine engine, GUIFramework framework) {
        super.postShowVisibaleMainFrame(engine, framework);

        // Properly wire the macOS About menu to the app's About dialog
        if (Desktop.isDesktopSupported() &&
            System.getProperty("os.name", "").toLowerCase().contains("mac")) {
            try {
                Desktop.getDesktop().setAboutHandler(e -> {
                    @SuppressWarnings("unchecked")
                    List<Plugin> corePlugins = (List<Plugin>) engine.getPluginProperty("Core", "PluginList");
                    @SuppressWarnings("unchecked")
                    List<GUIPlugin> guiPlugins = (List<GUIPlugin>) engine.getPluginProperty("GUI", "PluginList");

                    AboutDialog dialog = new AboutDialog(
                            framework.getMainFrame(), corePlugins, guiPlugins);
                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dialog.setVisible(true);
                    dialog.dispose();
                });
            } catch (UnsupportedOperationException ex) {
                System.err.println("macOS About handler unsupported: " + ex.getMessage());
            } catch (Throwable t) {
                System.err.println("Failed to set macOS About handler: " + t.getMessage());
            }
        }
    }
}
