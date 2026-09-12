package com.ramussoft.storage;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.ramussoft.common.Engine;
import com.ramussoft.common.PluginFactory;
import com.ramussoft.common.attribute.AttributePlugin;
import com.ramussoft.common.persistent.Persistent;
import com.ramussoft.core.format.PersistentCodec;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;

/**
 * Службовий дамп схеми типів атрибутів — для звірки документації з кодом.
 *
 * <pre>
 * ./gradlew :storage-test:test --tests '*DumpSchemaTest*' -Dramus.schema.target=/tmp/schema.txt
 * </pre>
 */
public class DumpSchemaTest {

    @Test
    public void dumpSchema() throws Exception {
        String target = System.getProperty("ramus.schema.target");
        if (target == null)
            return;

        RsfFixture.isolateHome(new File(target + "-home"));

        MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase();
        PrintWriter out = new PrintWriter(target, "UTF-8");
        try {
            Engine engine = database.getEngine(null);
            PluginFactory factory = (PluginFactory) engine.getPluginProperty(
                    "Core", "PluginFactory");
            PersistentCodec codec = new PersistentCodec();

            List<String> lines = new ArrayList<String>();
            for (AttributePlugin plugin : factory.getAttributePlugins()) {
                StringBuilder sb = new StringBuilder();
                sb.append(plugin.getName()).append('.')
                        .append(plugin.getTypeName());
                sb.append(plugin.isSystem() ? "  [системний]" : "");
                describe(sb, "значення", plugin.getAttributePersistents(),
                        codec);
                describe(sb, "властивості",
                        plugin.getAttributePropertyPersistents(), codec);
                lines.add(sb.toString());
            }
            Collections.sort(lines);
            for (String line : lines)
                out.println(line);
        } finally {
            out.close();
            database.close();
        }
    }

    private static void describe(StringBuilder sb, String title,
                                 Class<? extends Persistent>[] classes,
                                 PersistentCodec codec) {
        if (classes == null || classes.length == 0)
            return;
        sb.append("\n  ").append(title).append(':');
        for (Class<? extends Persistent> clazz : classes) {
            sb.append("\n    ").append(clazz.getSimpleName());
            String single = codec.singleField(clazz);
            if (single != null)
                sb.append("  (згортається до скаляра: ").append(single)
                        .append(')');
            for (String field : codec.wrapper(clazz).getFields()) {
                if ("elementId".equals(field) || "attributeId".equals(field)
                        || "valueBranchId".equals(field))
                    continue;
                sb.append("\n      ").append(field).append(" : ")
                        .append(type(codec.wrapper(clazz)
                                .getAnnotationType(field)));
            }
        }
    }

    private static String type(int annotation) {
        switch (annotation) {
            case com.ramussoft.common.persistent.PersistentField.ELEMENT:
                return "element";
            case com.ramussoft.common.persistent.PersistentField.ATTRIBUTE:
                return "attribute";
            case com.ramussoft.common.persistent.PersistentField.QUALIFIER:
                return "qualifier";
            case com.ramussoft.common.persistent.PersistentField.TEXT:
                return "text";
            case com.ramussoft.common.persistent.PersistentField.LONG:
                return "long";
            case com.ramussoft.common.persistent.PersistentField.DATE:
                return "date";
            case com.ramussoft.common.persistent.PersistentField.DOUBLE:
                return "double";
            case com.ramussoft.common.persistent.PersistentField.ID:
                return "id";
            case com.ramussoft.common.persistent.PersistentField.BINARY:
                return "binary";
            case com.ramussoft.common.persistent.PersistentField.INTEGER:
                return "integer";
            default:
                return "?" + annotation;
        }
    }
}
