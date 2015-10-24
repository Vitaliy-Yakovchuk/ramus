package com.ramussoft.server;

import java.io.File;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.sql.DataSource;

import com.ramussoft.common.AbstractPlugin;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Metadata;
import com.ramussoft.common.Plugin;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.PluginProvider;
import com.ramussoft.common.cached.CachedData;
import com.ramussoft.common.cached.CachedEngine;
import com.ramussoft.common.journal.DirectoryJournalFactory;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.common.journal.JournaledEngine;
import com.ramussoft.common.logger.EngineLogExtension;
import com.ramussoft.common.logger.Event;
import com.ramussoft.common.logger.ILog;
import com.ramussoft.common.logger.Log;
import com.ramussoft.common.logger.UpdateEventCallback;
import com.ramussoft.core.attribute.simple.SimpleAttributePluginSuit;
import com.ramussoft.core.impl.IntegrityAccessorSuit;
import com.ramussoft.core.persistent.PersistentFactory;
import com.ramussoft.core.persistent.PersistentsPlugin;
import com.ramussoft.core.persistent.PersistentsPluginProvider;
import com.ramussoft.core.persistent.UniversalPersistentFactory;
import com.ramussoft.idef0.IDEF0PluginProvider;
import com.ramussoft.jdbc.JDBCCallback;
import com.ramussoft.jdbc.JDBCTemplate;
import com.ramussoft.net.common.UserFactory;

