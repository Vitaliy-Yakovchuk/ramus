package com.ramussoft.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;

/**
 * Проєкт треба не лише прочитати, а й продовжити редагувати.
 * <p>
 * Ключі роздають лічильники, і після відкриття вони мають стояти за
 * найбільшим уже зайнятим значенням — інакше перший же створений елемент
 * дістав би чужий номер і затер сусіда.
 */
public class ProjectEditingTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File project;

    @Before
    public void setUp() throws Exception {
        RsfFixture.isolateHome(folder.newFolder("home"));
        File sample = RsfFixture.sampleFiles().get(0);
        project = new File(folder.newFolder("work"), "project.ramus");
        RsfFixture.exportProject(sample, project);
    }

    @Test
    public void newElementDoesNotReuseExistingKey() throws Exception {
        Set<Long> before = new HashSet<Long>();
        long created;

        MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase(project);
        try {
            Engine engine = database.getEngine(null);
            Qualifier qualifier = biggest(engine);
            for (Element element : engine.getElements(qualifier.getId()))
                before.add(Long.valueOf(element.getId()));

            Element element = engine.createElement(qualifier.getId());
            created = element.getId();
            assertFalse("новий елемент дістав уже зайнятий ключ " + created,
                    before.contains(Long.valueOf(created)));

            FileIEngineImpl impl = (FileIEngineImpl) engine.getDeligate();
            impl.saveProject(project);
            impl.close();
        } finally {
            database.close();
        }

        // Після повторного відкриття доданий елемент має бути на місці, а
        // решта — незмінною.
        database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase(project);
        try {
            Engine engine = database.getEngine(null);
            Qualifier qualifier = biggest(engine);
            Set<Long> after = new HashSet<Long>();
            for (Element element : engine.getElements(qualifier.getId()))
                after.add(Long.valueOf(element.getId()));

            assertTrue("доданий елемент зник після збереження",
                    after.contains(Long.valueOf(created)));
            after.remove(Long.valueOf(created));
            assertEquals("змінився склад решти елементів", before, after);
            ((FileIEngineImpl) engine.getDeligate()).close();
        } finally {
            database.close();
        }
    }

    /**
     * Значення атрибута має переживати збереження й відкриття — це найкоротший
     * шлях перевірити весь ланцюг «рушій → файл → рушій».
     */
    @Test
    public void editedValueSurvivesSaveAndReopen() throws Exception {
        long elementId;
        long attributeId;

        MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase(project);
        try {
            Engine engine = database.getEngine(null);
            Qualifier qualifier = biggest(engine);
            Attribute attribute = text(engine, qualifier);
            assertNotNull("у класифікаторі немає текстового атрибута",
                    attribute);
            Element element = engine.getElements(qualifier.getId()).get(0);
            elementId = element.getId();
            attributeId = attribute.getId();

            engine.setAttribute(element, attribute, "Перевірка збереження");

            FileIEngineImpl impl = (FileIEngineImpl) engine.getDeligate();
            impl.saveProject(project);
            impl.close();
        } finally {
            database.close();
        }

        database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase(project);
        try {
            Engine engine = database.getEngine(null);
            Object value = engine.getAttribute(engine.getElement(elementId),
                    engine.getAttribute(attributeId));
            assertEquals("Перевірка збереження", value);
            ((FileIEngineImpl) engine.getDeligate()).close();
        } finally {
            database.close();
        }
    }

    /**
     * Порожній проєкт — те, що отримує користувач після «Створити».
     * <p>
     * Тут немає жодного файлу, з якого можна було б щось успадкувати, тож
     * увесь вміст мають дати плагіни; якщо збереження такого проєкту не
     * читається назад, новий документ неможливо створити взагалі.
     */
    @Test
    public void freshProjectSavesAndOpens() throws Exception {
        File fresh = new File(folder.newFolder("fresh"), "Новий.ramus");

        MemoryDatabase database = (MemoryDatabase) FileDatabaseFactory
                .createDatabase();
        long qualifierId;
        try {
            Engine engine = database.getEngine(null);
            Qualifier qualifier = engine.createQualifier();
            qualifier.setName("Довідник");
            engine.updateQualifier(qualifier);
            qualifierId = qualifier.getId();

            FileIEngineImpl impl = (FileIEngineImpl) engine.getDeligate();
            impl.saveProject(fresh);
            impl.close();
        } finally {
            database.close();
        }

        database = (MemoryDatabase) FileDatabaseFactory.createDatabase(fresh);
        try {
            Engine engine = database.getEngine(null);
            Qualifier qualifier = engine.getQualifier(qualifierId);
            assertNotNull("класифікатор не пережив збереження", qualifier);
            assertEquals("Довідник", qualifier.getName());
            ((FileIEngineImpl) engine.getDeligate()).close();
        } finally {
            database.close();
        }
    }

    private static Qualifier biggest(Engine engine) {
        Qualifier result = null;
        int count = -1;
        for (Qualifier qualifier : engine.getQualifiers()) {
            int size = engine.getElements(qualifier.getId()).size();
            if (size > count) {
                count = size;
                result = qualifier;
            }
        }
        assertNotNull("у проєкті немає класифікаторів", result);
        return result;
    }

    private static Attribute text(Engine engine, Qualifier qualifier) {
        List<Attribute> attributes = qualifier.getAttributes();
        for (Attribute attribute : attributes)
            if ("Core.Text".equals(attribute.getAttributeType().toString()))
                return attribute;
        return null;
    }
}
