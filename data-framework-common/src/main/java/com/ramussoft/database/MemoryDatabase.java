package com.ramussoft.database;

import java.io.File;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipException;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.AdditionalPluginLoader;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Plugin;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.PluginProvider;
import com.ramussoft.common.cached.CachedEngine;
import com.ramussoft.common.journal.DirectoryJournalFactory;
import com.ramussoft.common.journal.JournalEngineImpl;
import com.ramussoft.common.journal.JournaledEngine;
import com.ramussoft.common.journal.SuperEngineFactory;
import com.ramussoft.core.attribute.simple.SimpleAttributePluginSuit;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.core.persistent.PersistentFactory;
import com.ramussoft.jdbc.JDBCTemplate;

public class MemoryDatabase extends AbstractDatabase {

    private AccessRules accessor;

    private Engine engine;

    protected JournaledEngine journaledEngine;

    protected JDBCTemplate template;

    private FileIEngineImpl impl;

    private DirectoryJournalFactory journalFactory;

    private boolean cached;

    public MemoryDatabase() {
        this(false);
    }

    public MemoryDatabase(boolean cached) {
        this.cached = cached;
        createEngines();
    }

    protected void createEngines() {
        try {
            this.template = createTemplate();
            List<PluginProvider> suits = new ArrayList<PluginProvider>();

            loadSuits(suits);

            PluginFactory factory = createPluginFactory(suits);
            impl = createFileIEngine(factory);

            accessor = impl.getAccessor();

            PersistentFactory persistentFactory = new PersistentFactory(
                    "ramus_", factory.getAttributePlugins(), template);

            persistentFactory.rebuild();

            File file = getFile();
            if (file != null)
                impl.open(file, isIrnoreUnregisteredPlugins());

            String jName = getJournalDirectoryName(impl.getTmpPath());
            File directory = null;
            if (jName != null) {
                directory = new File(jName);
            }

            journalFactory = createJournalFactory(directory);

            journaledEngine = createJournaledEngine(factory, persistentFactory);
            this.engine = (Engine) SuperEngineFactory
                    .createTransactionalEngine(engine, new JournalEngineImpl(
                            journaledEngine));
            this.engine.setPluginProperty("Core", "PluginList",
                    factory.getPlugins());
            this.engine.setPluginProperty("Core", "PluginFactory", factory);

        } catch (Exception e) {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            throw new RuntimeException(e);
        }
    }

    protected void loadSuits(List<PluginProvider> suits) {
        suits.add(new SimpleAttributePluginSuit());

        suits.addAll(getAdditionalSuits());

        AdditionalPluginLoader.loadAdditionalSuits(suits);
    }

    public static JDBCTemplate createStaticTemplate(Connection connection)
            throws SQLException {
        JDBCTemplate template = new JDBCTemplate(connection);

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
            template.executeResource("/com/ramussoft/jdbc/update3.sql");

        } catch (Exception e) {
        }
        try {
            template.executeResource("/com/ramussoft/jdbc/update4.sql",
                    "ramus_");

        } catch (Exception e) {
        }
        return template;
    }

    public JDBCTemplate createTemplate() throws SQLException {
        JDBCTemplate template = new JDBCTemplate(createConnection());

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
            template.executeResource("/com/ramussoft/jdbc/update4.sql",
                    "ramus_");

        } catch (Exception e) {
        }
        return template;
    }

    private JournaledEngine createJournaledEngine(PluginFactory factory,
                                                  PersistentFactory persistentFactory) throws ClassNotFoundException {
        if (cached) {
            JournaledEngine journaledEngine2 = new JournaledEngine(factory,
                    impl, persistentFactory.getRows(), journalFactory, accessor) {
                @Override
                protected void initPlugins(PluginFactory pluginFactory,
                                           AccessRules accessor) {

                }
            };
            CachedEngine cachedEngine = new CachedEngine(journaledEngine2);
            for (Plugin plugin : factory.getPlugins())
                plugin.init(cachedEngine, accessor);
            this.engine = cachedEngine;
            return journaledEngine2;
        } else {
            JournaledEngine journaledEngine2 = new JournaledEngine(factory,
                    impl, persistentFactory.getRows(), journalFactory, accessor);
            this.engine = journaledEngine2;
            return journaledEngine2;
        }
    }

    protected DirectoryJournalFactory createJournalFactory(File directory) {
        return new DirectoryJournalFactory(directory);
    }

    private boolean isIrnoreUnregisteredPlugins() {
        return false;
    }

    protected FileIEngineImpl createFileIEngine(PluginFactory factory)
            throws ClassNotFoundException, ZipException, IOException {
        return new FileIEngineImpl(0, template, factory);
    }

    protected FileIEngineImpl createNotSessionedFileIEngine(
            PluginFactory factory) throws ClassNotFoundException, ZipException,
            IOException {
        return new FileIEngineImpl(0, template, factory, null);
    }

    protected String getJournalDirectoryName(String tmp) {
        return tmp;
    }

    protected File getFile() {
        return null;
    }

    protected Collection<? extends PluginProvider> getAdditionalSuits() {
        return new ArrayList<PluginProvider>(0);
    }

    @Override
    public AccessRules getAccessRules(String name) {
        return accessor;
    }

    @Override
    public Engine getEngine(String name) {
        return engine;
    }

    public static Connection createStaticConnection() throws SQLException {

        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String url = "jdbc:h2:mem:~/" + System.currentTimeMillis() + ";";
        Connection conn = DriverManager.getConnection(url, "sa", "");

        return conn;

		/*
         * try { Class.forName("org.postgresql.Driver"); } catch
		 * (ClassNotFoundException e) { e.printStackTrace(); } return
		 * DriverManager
		 * .getConnection("jdbc:postgresql://127.0.0.1/ramus_public",
		 * "postgres", "postgres");
		 */

    }

    public Connection createConnection() throws SQLException {

        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String url = "jdbc:h2:mem:~/" + System.currentTimeMillis() + ";";
        Connection conn = DriverManager.getConnection(url, "sa", "");

        return conn;

		/*
         * try { Class.forName("org.postgresql.Driver"); } catch
		 * (ClassNotFoundException e) { e.printStackTrace(); } return
		 * DriverManager .getConnection("jdbc:postgresql://127.0.0.1/t2",
		 * "postgres", "postgres");
		 */

    }

    public DirectoryJournalFactory getJournalFactory() {
        return journalFactory;
    }

    public JDBCTemplate getTemplate() {
        return template;
    }

    public void close() {
        try {
            getTemplate().getConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public JournaledEngine getJournaledEngine() {
        return journaledEngine;
    }
}
