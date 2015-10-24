package com.ramussoft.client;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXLoginPane;
import org.jdesktop.swingx.JXLoginPane.JXLoginFrame;
import org.jdesktop.swingx.JXLoginPane.Status;
import org.jdesktop.swingx.auth.LoginService;

import com.ramussoft.client.log.LogPlugin;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Plugin;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.cached.Cached;
import com.ramussoft.common.cached.CachedEngine;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.common.logger.ILog;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.core.persistent.PersistentFactory;
import com.ramussoft.gui.common.GUIPlugin;
import com.ramussoft.gui.common.GUIPluginProvider;
import com.ramussoft.gui.common.UndoRedoPlugin;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.net.common.Group;
import com.ramussoft.net.common.User;
import com.ramussoft.net.common.UserFactory;
import com.ramussoft.net.common.UserProvider;
import com.ramussoft.net.common.tcp.CallParameters;
import com.ramussoft.net.common.tcp.EngineInvocker;
import com.ramussoft.net.common.tcp.EvenstHolder;
import com.ramussoft.net.common.tcp.Result;

public class TcpLightClient extends Client {
    private AccessRules rules;

    private UserProvider userProvider;

    private User me;

    private TcpClientConnection connection;

    private Long sessionId;

    private TcpClientEngine tcpClientEngine;

    private static boolean season;

    private String host;

    private String port;

