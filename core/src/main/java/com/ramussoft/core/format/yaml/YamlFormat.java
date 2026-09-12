package com.ramussoft.core.format.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.representer.StandardRepresenter;

/**
 * Канонічні налаштування YAML для файлів проєкту.
 * <p>
 * Формат мусить бути детермінованим і дружнім до {@code git diff}, тому тут
 * зібрані всі правила запису в одному місці:
 * <ul>
 * <li>YAML 1.2 (snakeyaml-engine) — {@code no}/{@code yes}/{@code on}/{@code off}
 * лишаються рядками, Norway problem не виникає;</li>
 * <li>рядки завжди в лапках, багаторядкові — літеральним блоком {@code |},
 * щоб один стиль замінив п'ять;</li>
 * <li>рядки ніколи не переносяться: автоперенесення давало б різні файли для
 * тих самих даних;</li>
 * <li>anchors/aliases не емітуються — інакше diff показує посилання замість
 * даних;</li>
 * <li>читання безпечне: жодного інстанціювання довільних Java-класів.</li>
 * </ul>
 */
public final class YamlFormat {

    private YamlFormat() {
    }

    public static Dump dump() {
        DumpSettings settings = DumpSettings.builder()
                .setDefaultFlowStyle(FlowStyle.BLOCK)
                .setIndent(2)
                // Ширина «нескінченна» + вимкнене розбиття рядків: емітер не
                // має права самовільно переносити довгі значення.
                .setWidth(Integer.MAX_VALUE)
                .setSplitLines(false)
                .setMultiLineFlow(false)
                .setDumpComments(false)
                .build();
        return new Dump(settings, new QuotingRepresenter(settings));
    }

    /**
     * Безпечне читання: {@link Load} з snakeyaml-engine не вміє створювати
     * довільні Java-об'єкти, тому шлях {@code !!javax…} тут відсутній як клас
     * проблеми.
     */
    public static Load load() {
        LoadSettings settings = LoadSettings.builder()
                .setAllowDuplicateKeys(false)
                .setAllowRecursiveKeys(false)
                .build();
        return new Load(settings);
    }

    public static void write(Map<String, Object> document, OutputStream out)
            throws IOException {
        Writer writer = new OutputStreamWriter(out, "UTF-8");
        writer.write(dump().dumpToString(deepCopy(document)));
        writer.flush();
    }

    /**
     * Копія дерева, у якій жодне значення не є спільним екземпляром.
     * <p>
     * Емітер перетворює повторно використаний об'єкт на anchor/alias. У файлі
     * це означало б посилання замість даних: git показував би незрозумілий
     * diff, а агент — не бачив справжнього значення. Дешевше скопіювати дерево,
     * ніж покладатися на те, що викликач ніде не переуживає екземпляр.
     */
    @SuppressWarnings("unchecked")
    static Object deepCopy(Object value) {
        if (value instanceof Map) {
            Map<Object, Object> source = (Map<Object, Object>) value;
            Map<Object, Object> copy = new java.util.LinkedHashMap<Object, Object>(
                    source.size());
            for (Map.Entry<Object, Object> entry : source.entrySet())
                copy.put(entry.getKey(), deepCopy(entry.getValue()));
            return copy;
        }
        if (value instanceof java.util.List) {
            java.util.List<Object> source = (java.util.List<Object>) value;
            java.util.List<Object> copy = new java.util.ArrayList<Object>(
                    source.size());
            for (Object item : source)
                copy.add(deepCopy(item));
            return copy;
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> deepCopy(Map<String, Object> document) {
        return (Map<String, Object>) deepCopy((Object) document);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> read(InputStream in) throws IOException {
        Reader reader = new InputStreamReader(in, "UTF-8");
        Object value = load().loadFromReader(reader);
        if (value == null)
            return new java.util.LinkedHashMap<String, Object>();
        if (!(value instanceof Map))
            throw new IOException("Очікувалась мапа на верхньому рівні, а не "
                    + value.getClass().getName());
        return (Map<String, Object>) value;
    }

    /**
     * Рядки виводяться в одинарних лапках, а багаторядкові — літеральним
     * блоком. Числа й булеві лишаються без лапок, інакше при читанні вони
     * перетворилися б на рядки.
     */
    private static final class QuotingRepresenter extends StandardRepresenter {

        QuotingRepresenter(DumpSettings settings) {
            super(settings);
            representers.put(String.class, data -> {
                String value = (String) data;
                ScalarStyle style = value.indexOf('\n') >= 0
                        ? ScalarStyle.LITERAL
                        : ScalarStyle.SINGLE_QUOTED;
                return representScalar(Tag.STR, value, style);
            });
        }

        /**
         * Ключі пишуться без лапок, значення — у лапках. Ключі в цьому форматі
         * походять зі схеми, а не з даних користувача, тож вони безпечні;
         * лапки на кожному ключі лише зашумили б файл. Ключ, який у plain-стилі
         * прочитався б як число чи булеве, усе одно береться в лапки.
         */
        @Override
        protected NodeTuple representMappingEntry(Map.Entry<?, ?> entry) {
            Object key = entry.getKey();
            if (key instanceof String && isSafePlainKey((String) key))
                return new NodeTuple(representScalar(Tag.STR, (String) key,
                        ScalarStyle.PLAIN), representData(entry.getValue()));
            return super.representMappingEntry(entry);
        }

        private static boolean isSafePlainKey(String key) {
            if (key.isEmpty())
                return false;
            char first = key.charAt(0);
            if (!Character.isLetter(first) && first != '_')
                return false;
            for (int i = 0; i < key.length(); i++) {
                char c = key.charAt(i);
                if (!Character.isLetterOrDigit(c) && c != '_' && c != '-'
                        && c != '.')
                    return false;
            }
            return true;
        }
    }
}
