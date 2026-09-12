package com.ramussoft.core.format;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.attribute.AttributePlugin;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.common.persistent.Transaction;
import com.ramussoft.core.format.yaml.YamlFormat;
import com.ramussoft.core.impl.IEngineImpl;

/**
 * Читає проєкт із дерева YAML-файлів у порожній рушій.
 * <p>
 * Симетричний до {@link ProjectWriter} і працює на тому самому рівні —
 * {@link IEngineImpl}, тобто нижче плагінів. Це принципово: плагіни реагують на
 * зміни моделі власною добудовою (створюють базові функції, доповнюють
 * ієрархію), і читання через прикладний API давало б то дублікати, то
 * пропуски. Тут же сутності відновлюються з тими самими числовими ключами, що
 * й були, а плагіни вмикаються вже над готовою моделлю — точно як при
 * відкритті {@code .rsf}.
 */
public class ProjectReader {

    private final IEngineImpl engine;

    private final PersistentCodec codec;

    private final PluginFactory factory;

    /**
     * Посилання з файлу → атрибут. Заповнюється з {@code attributes.yaml} за
     * тим самим правилом, за яким записувач будував посилання, тому розбирати
     * рядок не доводиться.
     */
    private final Map<String, Attribute> attributes =
            new HashMap<String, Attribute>();

    /**
     * Властивості атрибутів, відкладені до створення класифікаторів: вони на
     * них посилаються.
     */
    private final Map<String, Object> deferredProperties =
            new LinkedHashMap<String, Object>();

    /**
     * Уже записані зв'язки {@code Core.ElementList}.
     * <p>
     * Такий зв'язок належить двом елементам одночасно, тому у файлі він
     * трапляється двічі — у значеннях обох кінців. Запис іде прямими вставками,
     * без звіряння з наявним, тож повтор дав би дубльований рядок у таблиці.
     */
    private final Set<String> links = new HashSet<String>();

    public ProjectReader(IEngineImpl engine, PluginFactory factory) {
        this.engine = engine;
        this.factory = factory;
        this.codec = new PersistentCodec(engine);
    }

    /**
     * @return вміст опису проєкту
     */
    public static Map<String, Object> readProject(File directory)
            throws IOException {
        return readDocument(new File(directoryOf(directory),
                ProjectWriter.PROJECT_FILE));
    }

    /**
     * @return {@code true}, якщо шлях указує на проєкт нового формату — сам
     * каталог або його опис
     */
    public static boolean isProject(File file) {
        File directory = directoryOf(file);
        return directory != null && directory.isDirectory()
                && new File(directory, ProjectWriter.PROJECT_FILE).isFile();
    }

    /**
     * Каталог проєкту.
     * <p>
     * Приймає і сам каталог, і його опис: із робочого столу приходить файл,
     * із діалогу вибору — каталог, і жодна зі сторін не має про це знати.
     */
    public static File directoryOf(File file) {
        if (file == null)
            return null;
        if (ProjectWriter.PROJECT_FILE.equals(file.getName()))
            return file.getParentFile();
        return file;
    }

    public void read(File directory) throws IOException {
        Map<String, Object> project = readProject(directory);
        checkSchema(project);
        readSequences(project);

        readAttributes(directory);

        // Два проходи. Значення атрибутів посилаються на елементи інших
        // класифікаторів, тому спершу створюємо всі сутності, і лише потім
        // записуємо значення — інакше посилання вперед указувало б у порожнечу.
        List<DeferredValues> deferred = new ArrayList<DeferredValues>();
        File qualifiers = new File(directory, ProjectWriter.QUALIFIERS_DIR);
        File[] files = qualifiers.listFiles();
        if (files != null) {
            // Порядок читання на результат не впливає, але сортуємо, щоб
            // повідомлення про помилки були відтворюваними.
            Arrays.sort(files);
            for (File file : files)
                if (file.getName().endsWith(".yaml"))
                    deferred.add(readQualifier(file));
        }

        // Властивості атрибутів посилаються на класифікатори, а значення
        // елементів вимагають уже налаштованих властивостей (Core.ElementList
        // без них не знає, до яких класифікаторів належать кінці зв'язку).
        // Звідси порядок: сутності → властивості → значення.
        for (Map.Entry<String, Object> entry : deferredProperties.entrySet())
            applyProperty(entry.getKey(), entry.getValue());

        for (DeferredValues values : deferred)
            values.apply();

        readStreams(directory, ProjectWriter.STREAMS_FILE);
        readStreams(directory, ProjectWriter.LOCAL_DIR + "/"
                + ProjectWriter.STREAMS_FILE);
    }

