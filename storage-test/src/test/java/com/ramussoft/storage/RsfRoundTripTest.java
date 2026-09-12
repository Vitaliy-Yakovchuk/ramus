package com.ramussoft.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Перевіряє, що збереження {@code .rsf} детерміноване: два послідовні
 * перезаписи однієї моделі мають давати ідентичний файл.
 * <p>
 * Порівнювати вихідний файл із перезбереженим не можна — зразки в
 * {@code dest/doc} створені старою версією застосунку. Значущим є саме
 * порівняння другого перезапису з третім.
 */
public class RsfRoundTripTest {

    /**
     * Зразки, які поточна версія свідомо не відкриває. Тримаємо список явно,
     * щоб нова непрацездатність одразу впадала в очі, а не ховалась у skip.
     *
     * @see #knownUnopenableSamplesAreStillUnopenable()
     */
    private static final List<String> KNOWN_UNOPENABLE = Arrays.asList(
            // Посилається на плагін "Attribute.Doc.Way", якого в коді немає.
            "Пример модели.rsf");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        RsfFixture.isolateHome(folder.newFolder("home"));
    }

    @Test
    public void samplesArePresent() {
        assertFalse(
                "У dest/doc не знайдено жодного .rsf — тест не має що перевіряти",
                RsfFixture.sampleFiles().isEmpty());
    }

    /**
     * Список непрацездатних зразків має лишатись рівно таким, як записано.
     * Якщо файл раптом почав відкриватись — приберіть його з
     * {@link #KNOWN_UNOPENABLE}. Якщо перестав — це регресія.
     */
    @Test
    public void knownUnopenableSamplesAreStillUnopenable() {
        List<String> unexpectedlyBroken = new ArrayList<String>();
        List<String> unexpectedlyFixed = new ArrayList<String>();

        for (File sample : RsfFixture.sampleFiles()) {
            String failure = RsfFixture.openFailure(sample);
            boolean known = KNOWN_UNOPENABLE.contains(sample.getName());
            if (failure != null && !known)
                unexpectedlyBroken.add(sample.getName() + " — " + failure);
            if (failure == null && known)
                unexpectedlyFixed.add(sample.getName());
        }

        if (!unexpectedlyBroken.isEmpty() || !unexpectedlyFixed.isEmpty())
            fail("Змінився перелік зразків, які відкриваються."
                    + "\n  перестали відкриватись: " + unexpectedlyBroken
                    + "\n  почали відкриватись:    " + unexpectedlyFixed);
    }

    @Test
    public void resaveIsDeterministic() throws Exception {
        StringBuilder failures = new StringBuilder();

        for (File sample : openableSamples()) {
            File work = folder.newFolder(safeName(sample));

            File source = new File(work, "source.rsf");
            RsfFixture.copy(sample, source);

            // Перше збереження нормалізує файл під поточну версію формату;
            // значущим є порівняння другого з третім.
            File first = new File(work, "first.rsf");
            File second = new File(work, "second.rsf");
            File third = new File(work, "third.rsf");

            RsfFixture.resave(source, first);
            RsfFixture.resave(first, second);
            RsfFixture.resave(second, third);

            String diff = ZipArchives.diff(second, third);
            if (diff.length() > 0)
                failures.append('\n').append(sample.getName())
                        .append(" — вміст архіву змінюється між збереженнями:\n")
                        .append(diff);

            List<String> orderA = ZipArchives.entryOrder(second);
            List<String> orderB = ZipArchives.entryOrder(third);
            if (!orderA.equals(orderB))
                failures.append('\n').append(sample.getName())
                        .append(" — порядок записів у ZIP не стабільний:\n")
                        .append("  2: ").append(orderA).append('\n')
                        .append("  3: ").append(orderB).append('\n');
        }

        if (failures.length() > 0)
            fail("Збереження .rsf недетерміноване:" + failures);
    }

    /**
     * Найстрогіша форма вимоги: повторне збереження без змін має давати
     * побайтово той самий файл, інакше git бачить зміну там, де її немає.
     */
    @Test
    public void resaveIsByteIdentical() throws Exception {
        StringBuilder failures = new StringBuilder();

        for (File sample : openableSamples()) {
            File work = folder.newFolder(safeName(sample) + "-bytes");
            File source = new File(work, "source.rsf");
            RsfFixture.copy(sample, source);

            File first = new File(work, "first.rsf");
            File second = new File(work, "second.rsf");
            File third = new File(work, "third.rsf");

            RsfFixture.resave(source, first);
            RsfFixture.resave(first, second);
            RsfFixture.resave(second, third);

            byte[] a = readAll(second);
            byte[] b = readAll(third);
            if (!Arrays.equals(a, b))
                failures.append('\n').append(sample.getName())
                        .append(" — файли відрізняються побайтово (")
                        .append(a.length).append(" vs ").append(b.length)
                        .append(" байт), хоча вміст записів збігається;")
                        .append(" перевірте метадані ZIP.");
        }

        if (failures.length() > 0)
            fail("Збереження .rsf не побайтово стабільне:" + failures);
    }

    private static byte[] readAll(File file) throws Exception {
        java.io.InputStream in = new java.io.FileInputStream(file);
        try {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) > 0)
                out.write(buffer, 0, read);
            return out.toByteArray();
        } finally {
            in.close();
        }
    }

    @Test
    public void resaveKeepsAllEntries() throws Exception {
        for (File sample : openableSamples()) {
            File work = folder.newFolder(safeName(sample) + "-entries");
            File source = new File(work, "source.rsf");
            File saved = new File(work, "saved.rsf");
            RsfFixture.copy(sample, source);
            RsfFixture.resave(source, saved);

            assertTrue(sample.getName() + ": перезбережений файл порожній",
                    ZipArchives.read(saved).size() > 0);
        }
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
