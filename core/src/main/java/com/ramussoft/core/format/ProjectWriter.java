package com.ramussoft.core.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.core.format.yaml.YamlFormat;

public class ProjectWriter {

    public static final int SCHEMA_VERSION = 4;

    public static final String PROJECT_FILE = "project.ramus";

    static final String ATTRIBUTES_FILE = "attributes.yaml";

    static final String STREAMS_FILE = "streams.yaml";

    static final String QUALIFIERS_DIR = "qualifiers";

    static final String PROPERTIES_DIR = "properties";

    static final String ATTACHMENTS_DIR = "attachments";

    static final String OTHER_DIR = "streams";

    static final String LOCAL_DIR = ".local";

    static final String PROPERTIES_PREFIX = "/properties/";

    static final String ELEMENTS_PREFIX = "/elements/";

    static final String USER_PREFIX = "/user/";

    private final IEngine engine;

    private final PersistentCodec codec;

    private final Map<Long, String> attributeRefs = new HashMap<Long, String>();

    private final Set<String> written = new HashSet<String>();

    private final List<String> sequences;

    private final List<String> requiredPlugins;

    private final String applicationName;

    private final String applicationVersion;

    private final String minimumVersion;

    public ProjectWriter(IEngine engine, List<String> sequences,
                         List<String> requiredPlugins, String applicationName,
                         String applicationVersion, String minimumVersion) {
        this.engine = engine;
        this.codec = new PersistentCodec(engine);
        this.sequences = sequences;
        this.requiredPlugins = requiredPlugins;
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        this.minimumVersion = minimumVersion;
    }

    public void write(File directory) throws IOException {
        mkdirs(directory);
        buildAttributeRefs();

        writeDocument(directory, PROJECT_FILE, project());
        writeDocument(directory, ATTRIBUTES_FILE, attributes());
        writeStreams(directory);

        for (Qualifier qualifier : sortedQualifiers()) {
            String name = QUALIFIERS_DIR + "/"
                    + StableIds.fileName(PersistentCodec.QUALIFIER,
                    qualifier.getName(), qualifier.getId()) + ".yaml";
            writeDocument(directory, name, qualifier(qualifier));
        }

        writeGitignore(directory);
        removeStaleFiles(directory);
    }

    private void buildAttributeRefs() {
        Map<String, List<Attribute>> byName =
                new HashMap<String, List<Attribute>>();
        for (Attribute attribute : allAttributes()) {
            String name = attribute.getName();
            List<Attribute> list = byName.get(name);
            if (list == null) {
                list = new ArrayList<Attribute>(1);
                byName.put(name, list);
            }
            list.add(attribute);
        }
        for (Map.Entry<String, List<Attribute>> entry : byName.entrySet()) {
            String name = entry.getKey();
            List<Attribute> list = entry.getValue();
            boolean unique = list.size() == 1 && name != null
                    && name.length() > 0;
            for (Attribute attribute : list) {
                String id = StableIds.of(PersistentCodec.ATTRIBUTE,
                        attribute.getId());
                attributeRefs.put(Long.valueOf(attribute.getId()),
                        unique ? name : name + "#" + id);
            }
        }
    }

    private List<Attribute> allAttributes() {
        List<Attribute> all = new ArrayList<Attribute>(engine.getAttributes());
        all.addAll(engine.getSystemAttributes());
        Collections.sort(all, new Comparator<Attribute>() {
            @Override
            public int compare(Attribute a, Attribute b) {
                return Long.compare(a.getId(), b.getId());
            }
        });
        return all;
    }

    String attributeRef(Attribute attribute) {
        String ref = attributeRefs.get(Long.valueOf(attribute.getId()));
        if (ref != null)
            return ref;
        return StableIds.of(PersistentCodec.ATTRIBUTE, attribute.getId());
    }

