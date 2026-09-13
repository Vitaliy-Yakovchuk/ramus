package com.ramussoft.storage;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ramussoft.common.Engine;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;

public class DiagramGoldenTest {

    private static final String RESOURCE = "/diagram-golden.properties";

    static final double TOLERANCE = 0.0;

    private static final List<String> KNOWN_UNOPENABLE = Arrays
            .asList("Пример модели.rsf");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        RsfFixture.isolateHome(folder.newFolder("home"));
    }

    @Test
    public void diagramsMatchGolden() throws Exception {
        Map<String, String> actual = new LinkedHashMap<String, String>();
        for (File sample : openableSamples()) {
            MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                    .createDatabase(sample);
            try {
                Engine engine = database.getEngine(null);
                for (Map.Entry<String, String> entry : DiagramRenderer.render(
                        engine, database.getAccessRules(null)).entrySet())
                    actual.put(sample.getName() + "|" + entry.getKey(),
                            entry.getValue());
                ((FileIEngineImpl) engine.getDeligate()).close();
            } finally {
                database.close();
            }
        }

        if (System.getProperty("ramus.golden.update") != null) {
            writeGolden(actual);
            return;
        }

        Properties golden = readGolden();
        if (golden.isEmpty())
            fail("No golden file " + RESOURCE + "; create it by running with"
                    + " -Dramus.golden.update=1");

        List<String> problems = new ArrayList<String>();
        for (Map.Entry<String, String> entry : actual.entrySet()) {
            String expected = golden.getProperty(entry.getKey());
            if (expected == null)
                problems.add("new diagram: " + entry.getKey());
            else {
                double difference = DiagramRenderer.difference(expected,
                        entry.getValue());
                if (difference > TOLERANCE)
                    problems.add(String.format(
                            "image changed (difference %.2f): %s",
                            Double.valueOf(difference), entry.getKey()));
            }
        }
        for (Object key : golden.keySet())
            if (!actual.containsKey(key.toString()))
                problems.add("diagram gone: " + key);

        if (!problems.isEmpty())
            fail("Diagram rendering changed:\n  "
                    + String.join("\n  ", problems));
    }

    private static Properties readGolden() throws Exception {
        Properties properties = new Properties();
        InputStream in = DiagramGoldenTest.class.getResourceAsStream(RESOURCE);
        if (in == null)
            return properties;
        try {
            properties.load(new java.io.InputStreamReader(in, "UTF-8"));
        } finally {
            in.close();
        }
        return properties;
    }

    private static void writeGolden(Map<String, String> values)
            throws Exception {
        File target = new File(RsfFixture.projectRoot(),
                "storage-test/src/test/resources" + RESOURCE);
        target.getParentFile().mkdirs();
        Writer writer = new OutputStreamWriter(new FileOutputStream(target),
                "UTF-8");
        try {
            writer.write("# Fingerprints of the rendered diagrams.\n");
            writer.write("# Update deliberately only: a mismatch means a code change\n");
            writer.write("# has altered how the model looks.\n");
            List<String> keys = new ArrayList<String>(values.keySet());
            java.util.Collections.sort(keys);
            for (String key : keys)
                writer.write(escape(key) + " = " + values.get(key) + "\n");
        } finally {
            writer.close();
        }
        System.out.println("Golden file updated: " + target);
    }

    private static String escape(String key) {
        return key.replace("\\", "\\\\").replace("\n", "\\n")
                .replace("\r", "\\r").replace(" ", "\\ ")
                .replace("=", "\\=").replace(":", "\\:");
    }

    private List<File> openableSamples() {
        List<File> result = new ArrayList<File>();
        for (File sample : RsfFixture.sampleFiles())
            if (!KNOWN_UNOPENABLE.contains(sample.getName()))
                result.add(sample);
        return result;
    }
}
