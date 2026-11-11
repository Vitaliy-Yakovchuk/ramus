package com.ramussoft.local;

import java.awt.Desktop;
import java.io.File;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

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

        Main main = new Main();
        if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                .isSupported(Desktop.Action.APP_OPEN_FILE)) {
            Desktop.getDesktop().setOpenFileHandler(e -> {
                for (File file : e.getFiles())
                    main.openFromSystem(file);
            });
        }
        main.load(args);
    }

    private void openFromSystem(File file) {
        openedBySystem = true;
        SwingUtilities.invokeLater(() -> {
            if (launcher != null && launcher.isVisible())
                launcher.dispose();
        });
        new Thread(() -> open(file)).start();
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
