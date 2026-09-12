package com.ramussoft.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.dsoft.pb.types.FRectangle;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.idef0.attribute.TextLabelPersistent;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.MovingText;
import com.ramussoft.pb.print.PIDEF0painter;

/**
 * Підписи діаграми в новій, не двійковій формі.
 * <p>
 * Модель створюється з нуля, тож одразу має версію подання 3. Перевіряємо, що
 * підпис переживає збереження й читання, що він лежить в атрибуті, а не в
 * двійковому полі, і що двійкове поле стало вироджено малим.
 */
public class TextLabelStorageTest {

    private static final Dimension SIZE = new Dimension(1200, 900);

    private static final String TEXT = "Примітка до діаграми";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        RsfFixture.isolateHome(folder.newFolder("home"));
    }

    @Test
    public void labelSurvivesSaveAndReload() throws Exception {
        File file = new File(folder.getRoot(), "model.rsf");

        MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase();
        byte[] blob;
        try {
            Engine engine = database.getEngine(null);
            DataPlugin plugin = createModel(engine,
                    database.getAccessRules(null));
            Function base = plugin.getBaseFunction();

            MovingArea area = PIDEF0painter.createMovingArea(SIZE, plugin,
                    base);
            area.setActiveFunction(base);

            MovingText text = area.createText();
            text.setText(TEXT);
            text.setFont(new Font("Dialog", Font.BOLD, 14));
            text.setColor(Color.RED);
            text.setBounds(new FRectangle(20.0, 40.0, 200.0, 20.0));
            area.getRefactor().addText(text);
            area.getRefactor().saveToFunction(base);

            blob = base.getSectorData();
            assertEquals("підпис не потрапив в атрибут", 1,
                    base.getTextLabels().size());

            ((FileIEngineImpl) engine.getDeligate()).saveToFile(file);
        } finally {
            database.close();
        }

        // Двійкове поле тепер містить лише номер версії.
        assertTrue("двійкове поле досі велике: " + blob.length,
                blob.length <= 8);
        assertFalse("текст підпису лишився у двійковому полі",
                new String(blob, "UTF-8").contains("Примітка"));

        MemoryDatabase reopened = (MemoryDatabase) FileDatabaseFactory
                .createDatabase(file);
        try {
            Engine engine = reopened.getEngine(null);
            DataPlugin plugin = NDataPluginFactory.getDataPlugin(
                    IDEF0Plugin.getBaseQualifiers(engine).get(0), engine,
                    reopened.getAccessRules(null));
            Function base = plugin.getBaseFunction();

            assertEquals("підпис не збережено", 1,
                    base.getTextLabels().size());
            TextLabelPersistent label = base.getTextLabels().get(0);
            assertEquals(TEXT, label.getText());
            assertEquals(20.0, label.getX(), 0.001);
            assertEquals(40.0, label.getY(), 0.001);
            assertEquals(14, label.getFontSize());
            assertEquals(Font.BOLD, label.getFontStyle());
            assertEquals(Color.RED.getRGB(), label.getColor().intValue());

            // І він справді потрапляє на діаграму.
            MovingArea area = PIDEF0painter.createMovingArea(SIZE, plugin,
                    base);
            area.setActiveFunction(base);
            assertEquals("підпис не відновився на діаграмі", 1,
                    area.getRefactor().getTexts().size());
            assertEquals(TEXT, area.getRefactor().getTexts().get(0).getText());

            ((FileIEngineImpl) engine.getDeligate()).close();
        } finally {
            reopened.close();
        }
    }

    private static DataPlugin createModel(Engine engine, AccessRules rules) {
        Attribute name = engine.createAttribute(
                new AttributeType("Core", "Text", true));
        name.setName("Назва");
        engine.updateAttribute(name);

        Qualifier qualifier = engine.createQualifier();
        qualifier.setName("Тестова модель");
        qualifier.getAttributes().add(name);
        qualifier.setAttributeForName(name.getId());
        engine.updateQualifier(qualifier);

        IDEF0Plugin.installFunctionAttributes(
                engine.getQualifier(qualifier.getId()), engine);
        return NDataPluginFactory.getDataPlugin(
                engine.getQualifier(qualifier.getId()), engine, rules);
    }
}
