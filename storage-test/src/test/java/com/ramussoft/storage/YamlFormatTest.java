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

/**
 * Перевіряє правила запису YAML, на які спирається формат. Це не тест
 * бібліотеки, а фіксація саме тих гарантій, без яких формат перестає бути
 * придатним для git та для агентного редагування.
 */
public class YamlFormatTest {

    /**
     * Головна причина обрати YAML 1.2 замість 1.1: {@code no} лишається
     * рядком, а не перетворюється на {@code false}.
     */
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

        assertTrue("рядок має бути в лапках:\n" + text,
                text.contains("name: 'Виготовлення продукції'"));
        assertFalse("ключі не мають братися в лапки:\n" + text,
                text.contains("'name'"));
        assertTrue("число не має бути в лапках:\n" + text,
                text.contains("order: 2"));
        assertTrue(text.contains("x: 120.5"));
        assertTrue(text.contains("tunnel: false"));
    }

    /**
     * Типи мають пережити цикл запису й читання — інакше {@code 2} після
     * збереження стало б рядком {@code '2'}.
     */
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

    /**
     * Автоперенесення довгих рядків дало б різні файли для тих самих даних.
     */
    @Test
    public void longValuesAreNotWrapped() throws Exception {
        StringBuilder longValue = new StringBuilder();
        for (int i = 0; i < 40; i++)
            longValue.append("дуже довга назва функції ");

        Map<String, Object> document = new LinkedHashMap<String, Object>();
        document.put("name", longValue.toString());

        String text = write(document);

        assertEquals("значення має лишитись одним рядком:\n" + text,
                1, text.split("\n").length);
    }

    @Test
    public void multilineTextUsesLiteralBlock() throws Exception {
        Map<String, Object> document = new LinkedHashMap<String, Object>();
        document.put("note", "перший рядок\nдругий рядок");

        String text = write(document);

        assertTrue("очікувався літеральний блок:\n" + text,
                text.contains("note: |-") || text.contains("note: |"));
    }

    /**
     * Один і той самий об'єкт, використаний двічі, не має перетворюватись на
     * anchor/alias: інакше diff показує посилання замість даних, а агент не
     * бачить справжнього значення.
     */
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

        assertFalse("знайдено anchor:\n" + text, text.contains("&id"));
        assertFalse("знайдено alias:\n" + text, text.contains("*id"));
    }

    @Test
    public void writingIsDeterministic() throws Exception {
        Map<String, Object> document = new LinkedHashMap<String, Object>();
        document.put("b", "друге");
        document.put("a", "перше");

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
