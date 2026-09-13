package com.ramussoft.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import com.ramussoft.common.PropertiesXml;

public class PropertiesXmlTest {

    @Test
    public void roundTripsThroughLoadFromXML() throws Exception {
        Properties original = new Properties();
        original.setProperty("ApplicationName", "Ramus");
        original.setProperty("Special", "<tag> & \"quote\" 'apos' \t\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PropertiesXml.store(original, out, "test");

        Properties reloaded = new Properties();
        reloaded.loadFromXML(new ByteArrayInputStream(out.toByteArray()));

        assertEquals(original, reloaded);
    }

    @Test
    public void keysAreWrittenInSortedOrder() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("b", "1");
        properties.setProperty("a", "2");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PropertiesXml.store(properties, out, null);

        String xml = out.toString("UTF-8");
        assertTrue(xml.indexOf("key=\"a\"") < xml.indexOf("key=\"b\""));
    }

    @Test
    public void rejectsIllegalXmlControlCharacters() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("key", "before" + (char) 0x0B + "after");

        try {
            PropertiesXml.store(properties, new ByteArrayOutputStream(), null);
            fail("Should have thrown on character 0x0B");
        } catch (IOException expected) {
        }
    }
}