    private Map<String, Object> project() {
        Map<String, Object> document = new LinkedHashMap<String, Object>();
        document.put("schema", Integer.valueOf(SCHEMA_VERSION));
        document.put("application", applicationName);
        document.put("application-version", applicationVersion);
        if (minimumVersion != null)
            document.put("minimum-version", minimumVersion);

        if (!requiredPlugins.isEmpty()) {
            List<Object> plugins = new ArrayList<Object>(requiredPlugins);
            Collections.sort((List) plugins);
            document.put("plugins", plugins);
        }

        Map<String, Object> values = new TreeMap<String, Object>();
        for (String sequence : sequences)
            values.put(sequence, Long.valueOf(engine.nextValue(sequence)));
        if (!values.isEmpty())
            document.put("sequences", values);
        return document;
    }

    private Map<String, Object> attributes() {
        List<Object> rows = new ArrayList<Object>();
        for (Attribute attribute : allAttributes()) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("ref", attributeRef(attribute));
            row.put("id", StableIds.of(PersistentCodec.ATTRIBUTE,
                    attribute.getId()));
            row.put("name", attribute.getName());
            row.put("type", attribute.getAttributeType().toString());
            if (attribute.getAttributeType().isComparable())
                row.put("comparable", Boolean.TRUE);
            if (attribute.isSystem())
                row.put("system", Boolean.TRUE);
            Object properties = value(-1L, attribute.getId());
            if (properties != null)
                row.put("properties", properties);
            rows.add(row);
        }