    private void checkSchema(Map<String, Object> project) throws IOException {
        Object schema = project.get("schema");
        if (!(schema instanceof Number))
            throw new IOException("У " + ProjectWriter.PROJECT_FILE
                    + " немає поля schema");
        int version = ((Number) schema).intValue();
        if (version != ProjectWriter.SCHEMA_VERSION)
            throw new IOException("Версія формату " + version
                    + " не підтримується, очікувалась "
                    + ProjectWriter.SCHEMA_VERSION);
    }

    /**
     * Відновлює лічильники плагінів. Без цього наступний створений об'єкт
     * отримав би ключ, уже зайнятий у моделі.
     */
    @SuppressWarnings("unchecked")
    private void readSequences(Map<String, Object> project) {
        Object sequences = project.get("sequences");
        if (!(sequences instanceof Map))
            return;
        for (Map.Entry<String, Object> entry
                : ((Map<String, Object>) sequences).entrySet())
            if (entry.getValue() instanceof Number)
                engine.setSequenceValue(entry.getKey(),
                        ((Number) entry.getValue()).longValue());
    }

    @SuppressWarnings("unchecked")
    private void readAttributes(File directory) throws IOException {
        Map<String, Object> document = readDocument(new File(directory,
                ProjectWriter.ATTRIBUTES_FILE));
        List<Object> rows = (List<Object>) document.get("attributes");
        if (rows == null)
            return;

        for (Object row : rows) {
            Map<String, Object> map = (Map<String, Object>) row;
            String ref = text(map, "ref");
            long id = StableIds.toNumericId(PersistentCodec.ATTRIBUTE,
                    text(map, "id"));
            boolean system = Boolean.TRUE.equals(map.get("system"));
            AttributeType type = parseType(text(map, "type"),
                    Boolean.TRUE.equals(map.get("comparable")));

            Attribute attribute = engine.createAttribute(id, type, system);
            attribute.setName(text(map, "name"));
            engine.updateAttribute(attribute);
            attributes.put(ref, attribute);

            Object properties = map.get("properties");
            if (properties != null)
                deferredProperties.put(ref, properties);
        }
    }

    @SuppressWarnings("unchecked")
    private DeferredValues readQualifier(File file) throws IOException {
        Map<String, Object> document = readDocument(file);
        long id = StableIds.toNumericId(PersistentCodec.QUALIFIER,
                text(document, "id"));
        boolean system = Boolean.TRUE.equals(document.get("system"));

        Qualifier qualifier = system ? engine.createSystemQualifier(id)
                : engine.createQualifier(id);
        qualifier.setName(text(document, "name"));

        qualifier.getAttributes().clear();
        qualifier.getAttributes().addAll(
                resolve((List<Object>) document.get("attributes")));

        qualifier.getSystemAttributes().clear();
        qualifier.getSystemAttributes().addAll(
                resolve((List<Object>) document.get("system-attributes")));

        String nameAttribute = text(document, "name-attribute");
        if (nameAttribute != null) {
            Attribute forName = attributes.get(nameAttribute);
            if (forName != null)
                qualifier.setAttributeForName(forName.getId());
        }

        engine.updateQualifier(qualifier);

        return readElements(qualifier,
                (List<Object>) document.get("elements"));
    }

