package com.ramussoft.local.ai;

import com.ramussoft.common.Engine;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.pb.*;
import com.ramussoft.pb.idef.visual.MovingArea;
import java.io.File;
import java.util.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

/** Regression for the handout's renamed control and mechanism branches. */
public class RenamedBranchTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private static final String RULES = "Правила и процедуры", RULE_BRANCH = "Правила сборки и тестирования";
    private static final String SYSTEM = "Бухгалтерская система", SYSTEM_BRANCH = "Система оформления заказов";

    static AiPlan plan() {
        List<AiPlan.AiOperation> operations = new ArrayList<>();
        String root = TransportCompanyPlan.CONTEXT;
        for (var a : TransportCompanyPlan.create().operations()) {
            String name = a.name().equals("Правила перевозки") ? RULES
                    : a.name().equals("Персонал и инфраструктура") ? SYSTEM : a.name();
            String parent = "";
            if (a.qualifier().equals("Потоки::" + root) && a.target().equals("Подготовить перевозку") && name.equals(RULES)) {
                name = RULE_BRANCH; parent = RULES;
            }
            if (a.qualifier().equals("Потоки::" + root) && a.target().equals("Заключить договор") && name.equals(SYSTEM)) {
                name = SYSTEM_BRANCH; parent = SYSTEM;
            }
            if (a.qualifier().equals("Потоки::" + root + "::Подготовить перевозку") && name.equals(RULES)) name = RULE_BRANCH;
            if (a.qualifier().equals("Потоки::" + root + "::Заключить договор") && name.equals(SYSTEM)) name = SYSTEM_BRANCH;
            operations.add(new AiPlan.AiOperation(a.tool(), a.qualifier(), name, a.source(), a.target(),
                    a.icom(), a.style(), a.color(), a.description(), a.tunnel(), parent));
        }
        return new AiPlan("Тест переименованных ветвей", operations);
    }

    @Test public void acceptsExplicitAliasesAndRejectsUnknownOrWrongIcomParent() {
        AiPlan valid = plan();
        new Lab1PlanValidator().validate(valid);
        for (String wrong : List.of("Несуществующий поток", SYSTEM)) {
            List<AiPlan.AiOperation> broken = new ArrayList<>(valid.operations());
            for (int i = 0; i < broken.size(); i++) {
                var a = broken.get(i);
                if (!RULES.equals(a.parentStream())) continue;
                broken.set(i, new AiPlan.AiOperation(a.tool(), a.qualifier(), a.name(), a.source(), a.target(),
                        a.icom(), a.style(), a.color(), a.description(), a.tunnel(), wrong));
            }
            try { new Lab1PlanValidator().validate(new AiPlan("bad", broken)); fail("Invalid alias accepted"); }
            catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("баланс")); }
        }
    }

    @Test public void aliasRulesDoNotDependOnHandoutVocabulary() {
        Map<String, String> names = Map.of(RULES, "Регламент обслуживания",
                RULE_BRANCH, "Правила подготовки заказа", SYSTEM, "Ресурсы организации",
                SYSTEM_BRANCH, "Информационная система заказов");
        List<AiPlan.AiOperation> operations = new ArrayList<>();
        for (var a : plan().operations()) operations.add(new AiPlan.AiOperation(a.tool(), a.qualifier(),
                names.getOrDefault(a.name(), a.name()), a.source(), a.target(), a.icom(), a.style(), a.color(),
                a.description(), a.tunnel(), names.getOrDefault(a.parentStream(), a.parentStream())));
        new Lab1PlanValidator().validate(new AiPlan("Другие имена интерфейсов", operations));
    }

    @Test public void rejectsAliasWithoutExplicitParentAndAliasOnInternalArrow() {
        List<AiPlan.AiOperation> missingParent = new ArrayList<>();
        for (var a : plan().operations()) missingParent.add(new AiPlan.AiOperation(a.tool(), a.qualifier(),
                a.name(), a.source(), a.target(), a.icom(), a.style(), a.color(), a.description(), a.tunnel()));
        try { new Lab1PlanValidator().validate(new AiPlan("bad", missingParent)); fail("Guessed parent from label"); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("баланс")); }
        List<AiPlan.AiOperation> internal = new ArrayList<>(plan().operations());
        for (int i = 0; i < internal.size(); i++) {
            var a = internal.get(i);
            if (!a.tool().equals("create_stream") || a.source().startsWith("@") || a.target().startsWith("@")) continue;
            internal.set(i, new AiPlan.AiOperation(a.tool(), a.qualifier(), a.name(), a.source(), a.target(),
                    a.icom(), a.style(), a.color(), a.description(), a.tunnel(), RULES));
            break;
        }
        try { new Lab1PlanValidator().validate(new AiPlan("bad", internal)); fail("Internal alias accepted"); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("граничной")); }
    }

    @Test public void savesVisibleBranchNamesAndNativeParentLinksAcrossLevels() throws Exception {
        String previousHome = System.getProperty("user.home");
        System.setProperty("user.home", temporary.newFolder("home").getAbsolutePath());
        System.setProperty("java.awt.headless", "true");
        MemoryDatabase db = (MemoryDatabase) FileDatabaseFactory.createDatabase();
        Engine engine = db.getEngine(null);
        try {
            new RamusToolExecutor().execute(engine, db.getAccessRules(null), plan());
            long qualifierId = RamusToolExecutor.functionQualifier(engine).getId();
            DataPlugin original = NDataPluginFactory.getDataPlugin(engine.getQualifier(qualifierId), engine, db.getAccessRules(null));
            assertPlannedStreams((Function) original.getBaseFunction(), "", plan());
            var repeated = new RamusToolExecutor().execute(engine, db.getAccessRules(null), plan());
            assertTrue("Second run created: " + repeated, repeated.isEmpty());
            File saved = temporary.newFile("branches.rsf");
            ((FileIEngineImpl) engine.getDeligate()).saveToFile(saved);
            MemoryDatabase reopened = (MemoryDatabase) FileDatabaseFactory.createDatabase(saved);
            Engine restored = reopened.getEngine(null);
            try {
                DataPlugin data = NDataPluginFactory.getDataPlugin(restored.getQualifier(qualifierId), restored, reopened.getAccessRules(null));
                Function base = (Function) data.getBaseFunction();
                Function context = (Function) base.getChildAt(0);
                assertBranch(data, context, RULE_BRANCH, RULES);
                assertBranch(data, context, SYSTEM_BRANCH, SYSTEM);
                assertBranch(data, (Function) context.getChildAt(1), RULE_BRANCH, RULE_BRANCH);
                assertBranch(data, (Function) context.getChildAt(0), SYSTEM_BRANCH, SYSTEM_BRANCH);
                for (Sector arrow : base.getSectors()) {
                    assertNotNull(arrow.getStream());
                    assertFalse(RULE_BRANCH.equals(arrow.getStream().getName()));
                    assertFalse(SYSTEM_BRANCH.equals(arrow.getStream().getName()));
                }
            } finally {
                ((FileIEngineImpl) restored.getDeligate()).close(); reopened.close();
            }
        } finally {
            ((FileIEngineImpl) engine.getDeligate()).close(); db.close();
            System.setProperty("user.home", previousHome);
        }
    }

    private void assertBranch(DataPlugin data, Function diagram, String label, String parentName) {
        MovingArea area = new MovingArea(data, diagram);
        area.setActiveFunction(diagram);
        boolean found = false;
        for (int i = 0; i < area.getRefactor().getSectorsCount(); i++) {
            var arrow = area.getRefactor().getSector(i);
            assertNotNull("Unconnected native stub", arrow.getStream());
            if (!label.equals(arrow.getStream().getName())) continue;
            found = true;
            assertTrue(Arrays.stream(arrow.getSector().getStart().getCrosspoint().getIns()).anyMatch(
                    upper -> upper.getStream() != null && parentName.equals(upper.getStream().getName())
                            && diagram.equals(upper.getEnd().getFunction())));
        }
        assertTrue("Missing renamed branch: " + label, found);
    }

    private void assertPlannedStreams(Function diagram, String path, AiPlan plan) {
        if (diagram.getChildCount() == 0) return; // Native leaf stubs are not expanded diagrams.
        Set<String> names = new HashSet<>();
        for (var a : plan.operations()) if (a.tool().equals("create_stream")
                && Lab1PlanValidator.scope(a.qualifier(), "Потоки").equals(path)) names.add(a.name());
        for (Sector sector : diagram.getSectors()) assertTrue("Unexpected stream at " + path + ": "
                + (sector.getStream() == null ? "null" : sector.getStream().getName()),
                sector.getStream() != null && names.contains(sector.getStream().getName()));
        for (int i = 0; i < diagram.getChildCount(); i++) {
            Function child = (Function) diagram.getChildAt(i);
            assertPlannedStreams(child, path.isEmpty() ? child.getName() : path + "::" + child.getName(), plan);
        }
    }
}
