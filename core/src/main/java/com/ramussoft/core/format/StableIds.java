package com.ramussoft.core.format;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Стабільні зовнішні ідентифікатори для нового формату.
 * <p>
 * Числовий ключ БД лишається внутрішнім, а у файлах фігурує короткий
 * ідентифікатор, виведений з нього. Потрібні три властивості одночасно:
 * <ul>
 * <li><b>стабільність</b> — перейменування сутності не змінює id, бо він
 * виводиться з ключа, а не з назви;</li>
 * <li><b>детермінованість</b> — конвертація того самого проєкту двічі дає
 * ідентичний результат;</li>
 * <li><b>оборотність</b> — з id відновлюється числовий ключ, тому
 * {@code експорт → імпорт → експорт} дає ті самі ідентифікатори без
 * додаткової таблиці відповідності в базі.</li>
 * </ul>
 * Саме оборотність відрізняє це від звичайного хешу: без неї імпорт роздав би
 * нові числові ключі, і повторний експорт дав би інші id.
 * <p>
 * Перетворення — мережа Фейстеля на 30 бітах. Вона бієктивна за побудовою, тож
 * сусідні ключі дають несхожі рядки: {@code 767} і {@code 768} у файлі стоять
 * поруч, і візуально близькі id лише плутали б.
 * <p>
 * Ім'я файлу складається як {@code <slug>--<id>}: slug читає людина й агент,
 * id тримає посилання. Перейменування змінює лише slug, тому {@code git mv}
 * зберігає історію файлу.
 */
public final class StableIds {

    /**
     * Алфавіт без символів, які легко переплутати ({@code i}, {@code l},
     * {@code o}, {@code u}) — ідентифікатори доводиться читати очима.
     */
    private static final char[] ALPHABET = "0123456789abcdefghjkmnpqrstvwxyz"
            .toCharArray();

    private static final int[] DIGITS = new int[128];

    /**
     * Половина домену: 15 біт. Разом — 30 біт, тобто трохи більше мільярда
     * ключів, і рівно 6 символів у базі 32.
     */
    private static final int HALF_BITS = 15;

    private static final int HALF_MASK = (1 << HALF_BITS) - 1;

    private static final int DOMAIN_BITS = HALF_BITS * 2;

    private static final long DOMAIN = 1L << DOMAIN_BITS;

    private static final int ROUNDS = 4;

    private static final int ID_LENGTH = 6;

    /**
     * Префікс для ключів, що не вміщаються в домен мережі Фейстеля. Такі
     * значення кодуються напряму: довше, зате без втрати оборотності.
     * <p>
     * Символ обраний <b>поза</b> {@link #ALPHABET}: інакше звичайний
     * ідентифікатор, який випадково почався б із нього, читався б як «широка»
     * форма і давав інший числовий ключ.
     */
    private static final char WIDE_PREFIX = '_';

    private static final int MAX_SLUG_LENGTH = 48;

    static {
        for (int i = 0; i < DIGITS.length; i++)
            DIGITS[i] = -1;
        for (int i = 0; i < ALPHABET.length; i++)
            DIGITS[ALPHABET[i]] = i;
        if (DIGITS[WIDE_PREFIX] >= 0)
            throw new IllegalStateException(
                    "Префікс широкої форми не має входити в алфавіт");
    }

    private StableIds() {
    }

    /**
     * Ідентифікатор із числового ключа.
     *
     * @param kind        вид сутності ({@code element}, {@code qualifier}…);
     *                    входить у перемішування, тож елемент і класифікатор з
     *                    однаковим номером мають різні id
     * @param numericId   невід'ємний ключ БД
     */
    public static String of(String kind, long numericId) {
        if (numericId < 0)
            throw new IllegalArgumentException(
                    "Очікувався невід'ємний ключ, а не " + numericId);
        if (numericId >= DOMAIN)
            return WIDE_PREFIX + Long.toString(numericId, ALPHABET.length);
        return encode(encrypt((int) numericId, key(kind)));
    }

