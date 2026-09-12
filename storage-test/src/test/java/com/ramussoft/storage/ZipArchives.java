package com.ramussoft.storage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Читання ZIP-архіву у мапу «шлях → байти» та порівняння двох архівів
 * з діагностикою, придатною для читання людиною.
 */
public final class ZipArchives {

    /**
     * Максимум рядків розбіжностей на один запис, щоб звіт лишався оглядним.
     */
    private static final int MAX_REPORTED_LINES = 6;

    private ZipArchives() {
    }

    public static TreeMap<String, byte[]> read(File file) throws IOException {
        TreeMap<String, byte[]> result = new TreeMap<String, byte[]>();
        ZipFile zip = new ZipFile(file);
        try {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory())
                    continue;
                InputStream in = zip.getInputStream(entry);
                try {
                    result.put(entry.getName(), readAll(in));
                } finally {
                    in.close();
                }
            }
        } finally {
            zip.close();
        }
        return result;
    }

    /**
     * Порядок записів у архіві. На відміну від {@link #read}, зберігає
     * послідовність — саме вона плаває при недетермінованому записі.
     */
    public static List<String> entryOrder(File file) throws IOException {
        List<String> order = new ArrayList<String>();
        ZipFile zip = new ZipFile(file);
        try {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory())
                    order.add(entry.getName());
            }
        } finally {
            zip.close();
        }
        return order;
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) > 0)
            out.write(buffer, 0, read);
        return out.toByteArray();
    }

    /**
     * Порівнює вміст двох архівів, ігноруючи порядок записів і мітки часу ZIP.
     *
     * @return порожній рядок, якщо вміст збігається; інакше — звіт про різницю.
     */
    public static String diff(File expected, File actual) throws IOException {
        TreeMap<String, byte[]> a = read(expected);
        TreeMap<String, byte[]> b = read(actual);

        StringBuilder report = new StringBuilder();

        Set<String> onlyInA = new LinkedHashSet<String>(a.keySet());
        onlyInA.removeAll(b.keySet());
        Set<String> onlyInB = new LinkedHashSet<String>(b.keySet());
        onlyInB.removeAll(a.keySet());

        for (String name : onlyInA)
            report.append("  лише в першому:  ").append(name).append('\n');
        for (String name : onlyInB)
            report.append("  лише в другому:  ").append(name).append('\n');

        for (String name : a.keySet()) {
            byte[] left = a.get(name);
            byte[] right = b.get(name);
            if (right == null)
                continue;
            if (java.util.Arrays.equals(left, right))
                continue;
            report.append("  різний вміст:    ").append(name).append(" (")
                    .append(left.length).append(" vs ").append(right.length)
                    .append(" байт)\n");
            appendTextDiff(report, left, right);
        }

        return report.toString();
    }

    private static void appendTextDiff(StringBuilder report, byte[] left,
                                       byte[] right) {
        String[] leftLines = splitLines(left);
        String[] rightLines = splitLines(right);
        if (leftLines == null || rightLines == null)
            return;
        int reported = 0;
        int max = Math.max(leftLines.length, rightLines.length);
        for (int i = 0; i < max && reported < MAX_REPORTED_LINES; i++) {
            String l = i < leftLines.length ? leftLines[i] : "<немає рядка>";
            String r = i < rightLines.length ? rightLines[i] : "<немає рядка>";
            if (l.equals(r))
                continue;
            report.append("      рядок ").append(i + 1).append(":\n");
            report.append("        1: ").append(abbreviate(l)).append('\n');
            report.append("        2: ").append(abbreviate(r)).append('\n');
            reported++;
        }
    }

    private static String[] splitLines(byte[] data) {
        try {
            return new String(data, "UTF-8").split("\n", -1);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static String abbreviate(String value) {
        String single = value.replace('\r', ' ');
        if (single.length() <= 200)
            return single;
        return single.substring(0, 200) + "…";
    }
}
