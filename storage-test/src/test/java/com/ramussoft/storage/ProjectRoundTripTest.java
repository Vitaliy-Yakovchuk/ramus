package com.ramussoft.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.format.ProjectWriter;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;

/**
 * Головна перевірка нового формату: модель має переживати цикл
 * {@code .rsf → проєкт → .rsf → проєкт} без втрат.
 * <p>
 * Порівнюються всі файли проєкту, без винятків для системних класифікаторів:
 * читання відбувається нижче рівня плагінів, тож вони мають відновлюватися так
 * само дослівно, як і користувацькі дані.
 */
public class ProjectRoundTripTest {

    private static final List<String> KNOWN_UNOPENABLE = Arrays
            .asList("Пример модели.rsf");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        RsfFixture.isolateHome(folder.newFolder("home"));
    }

    /**
     * Цикл через {@code .rsf}: проєкт, зібраний назад у архів і знову
     * розкладений, має дати те саме дерево файлів.
     */
    @Test
    public void projectSurvivesRsfRoundTrip() throws Exception {
        StringBuilder failures = new StringBuilder();

        for (File sample : openableSamples()) {
            File work = folder.newFolder(safeName(sample) + "-rt");
            File first = new File(work, "first");
            File rebuilt = new File(work, "rebuilt.rsf");
            File second = new File(work, "second");

            RsfFixture.exportProject(sample, first);
            RsfFixture.importProject(first, rebuilt);
            RsfFixture.exportProject(rebuilt, second);

            compare(sample.getName(), first, second, failures);
        }

        if (failures.length() > 0)
            fail("Цикл проєкт → .rsf → проєкт не зберігає модель:" + failures);
    }

    /**
     * Цикл без {@code .rsf}: відкрити проєкт і зберегти його — саме те, що
     * робить застосунок. Повторне збереження без змін не має чіпати жодного
     * файлу, інакше кожне відкриття давало б коміт у сховищі версій.
     */
    @Test
    public void resavingProjectChangesNothing() throws Exception {
        StringBuilder failures = new StringBuilder();

        for (File sample : openableSamples()) {
            File work = folder.newFolder(safeName(sample) + "-resave");
            File first = new File(work, "first");
            File second = new File(work, "second");

            RsfFixture.exportProject(sample, first);
            RsfFixture.resaveProject(first, second);

            compare(sample.getName(), first, second, failures);
        }

        if (failures.length() > 0)
            fail("Повторне збереження проєкту змінює файли:" + failures);
    }

    /**
     * Найсильніша перевірка: діаграми, відмальовані з оригінального
     * {@code .rsf} і з архіву, зібраного з проєкту, мають збігатися. Порівняння
     * текстів довело б лише те, що збіглися тексти.
     */
    @Test
    public void diagramsLookIdenticalAfterRoundTrip() throws Exception {
        for (File sample : openableSamples()) {
            File work = folder.newFolder(safeName(sample) + "-render");
            File project = new File(work, "project");
            File rebuilt = new File(work, "rebuilt.rsf");

            RsfFixture.exportProject(sample, project);
            RsfFixture.importProject(project, rebuilt);

            Map<String, String> before = renderFrom(sample);
            Map<String, String> after = renderFrom(rebuilt);

            assertEquals(sample.getName() + ": різний набір діаграм",
                    before.keySet(), after.keySet());
            for (Map.Entry<String, String> entry : before.entrySet()) {
                double difference = DiagramRenderer.difference(
                        entry.getValue(), after.get(entry.getKey()));
                assertTrue(sample.getName() + ": діаграма «" + entry.getKey()
                                + "» відмальовується інакше (відмінність "
                                + difference + ")",
                        difference <= DiagramGoldenTest.TOLERANCE);
            }
        }
    }

    @Test
    public void openedProjectHasElements() throws Exception {
        File sample = openableSamples().get(0);
        File work = folder.newFolder("counts");
        File project = new File(work, "project");
        RsfFixture.exportProject(sample, project);

        MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase(project);
        try {
            Engine engine = database.getEngine(null);
            int elements = 0;
            for (Qualifier qualifier : engine.getQualifiers())
                elements += engine.getElements(qualifier.getId()).size();
            assertTrue("відкритий проєкт не містить елементів", elements > 0);
            ((FileIEngineImpl) engine.getDeligate()).close();
        } finally {
            database.close();
        }
    }

    /**
     * Проєкт має відкриватися і за своїм описом, а не лише за каталогом:
     * саме файл приходить із робочого столу при подвійному клацанні.
     */
    @Test
    public void projectOpensByItsDescriptionFile() throws Exception {
        File sample = openableSamples().get(0);
        File project = new File(folder.newFolder("by-file"), "model.ramus");
        RsfFixture.exportProject(sample, project);

        File description = new File(project, ProjectWriter.PROJECT_FILE);
        assertTrue("немає опису проєкту", description.isFile());

        MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase(description);
        try {
            Engine engine = database.getEngine(null);
            int elements = 0;
            for (Qualifier qualifier : engine.getQualifiers())
                elements += engine.getElements(qualifier.getId()).size();
            assertTrue("проєкт, відкритий за описом, порожній", elements > 0);
            ((FileIEngineImpl) engine.getDeligate()).close();
        } finally {
            database.close();
        }
    }

    /**
     * Стан інтерфейсу лежить окремо і не потрапляє під версійний контроль.
     */
    @Test
    public void interfaceStateGoesToLocalDirectory() throws Exception {
        File sample = openableSamples().get(0);
        File project = new File(folder.newFolder("local"), "project");
        RsfFixture.exportProject(sample, project);

        assertTrue("немає .gitignore",
                new File(project, ".gitignore").isFile());
        assertEquals(".local/\n", read(new File(project, ".gitignore")));

        // Файл належить користувачеві: збереження проєкту не має стирати
        // його власних правил.
        java.io.Writer writer = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(new File(project, ".gitignore")),
                "UTF-8");
        writer.write("*.bak\n.local/\n");
        writer.close();

        RsfFixture.resaveProject(project, project);
        assertEquals("*.bak\n.local/\n",
                read(new File(project, ".gitignore")));

        for (String name : listRelative(project))
            assertTrue("стан інтерфейсу потрапив у версійовану частину: "
                            + name,
                    !name.contains("/user/") || name.startsWith(".local/"));
    }

    private void compare(String sample, File first, File second,
                         StringBuilder failures) throws Exception {
        List<String> namesA = listRelative(first);
        List<String> namesB = listRelative(second);
        if (!namesA.equals(namesB)) {
            failures.append('\n').append(sample)
                    .append(" — різний набір файлів:\n  1: ").append(namesA)
                    .append("\n  2: ").append(namesB);
            return;
        }
        for (String name : namesA) {
            String a = read(new File(first, name));
            String b = read(new File(second, name));
            if (!a.equals(b))
                failures.append('\n').append(sample).append(" — ").append(name)
                        .append(" відрізняється:\n")
                        .append(firstDifference(a, b));
        }
    }

    private static Map<String, String> renderFrom(File source)
            throws Exception {
        MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase(source);
        try {
            Engine engine = database.getEngine(null);
            Map<String, String> result = DiagramRenderer.render(engine,
                    database.getAccessRules(null));
            ((FileIEngineImpl) engine.getDeligate()).close();
            return result;
        } finally {
            database.close();
        }
    }

    private static String firstDifference(String a, String b) {
        String[] linesA = a.split("\n");
        String[] linesB = b.split("\n");
        for (int i = 0; i < Math.max(linesA.length, linesB.length); i++) {
            String left = i < linesA.length ? linesA[i] : "<немає>";
            String right = i < linesB.length ? linesB[i] : "<немає>";
            if (!left.equals(right))
                return "    рядок " + (i + 1) + ":\n      1: "
                        + abbreviate(left) + "\n      2: " + abbreviate(right);
        }
        return "    (розбіжність лише в довжині)";
    }

    private static String abbreviate(String value) {
        return value.length() <= 160 ? value : value.substring(0, 160) + "…";
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
            java.io.ByteArrayOutputStream out =
                    new java.io.ByteArrayOutputStream();
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