public class EngineFactory {

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+2:00"));
    }

    private AccessRules accessor;

    protected Engine journaledEngine;

    private JDBCTemplate template;

    private ServerIEngineImpl impl;

    private Connection connection;

    private PersistentFactory persistentFactory;

    private PluginFactory factory;

    private boolean canUndoRedo;

    private ArrayList<PluginProvider> suits;

    private Log log;

    private static Connection staticConnection;

    private static CachedData cachedData = null;// new CachedData();

    protected PluginFactory createPluginFactory(List<PluginProvider> list) {
        ArrayList<Plugin> plugins = new ArrayList<Plugin>();

        for (PluginProvider suit : list) {
            plugins.addAll(suit.getPlugins());
        }

        plugins.add(new AbstractPlugin() {

            @Override
            public String getName() {
                return "Log";
            }

            @Override
            public Class getFunctionalInterface() {
                return ILog.class;
            }

            @Override
            public Object createFunctionalInterfaceObject(Engine engine,
                                                          IEngine iEngine) {
                return log;
            }
        });

        PluginFactory factory = new PluginFactory(plugins);
        return factory;
    }

    public EngineFactory() {
        connection = staticConnection;
        createTemplate();
    }

    public EngineFactory(DataSource dataSource) throws SQLException {
        connection = dataSource.getConnection();
        createTemplate();
    }

    public EngineFactory(Connection aConnection) {
        connection = aConnection;
        createTemplate();
    }

    private void createTemplate() {
        try {
            template = new JDBCTemplate(createConnection());

            try {
                template.executeResource("/com/ramussoft/jdbc/database.sql",
                        "ramus_");

            } catch (Exception e) {
            }
            try {
                template.executeResource("/com/ramussoft/jdbc/update1.sql",
                        "ramus_");

            } catch (Exception e) {
            }
            try {
                template.executeResource("/com/ramussoft/jdbc/update2.sql",
                        "ramus_");

            } catch (Exception e) {
            }
            try {
                template.executeResource("/com/ramussoft/jdbc/update3.sql",
                        "ramus_");

            } catch (Exception e) {
            }
            try {
                template.executeResource("/com/ramussoft/jdbc/update4.sql",
                        "ramus_");

            } catch (Exception e) {
            }
            /*try {
				template.executeResource("/com/ramussoft/jdbc/update5.sql",
						"ramus_");

			} catch (Exception e) {
				e.printStackTrace();
			}*/
            try {
                template.executeResource("/com/ramussoft/server/create.sql");

            } catch (Exception e) {
            }

            try {
                template.executeResource("/com/ramussoft/server/create2.sql");

            } catch (Exception e) {
            }

            try {
                template.executeResource(
                        "/com/ramussoft/server/create-log.sql", "ramus_");

            } catch (Exception e) {
            }

            journaledEngine = createJournaledEngine(new DirectoryJournalFactory(
                    null));

        } catch (Exception e) {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            throw new RuntimeException(e);
        }
    }

    private void createUniversalPersistentFactory(JDBCTemplate template,
                                                  String pluginsString) throws SQLException {
        UniversalPersistentFactory factory = new UniversalPersistentFactory(
                template);

        Collection<PersistentsPlugin> pls = getPersistentPlugins();

        ArrayList<PersistentsPlugin> plugins = new ArrayList<PersistentsPlugin>();
        plugins.addAll(pls);
        if (pluginsString != null) {
            StringTokenizer st = new StringTokenizer(pluginsString, ", ");
            while (st.hasMoreTokens()) {
                try {
                    String className = st.nextToken();
                    Class<?> clazz = Class.forName(className);
                    PersistentsPluginProvider plugin = (PersistentsPluginProvider) clazz
                            .newInstance();
                    plugins.addAll(plugin.getPersistentsPlugins());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        for (PluginProvider pp : suits) {
            if (pp instanceof PersistentsPluginProvider) {
                plugins.addAll(((PersistentsPluginProvider) pp)
                        .getPersistentsPlugins());
            }
        }

        for (PersistentsPlugin pp : plugins) {
            List<Class> list = new ArrayList<Class>();
            pp.addPersistents(list, factory);
            factory.addClasses(list);
        }

        factory.rebuild();
    }

    public Engine createJournaledEngine(DirectoryJournalFactory journalFactory) {
        try {
            JDBCTemplate template = new JDBCTemplate(createNewConnectionA());
            suits = new ArrayList<PluginProvider>();

            suits.add(new SimpleAttributePluginSuit());

            suits.add(new IDEF0PluginProvider());

            Properties ps = getPropeties();
            if (ps != null) {
                String suitNames = ps.getProperty("AdditionalSuits");
                if (suitNames != null)
                    PluginFactory.loadAdditionalSuits(suitNames, suits);
                canUndoRedo = !"false".equals(ps.getProperty("CanUndoRedo"));

            }

            suits.addAll(getAdditionalSuits());

            createUniversalPersistentFactory(
                    template,
                    (ps == null) ? null : ps
                            .getProperty("PersistentPluginsProvider"));

            factory = createPluginFactory(suits);
            String prefix = "ramus_";
            impl = new ServerIEngineImpl(0, template, prefix, factory);

            accessor = impl.getAccessor();
            persistentFactory = new PersistentFactory(prefix,
                    factory.getAttributePlugins(), template);

            persistentFactory.rebuild();
            checkIfGroupsExists();

            Engine result;

            Journaled journaled;

            if (cachedData == null) {
                JournaledEngine journaledEngine2 = new JournaledEngine(factory,
                        impl, persistentFactory.getRows(), journalFactory,
                        accessor);
                result = journaledEngine2;
                journaled = journaledEngine2.getJournal();
            } else {

                JournaledEngine engine = new JournaledEngine(factory, impl,
                        persistentFactory.getRows(), journalFactory, accessor) {
                    @Override
                    protected void initPlugins(PluginFactory pluginFactory,
                                               AccessRules accessor) {

                    }
                };
                journaled = engine.getJournal();

                CachedEngine cachedEngine = new CachedEngine(engine, cachedData);
                for (Plugin plugin : factory.getPlugins())
                    plugin.init(cachedEngine, accessor);
                result = cachedEngine;
            }

            EngineLogExtension engineLogExtension = new EngineLogExtension(
                    result, journaled);

            log = new Log(result, journaled) {

                protected Event createEvent(String type,
                                            UpdateEventCallback callback) {
                    String user = "admin";
                    if (impl.getServerAccessRules() != null)
                        user = impl.getServerAccessRules().getUser().getLogin();
                    long id = impl.nextValue("qualifiers_log_seq");

                    return callback.createEvent(this, id,
                            new Timestamp(System.currentTimeMillis()), type,
                            user, null);
                }

            };

            log.addExtension(engineLogExtension);
            log.addExtension(new StorageLogExtension(new JDBCTemplate(
                    createNewConnection()), prefix));

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkIfGroupsExists() {
        template.execute(new JDBCCallback() {

            @Override
            public Object execute(Connection connection) throws SQLException {
                List<String> uniqueGroups = new ArrayList<String>();
                for (Plugin plugin : factory.getPlugins()) {
                    for (String g : plugin.getGroups()) {
                        if (uniqueGroups.indexOf(g) < 0)
                            uniqueGroups.add(g);
                    }
                }

                PreparedStatement pSelect = connection
                        .prepareStatement("SELECT * FROM groups WHERE group_name=?");

                PreparedStatement pInsert = connection
                        .prepareStatement("INSERT INTO groups(group_name) VALUES(?)");

                for (String group : uniqueGroups) {
                    pSelect.setString(1, group);
                    ResultSet rs = pSelect.executeQuery();
                    boolean has = rs.next();
                    if (!has) {
                        pInsert.setString(1, group);
                        pInsert.execute();
                    }
                    rs.close();
                }

                pInsert.close();
                pSelect.close();

                return null;
            }
        });

    }

    protected Collection<? extends PluginProvider> getAdditionalSuits() {
        return new ArrayList<PluginProvider>(0);
    }

    protected Collection<PersistentsPlugin> getPersistentPlugins() {
        return new ArrayList<PersistentsPlugin>(0);
    }

    public AccessRules getAccessRules() {
        return accessor;
    }

    public IEngine getEngine() {
        getJournaledEngine();
        return impl;
    }

    public Engine getJournaledEngine() {
        if (journaledEngine == null) {
            journaledEngine = createJournaledEngine(new DirectoryJournalFactory(
                    null));
        }
        return journaledEngine;
    }

    public Connection createConnection() throws SQLException {
        if (connection != null)
            return connection;
        return createNewConnection();
    }

    public static Connection createNewConnection() throws SQLException {
        Connection connection = createNewConnectionA();
        staticConnection = connection;
        return connection;
    }

    public static void closeConnection() throws SQLException {
        if (staticConnection != null) {
            if (!staticConnection.isClosed())
                staticConnection.close();
            staticConnection = null;
        }
    }

    private static Connection createNewConnectionA() throws SQLException {

        if (!Metadata.CORPORATE) {
            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            String url = "jdbc:h2:mem:~/" + System.currentTimeMillis() + ";";
            Connection conn = DriverManager.getConnection(url, "sa", "");

            return conn;
        } else {

            Properties ps = getPropeties();
            if (ps != null) {
                try {
                    Class.forName(ps.getProperty("driver"));
                    Connection connection2 = DriverManager.getConnection(
                            ps.getProperty("url"), ps.getProperty("user"),
                            ps.getProperty("password"));
                    connection2.setAutoCommit(false);
                    return connection2;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Connection connection2 = DriverManager.getConnection(
                    "jdbc:postgresql://127.0.0.1/ramus_dev", "postgres",
                    "postgres");
            connection2.setAutoCommit(false);
            return connection2;
        }
    }

    public static Properties getPropeties() {
        File conf = null;
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase == null)
            catalinaBase = System.getProperty("ramus.server.base");
        if (catalinaBase != null) {
            File file = new File(catalinaBase, "conf");
            file = new File(file, "ramus-database.conf");
            if (file.exists())
                conf = file;
        }

        if (conf == null) {
            File file = new File("ramus-database.conf");
            if (file.exists())
                conf = file;
            else {
                file = new File("conf" + File.separator + "ramus-database.conf");
                if (file.exists())
                    conf = file;
            }
        }
        Properties ps = null;
        if (conf != null) {
            try {
                ps = new Properties();
                FileInputStream stream = new FileInputStream(conf);
                ps.load(stream);
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ps;
    }

    public JDBCTemplate getTemplate() {
        return template;
    }

    public void setInstallServerAccessRules(UserFactory factory) {
        ServerAccessRules rules = new ServerAccessRules(impl, factory);
        addAccessRules(rules);
    }

    public void addAccessRules(AccessRules rules) {
        ((IntegrityAccessorSuit) getAccessRules()).addAccessRules(rules);
    }

    /**
     * @param canUndoRedo the canUndoRedo to set
     */
    public void setCanUndoRedo(boolean canUndoRedo) {
        this.canUndoRedo = canUndoRedo;
    }

    /**
     * @return the canUndoRedo
     */
    public boolean isCanUndoRedo() {
        return canUndoRedo;
    }

    public static Connection getConnection() throws SQLException {
        if (staticConnection == null)
            new EngineFactory();
        return staticConnection;
    }

    public ArrayList<PluginProvider> getSuits() {
        return suits;
    }

    public PluginFactory getFactory() {
        return factory;
    }
}
