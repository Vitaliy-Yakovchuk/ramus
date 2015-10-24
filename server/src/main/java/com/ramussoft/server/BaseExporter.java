package com.ramussoft.server;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.PluginProvider;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.MemoryDatabase;
import com.ramussoft.idef0.IDEF0PluginProvider;

public class BaseExporter {

    public synchronized static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Enter output file as parameter");
            return;
        }
        new BaseExporter().export(new File(args[0]));
    }

    /**
     * @param args
     * @throws IOException
     */
    public void export(final File file) throws IOException {

        MemoryDatabase database = new MemoryDatabase() {
            @Override
            protected Collection<? extends PluginProvider> getAdditionalSuits() {
                ArrayList<PluginProvider> ps = new ArrayList<PluginProvider>(1);
                ps.add(new IDEF0PluginProvider());
                return ps;
            }

            @Override
            protected File getFile() {
                return null;
            }

            @Override
            public Connection createConnection() throws SQLException {
                return EngineFactory.createNewConnection();
            }

            @Override
            protected String getJournalDirectoryName(String tmp) {
                return null;
            }

            @Override
            protected FileIEngineImpl createFileIEngine(PluginFactory factory)
                    throws ClassNotFoundException, ZipException, IOException {
                return new FileIEngineImpl(0, template, factory, null);
            }
        };
        Engine s = database.getEngine(null);

        FileIEngineImpl impl = (FileIEngineImpl) s.getDeligate();

        EngineFactory factory = new EngineFactory();

        IEngine d = factory.getEngine();
        ZipOutputStream out = impl.saveToFileNotCloseFile(file);

        for (String streamName : d.getStreamNames()) {
            byte[] bs = d.getStream(streamName);
            if (bs == null) {

            } else {
                if (streamName.startsWith("/"))
                    streamName = streamName.substring(1);
                out.putNextEntry(new ZipEntry(streamName));
                out.write(bs);
            }
        }

        out.close();

        try {
            factory.getTemplate().getConnection().close();
            impl.getConnection().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