    @SuppressWarnings("unchecked")
    private DeferredValues readElements(Qualifier qualifier,
                                        List<Object> rows) {
        DeferredValues deferred = new DeferredValues();
        if (rows == null)
            return deferred;

        for (Object row : rows) {
            Map<String, Object> map = (Map<String, Object>) row;
            long id = StableIds.toNumericId(PersistentCodec.ELEMENT,
                    text(map, "id"));
            Element element = engine.createElement(qualifier.getId(), id);
            String name = text(map, "name");
            if (name != null && name.length() > 0)
                engine.setElementName(id, name);

            Object values = map.get("values");
            if (values instanceof Map)
                deferred.add(element, (Map<String, Object>) values);
        }
        return deferred;
    }

    /**
     * Значення елементів одного класифікатора, відкладені до другого проходу.
     */
    private final class DeferredValues {

        private final List<Element> elements = new ArrayList<Element>();

        private final List<Map<String, Object>> values =
                new ArrayList<Map<String, Object>>();

        void add(Element element, Map<String, Object> value) {
            elements.add(element);
            values.add(value);
        }

        void apply() {
            for (int i = 0; i < elements.size(); i++) {
                Element element = elements.get(i);
                for (Map.Entry<String, Object> entry : values.get(i).entrySet())
                    applyValue(element, entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Записує конфігурацію плагіна атрибута (значення з {@code elementId = -1}).
     */
    private void applyProperty(String ref, Object value) {
        Attribute attribute = attributes.get(ref);
        if (attribute == null)
            return;
        AttributePlugin plugin = factory.getAttributePlugin(
                attribute.getAttributeType());
        if (plugin == null)
            return;
        apply(attribute, plugin.getAttributePropertyPersistents(), value, -1L);
    }

    private void applyValue(Element element, String ref, Object value) {
        Attribute attribute = attributes.get(ref);
        if (attribute == null) {
            System.err.println("Пропущено значення невідомого атрибута «" + ref
                    + "» елемента " + element.getId());
            return;
        }
        AttributePlugin plugin = factory.getAttributePlugin(
                attribute.getAttributeType());
        if (plugin == null)
            return;
        apply(attribute, plugin.getAttributePersistents(), value,
                element.getId());
    }

    @SuppressWarnings("unchecked")
    private void apply(Attribute attribute,
                       Class<? extends Persistent>[] classes, Object value,
                       long elementId) {
        if (classes.length == 0)
            return;

        boolean elementList = elementId >= 0
                && "Core.ElementList".equals(
                attribute.getAttributeType().toString());

        List<Object> tables = asTables(value, classes.length);
        for (int i = 0; i < classes.length; i++) {
            if (i >= tables.size())
                continue;
            Transaction transaction = new Transaction();
            for (Object row : (List<Object>) tables.get(i)) {
                Persistent persistent;
                try {
                    persistent = codec.fromValue(classes[i], row);
                } catch (RuntimeException e) {
                    throw new IllegalStateException("Атрибут «"
                            + attribute.getName() + "» типу "
                            + attribute.getAttributeType() + " (таблиця "
                            + (i + 1) + " з " + classes.length + "): "
                            + e.getMessage(), e);
                }
                persistent.setValueBranchId(0L);
                if (elementList && !isNewLink(attribute, persistent))
                    continue;
                transaction.getSave().add(persistent);
            }
            if (!transaction.getSave().isEmpty())
                engine.setBinaryAttribute(elementId, attribute.getId(),
                        transaction);
        }
    }

    /**
     * @return {@code false}, якщо такий зв'язок уже записано з іншого кінця
     */
    private boolean isNewLink(Attribute attribute, Persistent persistent) {
        Object first = codec.wrapper(persistent.getClass())
                .getField(persistent, "element1Id");
        Object second = codec.wrapper(persistent.getClass())
                .getField(persistent, "element2Id");
        return links.add(attribute.getId() + ":" + first + ":" + second);
    }

    /**
     * Розгортає скорочення, які застосував {@link ProjectWriter}: одну таблицю
     * з одним рядком він пише як саме значення, а не як список списків.
     */
    @SuppressWarnings("unchecked")
    private static List<Object> asTables(Object value, int tableCount) {
        List<Object> tables = new ArrayList<Object>(tableCount);
        if (tableCount == 1) {
            if (value instanceof List)
                tables.add(value);
            else {
                List<Object> rows = new ArrayList<Object>(1);
                rows.add(value);
                tables.add(rows);
            }
            return tables;
        }
        if (value instanceof List)
            for (Object table : (List<Object>) value)
                tables.add(table instanceof List ? table
                        : java.util.Collections.singletonList(table));
        return tables;
    }

    private List<Attribute> resolve(List<Object> refs) {
        List<Attribute> result = new ArrayList<Attribute>();
        if (refs == null)
            return result;
        for (Object ref : refs) {
            Attribute attribute = attributes.get(ref.toString());
            if (attribute != null)
                result.add(attribute);
            else
                System.err.println("Не знайдено атрибута «" + ref
                        + "», згаданого класифікатором");
        }
        return result;
    }

    /**
     * Відновлює потоки за їхнім описом.
     * <p>
     * Опис зберігає початковий шлях, тому вкладення знаходять своє місце
     * незалежно від того, як їх назвали у файловій системі.
     */
    @SuppressWarnings("unchecked")
    private void readStreams(File directory, String manifestPath)
            throws IOException {
        File manifest = new File(directory, manifestPath);
        if (!manifest.isFile())
            return;
        Map<String, Object> document = readDocument(manifest);

        for (Object row : list(document, "properties")) {
            Map<String, Object> map = (Map<String, Object>) row;
            byte[] data = readBytes(new File(directory, text(map, "file")));
            if (data != null)
                engine.setStream(ProjectWriter.PROPERTIES_PREFIX
                        + text(map, "path"), data);
        }

        for (Object row : list(document, "attachments")) {
            Map<String, Object> map = (Map<String, Object>) row;
            Attribute attribute = attributes.get(text(map, "attribute"));
            if (attribute == null)
                continue;
            long elementId = StableIds.toNumericId(PersistentCodec.ELEMENT,
                    text(map, "element"));
            byte[] data = readBytes(new File(directory, text(map, "file")));
            if (data != null)
                engine.setStream(ProjectWriter.ELEMENTS_PREFIX + elementId + "/"
                        + attribute.getId() + "/" + text(map, "name"), data);
        }

        for (String key : new String[]{"other", "streams"})
            for (Object row : list(document, key)) {
                Map<String, Object> map = (Map<String, Object>) row;
                byte[] data = readBytes(new File(directory, text(map, "file")));
                if (data != null)
                    engine.setStream(text(map, "path"), data);
            }
    }

    @SuppressWarnings("unchecked")
    private static List<Object> list(Map<String, Object> document, String key) {
        Object value = document.get(key);
        return value instanceof List ? (List<Object>) value
                : java.util.Collections.emptyList();
    }

    private static byte[] readBytes(File file) throws IOException {
        if (file == null || !file.isFile())
            return null;
        InputStream in = new FileInputStream(file);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = in.read(buffer)) > 0)
                out.write(buffer, 0, count);
            return out.toByteArray();
        } finally {
            in.close();
        }
    }

    private static AttributeType parseType(String value, boolean comparable)
            throws IOException {
        int dot = value == null ? -1 : value.indexOf('.');
        if (dot <= 0)
            throw new IOException("Очікувався тип у вигляді «Плагін.Тип»,"
                    + " а не «" + value + "»");
        return new AttributeType(value.substring(0, dot),
                value.substring(dot + 1), comparable);
    }

    private static String text(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }

    private static Map<String, Object> readDocument(File file)
            throws IOException {
        if (!file.isFile())
            throw new IOException("Не знайдено " + file);
        InputStream in = new FileInputStream(file);
        try {
            return YamlFormat.read(in);
        } finally {
            in.close();
        }
    }
}
