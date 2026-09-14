package com.ramussoft.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;

public class IdlRoundTripTest {

    private static final String ENCODING = "cp1251";

    private static final String SAMPLE = "dest/doc/en/Enterprise activity.rsf";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        RsfFixture.isolateHome(folder.newFolder("home"));
    }

    @Test
    public void exportWritesTheWholeModel() throws Exception {
        MemoryDatabase database = open();
        try {
            Engine engine = database.getEngine(null);
            String idl = new String(export(engine, database), ENCODING);

            assertTrue("the export stopped before the end of the model",
                    idl.trim().endsWith("ENDKIT ;"));
            assertTrue("the context diagram is not marked A-0, so no importer"
                            + " can attach the model to its root",
                    idl.contains("DIAGRAM GRAPHIC A-0 ;"));
            assertTrue("arrow labels are missing from the export",
                    idl.contains("LABEL "));
            close(engine);
        } finally {
            database.close();
        }
    }

    @Test
    public void everyFunctionSurvivesExportAndImport() throws Exception {
        MemoryDatabase database = open();
        try {
            Engine engine = database.getEngine(null);
            DataPlugin source = plugin(engine, database,
                    IDEF0Plugin.getBaseQualifiers(engine).get(0));
            List<String> before = names(source.getBaseFunction());

            byte[] idl = export(engine, database);
            DataPlugin target = importInto(engine, database, idl);
            List<String> after = names(target.getBaseFunction());

            assertEquals("the import lost or invented functions",
                    sorted(before), sorted(after));
            assertEquals("the import changed the number of functions",
                    before.size(), after.size());
            close(engine);
        } finally {
            database.close();
        }
    }

    private MemoryDatabase open() throws Exception {
        File sample = new File(RsfFixture.projectRoot(), SAMPLE);
        return (MemoryDatabase) FileDatabaseFactory.createDatabase(sample);
    }

    private static void close(Engine engine) throws Exception {
        ((FileIEngineImpl) engine.getDeligate()).close();
    }

    private DataPlugin plugin(Engine engine, MemoryDatabase database,
                              Qualifier qualifier) {
        return NDataPluginFactory.getDataPlugin(qualifier, engine,
                database.getAccessRules(null));
    }

    private byte[] export(Engine engine, MemoryDatabase database)
            throws Exception {
        DataPlugin source = plugin(engine, database,
                IDEF0Plugin.getBaseQualifiers(engine).get(0));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        source.exportToIDL(source.getBaseFunction(), out, ENCODING);
        return out.toByteArray();
    }

    private DataPlugin importInto(Engine engine, MemoryDatabase database,
                                  byte[] idl) throws Exception {
        ((Journaled) engine).startUserTransaction();
        try {
            Qualifier qualifier = engine.createQualifier();
            Attribute name = engine.createAttribute(
                    new AttributeType("Core", "Text", true));
            name.setName("Name");
            engine.updateAttribute(name);
            qualifier.getAttributes().add(name);
            qualifier.setAttributeForName(name.getId());
            IDEF0Plugin.installFunctionAttributes(qualifier, engine);

            DataPlugin target = plugin(engine, database, qualifier);
            target.importFromIDL(target, ENCODING,
                    new ByteArrayInputStream(idl));
            ((Journaled) engine).commitUserTransaction();
            return target;
        } catch (Exception e) {
            ((Journaled) engine).rollbackUserTransaction();
            throw e;
        }
    }

    private static List<String> sorted(List<String> names) {
        List<String> result = new ArrayList<String>();
        for (String name : names)
            result.add(name.replaceAll("\\s+", " ").trim());
        Collections.sort(result);
        return result;
    }

    private static List<String> names(Function function) {
        List<String> result = new ArrayList<String>();
        collect(function, result);
        return result;
    }

    private static void collect(Function function, List<String> out) {
        for (int i = 0; i < function.getChildCount(); i++) {
            Function child = (Function) function.getChildAt(i);
            out.add(child.getName());
            collect(child, out);
        }
    }
}
