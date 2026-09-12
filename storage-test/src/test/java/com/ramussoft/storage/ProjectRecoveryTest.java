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

/**
 * Відновлення після збою.
 * <p>
 * Рушій кладе знімок відкритого проєкту в сеансовий каталог, а при аварійному
 * завершенні відкриває **цей самий знімок** і накочує на нього журнал. Тобто
 * джерело й призначення копіювання збігаються — випадок, у якому наївне
 * копіювання відкриває файл на запис перед читанням і обнуляє його.
 */
public class ProjectRecoveryTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        RsfFixture.isolateHome(folder.newFolder("home"));
    }

    @Test
    public void snapshotIsMadeOnOpen() throws Exception {
        File project = new File(folder.newFolder("work"), "Модель.ramus");
        RsfFixture.exportProject(RsfFixture.sampleFiles().get(0), project);

        final File session = folder.newFolder("session");
        openWithSession(project, session);

        File snapshot = new File(session, "source.rms");
        assertTrue("знімок не створено", snapshot.isDirectory());
        assertTrue("у знімку немає опису проєкту",
                new File(snapshot, "project.ramus").isFile());
    }

    /**
     * Повторюємо форму, у якій рушій запускається під час відновлення:
     * відкривається сам знімок, що лежить у сеансовому каталозі.
     */
    @Test
    public void openingTheSnapshotItselfDoesNotEmptyIt() throws Exception {
        File project = new File(folder.newFolder("work"), "Модель.ramus");
        RsfFixture.exportProject(RsfFixture.sampleFiles().get(0), project);

        File session = folder.newFolder("session");
        openWithSession(project, session);

        File snapshot = new File(session, "source.rms");
        long size = new File(snapshot, "project.ramus").length();
        assertTrue("порожній опис проєкту у знімку", size > 0);

        int qualifiers = openWithSession(snapshot, session);

        assertTrue("відкриття знімка не дало класифікаторів", qualifiers > 0);
        assertEquals("відкриття знімка обнулило його файли", size,
                new File(snapshot, "project.ramus").length());
    }

    /**
     * @return кількість класифікаторів у відкритому проєкті
     */
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
