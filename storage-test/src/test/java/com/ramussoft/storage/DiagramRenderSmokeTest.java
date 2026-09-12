package com.ramussoft.storage;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ramussoft.common.Engine;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;

/**
 * Чи вдається взагалі відмалювати діаграми без екрана.
 */
public class DiagramRenderSmokeTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        RsfFixture.isolateHome(folder.newFolder("home"));
    }

    /**
     * Поріг еталонного тесту має бути значно меншим за різницю між справді
     * різними діаграмами, інакше той тест ніколи не спрацює.
     */
    @Test
    public void signaturesDistinguishDifferentDiagrams() throws Exception {
        File sample = new File(RsfFixture.projectRoot(),
                "dest/doc/en/Enterprise activity.rsf");
        MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase(sample);
        try {
            Engine engine = database.getEngine(null);
            java.util.List<String> signatures = new java.util.ArrayList<String>(
                    DiagramRenderer.render(engine,
                            database.getAccessRules(null)).values());
            org.junit.Assume.assumeTrue(signatures.size() >= 2);

            double worst = Double.MAX_VALUE;
            for (int i = 1; i < signatures.size(); i++)
                worst = Math.min(worst, DiagramRenderer.difference(
                        signatures.get(0), signatures.get(i)));

            org.junit.Assert.assertTrue(
                    "різні діаграми надто схожі за відбитком: " + worst,
                    worst > 5.0);
            ((FileIEngineImpl) engine.getDeligate()).close();
        } finally {
            database.close();
        }
    }

    @Test
    public void rendersDiagrams() throws Exception {
        File sample = new File(RsfFixture.projectRoot(),
                "dest/doc/en/Enterprise activity.rsf");
        MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase(sample);
        try {
            Engine engine = database.getEngine(null);
            Map<String, String> images = DiagramRenderer.render(engine,
                    database.getAccessRules(null));
            System.out.println("Відмальовано діаграм: " + images.size());
            for (Map.Entry<String, String> e : images.entrySet())
                System.out.println("  " + e.getValue().substring(0, 12) + "  "
                        + e.getKey());
            assertFalse("не відмальовано жодної діаграми", images.isEmpty());
            ((FileIEngineImpl) engine.getDeligate()).close();
        } finally {
            database.close();
        }
    }
}
