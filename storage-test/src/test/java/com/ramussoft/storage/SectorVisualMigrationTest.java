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

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.print.PIDEF0painter;

/**
 * Перенесення вигляду стрілок (обведення, шрифт, колір) із двійкового поля
 * {@code SectorPersistent.visualAttributes} в іменовані поля.
 * <p>
 * Перевіряємо саме те, що має значення: після перенесення двійкове поле
 * порожнє, а діаграми виглядають так само.
 */
public class SectorVisualMigrationTest {

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
    public void arrowLooksTheSameAfterLeavingTheBlob() throws Exception {
        for (File sample : openableSamples()) {
            File work = folder.newFolder(safeName(sample));
            File source = new File(work, "source.rsf");
            File migrated = new File(work, "migrated.rsf");
            RsfFixture.copy(sample, source);

            Map<String, String> before;
            int migrated_count;

            MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                    .createDatabase(source);
            try {
                Engine engine = database.getEngine(null);
                before = DiagramRenderer.render(engine,
                        database.getAccessRules(null));
                migrated_count = migrateVisuals(engine,
                        database.getAccessRules(null));
                ((FileIEngineImpl) engine.getDeligate()).saveToFile(migrated);
            } finally {
                database.close();
            }

            assertTrue(sample.getName()
                    + ": у зразку немає стрілок із власним виглядом",
                    migrated_count > 0);

            MemoryDatabase reopened = (MemoryDatabase) FileDatabaseFactory
                    .createDatabase(migrated);
            try {
                Engine engine = reopened.getEngine(null);
                assertEquals(sample.getName()
                                + ": у секторах лишилися двійкові дані", 0,
                        countBlobs(engine, reopened.getAccessRules(null)));

                Map<String, String> after = DiagramRenderer.render(engine,
                        reopened.getAccessRules(null));
                List<String> changed = new ArrayList<String>();
                for (Map.Entry<String, String> entry : before.entrySet()) {
                    String other = after.get(entry.getKey());
                    double difference = other == null ? 255.0
                            : DiagramRenderer.difference(entry.getValue(),
                                    other);
                    if (difference > 1.0)
                        changed.add(String.format("%s (різниця %.2f)",
                                entry.getKey(), Double.valueOf(difference)));
                }
                if (!changed.isEmpty())
                    fail(sample.getName() + ": змінився вигляд діаграм:\n  "
                            + String.join("\n  ", changed));
                ((FileIEngineImpl) engine.getDeligate()).close();
            } finally {
                reopened.close();
            }
        }
    }

    /**
     * @return скільки секторів мали вигляд у двійковому полі
     */
    private static int migrateVisuals(Engine engine, AccessRules rules) {
        int count = 0;
        for (Qualifier model : IDEF0Plugin.getBaseQualifiers(engine)) {
            DataPlugin plugin = NDataPluginFactory.getDataPlugin(model, engine,
                    rules);
            Function base = plugin.getBaseFunction();
            if (base != null)
                count += migrate(plugin, base);
        }
        return count;
    }

    private static int migrate(DataPlugin plugin, Function function) {
        MovingArea area = PIDEF0painter.createMovingArea(SIZE, plugin,
                function);
        area.setActiveFunction(function);
        SectorRefactor refactor = area.getRefactor();

        int count = 0;
        for (int i = 0; i < refactor.getSectorsCount(); i++) {
            Sector sector = refactor.getSector(i).getSector();
            if (sector != null && sector.getVisualAttributes() != null
                    && sector.getVisualAttributes().length > 0)
                count++;
            refactor.getSector(i).saveVisual();
        }

        for (int i = 0; i < function.getChildCount(); i++)
            count += migrate(plugin, (Function) function.getChildAt(i));
        return count;
    }

    private static int countBlobs(Engine engine, AccessRules rules) {
        int blobs = 0;
        for (Qualifier model : IDEF0Plugin.getBaseQualifiers(engine)) {
            DataPlugin plugin = NDataPluginFactory.getDataPlugin(model, engine,
                    rules);
            Function base = plugin.getBaseFunction();
            if (base != null)
                blobs += count(plugin, base);
        }
        return blobs;
    }

    /**
     * Рахує так само, як переносить: по секторах, що справді з'являються на
     * діаграмах. Інакше порівнювалися б різні множини.
     */
    private static int count(DataPlugin plugin, Function function) {
        MovingArea area = PIDEF0painter.createMovingArea(SIZE, plugin,
                function);
        area.setActiveFunction(function);
        SectorRefactor refactor = area.getRefactor();

        int blobs = 0;
        for (int i = 0; i < refactor.getSectorsCount(); i++) {
            Sector sector = refactor.getSector(i).getSector();
            if (sector != null && sector.getVisualAttributes() != null
                    && sector.getVisualAttributes().length > 0)
                blobs++;
        }
        for (int i = 0; i < function.getChildCount(); i++)
            blobs += count(plugin, (Function) function.getChildAt(i));
        return blobs;
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
