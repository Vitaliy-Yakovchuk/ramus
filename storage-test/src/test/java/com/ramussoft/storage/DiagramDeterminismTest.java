package com.ramussoft.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;

/**
 * Чи не залежить відмальована діаграма від машини й дня.
 * <p>
 * {@link DiagramGoldenTest} звіряє знімки з нульовим допуском, тому будь-яка
 * така залежність ламає його на чужому комп'ютері або наступного ранку — і
 * ламає незрозуміло, різницею зображень. Цей тест перевіряє причину, а не
 * наслідок: шрифт кожного напису й дату кожної функції.
 */
public class DiagramDeterminismTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        RsfFixture.isolateHome(folder.newFolder("home"));
    }

    /**
     * Жоден напис не малюється системним шрифтом.
     * <p>
     * Підміна йде поелементно — функції, стрілки, написи, типовий шрифт
     * полотна, — тож новий вид елемента легко проґавити. Тоді еталон знову
     * почне залежати від того, який фізичний шрифт система підставляє під
     * логічний {@code Dialog}.
     */
    @Test
    public void everyPaintedFontIsTheBundledOne() throws Exception {
        List<Font> fonts = new ArrayList<Font>();
        withSample(new Visitor() {
            @Override
            public void visit(DataPlugin plugin, Function base) {
                fonts.addAll(DiagramRenderer.paintedFonts(plugin, base));
            }
        });

        assertFalse("не знайдено жодного шрифту", fonts.isEmpty());
        for (Font font : fonts)
            assertEquals("напис малюється не вбудованим шрифтом",
                    TestFonts.FAMILY, font.getFamily(Locale.ROOT));
    }

    /**
     * Дати в рамці не залежать від дня запуску.
     */
    @Test
    public void datesAreFixed() throws Exception {
        final int[] checked = new int[1];
        // Перевіряємо на відкритій базі: функція читає поля крізь рушій, і
        // після закриття з'єднання дати вже не дістати.
        withSample(new Visitor() {
            @Override
            public void visit(DataPlugin plugin, Function base) {
                checked[0] += checkDates(base);
            }
        });
        assertTrue("не знайдено жодної функції", checked[0] > 0);
    }

    private static int checkDates(Function function) {
        assertEquals("дата створення лишилась поточною", RsfFixture.FIXED_DATE,
                function.getCreateDate());
        assertEquals("дата перегляду лишилась поточною", RsfFixture.FIXED_DATE,
                function.getRevDate());
        int count = 1;
        for (int i = 0; i < function.getChildCount(); i++)
            count += checkDates((Function) function.getChildAt(i));
        return count;
    }

    /**
     * Два відмальовування того самого файлу дають той самий відбиток.
     */
    @Test
    public void renderingRepeatsItself() throws Exception {
        assertEquals(render(), render());
    }

    private static java.util.Map<String, String> render() throws Exception {
        MemoryDatabase database = open();
        try {
            Engine engine = database.getEngine(null);
            java.util.Map<String, String> result = DiagramRenderer.render(
                    engine, database.getAccessRules(null));
            assertTrue("не відмальовано жодної діаграми", result.size() > 0);
            ((FileIEngineImpl) engine.getDeligate()).close();
            return result;
        } finally {
            database.close();
        }
    }

    private interface Visitor {
        void visit(DataPlugin plugin, Function base);
    }

    /**
     * Відкриває зразок і проходить його моделі так само, як це робить
     * {@link DiagramRenderer}: із заміною шрифтів і дат.
     */
    private void withSample(Visitor visitor) throws Exception {
        MemoryDatabase database = open();
        try {
            Engine engine = database.getEngine(null);
            for (Qualifier model : IDEF0Plugin.getBaseQualifiers(engine)) {
                DataPlugin plugin = NDataPluginFactory.getDataPlugin(model,
                        engine, database.getAccessRules(null));
                Function base = plugin.getBaseFunction();
                if (base == null)
                    continue;
                DiagramRenderer.prepare(base);
                visitor.visit(plugin, base);
            }
            ((FileIEngineImpl) engine.getDeligate()).close();
        } finally {
            database.close();
        }
    }

    private static MemoryDatabase open() throws Exception {
        File sample = new File(RsfFixture.projectRoot(),
                "dest/doc/en/Enterprise activity.rsf");
        return (MemoryDatabase) FileDatabaseFactory.createDatabase(sample);
    }

}
