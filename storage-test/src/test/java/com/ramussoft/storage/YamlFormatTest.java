package com.ramussoft.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ramussoft.core.format.yaml.YamlFormat;

public class YamlFormatTest {

    @Test
    public void norwayProblemDoesNotExist() throws Exception {
        Map<String, Object> loaded = read("country: no\nswitch: on\nanswer: yes\n");

        assertEquals("no", loaded.get("country"));
        assertEquals("on", loaded.get("switch"));
        assertEquals("yes", loaded.get("answer"));
    }

    @Test
    public void stringsAreQuotedAndNumbersAreNot() throws Exception {
        Map<String, Object> document = new LinkedHashMap<String, Object>();
        document.put("name", "Виготовлення продукції");
        document.put("order", java.lang.Integer.valueOf(2));
        document.put("x", java.lang.Double.valueOf(120.5));
        document.put("tunnel", Boolean.FALSE);

        String text = write(document);

        assertTrue("the string must be quoted:\n" + text,
                text.contains("name: 'Виготовлення продукції'"));
        assertFalse("keys must not be quoted:\n" + text,
                text.contains("'name'"));
        assertTrue("the number must not be quoted:\n" + text,
                text.contains("order: 2"));
        assertTrue(text.contains("x: 120.5"));
        assertTrue(text.contains("tunnel: false"));
    }

    @Test
    public void typesSurviveRoundTrip() throws Exception {
        Map<String, Object> document = new LinkedHashMap<String, Object>();
        document.put("text", "123");
        document.put("number", java.lang.Integer.valueOf(123));
        document.put("flag", Boolean.TRUE);

        Map<String, Object> loaded = read(write(document));

        assertEquals("123", loaded.get("text"));
        assertEquals(java.lang.Integer.valueOf(123), loaded.get("number"));
        assertEquals(Boolean.TRUE, loaded.get("flag"));
    }

    @Test
    public void longValuesAreNotWrapped() throws Exception {
        StringBuilder longValue = new StringBuilder();
        for (int i = 0; i < 40; i++)
            longValue.append("a very long function name ");

        Map<String, Object> document = new LinkedHashMap<String, Object>();
        document.put("name", longValue.toString());

        String text = write(document);

        assertEquals("the value must stay on one line:\n" + text,
                1, text.split("\n").length);
    }

    @Test
    public void multilineTextUsesLiteralBlock() throws Exception {
        Map<String, Object> document = new LinkedHashMap<String, Object>();
        document.put("note", "first line\nsecond line");

        String text = write(document);

        assertTrue("a literal block was expected:\n" + text,
                text.contains("note: |-") || text.contains("note: |"));
    }

    @Test
    public void repeatedValuesDoNotBecomeAliases() throws Exception {
        Map<String, Object> shared = new LinkedHashMap<String, Object>();
        shared.put("x", java.lang.Double.valueOf(10.0));

        List<Object> list = new ArrayList<Object>();
        list.add(shared);
        list.add(shared);

        Map<String, Object> document = new LinkedHashMap<String, Object>();
        document.put("points", list);

        String text = write(document);

        assertFalse("anchor found:\n" + text, text.contains("&id"));
        assertFalse("alias found:\n" + text, text.contains("*id"));
    }

    @Test
    public void writingIsDeterministic() throws Exception {
        Map<String, Object> document = new LinkedHashMap<String, Object>();
        document.put("b", "second");
        document.put("a", "first");

        assertEquals(write(document), write(document));
    }

    private static String write(Map<String, Object> document) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YamlFormat.write(document, out);
        return new String(out.toByteArray(), "UTF-8");
    }

    private static Map<String, Object> read(String text) throws Exception {
        return YamlFormat.read(new ByteArrayInputStream(text.getBytes("UTF-8")));
    }
}
