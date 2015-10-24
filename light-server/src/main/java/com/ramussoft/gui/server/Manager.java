package com.ramussoft.gui.server;

import java.awt.AWTException;
import java.awt.Desktop;
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
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.ramussoft.Startup;
import com.ramussoft.common.Metadata;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.server.EngineFactory;

public class Manager {

    public static final ResourceBundle RES = ResourceBundle
            .getBundle("com.ramussoft.gui.server.gui");

    private OutputStream out;

    private MenuItem stopItem;

    private MenuItem startItem;

    private MenuItem restartItem;

    private boolean setup = false;

    private Preferences preferences = new Preferences();

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.setProperty("user.ramus.application.name", "Ramus Light Server");
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

        String path = Options.getPreferencesPath();
        if ((path.endsWith(File.separator)) && (path.length() > 1))
            path = path.substring(0, path.length() - 1);
        System.setProperty("ramus.server.base", path);

        Manager manager = new Manager();

        manager.start(args);

        String conf = System.getProperty("ramus.server.base") + File.separator
                + "conf" + File.separator + "ramus-database.conf";
        Properties ps = new Properties();
        File file = new File(conf);
        if (file.exists()) {
            try {
                FileInputStream stream = new FileInputStream(file);
                ps.load(stream);
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if ("true".equals(ps.getProperty("Autostart"))) {
                manager.start();
            }
        } else {
            manager.setup();
        }
    }

    private void start(String[] args) {
        SystemTray tray = SystemTray.getSystemTray();
        TrayIcon icon = new TrayIcon(Toolkit.getDefaultToolkit().createImage(
                getClass().getResource(
                        "/com/ramussoft/gui/server/application.png")),
                getString("Server") + " " + Metadata.getApplicationName(),
                createPopupMenu());
        icon.setImageAutoSize(true);
        try {
            tray.add(icon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private PopupMenu createPopupMenu() {
        PopupMenu menu = new PopupMenu(getString("Server") + " "
                + Metadata.getApplicationName());
        menu.add(createStart());
        menu.add(createStop());
        menu.addSeparator();
        menu.add(createRestart());
        menu.addSeparator();
        menu.add(createPreferences());
        menu.addSeparator();
        menu.add(createExit());
        return menu;
    }

    private MenuItem createPreferences() {
        MenuItem pItem = new MenuItem(getString("Action.Preferences"));
        pItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setup();
            }
        });
        return pItem;
    }

    protected void setup() {
        if (setup) {
            preferences.requestFocus();
        } else {
            setup = true;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    preferences.load();
                    preferences.setVisible(true);
                    setup = false;
                }
            });
        }
    }

    private MenuItem createExit() {
        MenuItem exitItem = new MenuItem(GlobalResourcesManager
                .getString("Action.Exit"));
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
        return exitItem;
    }

    protected void exit() {
        if (stopItem.isEnabled())
            stop();
        System.exit(0);
    }

    private MenuItem createStart() {
        startItem = new MenuItem(getString("Action.Start"));
        startItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                start();
            }
        });
        return startItem;
    }

    protected void start() {
        Startup startup = new Startup() {
            @Override
            protected void addHooks(boolean close, Process p) {
                super.addHooks(close, p);
                out = p.getOutputStream();
            }
        };
        try {
            startup.start(new String[]{"com.ramussoft.gui.server.Main"});

            try {
                Desktop.getDesktop().browse(
                        new URI("http://127.0.0.1:"
                                + EngineFactory.getPropeties().getProperty(
                                "WebPort") + "/"));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        stopItem.setEnabled(true);
        restartItem.setEnabled(true);
        startItem.setEnabled(false);
    }

    private MenuItem createStop() {
        stopItem = new MenuItem(getString("Action.Stop"));
        stopItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stop();
            }
        });
        stopItem.setEnabled(false);
        return stopItem;
    }

    protected void stop() {
        PrintStream printStream = new PrintStream(out);
        printStream.println("stop");
        printStream.flush();
        printStream.close();
        restartItem.setEnabled(false);
        stopItem.setEnabled(false);
        startItem.setEnabled(true);
    }

    private MenuItem createRestart() {
        restartItem = new MenuItem(getString("Action.Restart"));
        restartItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restart();
            }
        });
        restartItem.setEnabled(false);
        return restartItem;
    }

    protected void restart() {
        stop();
        start();
    }

    public static String getString(String key) {
        return RES.getString(key);
    }

}
