package com.ramussoft.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.ramussoft.core.format.StableIds;

/**
 * Властивості стабільних ідентифікаторів, на яких тримається формат.
 */
public class StableIdsTest {

    /**
     * Без оборотності імпорт роздав би нові числові ключі, і повторний експорт
     * дав би інші id — формат перестав би бути стабільним між циклами.
     */
    @Test
    public void idIsReversible() {
        for (long id = 0; id < 5000; id++) {
            assertEquals(id,
                    StableIds.toNumericId("element", StableIds.of("element", id)));
            assertEquals(id,
                    StableIds.toNumericId("qualifier",
                            StableIds.of("qualifier", id)));
        }
    }

    @Test
    public void largeIdsAreReversibleToo() {
        long[] ids = {1L << 29, (1L << 30) - 1, 1L << 30, 1L << 40,
                Long.MAX_VALUE / 2};
        for (long id : ids)
            assertEquals(id,
                    StableIds.toNumericId("element", StableIds.of("element", id)));
    }

    @Test
    public void idsAreUniqueWithinKind() {
        Set<String> seen = new HashSet<String>();
        for (long id = 0; id < 20000; id++)
            assertTrue("повтор ідентифікатора для " + id,
                    seen.add(StableIds.of("element", id)));
    }

    /**
     * Елемент і класифікатор з однаковим номером не мають ділити ідентифікатор:
     * інакше посилання між файлами стали б неоднозначними.
     */
    @Test
    public void kindsDoNotCollide() {
        int collisions = 0;
        for (long id = 0; id < 1000; id++)
            if (StableIds.of("element", id).equals(
                    StableIds.of("qualifier", id)))
                collisions++;
        assertEquals(0, collisions);
    }

    /**
     * Сусідні ключі мають давати несхожі рядки — у файлі вони стоять поруч.
     */
    @Test
    public void neighbouringIdsLookDifferent() {
        String a = StableIds.of("element", 767);
        String b = StableIds.of("element", 768);
        int same = 0;
        for (int i = 0; i < a.length(); i++)
            if (a.charAt(i) == b.charAt(i))
                same++;
        assertTrue("надто схожі: " + a + " і " + b, same <= 2);
    }

    @Test
    public void idIsDeterministic() {
        assertEquals(StableIds.of("element", 42), StableIds.of("element", 42));
    }

    @Test
    public void slugIsReadableAndSafe() {
        assertEquals("enterprise-activity",
                StableIds.slug("Enterprise activity"));
        assertEquals("виготовлення-продукції",
                StableIds.slug("Виготовлення продукції"));
        assertEquals("unnamed", StableIds.slug(""));
        assertEquals("unnamed", StableIds.slug(null));
        assertFalse(StableIds.slug("a/b\\c:d").contains("/"));
    }

    @Test
    public void fileNameJoinsSlugAndId() {
        String name = StableIds.fileName("qualifier", "Enterprise activity", 9);
        assertTrue(name, name.startsWith("enterprise-activity--"));
        assertEquals(9, StableIds.toNumericId("qualifier",
                name.substring(name.indexOf("--") + 2)));
    }
}
