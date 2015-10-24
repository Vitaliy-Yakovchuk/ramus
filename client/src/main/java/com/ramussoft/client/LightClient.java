package com.ramussoft.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXLoginPane;
import org.jdesktop.swingx.JXLoginPane.Status;
import org.jdesktop.swingx.auth.LoginService;

import com.caucho.hessian.client.HessianProxyFactory;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.journal.DirectoryJournalFactory;
import com.ramussoft.common.journal.Journal;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.common.journal.JournaledEngine;
import com.ramussoft.common.journal.event.JournalListener;
import com.ramussoft.core.persistent.PersistentFactory;
import com.ramussoft.gui.common.GUIPlugin;
import com.ramussoft.gui.common.GUIPluginProvider;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.net.common.Group;
import com.ramussoft.net.common.User;
import com.ramussoft.net.common.UserFactory;
import com.ramussoft.net.common.UserProvider;

public class LightClient extends Client {

    public static Engine staticEngine;

    public static AccessRules staticAccessRules;

    private static String URL_SECURE;

    private static String URL_ACCESS_RULES;

    private static String URL_IENGINE;

    private static String URL_PROVIDER;

    private static String URL_USER_FACTORY;

    private AccessRules accessor;

    private IEngine impl;

    private UserProvider userProvider;

    private HessianProxyFactory proxyFactory;

    private User me;

    public static void main(String[] args) {
        new LightClient().start(args);
    }

    public void start(String[] args) {

        if (args.length < 1) {
            System.err
                    .println("Usage java -jar ... http://host:8080...any port/..., for example http://192.168.0.1/ramus/remoting/");
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
        } catch (Exception e1) {
            e1.printStackTrace();
        }

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

            String from = args[0];

            URL_SECURE = from + "Test";
            URL_ACCESS_RULES = from + "AccessRulesService";
            URL_IENGINE = from + "IEngineService";

            URL_PROVIDER = from + "UserProvider";

            URL_USER_FACTORY = from + "UserFactory";

            proxyFactory = new HessianProxyFactory();

            // com.caucho.hessian.client.

            proxyFactory.setOverloadEnabled(true);

            userProvider = (UserProvider) proxyFactory.create(
                    UserProvider.class, URL_PROVIDER);

            Status status = JXLoginPane.showLoginDialog(null,
                    new LoginService() {

                        @Override
                        public boolean authenticate(String name,
                                                    char[] password, String server)
                                throws Exception {

                            proxyFactory.setUser(name);
                            String pass = new String(password);
                            proxyFactory.setPassword(pass);

                            try {
                                boolean b = (userProvider.me() != null)
                                        && (pass.equals(userProvider.me()
                                        .getPassword()));
                                return b;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return false;
                        }

                    });
            if ((status.equals(Status.CANCELLED))
                    || (status.equals(Status.NOT_STARTED))
                    || (status.equals(Status.FAILED))) {
                System.exit(0);
                return;
            }

            accessor = (AccessRules) proxyFactory.create(AccessRules.class,
                    URL_ACCESS_RULES);

            impl = (IEngine) proxyFactory.create(IEngine.class, URL_IENGINE);
            run(args);
        } catch (Exception e) {
            System.err.println("Can not connect to the server.");
            System.err.println("URL_SECURE: " + URL_SECURE);
            System.err.println("URL_ACCESS_RULES: " + URL_ACCESS_RULES);
            System.err.println("URL_IENGINE: " + URL_IENGINE);
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Сталась критична помилка, дивіться відповідний log для деталей "
                            + e.getLocalizedMessage());
            System.exit(1);
        }
    }

    private static boolean isAdmin(User me) {
        for (Group group : me.getGroups())
            if (group.getName().equals("admin"))
                return true;
        return false;
    }

    @Override
    protected AccessRules getAccessRules() {
        return accessor;
    }

    @Override
    protected Engine getEngine(PluginFactory factory,
                               PersistentFactory persistentFactory) {
        try {
            Journal journal = new Journal(null, -1l);
            journal.setEnable(false);
            final JournaledEngine journaledEngine = new JournaledEngine(
                    factory, impl, persistentFactory.getRows(),
                    new DirectoryJournalFactory(null), accessor);
            Engine engine = (Engine) Proxy.newProxyInstance(getClass()
                    .getClassLoader(), new Class[]{Engine.class,
                    Journaled.class}, new InvocationHandler() {

                private Journaled journaled = new Journaled() {

                    boolean started = false;

                    @Override
                    public void undoUserTransaction() {
                    }

                    @Override
                    public void startUserTransaction() {
                        started = true;
                    }

                    @Override
                    public void setNoUndoPoint() {
                    }

                    @Override
                    public void setEnable(boolean b) {
                    }

                    @Override
                    public void rollbackUserTransaction() {
                        started = false;
                    }

                    @Override
                    public void removeJournalListener(JournalListener listener) {
                    }

                    @Override
                    public void redoUserTransaction() {
                    }

                    @Override
                    public boolean isUserTransactionStarted() {
                        return started;
                    }

                    @Override
                    public boolean isEnable() {
                        return false;
                    }

                    @Override
                    public JournalListener[] getJournalListeners() {
                        return new JournalListener[]{};
                    }

                    @Override
                    public void commitUserTransaction() {
                        started = false;
                    }

                    @Override
                    public void close() {
                    }

                    @Override
                    public boolean canUndo() {
                        return false;
                    }

                    @Override
                    public boolean canRedo() {
                        return false;
                    }

                    @Override
                    public void addJournalListener(JournalListener listener) {
                    }

                    @Override
                    public long getBranch() {
                        return -1l;
                    }
                };

                @Override
                public Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                    if (method.getDeclaringClass().equals(Journaled.class))
                        return method.invoke(journaled, args);
                    return method.invoke(journaledEngine, args);
                }
            });
            return engine;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void initAdditionalGuiPlugins(List<GUIPlugin> list) {
        super.initAdditionalGuiPlugins(list);
        list.add(new ClientPlugin(userProvider, true));
        if (isAdmin(getMe())) {
            UserFactory userFactory;
            try {
                userFactory = (UserFactory) proxyFactory.create(
                        UserFactory.class, URL_USER_FACTORY);
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
                throw new RuntimeException(e1);
            }
            AdminPanelPlugin adminPanelPlugin = new AdminPanelPlugin(
                    userFactory, e);
            list.add(adminPanelPlugin);
        }
    }

    @Override
    protected String getTitle() {
        return super.getTitle() + " - HTTP - " + getMe().getName();
    }

    @Override
    protected User getMe() {
        if (me == null)
            me = userProvider.me();
        return me;
    }

    @Override
    protected String getType() {
        return GUIPluginProvider.HTTP;
    }

}