        Map<String, Object> document = new LinkedHashMap<String, Object>();
        document.put("schema", Integer.valueOf(SCHEMA_VERSION));
        document.put("attributes", rows);
        return document;
    }

    private Map<String, Object> qualifier(Qualifier qualifier) {
        Map<String, Object> document = new LinkedHashMap<String, Object>();
        document.put("schema", Integer.valueOf(SCHEMA_VERSION));
        document.put("id", StableIds.of(PersistentCodec.QUALIFIER,
                qualifier.getId()));
        document.put("name", qualifier.getName());
        if (qualifier.isSystem())
            document.put("system", Boolean.TRUE);

        if (qualifier.getAttributeForName() >= 0) {
            Attribute forName = engine.getAttribute(
                    qualifier.getAttributeForName());
            if (forName != null)
                document.put("name-attribute", attributeRef(forName));
        }

        document.put("attributes", refs(qualifier.getAttributes()));

        List<Object> system = refs(qualifier.getSystemAttributes());
        Collections.sort((List) system);
        if (!system.isEmpty())
            document.put("system-attributes", system);

        document.put("elements", elements(qualifier));
        return document;
    }

    private List<Object> refs(List<Attribute> attributes) {
        List<Object> result = new ArrayList<Object>();
        if (attributes != null)
            for (Attribute attribute : attributes)
                result.add(attributeRef(attribute));
        return result;
    }

    private List<Object> elements(Qualifier qualifier) {
        List<Element> all = new ArrayList<Element>(
                engine.getElements(qualifier.getId()));
        Collections.sort(all, new Comparator<Element>() {
            @Override
            public int compare(Element a, Element b) {
                return Long.compare(a.getId(), b.getId());
            }
        });

        List<Attribute> attributes = allAttributes(qualifier);

        List<Object> rows = new ArrayList<Object>(all.size());
        for (Element element : all) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("id", StableIds.of(PersistentCodec.ELEMENT,
                    element.getId()));
            if (element.getName() != null && element.getName().length() > 0)
                row.put("name", element.getName());
            Map<String, Object> values = values(element, attributes);
            if (!values.isEmpty())
                row.put("values", values);
            rows.add(row);
        }
        return rows;
    }

    private Map<String, Object> values(Element element,
                                       List<Attribute> attributes) {
        Map<String, Object> values = new TreeMap<String, Object>();
        for (Attribute attribute : attributes) {
            Object value = value(element.getId(), attribute.getId());
            if (value != null)
                values.put(attributeRef(attribute), value);
        }
        return values;
    }

    private List<Attribute> allAttributes(Qualifier qualifier) {
        List<Attribute> all = new ArrayList<Attribute>(
                qualifier.getAttributes());
        if (qualifier.getSystemAttributes() != null)
            all.addAll(qualifier.getSystemAttributes());
        return all;
    }

    private Object value(long elementId, long attributeId) {
        List<Persistent>[] lists;
        try {
            lists = engine.getBinaryAttribute(elementId, attributeId);
        } catch (RuntimeException e) {
            System.err.println("Can not read attribute " + attributeId
                    + " of element " + elementId + ": " + e);
            return null;
        }
        if (lists == null || lists.length == 0)
            return null;

        List<Object> tables = new ArrayList<Object>(lists.length);
        boolean empty = true;
        for (List<Persistent> list : lists) {
            List<Object> rows = new ArrayList<Object>();
            if (list != null)
                for (Persistent persistent : list) {
                    rows.add(codec.toValue(persistent));
                    empty = false;
                }
            tables.add(rows);
        }
        if (empty)
            return null;

        if (tables.size() == 1) {
            List<?> rows = (List<?>) tables.get(0);
            if (rows.size() == 1)
                return rows.get(0);
            return rows;
        }
        return tables;
    }

    private List<Qualifier> sortedQualifiers() {
        List<Qualifier> all = new ArrayList<Qualifier>(engine.getQualifiers());
        all.addAll(engine.getSystemQualifiers());
        Collections.sort(all, new Comparator<Qualifier>() {
            @Override
            public int compare(Qualifier a, Qualifier b) {
                return Long.compare(a.getId(), b.getId());
            }
        });
        return all;
    }

    private void writeStreams(File directory) throws IOException {
        List<Object> properties = new ArrayList<Object>();
        List<Object> attachments = new ArrayList<Object>();
        List<Object> other = new ArrayList<Object>();
        List<Object> local = new ArrayList<Object>();

        String[] names = engine.getStreamNames();
        Arrays.sort(names);

        for (String name : names) {
            byte[] data = engine.getStream(name);
            if (data == null)
                continue;

            if (name.startsWith(USER_PREFIX)) {
                String file = LOCAL_DIR + "/user/"
                        + safePath(name.substring(USER_PREFIX.length()));
                Map<String, Object> row = new LinkedHashMap<String, Object>();
                row.put("path", name);
                row.put("file", file);
                local.add(row);
                writeBytes(directory, file, data);
            } else if (name.startsWith(PROPERTIES_PREFIX)) {
                String relative = name.substring(PROPERTIES_PREFIX.length());
                String file = PROPERTIES_DIR + "/" + safePath(relative);
                Map<String, Object> row = new LinkedHashMap<String, Object>();
                row.put("path", relative);
                row.put("file", file);
                properties.add(row);
                writeBytes(directory, file, data);
            } else {
                Map<String, Object> row = attachmentRow(name);
                if (row != null) {
                    String file = ATTACHMENTS_DIR + "/" + row.get("element")
                            + "/" + safeSegment(row.get("attribute").toString())
                            + "/" + safeSegment(row.get("name").toString());
                    row.put("file", file);
                    attachments.add(row);
                    writeBytes(directory, file, data);
                } else {
                    String file = OTHER_DIR + "/" + safePath(name);
                    Map<String, Object> raw =
                            new LinkedHashMap<String, Object>();
                    raw.put("path", name);
                    raw.put("file", file);
                    other.add(raw);
                    writeBytes(directory, file, data);
                }
            }
        }

        Map<String, Object> document = new LinkedHashMap<String, Object>();
        document.put("schema", Integer.valueOf(SCHEMA_VERSION));
        if (!properties.isEmpty())
            document.put("properties", properties);
        if (!attachments.isEmpty())
            document.put("attachments", attachments);
        if (!other.isEmpty())
            document.put("other", other);
        writeDocument(directory, STREAMS_FILE, document);

        Map<String, Object> localDocument = new LinkedHashMap<String, Object>();
        localDocument.put("schema", Integer.valueOf(SCHEMA_VERSION));
        localDocument.put("streams", local);
        writeDocument(directory, LOCAL_DIR + "/" + STREAMS_FILE,
                localDocument);
    }

    private Map<String, Object> attachmentRow(String name) {
        if (!name.startsWith(ELEMENTS_PREFIX))
            return null;
        String rest = name.substring(ELEMENTS_PREFIX.length());
        int firstSlash = rest.indexOf('/');
        if (firstSlash < 0)
            return null;
        int secondSlash = rest.indexOf('/', firstSlash + 1);
        if (secondSlash < 0)
            return null;
        long elementId;
        long attributeId;
        try {
            elementId = Long.parseLong(rest.substring(0, firstSlash));
            attributeId = Long.parseLong(rest.substring(firstSlash + 1,
                    secondSlash));
        } catch (NumberFormatException e) {
            return null;
        }
        Attribute attribute = engine.getAttribute(attributeId);
        if (attribute == null || engine.getElement(elementId) == null)
            return null;

        Map<String, Object> row = new LinkedHashMap<String, Object>();
        row.put("element", StableIds.of(PersistentCodec.ELEMENT, elementId));
        row.put("attribute", attributeRef(attribute));
        row.put("name", rest.substring(secondSlash + 1));
        return row;
    }

    private void writeGitignore(File directory) throws IOException {
        File file = new File(directory, ".gitignore");
        String rule = LOCAL_DIR + "/";
        if (!file.isFile()) {
            writeBytes(directory, ".gitignore", (rule + "\n")
                    .getBytes("UTF-8"));
            return;
        }
        String text = new String(read(file), "UTF-8");
        for (String line : text.split("\n"))
            if (rule.equals(line.trim()) || LOCAL_DIR.equals(line.trim()))
                return;
        String separator = text.length() == 0 || text.endsWith("\n") ? "" : "\n";
        writeBytes(directory, ".gitignore",
                (text + separator + rule + "\n").getBytes("UTF-8"));
    }

    private static byte[] read(File file) throws IOException {
        java.io.InputStream in = new java.io.FileInputStream(file);
        try {
            java.io.ByteArrayOutputStream out =
                    new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = in.read(buffer)) > 0)
                out.write(buffer, 0, count);
            return out.toByteArray();
        } finally {
            in.close();
        }
    }

    private void removeStaleFiles(File directory) {
        String[] managed = {QUALIFIERS_DIR, PROPERTIES_DIR, ATTACHMENTS_DIR,
                OTHER_DIR, LOCAL_DIR};
        for (String dir : managed)
            removeStaleFiles(directory, new File(directory, dir), dir);
    }

    private void removeStaleFiles(File root, File file, String path) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children == null)
                return;
            Arrays.sort(children);
            for (File child : children)
                removeStaleFiles(root, child, path + "/" + child.getName());
            file.delete();
        } else if (file.isFile() && !written.contains(path)) {
            file.delete();
        }
    }

    static String safeSegment(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            sb.append(Character.isLetterOrDigit(c) || c == '.' || c == '-'
                    || c == '_' ? c : '-');
        }
        return sb.length() == 0 ? "unnamed" : sb.toString();
    }

    static String safePath(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        for (String segment : value.split("/")) {
            if (segment.length() == 0 || ".".equals(segment)
                    || "..".equals(segment))
                continue;
            if (sb.length() > 0)
                sb.append('/');
            sb.append(safeSegment(segment));
        }
        return sb.length() == 0 ? "unnamed" : sb.toString();
    }

    private void writeDocument(File directory, String path,
                               Map<String, Object> document)
            throws IOException {
        File file = new File(directory, path);
        mkdirs(file.getParentFile());
        OutputStream out = new FileOutputStream(file);
        try {
            YamlFormat.write(document, out);
        } finally {
            out.close();
        }
        written.add(path);
    }

    private void writeBytes(File directory, String path, byte[] data)
            throws IOException {
        File file = new File(directory, path);
        mkdirs(file.getParentFile());
        OutputStream out = new FileOutputStream(file);
        try {
            out.write(data);
        } finally {
            out.close();
        }
        written.add(path);
    }

    private static void mkdirs(File directory) throws IOException {
        if (directory != null && !directory.isDirectory()
                && !directory.mkdirs())
            throw new IOException("Can not create directory " + directory);
    }
}