    /**
     * Відновлює числовий ключ з ідентифікатора.
     *
     * @throws IllegalArgumentException якщо рядок не є ідентифікатором цього
     *                                  виду сутності
     */
    public static long toNumericId(String kind, String id) {
        if (id == null || id.isEmpty())
            throw new IllegalArgumentException("Порожній ідентифікатор");
        if (id.charAt(0) == WIDE_PREFIX)
            return Long.parseLong(id.substring(1), ALPHABET.length);
        return decrypt(decode(id), key(kind));
    }

    /**
     * Пряме перетворення: {@code (L, R) -> (R, L ^ F(R, k))} на кожному раунді.
     */
    private static int encrypt(int value, int key) {
        int left = (value >>> HALF_BITS) & HALF_MASK;
        int right = value & HALF_MASK;
        for (int i = 0; i < ROUNDS; i++) {
            int next = left ^ round(right, key, i);
            left = right;
            right = next;
        }
        return join(left, right);
    }

    /**
     * Зворотне перетворення. Записане окремим циклом, а не «тим самим кодом із
     * реверсом ключів»: така економія потребує додаткової перестановки половин
     * і легко дає помилку, яку видно лише на конкретних значеннях.
     */
    private static int decrypt(int value, int key) {
        int left = (value >>> HALF_BITS) & HALF_MASK;
        int right = value & HALF_MASK;
        for (int i = ROUNDS - 1; i >= 0; i--) {
            int previous = right ^ round(left, key, i);
            right = left;
            left = previous;
        }
        return join(left, right);
    }

    private static int join(int left, int right) {
        return ((left & HALF_MASK) << HALF_BITS) | (right & HALF_MASK);
    }

    private static int round(int value, int key, int index) {
        int h = value * 0x9e3779b1 + key + index * 0x7f4a7c15;
        h ^= h >>> 15;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        return h & HALF_MASK;
    }

    private static int key(String kind) {
        int hash = 0x811c9dc5;
        for (int i = 0; i < kind.length(); i++) {
            hash ^= kind.charAt(i);
            hash *= 0x01000193;
        }
        return hash;
    }

    private static String encode(int value) {
        char[] chars = new char[ID_LENGTH];
        int rest = value;
        for (int i = ID_LENGTH - 1; i >= 0; i--) {
            chars[i] = ALPHABET[rest & (ALPHABET.length - 1)];
            rest >>>= 5;
        }
        return new String(chars);
    }

    private static int decode(String id) {
        if (id.length() != ID_LENGTH)
            throw new IllegalArgumentException(
                    "Очікувалось " + ID_LENGTH + " символів, а не «" + id + "»");
        int value = 0;
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            int digit = c < DIGITS.length ? DIGITS[c] : -1;
            if (digit < 0)
                throw new IllegalArgumentException(
                        "Недопустимий символ «" + c + "» в ідентифікаторі «"
                                + id + "»");
            value = (value << 5) | digit;
        }
        return value;
    }

    /**
     * Читабельна частина імені файлу. Порожня назва дає {@code "unnamed"},
     * щоб ім'я ніколи не починалося з роздільника.
     */
    public static String slug(String name) {
        if (name == null)
            return "unnamed";
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFC)
                .toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder(normalized.length());
        boolean lastDash = false;
        for (int i = 0; i < normalized.length()
                && sb.length() < MAX_SLUG_LENGTH; i++) {
            char c = normalized.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
                lastDash = false;
            } else if (!lastDash && sb.length() > 0) {
                sb.append('-');
                lastDash = true;
            }
        }
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '-')
            sb.setLength(sb.length() - 1);
        return sb.length() == 0 ? "unnamed" : sb.toString();
    }

    /**
     * Повне ім'я файлу без розширення: {@code <slug>--<id>}.
     */
    public static String fileName(String kind, String name, long numericId) {
        return slug(name) + "--" + of(kind, numericId);
    }
}
