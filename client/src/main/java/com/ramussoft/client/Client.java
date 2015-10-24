package com.ramussoft.client;

import java.util.ArrayList;
import java.util.TimeZone;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.AdditionalPluginLoader;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Metadata;
import com.ramussoft.common.Plugin;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.PluginProvider;
import com.ramussoft.core.attribute.simple.SimpleAttributePluginSuit;
import com.ramussoft.core.persistent.PersistentFactory;
import com.ramussoft.gui.attribute.icon.IconFactory;
import com.ramussoft.gui.common.AbstractGUIPluginFactory;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GUIPlugin;
import com.ramussoft.gui.common.SplashScreen;
import com.ramussoft.gui.common.event.CloseMainFrameAdapter;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.core.GUIPluginFactory;
import com.ramussoft.gui.core.simple.SimleGUIPluginFactory;
import com.ramussoft.gui.qualifier.QualifierPluginSuit;
import com.ramussoft.net.common.Group;
import com.ramussoft.net.common.User;

public abstract class Client {

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+2:00"));
    }

    protected Engine e;

    protected GUIFramework framework;

    /**
     * Ознака, чи використовувати механізм автозаванатаження додаткових модулів.
     */
    protected boolean loadPlugins = true;

    private PluginFactory createPluginFactory(List<PluginProvider> list) {
        ArrayList<Plugin> plugins = new ArrayList<Plugin>();

        for (PluginProvider suit : list) {
            plugins.addAll(suit.getPlugins());
        }

        PluginFactory factory = new PluginFactory(plugins);
        return factory;
    }

    public void run(final String[] args) {
        final SplashScreen screen = new SplashScreen() {
            /**
             *
             */
            private static final long serialVersionUID = -4641958771849938048L;

            @Override
            protected String getImageName() {
                for (String s : args)
                    if (s.equals("-season"))
                        return "/com/ramussoft/season/about.png";
                return "/com/ramussoft/gui/about.png";
            }
        };
        if (Metadata.CLIENT) {
            Metadata.REGISTERED_FOR = ClientConnection.getName();
            Metadata.DEMO_REGISTERED = ClientConnection.getName() != null;
        }

        screen.setLocationRelativeTo(null);
        boolean hide = false;
        for (String s : args)
            if ("--hide-splash".equals(s))
                hide = true;
        if (!hide)
            screen.setVisible(true);

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {

                List<PluginProvider> suits = new ArrayList<PluginProvider>();

                suits.add(new SimpleAttributePluginSuit());

                initAdditionalPluginSuits(suits);

                PluginFactory factory = createPluginFactory(suits);

                PersistentFactory persistentFactory = new PersistentFactory(
                        null, factory.getAttributePlugins(), null);

                persistentFactory.reinit();

                Engine engine = getEngine(factory, persistentFactory);

                AccessRules rules = getAccessRules();

                e = engine;

                e.setPluginProperty("Core", "PluginList", factory.getPlugins());
                e.setPluginProperty("Core", "PluginFactory", factory);

                LightClient.staticEngine = e;
                LightClient.staticAccessRules = rules;

                List<GUIPlugin> list = new ArrayList<GUIPlugin>();

                QualifierPluginSuit.addPlugins(list, e, rules);

                initAdditionalGuiPlugins(list);

                User me = getMe();

                List<Group> groups = me.getGroups();
                String[] grps = new String[groups.size()];
                for (int i = 0; i < grps.length; i++)
                    grps[i] = groups.get(i).getName();

                AbstractGUIPluginFactory factory1;
                String ws = Options.getString("WindowsControl", "simple");
                if (ws.equals("simple"))
                    factory1 = new SimleGUIPluginFactory(list, e, rules,
                            getType(), grps, loadPlugins);
                else
                    factory1 = new GUIPluginFactory(list, e, rules, getType(),
                            grps, loadPlugins);

                framework = factory1.getFramework();
                framework
                        .addCloseMainFrameListener(new CloseMainFrameAdapter() {
                            @Override
                            public void afterClosed() {
                                Client.this.close();
                            }
                        });

                final JFrame frame = factory1.getMainFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                String title = getTitle();
                frame.setTitle(title);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        frame.setVisible(true);
                        screen.setVisible(false);
                    }
                });
            }
        });
        t.start();
        Thread thread = new Thread("Icons-buffer-cleaner") {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(20000);
                        IconFactory.clearIconsBuffer(LightClient.staticEngine);
                        IconFactory
                                .clearQualifierIconsBuffer(LightClient.staticEngine);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    protected void initAdditionalPluginSuits(List<PluginProvider> suits) {
        AdditionalPluginLoader.loadAdditionalSuits(suits);
    }

    protected String getTitle() {
        return Metadata.getApplicationName();
    }

    protected void initAdditionalGuiPlugins(List<GUIPlugin> plugins) {
    }

    protected abstract Engine getEngine(PluginFactory factory,
                                        PersistentFactory persistentFactory);

    protected abstract AccessRules getAccessRules();

    protected abstract User getMe();

    protected abstract String getType();

    protected void close() {
    }

}
