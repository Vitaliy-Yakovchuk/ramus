package com.ramussoft.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ramussoft.common.Engine;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;

public class ProjectRecoveryTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        RsfFixture.isolateHome(folder.newFolder("home"));
    }

    @Test
    public void snapshotIsMadeOnOpen() throws Exception {
        File project = new File(folder.newFolder("work"), "Model.ramus");
        RsfFixture.exportProject(RsfFixture.sampleFiles().get(0), project);

        final File session = folder.newFolder("session");
        openWithSession(project, session);

        File snapshot = new File(session, "source.rms");
        assertTrue("no snapshot was made", snapshot.isDirectory());
        assertTrue("the snapshot has no project description",
                new File(snapshot, "project.ramus").isFile());
    }

    @Test
    public void openingTheSnapshotItselfDoesNotEmptyIt() throws Exception {
        File project = new File(folder.newFolder("work"), "Model.ramus");
        RsfFixture.exportProject(RsfFixture.sampleFiles().get(0), project);

        File session = folder.newFolder("session");
        openWithSession(project, session);

        File snapshot = new File(session, "source.rms");
        long size = new File(snapshot, "project.ramus").length();
        assertTrue("the project description in the snapshot is empty", size > 0);

        int qualifiers = openWithSession(snapshot, session);

        assertTrue("opening the snapshot gave no qualifiers", qualifiers > 0);
        assertEquals("opening the snapshot emptied its files", size,
                new File(snapshot, "project.ramus").length());
    }

    private int openWithSession(final File project, final File session)
            throws Exception {
        MemoryDatabase database = new MemoryDatabase() {
            @Override
            protected File getFile() {
                return project;
            }

            @Override
            protected FileIEngineImpl createFileIEngine(PluginFactory factory)
                    throws ClassNotFoundException, ZipException, IOException {
                return new FileIEngineImpl(0, getTemplate(), factory,
                        session.getAbsolutePath());
            }
        };
        try {
            Engine engine = database.getEngine(null);
            int count = 0;
            for (Qualifier qualifier : engine.getQualifiers())
                count++;
            FileIEngineImpl impl = (FileIEngineImpl) engine.getDeligate();
            impl.setClearSessionPath(false);
            impl.close();
            return count;
        } finally {
            database.close();
        }
    }
}
