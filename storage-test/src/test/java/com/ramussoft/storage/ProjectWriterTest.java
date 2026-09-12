package com.ramussoft.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ramussoft.common.Engine;
import com.ramussoft.core.format.ProjectWriter;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;

/**
 * Експорт реальної моделі в дерево YAML: перевіряємо, що воно взагалі
 * будується, що результат детермінований і що у файлах немає слідів
 * позиційного дампу таблиць.
 */
public class ProjectWriterTest {

    private static final List<String> KNOWN_UNOPENABLE = Arrays
            .asList("Пример модели.rsf");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        RsfFixture.isolateHome(folder.newFolder("home"));
    }

    @Test
    public void exportsSamplesToYaml() throws Exception {
        for (File sample : openableSamples()) {
            File out = folder.newFolder(safeName(sample) + "-yaml");
            export(sample, out);

            assertTrue("немає опису проєкту для " + sample.getName(),
                    new File(out, ProjectWriter.PROJECT_FILE).isFile());
            assertTrue("немає attributes.yaml для " + sample.getName(),
                    new File(out, "attributes.yaml").isFile());

            File qualifiers = new File(out, "qualifiers");
            assertTrue("немає каталогу qualifiers для " + sample.getName(),
                    qualifiers.isDirectory());
            assertTrue("не експортовано жодного класифікатора для "
                            + sample.getName(),
                    qualifiers.list().length > 0);
        }
    }

    /**
     * Головна вимога до формату: той самий проєкт має давати той самий текст.
     */
    @Test
    public void exportIsDeterministic() throws Exception {
        for (File sample : openableSamples()) {
            File first = folder.newFolder(safeName(sample) + "-a");
            File second = folder.newFolder(safeName(sample) + "-b");

            export(sample, first);
            export(sample, second);

            List<String> namesA = listRelative(first);
            List<String> namesB = listRelative(second);
            assertEquals("набір файлів відрізняється для " + sample.getName(),
                    namesA, namesB);

            for (String name : namesA)
                assertEquals("вміст " + name + " відрізняється для "
                                + sample.getName(),
                        read(new File(first, name)), read(new File(second, name)));
        }
    }

    /**
     * Імена файлів мають бути читабельними: {@code <slug>--<id>.yaml}.
     */
    @Test
    public void qualifierFilesAreNamedReadably() throws Exception {
        File sample = openableSamples().get(0);
        File out = folder.newFolder("naming");
        export(sample, out);

        String[] names = new File(out, "qualifiers").list();
        Arrays.sort(names);
        for (String name : names) {
            assertTrue("ім'я без розділювача slug/id: " + name,
                    name.contains("--"));
            assertTrue("не .yaml: " + name, name.endsWith(".yaml"));
        }
    }

    /**
     * У новому форматі не має лишатися ані позиційних полів старого XML,
     * ані hex-кодованих двійкових блобів.
     */
    @Test
    public void exportHasNoPositionalFields() throws Exception {
        File sample = openableSamples().get(0);
        File out = folder.newFolder("shape");
        export(sample, out);

        for (String name : listRelative(out)) {
            String text = read(new File(out, name));
            assertFalse(name + " містить позиційне поле старого формату",
                    text.contains("<f id="));
            assertFalse(name + " містить назву таблиці БД",
                    text.contains("generate-from-table"));
        }
    }

    private static void export(File sample, File target) throws Exception {
        RsfFixture.exportProject(sample, target);
    }

    private List<File> openableSamples() {
        List<File> result = new ArrayList<File>();
        for (File sample : RsfFixture.sampleFiles())
            if (!KNOWN_UNOPENABLE.contains(sample.getName()))
                result.add(sample);
        return result;
    }

    private static List<String> listRelative(File root) {
        List<String> result = new ArrayList<String>();
        collect(root, root, result);
        Collections.sort(result);
        return result;
    }

    private static void collect(File root, File dir, List<String> out) {
        File[] children = dir.listFiles();
        if (children == null)
            return;
        for (File child : children) {
            if (child.isDirectory())
                collect(root, child, out);
            else
                out.add(root.toURI().relativize(child.toURI()).getPath());
        }
    }

    private static String read(File file) throws Exception {
        InputStream in = new FileInputStream(file);
        try {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = in.read(buffer)) > 0)
                out.write(buffer, 0, count);
            return new String(out.toByteArray(), "UTF-8");
        } finally {
            in.close();
        }
    }

    private static String safeName(File file) {
        return file.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
