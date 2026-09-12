package com.ramussoft.common;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Запис {@link Properties} у XML з детермінованим порядком ключів.
 * <p>
 * Штатний {@link Properties#storeToXML} успадковує порядок від
 * {@link java.util.Hashtable}, тож той самий набір властивостей щоразу
 * потрапляє у файл в іншій послідовності. Формат тут той самий, тому файли
 * читаються звичайним {@link Properties#loadFromXML}.
 */
public final class PropertiesXml {

    private static final String DOCTYPE = "<!DOCTYPE properties SYSTEM "
            + "\"http://java.sun.com/dtd/properties.dtd\">";

    private PropertiesXml() {
    }

    public static void store(Properties properties, OutputStream out,
                             String comment) throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\""
                + " standalone=\"no\"?>\n");
        writer.write(DOCTYPE);
        writer.write('\n');
        writer.write("<properties>\n");
        if (comment != null) {
            writer.write("<comment>");
            escape(writer, comment);
            writer.write("</comment>\n");
        }
        for (String key : sortedKeys(properties)) {
            writer.write("<entry key=\"");
            escape(writer, key);
            writer.write("\">");
            escape(writer, properties.getProperty(key));
            writer.write("</entry>\n");
        }
        writer.write("</properties>\n");

        // Потік належить викликачу (це запис у ZipOutputStream), тому
        // скидаємо буфер, але не закриваємо.
        writer.flush();
    }

    private static List<String> sortedKeys(Properties properties) {
        List<String> keys = new ArrayList<String>();
        Enumeration<?> names = properties.propertyNames();
        while (names.hasMoreElements())
            keys.add(names.nextElement().toString());
        Collections.sort(keys);
        return keys;
    }

    private static void escape(Writer writer, String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '<':
                    writer.write("&lt;");
                    break;
                case '>':
                    writer.write("&gt;");
                    break;
                case '&':
                    writer.write("&amp;");
                    break;
                case '"':
                    writer.write("&quot;");
                    break;
                case '\'':
                    writer.write("&apos;");
                    break;
                default:
                    if (c < 0x20 && c != '\t' && c != '\n' && c != '\r')
                        writer.write("&#" + (int) c + ";");
                    else
                        writer.write(c);
            }
        }
    }
}
