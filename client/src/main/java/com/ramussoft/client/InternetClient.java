package com.ramussoft.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXLoginPane;
import org.jdesktop.swingx.JXLoginPane.Status;
import org.jdesktop.swingx.auth.LoginService;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.journal.BinaryAccessFile;
import com.ramussoft.common.journal.SuperEngineFactory;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.core.impl.IEngineImpl;
import com.ramussoft.core.impl.XMLToTable;
import com.ramussoft.core.impl.FileIEngineImpl.PersistentInfo;
import com.ramussoft.core.persistent.PersistentFactory;
import com.ramussoft.database.MemoryDatabase;
import com.ramussoft.gui.common.GUIPlugin;
import com.ramussoft.gui.common.GUIPluginProvider;
import com.ramussoft.gui.common.UndoRedoPlugin;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.jdbc.RowMapper;
import com.ramussoft.net.common.User;
import com.ramussoft.net.common.UserFactory;
import com.ramussoft.net.common.UserProvider;
import com.ramussoft.net.common.internet.InternetEngine;
import com.ramussoft.net.common.internet.InternetHookJournal;
import com.ramussoft.net.common.internet.InternetSyncJournal;
import com.ramussoft.net.common.internet.RedoObject;
import com.ramussoft.net.common.internet.UndoObject;
import com.ramussoft.net.common.tcp.CallParameters;
import com.ramussoft.net.common.tcp.Result;

public class InternetClient extends Client {

    private static boolean season;

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
            new InternetClient().start(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String host;
    private String port;
    private TcpClientConnection connection;
    private UserProvider userProvider;
    private AccessRules rules;
    protected String login;
    private User me;
    private InternetSyncJournal syncJournal;
    private JDBCTemplate template;
    private String prefix;
    private ZipFile zFile;
    private FileIEngineImpl impl;

    private List<Object> runCallbacks = new ArrayList<Object>();

    private Object startLock = new Object();

    private void start(String[] args) {
        if (args.length < 2) {
            System.err
                    .println("Usage java -jar ... url ..., for example: java -jar my.jar localhost 38666 ");
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
                    synchronized (startLock) {
                        if (runCallbacks != null) {
                            runCallbacks.add(object);
                            return;
                        }
                    }
                    asyncCall(object);
                }

                @Override
                protected void showDialogEndExit(String message) {
                    if (exitShown)
                        return;
                    exitShown = true;
                    JOptionPane.showMessageDialog(framework.getMainFrame(),
                            message);
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

            Status status = JXLoginPane.showLoginDialog(null,
                    new LoginService() {

                        @Override
                        public boolean authenticate(String name,
                                                    char[] passwordChars, String server)
                                throws Exception {

                            String password = new String(passwordChars);

                            Long sessionId;
                            try {
                                sessionId = (Long) connection.invoke("login",
                                        new Object[]{name, password});
                                if ((sessionId == null)
                                        || (sessionId.longValue() < 0l)) {
                                    throw new RuntimeException(
                                            "Login or password not correct!");
                                }
                                InternetClient.this.login = name;
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw e;
                            }
                            return true;
                        }

                    });
            if ((status.equals(Status.CANCELLED))
                    || (status.equals(Status.NOT_STARTED))
                    || (status.equals(Status.FAILED))) {
                System.exit(0);
                return;
            }
            if (season)
                if (!InternetClient.this.login.equals("admin")) {
                    JOptionPane
                            .showMessageDialog(null,
                                    "Тільки користувач admin може працювати з системою планування");
                    System.exit(5);
                    return;
                }

            userProvider = (UserProvider) createDeligate(UserProvider.class);
            rules = (AccessRules) createDeligate(AccessRules.class);

            run(args);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Сталась критична помилка, дивіться відповідний log для деталей "
                            + e.getLocalizedMessage());
            System.exit(1);
        }
    }

    @SuppressWarnings("unchecked")
    private Object createDeligate(Class class1) {
        return createDeligate(new Class[]{class1});
    }

