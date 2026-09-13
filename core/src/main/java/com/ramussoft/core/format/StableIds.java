package com.ramussoft.core.format;

import java.text.Normalizer;
import java.util.Locale;

public final class StableIds {

    private static final char[] ALPHABET = "0123456789abcdefghjkmnpqrstvwxyz"
            .toCharArray();

    private static final int[] DIGITS = new int[128];

    private static final int HALF_BITS = 15;

    private static final int HALF_MASK = (1 << HALF_BITS) - 1;

    private static final int DOMAIN_BITS = HALF_BITS * 2;

    private static final long DOMAIN = 1L << DOMAIN_BITS;

    private static final int ROUNDS = 4;

    private static final int ID_LENGTH = 6;

    private static final char WIDE_PREFIX = '_';

    private static final int MAX_SLUG_LENGTH = 48;

    static {
        for (int i = 0; i < DIGITS.length; i++)
            DIGITS[i] = -1;
        for (int i = 0; i < ALPHABET.length; i++)
            DIGITS[ALPHABET[i]] = i;
        if (DIGITS[WIDE_PREFIX] >= 0)
            throw new IllegalStateException(
                    "The wide form prefix must stay out of the alphabet");
    }

    private StableIds() {
    }

    public static String of(String kind, long numericId) {
        if (numericId < 0)
            throw new IllegalArgumentException(
                    "Expected a non-negative key, not " + numericId);
        if (numericId >= DOMAIN)
            return WIDE_PREFIX + Long.toString(numericId, ALPHABET.length);
        return encode(encrypt((int) numericId, key(kind)));
    }

    public static long toNumericId(String kind, String id) {
        if (id == null || id.isEmpty())
            throw new IllegalArgumentException("Empty identifier");
        if (id.charAt(0) == WIDE_PREFIX)
            return Long.parseLong(id.substring(1), ALPHABET.length);
        return decrypt(decode(id), key(kind));
    }

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
                    "Expected " + ID_LENGTH + " characters, not \"" + id + "\"");
        int value = 0;
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            int digit = c < DIGITS.length ? DIGITS[c] : -1;
            if (digit < 0)
                throw new IllegalArgumentException(
                        "Illegal character \"" + c + "\" in identifier \""
                                + id + "\"");
            value = (value << 5) | digit;
        }
        return value;
    }

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

    public static String fileName(String kind, String name, long numericId) {
        return slug(name) + "--" + of(kind, numericId);
    }
}