    public static void main(String[] args) {
        try {
            for (String s : args)
                if (s.equals("-season")) {
                    System.setProperty("user.ramus.application.name", "Season");
                    // System.setProperty("user.ramus.application.version",
                    // "0.0.4");
                    // System.setProperty("user.ramus.application.extension",
                    // ".snf");
                    // System.setProperty("user.ramus.file.version", "0.0.3");
                    season = true;
                }
            new TcpLightClient().start(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void start(final String[] args) throws Exception {

        if (args.length < 2) {
            System.err
                    .println("Usage java -jar ... url ..., for example: java -jar my.jar localhost 38617 ");
            return;
        }
        try {

            String lookAndFeel = Options.getString("LookAndFeel");

            if (lookAndFeel != null)
                UIManager.setLookAndFeel(lookAndFeel);
            else {
                if ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
                        .equals(UIManager.getSystemLookAndFeelClassName()))
                    UIManager.setLookAndFeel(UIManager
                            .getCrossPlatformLookAndFeelClassName());
                else
                    UIManager.setLookAndFeel(UIManager
                            .getSystemLookAndFeelClassName());
            }
            UIManager.put("swing.boldMetal", Boolean.FALSE);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        StandardAttributesPlugin.setDefaultDisableAutoupdate(true);

        this.host = args[0];
        this.port = args[1];

        try {

            final PrintStream old = System.err;
            System.setErr(new PrintStream(new OutputStream() {

                FileOutputStream fos = null;

                private boolean err = false;

                @Override
                public void write(final int b) throws IOException {
                    getFos();
                    if (!err)
                        fos.write(b);
                    old.write(b);
                }

                private FileOutputStream getFos() throws IOException {
                    if (fos == null) {
                        try {
                            System.out.println("Getting calendar");
                            final Calendar c = Calendar.getInstance();
                            System.out.println("Getting options path");
                            String name = System.getProperty("user.home");
                            if (!name.equals(File.separator))
                                name += File.separator;
                            name += ".ramus" + File.separator + "log";
                            System.out.println("Creating dir: " + name);
                            new File(name).mkdirs();
                            name += File.separator + c.get(Calendar.YEAR) + "_"
                                    + c.get(Calendar.MONTH) + "_"
                                    + c.get(Calendar.DAY_OF_MONTH) + "_"
                                    + c.get(Calendar.HOUR_OF_DAY) + "_"
                                    + c.get(Calendar.MINUTE) + "_"
                                    + c.get(Calendar.SECOND) + "_"
                                    + c.get(Calendar.MILLISECOND)
                                    + "-client.log";
                            fos = new FileOutputStream(name);
                        } catch (final Exception e) {
                            err = true;
                            e.printStackTrace(System.out);
                            // throw e;
                        }
                    }
                    return fos;
                }

            }));

            connection = new TcpClientConnection(args[0],
                    Integer.parseInt(args[1])) {

                private boolean exitShown = false;

                @Override
                protected void objectReaded(Object object) {
                    if (tcpClientEngine != null)
                        tcpClientEngine.call((EvenstHolder) object);
                }

                @Override
                protected void showDialogEndExit(String message) {
                    if (exitShown)
                        return;
                    exitShown = true;
                    System.err.println("Connection lost");
                    // JOptionPane.showMessageDialog(framework.getMainFrame(),
                    // message);
                    System.exit(1);
                }
            };

            connection.start();

            Boolean canLogin = (Boolean) connection.invoke("canLogin",
                    new Object[]{});
            if (!canLogin) {
                ResourceBundle bundle = ResourceBundle
                        .getBundle("com.ramussoft.client.client");
                JOptionPane.showMessageDialog(null,
                        bundle.getString("Message.ServerBusy"));
                System.exit(1);
                return;
            }

            final JXLoginFrame frame = JXLoginPane
                    .showLoginFrame(new LoginService() {

                        @Override
                        public boolean authenticate(String name,
                                                    char[] passwordChars, String server)
                                throws Exception {

                            String password = new String(passwordChars);
                            try {
                                sessionId = (Long) connection.invoke("login",
                                        new Object[]{name, password});
                                if ((sessionId == null)
                                        || (sessionId.longValue() < 0l)) {
                                    return false;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw e;
                            }
                            return sessionId != null;
                        }

                        @Override
                        public void cancelAuthentication() {
                            System.exit(0);
                        }

                    });
            frame.setIconImage(Toolkit.getDefaultToolkit().getImage(
                    getClass()
                            .getResource("/com/ramussoft/gui/application.png")));
            frame.setVisible(true);

            frame.addPropertyChangeListener("status",
                    new PropertyChangeListener() {

                        private boolean run = false;

                        {
                            Thread t = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(120000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if (!run)
                                        System.exit(0);
                                }
                            };

                            t.start();
                        }

                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            Status status = frame.getStatus();
                            if ((status.equals(Status.CANCELLED))
                                    || (status.equals(Status.NOT_STARTED))
                                    || (status.equals(Status.FAILED))
                                    || (status.equals(Status.IN_PROGRESS))) {
                                return;
                            }

                            if (run)
                                return;

                            run = true;

                            userProvider = (UserProvider) createDeligate(UserProvider.class);

                            if (season) {
                                boolean exit = true;
                                for (Group g : getMe().getGroups()) {
                                    if (("admin".equals(g.getName()))
                                            || ("season".equals(g.getName()))) {
                                        exit = false;
                                    }
                                }
                                if (exit) {
                                    JOptionPane
                                            .showMessageDialog(null,
                                                    "Тільки користувач групи season або admin може працювати з системою планування");
                                    System.exit(5);
                                    return;
                                }
                            }

                            rules = (AccessRules) createDeligate(AccessRules.class);
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    TcpLightClient.this.run(args);
                                }
                            });
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Неможливо з’єднатись з сервером, дивіться log-файл для деталей "
                            + e.getLocalizedMessage());
            System.exit(1);
        }
    }

    private Object createDeligate(@SuppressWarnings("rawtypes") Class[] classes) {
        return Proxy.newProxyInstance(getClass().getClassLoader(), classes,
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method,
                                         Object[] args) throws Throwable {

                        CallParameters parameters = new CallParameters(method
                                .getName(), args);

                        Result result = connection.call(parameters);
                        if (result.exception != null)
                            throw result.exception;
                        return result.result;
                    }
                });
    }

    @Override
    protected AccessRules getAccessRules() {
        return rules;
    }

    @Override
    protected void initAdditionalGuiPlugins(List<GUIPlugin> list) {
        super.initAdditionalGuiPlugins(list);
        list.add(new ClientPlugin(userProvider, true));
        list.add(new LogPlugin());
        try {
            if ((Boolean) connection.invoke("canUndoRedo", new Object[]{}))
                list.add(new UndoRedoPlugin(e));
            if ((Boolean) connection.invoke("isAdmin", new Object[]{})) {
                UserFactory userFactory;
                userFactory = (UserFactory) createDeligate(UserFactory.class);
                AdminPanelPlugin adminPanelPlugin = new AdminPanelPlugin(
                        userFactory, e);
                list.add(adminPanelPlugin);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Object createDeligate(@SuppressWarnings("rawtypes") Class class1) {
        return createDeligate(new Class[]{class1});
    }

    @Override
    protected String getTitle() {
        return super.getTitle() + " - TCP (" + host + ":" + port + ") - "
                + getMe().getName();
    }

    @Override
    protected void close() {
        try {
            connection.setClosed(true);
            connection.invoke("closeConnection", new Object[]{});
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Engine getEngine(PluginFactory factory,
                               PersistentFactory persistentFactory) {
        tcpClientEngine = new TcpClientEngine(
                (EngineInvocker) Proxy.newProxyInstance(getClass()
                                .getClassLoader(),
                        new Class[]{EngineInvocker.class},
                        new InvocationHandler() {

                            @Override
                            public Object invoke(Object proxy, Method method,
                                                 Object[] args) throws Throwable {
                                return connection.invoke(method.getName(), args);
                            }
                        }), connection);
        List<Class> interfaces = new ArrayList<Class>();
        interfaces.add(Engine.class);
        interfaces.add(Journaled.class);
        interfaces.add(ILog.class);


        for (Plugin plugin : factory.getPlugins())
            if (plugin.getFunctionalInterface() != null)
                interfaces.add(plugin.getFunctionalInterface());

        final Engine engine1 = (Engine) Proxy.newProxyInstance(getClass()
                .getClassLoader(), interfaces.toArray(new Class[interfaces
                .size()]), tcpClientEngine);

        final Engine cachedEngine = createCachedEngine(engine1);

        final Hashtable<Method, Object> hashtable = new Hashtable<Method, Object>();

        for (Method m : Engine.class.getMethods()) {
            hashtable.put(m, cachedEngine);
        }

        for (Method m : Cached.class.getMethods()) {
            hashtable.put(m, cachedEngine);
        }
        interfaces.add(Cached.class);

        Engine engine = (Engine) Proxy.newProxyInstance(getClass()
                .getClassLoader(), interfaces.toArray(new Class[interfaces
                .size()]), new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {
                Object o = hashtable.get(method);
                if (o == null)
                    return tcpClientEngine.invoke(proxy, method, args);
                return method.invoke(o, args);
            }
        });

        tcpClientEngine.setEngine(engine);

        for (Plugin plugin : factory.getPlugins())
            plugin.init(engine, rules);

        return engine;
    }

    protected CachedEngine createCachedEngine(final Engine engine1) {
        return new CachedEngine(engine1);
    }

    @Override
    protected User getMe() {
        if (me == null)
            me = userProvider.me();
        return me;
    }

    @Override
    protected String getType() {
        return GUIPluginProvider.CAJO;
    }

}
