package com.ramussoft.local.ai;

import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.impl.FileIEngineImpl;
import com.ramussoft.database.FileDatabaseFactory;
import com.ramussoft.database.MemoryDatabase;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.pb.*;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.dsoft.pb.types.FRectangle;
import java.io.File;
import java.util.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class TransportDiagramIntegrationTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void savesBalancedHierarchyAndReopensWithoutDuplicatingObjects() throws Exception {
        String previousHome = System.getProperty("user.home");
        System.setProperty("java.awt.headless", "true");
        System.setProperty("user.home", temporary.newFolder("home").getAbsolutePath());
        File root = new File(System.getProperty("user.dir"));
        while (root != null && !new File(root, "settings.gradle").isFile()) root = root.getParentFile();
        assertNotNull(root);
        MemoryDatabase db = (MemoryDatabase) FileDatabaseFactory.createDatabase();
        Engine engine = null;
        try {
            engine = db.getEngine(null);
            AiPlan plan = TransportCompanyPlan.create();
            StringBuilder json = new StringBuilder("{\"operations\":[");
            for (AiPlan.AiOperation a : plan.operations()) {
                if (json.charAt(json.length() - 1) != '[') json.append(',');
                json.append("{\"tool\":\"").append(DeepSeekClient.esc(a.tool()))
                        .append("\",\"qualifier\":\"").append(DeepSeekClient.esc(a.qualifier()))
                        .append("\",\"name\":\"").append(DeepSeekClient.esc(a.name()))
                        .append("\",\"source\":\"").append(DeepSeekClient.esc(a.source()))
                        .append("\",\"target\":\"").append(DeepSeekClient.esc(a.target()))
                        .append("\",\"icom\":\"").append(a.icom())
                        .append("\",\"description\":\"").append(DeepSeekClient.esc(a.description()))
                        .append("\",\"tunnel\":\"").append(a.tunnel()).append("\"}");
            }
            json.append("]}");
            java.nio.file.Files.writeString(new File(root, "local-client/build/lab1-plan.json").toPath(), json);
            new RamusToolExecutor().execute(engine, db.getAccessRules(null), plan);
            long modelId = RamusToolExecutor.functionQualifier(engine).getId();
            DataPlugin nativeData = NDataPluginFactory.getDataPlugin(RamusToolExecutor.functionQualifier(engine), engine, db.getAccessRules(null));
            Sector temporarySector = nativeData.createSector();
            temporarySector.setShowText(false);
            temporarySector.setAlternativeText("Проверка свойства");
            temporarySector.setCreateState(-1, 12.5);
            assertFalse(temporarySector.isShowText());
            assertEquals("Проверка свойства", temporarySector.getAlternativeText());
            assertEquals(-1, temporarySector.getCreateState());
            temporarySector.remove();
            assertTrue("A second run must reuse existing functions and streams",
                    new RamusToolExecutor().execute(engine, db.getAccessRules(null), plan).isEmpty());
            assertEquals(modelId, RamusToolExecutor.functionQualifier(engine).getId());
            File saved = new File(root, "local-client/build/lab1-transport.rsf");
            ((FileIEngineImpl) engine.getDeligate()).saveToFile(saved);
            MemoryDatabase reopened = (MemoryDatabase) FileDatabaseFactory.createDatabase(saved);
            Engine restored = reopened.getEngine(null);
            try {
                Qualifier qualifier = restored.getQualifier(modelId);
                DataPlugin data = NDataPluginFactory.getDataPlugin(qualifier, restored, reopened.getAccessRules(null));
                Function base = (Function) data.getBaseFunction();
                assertEquals("A-0 must contain one function", 1, base.getChildCount());
                Function context = (Function) base.getChildAt(0);
                assertEquals(TransportCompanyPlan.CONTEXT, context.getName());
                assertEquals("A0 must have the three stated stages", 3, context.getChildCount());
                checkDiagram(data, base, new File(root, "local-client/build/lab1-A-0.png"), false);
                checkDiagram(data, context, new File(root, "local-client/build/lab1-A0.png"), true);
                int[] childCounts = {4, 5, 4};
                for (int i = 0; i < 3; i++) {
                    Function stage = (Function) context.getChildAt(i);
                    assertEquals(childCounts[i], stage.getChildCount());
                    assertFalse("Function description must persist", ((String) restored.getAttribute(
                            stage.getElement(), restored.getAttributeByName("Описание AI"))).isBlank());
                    checkDiagram(data, stage, new File(root, "local-client/build/lab1-A" + (i + 1) + ".png"), true);
                }
                Function preparation = (Function) context.getChildAt(1);
                assertEquals("Спланировать логистику", ((Function) preparation.getChildAt(0)).getName());
                assertEquals("Погрузить груз", ((Function) preparation.getChildAt(4)).getName());
                assertTrue("The explicit local maintenance tunnel must persist",
                        data.getAllSectors().stream().anyMatch(s -> s.getStream() != null && "Заявка на ТО".equals(s.getStream().getName())
                            && s.getEnd().getTunnelSoft() == Crosspoint.TUNNEL_SOFT));
                // A second vocabulary must not replace or rename the transport project.
                new RamusToolExecutor().execute(engine, db.getAccessRules(null), Lab1PlanValidatorTest.libraryPlan());
                assertTrue(modelId != RamusToolExecutor.functionQualifier(engine).getId());
                assertEquals(1, base.getChildCount());
                assertEquals(TransportCompanyPlan.CONTEXT, context.getName());
            } finally {
                ((FileIEngineImpl) restored.getDeligate()).close();
                reopened.close();
            }
        } finally {
            if (engine != null) ((FileIEngineImpl) engine.getDeligate()).close();
            db.close();
            System.setProperty("user.home", previousHome);
        }
    }

    private static void checkDiagram(DataPlugin data, Function diagram, File image, boolean inherited) throws Exception {
        MovingArea area = new MovingArea(data, diagram);
        area.setActiveFunction(diagram);
        List<FRectangle> labels = new ArrayList<>();
        Set<String> printedNames = new HashSet<>();
        int internal = 0;
        for (int i = 0; i < area.getRefactor().getSectorsCount(); i++) {
            PaintSector arrow = area.getRefactor().getSector(i);
            assertNotNull("Unnamed native stub on " + diagram.getName(), arrow.getStream());
            assertFalse(arrow.getStream().getName().isBlank());
            var start = arrow.getSector().getStart();
            var end = arrow.getSector().getEnd();
            assertTrue("Arrow has a dangling source", start.getFunction() != null || start.getBorderType() >= 0);
            assertTrue("Arrow has a dangling destination", end.getFunction() != null || end.getBorderType() >= 0);
            var path = arrow.getPoints();
            List<FRectangle> blocks = new ArrayList<>();
            for (int j = 0; j < diagram.getChildCount(); j++) blocks.add(((Function) diagram.getChildAt(j)).getBounds());
            for (int j = 1; j < path.length; j++) assertFalse("Arrow crosses a block: " + arrow.getStream().getName(),
                    ArrowRouter.crossesBlock(path[j - 1].getX(), path[j - 1].getY(), path[j].getX(), path[j].getY(), blocks));
            if (start.getFunction() != null && end.getFunction() != null) internal++;
            if (inherited && start.getBorderType() >= 0) {
                assertTrue("Boundary must be linked to a parent-level arrow: " + arrow.getStream().getName(),
                        start.getCrosspoint().getIns().length > 0);
            }
            if (inherited && end.getBorderType() >= 0 && end.getTunnelSoft() != Crosspoint.TUNNEL_SOFT) {
                assertTrue("Output must be linked to a parent-level arrow: " + arrow.getStream().getName(),
                        end.getCrosspoint().getOuts().length > 0);
            }
            if (arrow.getText() != null && arrow.isShowText()) {
                assertTrue("Duplicate label: " + arrow.getStream().getName(), printedNames.add(arrow.getStream().getName()));
                FRectangle label = arrow.getText().getBounds();
                assertTrue("Label outside page", label.getLeft() >= 0 && label.getTop() >= 0
                        && label.getRight() <= area.getDoubleWidth() && label.getBottom() <= area.getDoubleHeight());
                for (int j = 0; j < diagram.getChildCount(); j++)
                    assertFalse("Label overlaps block: " + arrow.getStream().getName(),
                            overlaps(label, ((Function) diagram.getChildAt(j)).getBounds()));
                for (FRectangle other : labels) assertFalse("Labels overlap: " + arrow.getStream().getName(), overlaps(label, other));
                for (int j = 0; j < area.getRefactor().getSectorsCount(); j++) {
                    var line = area.getRefactor().getSector(j).getPoints();
                    for (int k = 1; k < line.length; k++) assertFalse("Text overlaps an arrow: " + arrow.getStream().getName(),
                            overlaps(label, new FRectangle(Math.min(line[k - 1].getX(), line[k].getX()),
                                    Math.min(line[k - 1].getY(), line[k].getY()),
                                    Math.max(0.1, Math.abs(line[k].getX() - line[k - 1].getX())),
                                    Math.max(0.1, Math.abs(line[k].getY() - line[k - 1].getY())))));
                }
                labels.add(label);
            }
        }
        if (diagram.getChildCount() > 1) assertTrue("No internal arrows", internal > 0);
        com.ramussoft.pb.print.PIDEF0painter painter = new com.ramussoft.pb.print.PIDEF0painter(
                diagram, new java.awt.Dimension(2000, 1400), data);
        javax.imageio.ImageIO.write(painter.createImage(null), "png", image);
    }

    private static boolean overlaps(FRectangle a, FRectangle b) {
        return a.getLeft() < b.getRight() && a.getRight() > b.getLeft()
                && a.getTop() < b.getBottom() && a.getBottom() > b.getTop();
    }
}
