package com.ramussoft.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.print.PIDEF0painter;

public class BlobMigrationTest {

    private static final Dimension SIZE = new Dimension(1200, 900);

    private static final List<String> KNOWN_UNOPENABLE = Arrays
            .asList("Пример модели.rsf");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        RsfFixture.isolateHome(folder.newFolder("home"));
    }

    @Test
    public void textLabelsMoveOutOfBlobWithoutChangingDiagrams()
            throws Exception {
        for (File sample : openableSamples()) {
            File work = folder.newFolder(safeName(sample));
            File source = new File(work, "source.rsf");
            File migrated = new File(work, "migrated.rsf");
            RsfFixture.copy(sample, source);

            Map<String, String> before;
            int labelsBefore;

            MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                    .createDatabase(source);
            try {
                Engine engine = database.getEngine(null);
                before = DiagramRenderer.render(engine,
                        database.getAccessRules(null));
                labelsBefore = migrateAll(engine,
                        database.getAccessRules(null));
                ((FileIEngineImpl) engine.getDeligate()).saveToFile(migrated);
            } finally {
                database.close();
            }

            assertTrue(sample.getName()
                    + ": the sample has no labels, the test checks"
                    + " nothing", labelsBefore > 0);

            MemoryDatabase reopened = (MemoryDatabase) FileDatabaseFactory
                    .createDatabase(migrated);
            try {
                Engine engine = reopened.getEngine(null);
                Map<String, String> after = DiagramRenderer.render(engine,
                        reopened.getAccessRules(null));
                int labelsAfter = countLabels(engine,
                        reopened.getAccessRules(null));

                assertEquals(sample.getName() + ": labels disappeared",
                        labelsBefore, labelsAfter);
                assertEquals(sample.getName() + ": a different set of diagrams",
                        before.keySet(), after.keySet());

                List<String> changed = new ArrayList<String>();
                for (Map.Entry<String, String> entry : before.entrySet()) {
                    double difference = DiagramRenderer.difference(
                            entry.getValue(), after.get(entry.getKey()));
                    if (difference > 1.0)
                        changed.add(String.format("%s (difference %.2f)",
                                entry.getKey(), Double.valueOf(difference)));
                }
                if (!changed.isEmpty())
                    fail(sample.getName()
                            + ": moving the labels changed how it looks:\n  "
                            + String.join("\n  ", changed));
                ((FileIEngineImpl) engine.getDeligate()).close();
            } finally {
                reopened.close();
            }
        }
    }

    private static int migrateAll(Engine engine,
                                  com.ramussoft.common.AccessRules rules) {
        int labels = 0;
        for (Qualifier model : IDEF0Plugin.getBaseQualifiers(engine)) {
            DataPlugin plugin = NDataPluginFactory.getDataPlugin(model, engine,
                    rules);
            Function base = plugin.getBaseFunction();
            if (base != null)
                labels += migrate(plugin, base);
        }
        return labels;
    }

    private static int migrate(DataPlugin plugin, Function function) {
        MovingArea area = PIDEF0painter.createMovingArea(SIZE, plugin,
                function);
        area.setActiveFunction(function);
        int labels = area.getRefactor().getTexts().size();
        area.getRefactor().saveTextLabels(function);

        for (int i = 0; i < function.getChildCount(); i++)
            labels += migrate(plugin, (Function) function.getChildAt(i));
        return labels;
    }

    private static int countLabels(Engine engine,
                                   com.ramussoft.common.AccessRules rules) {
        int labels = 0;
        for (Qualifier model : IDEF0Plugin.getBaseQualifiers(engine)) {
            DataPlugin plugin = NDataPluginFactory.getDataPlugin(model, engine,
                    rules);
            Function base = plugin.getBaseFunction();
            if (base != null)
                labels += count(base);
        }
        return labels;
    }

    private static int count(Function function) {
        int labels = function.getTextLabels().size();
        for (int i = 0; i < function.getChildCount(); i++)
            labels += count((Function) function.getChildAt(i));
        return labels;
    }

    private List<File> openableSamples() {
        List<File> result = new ArrayList<File>();
        for (File sample : RsfFixture.sampleFiles())
            if (!KNOWN_UNOPENABLE.contains(sample.getName()))
                result.add(sample);
        return result;
    }

    private static String safeName(File file) {
        return file.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
