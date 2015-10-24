package com.ramussoft.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.PluginProvider;
import com.ramussoft.common.journal.DirectoryJournalFactory;
import com.ramussoft.common.journal.JournaledEngine;
import com.ramussoft.core.attribute.simple.SimpleAttributePluginSuit;
import com.ramussoft.core.impl.IEngineImpl;
import com.ramussoft.core.persistent.PersistentFactory;
import com.ramussoft.jdbc.JDBCTemplate;

public class H2Database extends AbstractDatabase {
    private static final String PREFIX = "test_";

    private AccessRules accessor;

    private Engine engine;

    public H2Database() {
        createEngines();
    }

    private void createEngines() {
        try {
            JDBCTemplate template = createTemplate();

            List<PluginProvider> suits = new ArrayList<PluginProvider>();

            suits.add(new SimpleAttributePluginSuit());

            PluginFactory factory = createPluginFactory(suits);
            IEngineImpl impl = new IEngineImpl(0, template, PREFIX, factory) {

                @Override
                protected boolean deleteStreamBytes(String path) {
                    return false;
                }

                @Override
                public byte[] getStream(String path) {
                    return null;
                }

                @Override
                protected void writeStream(String path, byte[] bytes) {

                }

            };

            accessor = impl.getAccessor();

            PersistentFactory persistentFactory = new PersistentFactory(PREFIX,
                    factory.getAttributePlugins(), template);

            persistentFactory.rebuild();

            String tmp = System.getProperty("java.io.tmpdir");

            engine = new JournaledEngine(factory, impl,
                    persistentFactory.getRows(), new DirectoryJournalFactory(
                    new File(tmp)), accessor);

        } catch (Exception e) {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            throw new RuntimeException();
        }
    }

    @Override
    public AccessRules getAccessRules(String name) {
        return accessor;
    }

    @Override
    public Engine getEngine(String name) {
        return engine;
    }

    public static Connection createConnection() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection conn = DriverManager.getConnection(
                "jdbc:h2:/home/zdd/tmp/my_base" + System.currentTimeMillis()
                        + ";", "sa", "");

        return conn;
    }

    public static JDBCTemplate createTemplate() throws SQLException,
            IOException {
        JDBCTemplate template = new JDBCTemplate(createConnection());

        template.executeResource("/com/ramussoft/jdbc/database.sql", PREFIX);
        return template;
    }
}
