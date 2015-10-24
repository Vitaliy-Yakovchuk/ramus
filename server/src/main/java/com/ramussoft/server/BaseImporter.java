package com.ramussoft.server;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.PluginProvider;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.MemoryDatabase;
import com.ramussoft.idef0.IDEF0PluginProvider;

public class BaseImporter {

    /**
     * @param args
     */
    public static void main(final String[] args) {
        if (args.length == 0) {
            System.err.println("Enter input file as parameter");
            return;
        }

        MemoryDatabase database = new MemoryDatabase() {
            @Override
            protected Collection<? extends PluginProvider> getAdditionalSuits() {
                ArrayList<PluginProvider> ps = new ArrayList<PluginProvider>(1);
                ps.add(new IDEF0PluginProvider());
                return ps;
            }

            @Override
            protected File getFile() {
                return new File(args[0]);
            }

            @Override
            public Connection createConnection() throws SQLException {
                return EngineFactory.createNewConnection();
            }

            @Override
            protected String getJournalDirectoryName(String tmp) {
                return null;
            }
        };

        EngineFactory factory = new EngineFactory();

        Engine s = database.getEngine(null);
        IEngine d = factory.getEngine();
        for (String streamName : s.getStreamNames()) {
            byte[] bs = s.getStream(streamName);
            if (bs == null) {
                System.err.println("WARNING: stream " + streamName
                        + " not found in the source");
            } else
                d.setStream(streamName, bs);
        }
        try {
            ((FileIEngineImpl) (database.getEngine(null).getDeligate()))
                    .close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