    @SuppressWarnings("unchecked")
    private Object createDeligate(Class[] classes) {
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

    @SuppressWarnings("unused")
    @Override
    protected Engine getEngine(PluginFactory factory, PersistentFactory pf) {

        File tmp = new File(System.getProperty("java.io.tmpdir"), "ramus-"
                + String.valueOf(System.currentTimeMillis()));

        String tmpPath = tmp.getAbsolutePath()
                + Math.round(Math.random() * 1000);

        try {
            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:h2:" + tmpPath + File.separator + "client-cache"
                    + ";";
            Connection conn = DriverManager.getConnection(url, "sa", "");

            template = MemoryDatabase.createStaticTemplate(conn);
            String dump = tmpPath + File.separator + "dump.rsf";

            new File(tmpPath).mkdirs();

            load(dump, factory);

            impl = new FileIEngineImpl(0, template, factory, tmpPath) {
                @Override
                protected boolean deleteStreamBytes(String path) {
                    try {
                        return (Boolean) connection.invoke("deleteStream",
                                new Object[]{path});
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public byte[] getStream(String path) {
                    try {
                        return (byte[]) connection.invoke("getStream",
                                new Object[]{path});
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                protected void writeStream(String path, byte[] bytes) {
                    try {
                        connection.invoke("setStream", new Object[]{path,
                                bytes});
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public long nextValue(String sequence) {
                    try {
                        return (Long) connection.invoke("nextValue",
                                new Object[]{sequence});
                    } catch (Exception e) {
                        return super.nextValue(sequence);
                    }
                }
            };

            AccessRules accessor = impl.getAccessor();

            String jName = getJournalFileName(impl.getTmpPath()
                    + File.separator);

            BinaryAccessFile accessFile = null;
            if (jName != null) {
                accessFile = new BinaryAccessFile(jName, "rw");
            }

            final InternetHookJournal journal = new InternetHookJournal(
                    accessFile) {

                @Override
                public void onUndo(byte[] bs) {
                    try {
                        connection.invoke("undo", new Object[]{bs});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onRedo(byte[] bs) {
                    try {
                        connection.invoke("redo", new Object[]{bs});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };
            if (true)
                throw new RuntimeException("Not implementated");
            InternetEngine internetEngine = new InternetEngine(factory, impl,
                    impl.getPersistentFactory().getRows(), null, accessor) {
                @Override
                public void replaceElements(Element[] oldElements,
                                            Element newElement) {
                    try {
                        byte[] bs = (byte[]) connection.invoke(
                                "replaceElements", new Object[]{oldElements,
                                        newElement});
                        journal.serverCopy(bs, this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            BinaryAccessFile binaryAccessFile = new BinaryAccessFile(new File(
                    impl.getTmpPath() + File.separator + "hook.tmp"), "rw");

            syncJournal = new InternetSyncJournal(binaryAccessFile);
            syncJournal.registerEngine(internetEngine);

            Engine engine = internetEngine;
            if (accessFile != null)
                journal.setEnable(true);
            engine = (Engine) SuperEngineFactory.createTransactionalEngine(
                    engine, journal);
            engine.setPluginProperty("Core", "PluginList", factory.getPlugins());
            engine.setPluginProperty("Core", "PluginFactory", factory);
            synchronized (startLock) {
                for (Object object : runCallbacks)
                    asyncCall(object);
                runCallbacks = null;
            }
            return engine;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void load(String dump, PluginFactory factory) throws Exception,
            FileNotFoundException, IOException, ZipException,
            InvalidPropertiesFormatException {
        new IEngineImpl(0, template, "ramus_", factory) {// to create all tables

            @Override
            protected void writeStream(String path, byte[] bytes) {
            }

            @Override
            public byte[] getStream(String path) {
                return null;
            }

            @Override
            protected boolean deleteStreamBytes(String path) {
                return false;
            }
        };

        template.execute("DELETE FROM {0}persistent_fields", "ramus_");
        template.execute("DELETE FROM {0}persistents", "ramus_");

        byte[] bs = (byte[]) connection.invoke("loadAllData", new Object[]{});

        File file = new File(dump);

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bs);
        fos.close();

        bs = null;

        prefix = "ramus_";

        zFile = new ZipFile(file);
        ZipEntry entry = new ZipEntry(FileIEngineImpl.SEQUENCES);
        InputStream is = zFile.getInputStream(entry);
        Connection c = template.getConnection();
        try {
            if (is != null) {
                Properties ps = new Properties();
                ps.loadFromXML(is);
                Statement st = c.createStatement();
                Enumeration<Object> keys = ps.keys();
                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    try {
                        st.execute("DROP SEQUENCE " + prefix + key + ";");

                    } catch (SQLException e) {

                    }
                    st.execute("CREATE SEQUENCE " + prefix + key + " START "
                            + ps.getProperty(key) + ";");
                }
                st.close();
                is.close();
            }
            c.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        loadTable("", "qualifiers");
        loadTable("", "attributes");
        loadTable("", "qualifiers_attributes");
        loadTable("", "elements");
        loadTable("", "persistents");
        loadTable("", "persistent_fields");
        loadTable("", "application_preferencies");
        loadTable("", "streams");
        loadTable("", "formulas");
        loadTable("", "formula_dependences");
        openPersistentTables();

        zFile.close();
        zFile = null;
    }

    @SuppressWarnings("unchecked")
    private void openPersistentTables() {
        List<PersistentInfo> list = template.query("SELECT * FROM " + prefix
                + "persistents", new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new PersistentInfo(rs.getString("PLUGIN_NAME").trim(),
                        rs.getString("TABLE_NAME").trim());
            }
        });
        for (PersistentInfo info : list) {
            loadTable(info.plugin + "/", info.table.substring(prefix.length()));
        }

    }

    private void loadTable(String dir, String tableName) {
        ZipEntry ze = new ZipEntry("data/" + dir + tableName + ".xml");
        try {
            InputStream stream = zFile.getInputStream(ze);
            if (stream != null) {
                XMLToTable toTable = new XMLToTable(template, stream,
                        tableName, prefix);
                toTable.load();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected User getMe() {
        if (me == null)
            me = userProvider.me();
        return me;

    }

    @Override
    protected void close() {
        super.close();
        try {
            impl.getTemplate().close();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        try {
            impl.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getType() {
        return GUIPluginProvider.CAJO;
    }

    protected String getJournalFileName(String tmp) {
        return tmp + "main.journal";
    }

    @Override
    protected void initAdditionalGuiPlugins(List<GUIPlugin> plugins) {
        super.initAdditionalGuiPlugins(plugins);
        plugins.add(new UndoRedoPlugin(e));
        plugins.add(new ClientPlugin(userProvider, true));
        try {
            if ((Boolean) connection.invoke("isAdmin", new Object[]{})) {
                UserFactory userFactory;
                userFactory = (UserFactory) createDeligate(UserFactory.class);
                AdminPanelPlugin adminPanelPlugin = new AdminPanelPlugin(
                        userFactory, e);
                plugins.add(adminPanelPlugin);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getTitle() {
        return super.getTitle() + " - Internet client (" + host + ":" + port
                + ") - " + getMe().getName();
    }

    private void asyncCall(final Object object) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (object instanceof UndoObject) {
                    UndoObject undoObject = (UndoObject) object;
                    syncJournal.undo(undoObject.data);
                }

                if (object instanceof RedoObject) {
                    RedoObject redoObject = (RedoObject) object;
                    syncJournal.redo(redoObject.data);
                }
            }
        });
    }
}
