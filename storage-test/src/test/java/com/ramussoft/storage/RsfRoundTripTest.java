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

public class RsfRoundTripTest {

    private static final List<String> KNOWN_UNOPENABLE = Arrays.asList(
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
                "No .rsf found in dest/doc, the test has nothing to check",
                RsfFixture.sampleFiles().isEmpty());
    }

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
            fail("The set of samples that open has changed."
                    + "\n  stopped opening: " + unexpectedlyBroken
                    + "\n  started opening: " + unexpectedlyFixed);
    }

    @Test
    public void resaveIsDeterministic() throws Exception {
        StringBuilder failures = new StringBuilder();

        for (File sample : openableSamples()) {
            File work = folder.newFolder(safeName(sample));

            File source = new File(work, "source.rsf");
            RsfFixture.copy(sample, source);

            File first = new File(work, "first.rsf");
            File second = new File(work, "second.rsf");
            File third = new File(work, "third.rsf");

            RsfFixture.resave(source, first);
            RsfFixture.resave(first, second);
            RsfFixture.resave(second, third);

            String diff = ZipArchives.diff(second, third);
            if (diff.length() > 0)
                failures.append('\n').append(sample.getName())
                        .append(" - the archive content changes between saves:\n")
                        .append(diff);

            List<String> orderA = ZipArchives.entryOrder(second);
            List<String> orderB = ZipArchives.entryOrder(third);
            if (!orderA.equals(orderB))
                failures.append('\n').append(sample.getName())
                        .append(" - the order of ZIP entries is not stable:\n")
                        .append("  2: ").append(orderA).append('\n')
                        .append("  3: ").append(orderB).append('\n');
        }

        if (failures.length() > 0)
            fail("Saving an .rsf is not deterministic:" + failures);
    }

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
                        .append(" - the files differ byte for byte (")
                        .append(a.length).append(" vs ").append(b.length)
                        .append(" bytes), though the entries match;")
                        .append(" check the ZIP metadata.");
        }

        if (failures.length() > 0)
            fail("Saving an .rsf is not byte-stable:" + failures);
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

            assertTrue(sample.getName() + ": the resaved file is empty",
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
