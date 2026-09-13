package com.ramussoft.storage;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.util.Locale;

public final class TestFonts {

    public static final String FAMILY = "Ramus Golden Sans";

    private static final String RESOURCE = "/fonts/RamusGoldenSans.ttf";

    private static boolean installed;

    private TestFonts() {
    }

    public static synchronized void install() {
        if (installed)
            return;
        InputStream in = TestFonts.class.getResourceAsStream(RESOURCE);
        if (in == null)
            throw new IllegalStateException("No resource " + RESOURCE);
        try {
            try {
                Font font = Font.createFont(Font.TRUETYPE_FONT, in);
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .registerFont(font);
            } finally {
                in.close();
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Can not register " + RESOURCE, e);
        }
        installed = true;

        String family = new Font(FAMILY, Font.PLAIN, 12).getFamily(Locale.ROOT);
        if (!FAMILY.equals(family))
            throw new IllegalStateException("Font \"" + FAMILY
                    + "\" did not register: the request gave the family \"" + family + "\"");
    }

    public static Font like(Font font) {
        install();
        if (font == null)
            return new Font(FAMILY, Font.PLAIN, 12);
        return new Font(FAMILY, font.getStyle(), font.getSize());
    }

    public static Font of(int style, int size) {
        install();
        return new Font(FAMILY, style, size);
    }
}
