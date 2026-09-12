package com.ramussoft.storage;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.util.Locale;

/**
 * Шрифт, яким малюються діаграми в тестах.
 * <p>
 * Еталонні знімки {@link DiagramGoldenTest} порівнюються попіксельно, тож
 * відмальовування має залежати лише від коду. Системні шрифти цього не дають:
 * моделі зберігають логічний {@code Dialog}, а налаштування за замовчуванням —
 * {@code Arial}, і кожна машина відображає їх власним фізичним шрифтом. Різні
 * метрики переносять рядки в інших місцях, і та сама модель дає інший знімок.
 * <p>
 * Тому в ресурсах лежить власний {@code .ttf}, і тест реєструє його в JVM.
 * Родина називається {@value #FAMILY}, а не як вихідний шрифт: реєстрація не
 * перекриває вже встановлений системний шрифт тієї ж назви, тож назва мусить
 * бути такою, якої в системі напевно немає.
 */
public final class TestFonts {

    /**
     * Назва родини всередині {@code RamusGoldenSans.ttf}.
     */
    public static final String FAMILY = "Ramus Golden Sans";

    private static final String RESOURCE = "/fonts/RamusGoldenSans.ttf";

    private static boolean installed;

    private TestFonts() {
    }

    /**
     * Реєструє вбудований шрифт. Повторні виклики нічого не роблять: у межах
     * однієї JVM реєстрація потрібна один раз, а друга спроба повернула б
     * {@code false} саме тому, що перша вдалася.
     */
    public static synchronized void install() {
        if (installed)
            return;
        InputStream in = TestFonts.class.getResourceAsStream(RESOURCE);
        if (in == null)
            throw new IllegalStateException("Немає ресурсу " + RESOURCE);
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
                    "Не вдалося зареєструвати " + RESOURCE, e);
        }
        installed = true;

        // Якщо родини немає, JVM мовчки підставляє інший шрифт, і тест впав би
        // незрозумілою різницею зображень замість зрозумілої помилки.
        String family = new Font(FAMILY, Font.PLAIN, 12).getFamily(Locale.ROOT);
        if (!FAMILY.equals(family))
            throw new IllegalStateException("Шрифт «" + FAMILY
                    + "» не зареєструвався: запит дав родину «" + family + "»");
    }

    /**
     * Той самий шрифт зі збереженими накресленням і розміром.
     *
     * @param font зразок; {@code null} дає шрифт розміру за замовчуванням
     */
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
