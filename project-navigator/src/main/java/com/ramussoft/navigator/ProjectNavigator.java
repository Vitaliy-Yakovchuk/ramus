package com.ramussoft.navigator;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.ramussoft.gui.common.prefrence.Options;

public class ProjectNavigator {

    private static ResourceBundle bundle = ResourceBundle
            .getBundle("com.ramussoft.navigator.global");

    /**
     * @param args
     */
    public static void main(String[] args) {
        new ProjectNavigator().start(args);
    }

    private Navigator navigator;

    private MenuItem start;

    private MenuItem stop;

    private MenuItem openBrowser;

    private MenuItem preferences;

    private Preferences preferencesDialog;

    private void start(String[] args) {
        try {
            final NavigatorDesktopComunication comunication = new NavigatorDesktopComunication() {

                @Override
                public void applyArgs(String[] args) {
                    if ((args.length >= 1) && (args[0].equals("--close"))) {
                        stop();
                        System.exit(0);
                    } else
                        preferences();
                }
            };
            if (comunication.isClient()) {
                comunication.send(args);
                System.exit(0);
                return;
            } else {
                if ((args.length >= 1) && (args[0].equals("--close"))) {
                    System.exit(0);
                    return;
                }
                Thread hook = new Thread() {
                    @Override
                    public void run() {
                        try {
                            comunication.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Runtime.getRuntime().addShutdownHook(hook);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }
        SystemTray tray = SystemTray.getSystemTray();
        TrayIcon icon = new TrayIcon(Toolkit.getDefaultToolkit().createImage(
                getClass().getResource("/com/ramussoft/navigator/navigator16x16.png")),
                getString("Application.Name"), createPopupMenu());
        icon.setImageAutoSize(true);
        try {
            tray.add(icon);
        } catch (AWTException e) {
            e.printStackTrace();
        }

        File file = new File(getPreferencesFileName());
        if ((file.exists()) && (file.canRead()))
            start();
        else
            preferences();
    }

    private PopupMenu createPopupMenu() {
        PopupMenu menu = new PopupMenu(getString("Application.Name"));
        start = new MenuItem(getString("Action.Start"));
        start.setEnabled(false);
        start.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                start();

            }
        });
        menu.add(start);
        stop = new MenuItem(getString("Action.Stop"));
        stop.setEnabled(false);
        stop.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stop();
            }
        });
        menu.add(stop);
        openBrowser = new MenuItem(getString("Action.OpenBrowser"));
        openBrowser.setEnabled(false);
        openBrowser.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                openBrowser();
            }
        });
        menu.add(openBrowser);
        menu.addSeparator();
        preferences = new MenuItem(getString("Action.Preferences"));
        menu.add(preferences);
        preferences.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                preferences();
            }
        });
        menu.addSeparator();
        menu.add(new MenuItem(getString("Action.Exit"))).addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ProjectNavigator.this.stop();
                        System.exit(0);
                    }
                });
        return menu;
    }

    protected void preferences() {
        if (preferencesDialog != null) {
            preferencesDialog.setVisible(true);
        } else {
            preferencesDialog = new Preferences(this) {
                /**
                 *
                 */
                private static final long serialVersionUID = -9084644184983934710L;

                @Override
                public void setVisible(boolean b) {
                    if (!b)
                        preferencesDialog = null;
                    super.setVisible(b);
                }
            };
            preferencesDialog.setVisible(true);

        }
    }

    protected void start() {
        Properties properties = new Properties();
        File file = new File(getPreferencesFileName());
        if ((file.exists()) && (file.canRead())) {
            try {
                FileInputStream inStream = new FileInputStream(file);
                properties.load(inStream);
                inStream.close();
                navigator = new Navigator(Integer.parseInt(properties
                        .getProperty("WebPort")));
                navigator.loadModels(properties);
                start.setEnabled(false);
                stop.setEnabled(true);
                openBrowser.setEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void stop() {
        if (navigator != null) {
            navigator.stop();
            navigator = null;
        }
        start.setEnabled(true);
        openBrowser.setEnabled(false);
        stop.setEnabled(false);
    }

    public void openBrowser() {
        navigator.openBrowser();
    }

    public static String getString(String key) {
        return bundle.getString(key);
    }

    public String getPreferencesFileName() {
        String path = Options.getPreferencesPath() + "navigator";
        new File(path).mkdirs();
        return path + File.separator + "web.conf";
    }
}
