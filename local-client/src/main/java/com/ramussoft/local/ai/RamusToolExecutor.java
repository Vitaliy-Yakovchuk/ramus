package com.ramussoft.local.ai;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Element;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.AccessRules;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.data.AbstractRow;
import com.ramussoft.pb.idef.elements.Ordinate;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.Point;
import com.ramussoft.pb.idef.elements.SectorRefactor;
import com.ramussoft.pb.idef.visual.MovingArea;
import com.ramussoft.pb.idef.visual.WayStroke;
import com.dsoft.pb.types.FRectangle;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import com.ramussoft.idef0.NDataPluginFactory;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Allow-listed write tools for the AI. It uses the normal Ramus Engine, so
 * journal/undo integration remains in the application. No raw YAML or shell
 * operation is exposed here.
 */
public final class RamusToolExecutor {
    public List<Element> execute(Engine engine, AccessRules rules, AiPlan plan) {
        return execute(engine, rules, plan, message -> { });
    }

    public List<Element> execute(Engine engine, AccessRules rules, AiPlan plan, Consumer<String> progress) {
        new Lab1PlanValidator().validate(plan);
        List<Element> created = new ArrayList<>();
        Qualifier functionQualifier = selectQualifier(engine, rules, plan);
        if (!IDEF0Plugin.isFunction(functionQualifier))
            IDEF0Plugin.installFunctionAttributes(functionQualifier, engine);
        Attribute functionName = StandardAttributesPlugin.getAttributeNameAttribute(engine);
        boolean hasNameAttribute = functionQualifier.getAttributes().stream()
                .anyMatch(attribute -> attribute.getId() == functionName.getId());
        if (!hasNameAttribute) functionQualifier.getAttributes().add(functionName);
        functionQualifier.setAttributeForName(functionName.getId());
        Attribute description = engine.getAttributeByName("Описание AI");
        if (description == null) {
            description = engine.createAttribute(new com.ramussoft.common.AttributeType("Core", "Text", true));
            description.setName("Описание AI");
            engine.updateAttribute(description);
        }
        if (!functionQualifier.getAttributes().contains(description)) functionQualifier.getAttributes().add(description);
        engine.updateQualifier(functionQualifier);
        // A contextual DataPlugin is essential: the raw NDataPlugin has no
        // base function and cannot own visible IDEF0 boxes or sectors.
        DataPlugin data = NDataPluginFactory.getDataPlugin(functionQualifier, engine, rules);
        // The qualifier may have been cached before its Name attribute was
        // installed. Rebuild row sets so Function.setName uses the same
        // attribute mapping as the editor.
        data.clear();
        Function diagram = (Function) data.getBaseFunction();
        if (diagram == null)
            throw new IllegalStateException("Не удалось получить корневую функцию IDEF0-модели.");
        String contextName = plan.operations().stream().filter(op -> "create_element".equals(op.tool())
                && "Работы".equals(op.qualifier())).findFirst().orElseThrow().name();
        diagram.setName(contextName);
        diagram.setDecompositionType(0);
        com.dsoft.pb.idef.elements.ProjectOptions options = diagram.getProjectOptions();
        options.setProjectName(contextName);
        options.setUsedAt("Учебная лабораторная работа №1");
        options.setDefinition(plan.summary());
        diagram.setProjectOptions(options);
        java.util.Map<String, Row> functionsByPath = new java.util.LinkedHashMap<>();
        java.util.Map<String, java.util.Map<String, Row>> functionsByDiagram = new java.util.LinkedHashMap<>();
        progress.accept("Шаг 1/4 — создаю контекстную диаграмму A-0");
        // A-0 contains one context function. The assignment's operational
        // activities belong to A0 and the second-stage breakdown to A2.
        for (AiPlan.AiOperation operation : plan.operations()) {
            if (!"create_element".equals(operation.tool())) continue;
            if (!"Работы".equals(operation.qualifier())) continue;
            Row row = findChildByName(data, diagram, operation.name());
            if (row == null) {
                row = data.createFunction(diagram, Function.TYPE_PROCESS);
                if (row == null) throw new IllegalStateException("Ramus не создал контекстный блок: " + operation.name());
                row.setName(operation.name());
                created.add(row.getElement());
            }
            if (!(row instanceof Function)) throw new IllegalStateException("Не найден контекстный блок: " + operation.name());
            layout((Function) row, 0);
            engine.setAttribute(row.getElement(), description, operation.description());
            functionsByPath.put(operation.name(), row);
            functionsByDiagram.computeIfAbsent("", key -> new java.util.LinkedHashMap<>()).put(operation.name(), row);
        }
        // Never delete older diagrams based solely on their names: those may
        // contain the user's own work. Rebuild in a fresh project if needed.
        progress.accept("Шаг 2/4 — размещаю работы на диаграммах");
        // Parent paths use the exact visible hierarchy, e.g. Context::A2.
        List<AiPlan.AiOperation> orderedFunctions = new ArrayList<>(plan.operations());
        orderedFunctions.sort(java.util.Comparator.comparingInt(op -> op.qualifier().split("::").length));
        for (AiPlan.AiOperation operation : orderedFunctions) {
            if (!"create_element".equals(operation.tool()) || !operation.qualifier().startsWith("Работы::")) continue;
            String parentPath = operation.qualifier().substring("Работы::".length());
            Row parent = functionsByPath.get(parentPath);
            if (!(parent instanceof Function)) throw new IllegalArgumentException("Не найдена родительская работа: " + parentPath);
            Row row = findChildByName(data, (Function) parent, operation.name());
            if (row == null) {
                row = data.createFunction((Function) parent, Function.TYPE_PROCESS);
                if (row == null) throw new IllegalStateException("Ramus не создал дочерний блок: " + operation.name());
                row.setName(operation.name());
                created.add(row.getElement());
            }
            java.util.Map<String, Row> local = functionsByDiagram.computeIfAbsent(parentPath, key -> new java.util.LinkedHashMap<>());
            int childIndex = local.size();
            String path = parentPath + "::" + operation.name();
            functionsByPath.put(path, row);
            local.put(operation.name(), row);
            engine.setAttribute(row.getElement(), description, operation.description());
        }
        // Reflow each complete level only after all of its siblings exist.
        // This keeps the same plan readable when the number of blocks changes.
        for (java.util.Map.Entry<String, java.util.Map<String, Row>> entry : functionsByDiagram.entrySet()) {
            String parentPath = entry.getKey();
            if (parentPath.isEmpty()) continue;
            List<Function> siblings = new ArrayList<>();
            for (Row row : entry.getValue().values())
                if (row instanceof Function) siblings.add((Function) row);
            int longestCaption = siblings.stream().mapToInt(row -> row.getName().length()).max().orElse(0);
            for (int i = 0; i < siblings.size(); i++) {
                if (parentPath.contains("::") || siblings.size() > 3)
                    layoutChild(siblings.get(i), i, siblings.size(), longestCaption);
                else layoutStage(siblings.get(i), i, siblings.size(), longestCaption);
            }
        }
        // DeepSeek may return operations in arbitrary order. Resolve all
        // function names first, then build streams from the completed map.
        for (AiPlan.AiOperation operation : plan.operations()) {
            if (!"create_stream".equals(operation.tool())) continue;
            if (!"Потоки".equals(operation.qualifier()) && !operation.qualifier().startsWith("Потоки::"))
                throw new IllegalArgumentException("Неизвестная область стрелок: " + operation.qualifier());
        }
        java.util.Map<String, java.util.List<AiPlan.AiOperation>> streamsByParent = new java.util.LinkedHashMap<>();
        for (AiPlan.AiOperation operation : plan.operations()) {
            if (!"create_stream".equals(operation.tool())) continue;
            String parentPath = operation.qualifier().equals("Потоки") ? "" : operation.qualifier().substring("Потоки::".length());
            streamsByParent.computeIfAbsent(parentPath, key -> new ArrayList<>()).add(operation);
        }
        progress.accept("Шаг 3/4 — прокладываю стрелки по уровням диаграммы");
        List<java.util.Map.Entry<String, java.util.List<AiPlan.AiOperation>>> orderedLevels = new ArrayList<>(streamsByParent.entrySet());
        orderedLevels.sort(java.util.Comparator.comparingInt(e -> e.getKey().isEmpty() ? 0 : e.getKey().split("::").length));
        for (java.util.Map.Entry<String, java.util.List<AiPlan.AiOperation>> entry : orderedLevels) {
            String parentPath = entry.getKey();
            Function activeDiagram = parentPath.isEmpty() ? diagram : (Function) functionsByPath.get(parentPath);
            java.util.Map<String, Row> localFunctions = functionsByDiagram.get(parentPath);
            if (activeDiagram == null || localFunctions == null) throw new IllegalArgumentException("Не найдена декомпозиция: " + parentPath);
            MovingArea area = new MovingArea(data, activeDiagram);
            area.setActiveFunction(activeDiagram);
            // Replace only sectors actually belonging to this diagram, never
            // all same-named streams elsewhere in the project.
            java.util.Set<String> namesToReplace = new java.util.HashSet<>();
            for (AiPlan.AiOperation operation : entry.getValue()) {
                namesToReplace.add(operation.name());
                // Native inherited stubs still carry the enclosing interface's name.
                if (!operation.parentStream().isBlank()) namesToReplace.add(operation.parentStream());
            }
            List<Sector> sectorsToReplace = new ArrayList<>();
            for (int i = 0; i < area.getRefactor().getSectorsCount(); i++) {
                Sector existing = area.getRefactor().getSector(i).getSector();
                if (existing.getStream() != null && namesToReplace.contains(existing.getStream().getName()))
                    sectorsToReplace.add(existing);
            }
            for (Sector existing : sectorsToReplace) existing.remove();
            if (!sectorsToReplace.isEmpty()) {
                area = new MovingArea(data, activeDiagram);
                area.setActiveFunction(activeDiagram);
            }
            progress.accept("Обрабатываю уровень: " + (parentPath.isEmpty() ? "A-0" : parentPath.replace("::", " → ")));
            java.util.IdentityHashMap<Function, java.util.Map<Integer, Integer>> portUse = new java.util.IdentityHashMap<>();
            List<FRectangle> labelBounds = new ArrayList<>();
            List<FRectangle> blockBounds = new ArrayList<>();
            for (Row row : localFunctions.values())
                if (row instanceof Function) blockBounds.add(((Function) row).getBounds());
            java.util.Set<String> labeledStreams = new java.util.HashSet<>();
            int routeIndex = 0;
            for (AiPlan.AiOperation operation : entry.getValue()) {
                if (operation.source().isBlank() || operation.target().isBlank()) throw new IllegalArgumentException("У стрелки не заданы начало и конец: " + operation.name());
                Stream stream = findStream(data, operation.name());
                if (stream == null) {
                    stream = (Stream) data.createRow(data.getBaseStream(), true);
                    stream.setName(operation.name());
                    created.add(stream.getElement());
                }
                if (!operation.description().isBlank()) engine.setAttribute(stream.getElement(), description, operation.description());
                if (operation.source().startsWith("@")) {
                    Row target = resolveFunction(localFunctions, operation.target());
                    if (!(target instanceof Function)) throw new IllegalArgumentException("Не найдена работа-получатель стрелки: " + operation.target());
                    int side = boundarySide(operation.source());
                    createBoundaryArrow(area, (Function) target, stream, true, side,
                            nextPort(portUse, (Function) target, side), routeIndex++, labelBounds, blockBounds, labeledStreams,
                            operation.style(), operation.color(), operation.tunnel(), operation.parentStream());
                } else if (operation.target().startsWith("@")) {
                    Row source = resolveFunction(localFunctions, operation.source());
                    if (!(source instanceof Function)) throw new IllegalArgumentException("Не найдена работа-источник стрелки: " + operation.source());
                    int side = boundarySide(operation.target());
                    createBoundaryArrow(area, (Function) source, stream, false, side,
                            nextPort(portUse, (Function) source, side), routeIndex++, labelBounds, blockBounds, labeledStreams,
                            operation.style(), operation.color(), operation.tunnel(), operation.parentStream());
                } else {
                    Row source = resolveFunction(localFunctions, operation.source()), target = resolveFunction(localFunctions, operation.target());
                    if (!(source instanceof Function) || !(target instanceof Function)) throw new IllegalArgumentException("Стрелка ссылается на неизвестную работу: " + operation.source() + " -> " + operation.target());
                    Function from = (Function) source, to = (Function) target;
                    int sourcePort = nextPort(portUse, from, com.ramussoft.pb.idef.visual.MovingPanel.RIGHT);
                    int targetSide = "control".equalsIgnoreCase(operation.icom())
                            ? com.ramussoft.pb.idef.visual.MovingPanel.TOP
                            : "mechanism".equalsIgnoreCase(operation.icom())
                            ? com.ramussoft.pb.idef.visual.MovingPanel.BOTTOM
                            : com.ramussoft.pb.idef.visual.MovingPanel.LEFT;
                    int targetPort = nextPort(portUse, to, targetSide);
                    createVisibleArrow(area, from, to, stream, operation.icom(), sourcePort, targetPort,
                            routeIndex++, labelBounds, blockBounds, labeledStreams, operation.style(), operation.color());
                }
            }
            // Reconsider labels after every line exists, including the native
            // boundary stubs inherited from the context diagram.
            List<FRectangle> lineBounds = new ArrayList<>();
            for (int i = 0; i < area.getRefactor().getSectorsCount(); i++) {
                Point[] path = area.getRefactor().getSector(i).getPoints();
                for (int j = 1; j < path.length; j++) {
                    Point a = path[j - 1], b = path[j];
                    lineBounds.add(new FRectangle(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()),
                            Math.max(1, Math.abs(a.getX() - b.getX())), Math.max(1, Math.abs(a.getY() - b.getY()))));
                }
            }
            labelBounds.clear(); labeledStreams.clear();
            List<PaintSector> arrowsToLabel = new ArrayList<>();
            for (int i = 0; i < area.getRefactor().getSectorsCount(); i++) {
                PaintSector arrow = area.getRefactor().getSector(i);
                if (arrow.getStream() != null && arrow.getText() != null)
                    arrowsToLabel.add(arrow);
            }
            // Place the widest labels first, when the largest gaps are still free.
            arrowsToLabel.sort((a, b) -> Integer.compare(b.getStream().getName().length(), a.getStream().getName().length()));
            for (PaintSector arrow : arrowsToLabel)
                if (arrow.getText() != null)
                    positionLabel(arrow, labelBounds, blockBounds, lineBounds, labeledStreams, arrow.getStream().getName());
            area.getRefactor().saveToFunction();
            MovingArea persisted = new MovingArea(data, activeDiagram);
            persisted.setActiveFunction(activeDiagram);
            if (persisted.getRefactor().getSectorsCount() < entry.getValue().size())
                throw new IllegalStateException("Ramus сохранил не все стрелки диаграммы «"
                        + (parentPath.isEmpty() ? "A-0" : parentPath.replace("::", " → "))
                        + "»: ожидалось " + entry.getValue().size() + ", после перечитывания найдено "
                        + persisted.getRefactor().getSectorsCount());
        }
        progress.accept("Шаг 4/4 — проверяю названия и сохраняю результат");
        // Cross-level branching can recreate native text while the next level
        // is being connected. Finalize text only after the entire graph exists.
        for (var level : orderedLevels) {
            Function active = level.getKey().isEmpty() ? diagram : (Function) functionsByPath.get(level.getKey());
            finalizeLabels(new MovingArea(data, active));
        }
        // Do not report a successful run if the model API silently failed to
        // persist the labels or the drawable IDEF0 geometry.
        for (AiPlan.AiOperation operation : plan.operations()) {
            if (!"create_element".equals(operation.tool())) continue;
            Row function;
            String path = "Работы".equals(operation.qualifier()) ? operation.name()
                    : operation.qualifier().startsWith("Работы::")
                    ? operation.qualifier().substring("Работы::".length()) + "::" + operation.name() : null;
            if (path == null) continue;
            function = functionsByPath.get(path);
            if (function == null || !operation.name().equals(function.getName()))
                throw new IllegalStateException("Ramus не сохранил название блока: " + operation.name());
        }
        return created;
    }

    private static Row findChildByName(DataPlugin data, Function parent, String name) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            Row child = (Row) parent.getChildAt(i);
            if (child instanceof Function && name.equals(child.getName())) return child;
        }
        return null;
    }

    private static void finalizeLabels(MovingArea area) {
        area.setActiveFunction(area.getActiveFunction());
        List<FRectangle> blocks = new ArrayList<>(), lines = new ArrayList<>(), occupied = new ArrayList<>();
        Function active = area.getActiveFunction();
        for (int i = 0; i < active.getChildCount(); i++) blocks.add(((Function) active.getChildAt(i)).getBounds());
        List<PaintSector> arrows = new ArrayList<>();
        for (int i = 0; i < area.getRefactor().getSectorsCount(); i++) {
            PaintSector arrow = area.getRefactor().getSector(i);
            arrow.setText(null);
            arrow.setShowTilda(false);
            arrow.setShowText(false);
            arrows.add(arrow);
            Point[] points = arrow.getPoints();
            for (int j = 1; j < points.length; j++) lines.add(new FRectangle(
                    Math.min(points[j - 1].getX(), points[j].getX()), Math.min(points[j - 1].getY(), points[j].getY()),
                    Math.max(0.1, Math.abs(points[j].getX() - points[j - 1].getX())),
                    Math.max(0.1, Math.abs(points[j].getY() - points[j - 1].getY()))));
        }
        arrows.sort((a, b) -> Integer.compare(b.getStream().getName().length(), a.getStream().getName().length()));
        java.util.Set<String> printed = new java.util.HashSet<>();
        for (PaintSector arrow : arrows) {
            if (printed.contains(arrow.getStream().getName())) continue;
            arrow.setShowText(true);
            arrow.setText(new com.ramussoft.pb.idef.visual.MovingLabel(area));
            arrow.getText().setSector(arrow);
            positionLabel(arrow, occupied, blocks, lines, printed, arrow.getStream().getName());
        }
        area.getRefactor().saveToFunction();
    }

    public static Qualifier functionQualifier(Engine engine) {
        Object last = engine.getPluginProperty("RamusAI", "LastQualifier");
        if (last instanceof Long && engine.getQualifier((Long) last) != null) return engine.getQualifier((Long) last);
        Qualifier named = engine.getQualifierByName("Работы");
        if (named != null && IDEF0Plugin.isFunction(named)) return named;
        for (Qualifier qualifier : engine.getQualifiers()) if (IDEF0Plugin.isFunction(qualifier)) return qualifier;
        return null;
    }

    private static Qualifier selectQualifier(Engine engine, AccessRules rules, AiPlan plan) {
        // Reuse only a compatible hierarchy. Earlier flat drafts and user models
        // stay intact in their original classifier, outside the generated A-0.
        Qualifier selected = functionQualifier(engine);
        if (selected != null && compatible(NDataPluginFactory.getDataPlugin(selected, engine, rules), plan)) {
            engine.setPluginProperty("RamusAI", "LastQualifier", selected.getId());
            return selected;
        }
        String root = plan.operations().stream().filter(op -> "create_element".equals(op.tool())
                && "Работы".equals(op.qualifier())).findFirst().orElseThrow().name();
        String prefix = "Лаб 1 — " + root;
        for (int index = 1; ; index++) {
            String name = index == 1 ? prefix : prefix + " (" + index + ")";
            Qualifier existing = engine.getQualifierByName(name);
            if (existing != null) {
                if (IDEF0Plugin.isFunction(existing) && compatible(NDataPluginFactory.getDataPlugin(existing, engine, rules), plan)) {
                    engine.setPluginProperty("RamusAI", "LastQualifier", existing.getId());
                    return existing;
                }
                continue;
            }
            Qualifier created = engine.createQualifier();
            created.setName(name);
            engine.updateQualifier(created);
            IDEF0Plugin.installFunctionAttributes(created, engine);
            engine.setPluginProperty("RamusAI", "LastQualifier", created.getId());
            return created;
        }
    }

    private static boolean compatible(DataPlugin data, AiPlan plan) {
        java.util.Map<String, java.util.Set<String>> expected = new java.util.HashMap<>();
        java.util.Map<String, java.util.Set<String>> streams = new java.util.HashMap<>();
        for (AiPlan.AiOperation op : plan.operations()) if ("create_element".equals(op.tool()))
            expected.computeIfAbsent(Lab1PlanValidator.scope(op.qualifier(), "Работы"), key -> new java.util.HashSet<>()).add(op.name());
        else streams.computeIfAbsent(Lab1PlanValidator.scope(op.qualifier(), "Потоки"), key -> new java.util.HashSet<>()).add(op.name());
        return compatibleChildren((Function) data.getBaseFunction(), "", expected, streams);
    }

    private static boolean compatibleChildren(Function parent, String path, java.util.Map<String, java.util.Set<String>> expected,
            java.util.Map<String, java.util.Set<String>> streams) {
        if (expected.containsKey(path)) for (Sector existing : parent.getSectors())
            if (existing.getStream() == null || !streams.getOrDefault(path, java.util.Set.of()).contains(existing.getStream().getName())) return false;
        for (int i = 0; i < parent.getChildCount(); i++) {
            Row child = (Row) parent.getChildAt(i);
            if (!expected.getOrDefault(path, java.util.Set.of()).contains(child.getName())) return false;
            String childPath = path.isEmpty() ? child.getName() : path + "::" + child.getName();
            if (child instanceof Function && !compatibleChildren((Function) child, childPath, expected, streams)) return false;
        }
        return true;
    }

    private static Stream findStream(DataPlugin data, String name) {
        for (Object value : data.getChilds(data.getBaseStream(), true))
            if (value instanceof Stream && name.equals(((Stream) value).getName())) return (Stream) value;
        return null;
    }

    private static void createVisibleArrow(MovingArea area, Function source, Function target, Stream stream,
                                           String icom, int sourcePort, int targetPort, int routeIndex,
                                           List<FRectangle> labelBounds, List<FRectangle> blockBounds,
                                           java.util.Set<String> labeledStreams,
                                           String style, String color) {
        FRectangle from = source.getBounds();
        FRectangle to = target.getBounds();
        int targetSide = "control".equalsIgnoreCase(icom)
                ? com.ramussoft.pb.idef.visual.MovingPanel.TOP
                : "mechanism".equalsIgnoreCase(icom)
                ? com.ramussoft.pb.idef.visual.MovingPanel.BOTTOM
                : com.ramussoft.pb.idef.visual.MovingPanel.LEFT;
        double targetX = targetSide == com.ramussoft.pb.idef.visual.MovingPanel.LEFT ? to.getLeft()
                : targetSide == com.ramussoft.pb.idef.visual.MovingPanel.TOP || targetSide == com.ramussoft.pb.idef.visual.MovingPanel.BOTTOM
                ? clamp(to.getLeft() + to.getWidth() / 2 + portOffset(targetPort), to.getLeft() + 20, to.getRight() - 20) : to.getRight();
        double targetY = targetSide == com.ramussoft.pb.idef.visual.MovingPanel.TOP ? to.getTop()
                : targetSide == com.ramussoft.pb.idef.visual.MovingPanel.BOTTOM ? to.getBottom()
                : clamp(to.getTop() + to.getHeight() / 2 + portOffset(targetPort), to.getTop() + 15, to.getBottom() - 15);
        // In IDEF0 every internal output leaves the right side, including
        // feedback to a block positioned above or to the left.
        int sourceSide = com.ramussoft.pb.idef.visual.MovingPanel.RIGHT;
        double sourceX = sourceSide == com.ramussoft.pb.idef.visual.MovingPanel.RIGHT ? from.getRight()
                : sourceSide == com.ramussoft.pb.idef.visual.MovingPanel.LEFT ? from.getLeft()
                : clamp(from.getLeft() + from.getWidth() / 2 + portOffset(sourcePort), from.getLeft() + 20, from.getRight() - 20);
        double sourceY = sourceSide == com.ramussoft.pb.idef.visual.MovingPanel.BOTTOM ? from.getBottom()
                : sourceSide == com.ramussoft.pb.idef.visual.MovingPanel.TOP ? from.getTop()
                : clamp(from.getTop() + from.getHeight() / 2 + portOffset(sourcePort), from.getTop() + 15, from.getBottom() - 15);
        createArrow(area,
                functionPoint(source, sourceX, sourceY, sourceSide, SectorRefactor.TYPE_START),
                functionPoint(target, targetX, targetY, targetSide, SectorRefactor.TYPE_END),
                new Row[]{source, target}, stream, routeIndex, icom, labelBounds, blockBounds, labeledStreams, style, color);
    }

    private static void createBoundaryArrow(MovingArea area, Function function, Stream stream, boolean incoming,
                                            int side, int port, int routeIndex, List<FRectangle> labelPositions,
                                            List<FRectangle> blockBounds, java.util.Set<String> labeledStreams,
                                            String style, String color, String tunnel, String parentStream) {
        FRectangle bounds = function.getBounds();
        double centerX = clamp(bounds.getLeft() + bounds.getWidth() / 2 + portOffset(port), bounds.getLeft() + 20, bounds.getRight() - 20);
        double centerY = clamp(bounds.getTop() + bounds.getHeight() / 2 + portOffset(port), bounds.getTop() + 15, bounds.getBottom() - 15);
        SectorRefactor.PerspectivePoint functionPoint;
        switch (side) {
            case com.ramussoft.pb.idef.visual.MovingPanel.LEFT -> functionPoint = functionPoint(function, bounds.getLeft(), centerY, side, incoming ? SectorRefactor.TYPE_END : SectorRefactor.TYPE_START);
            case com.ramussoft.pb.idef.visual.MovingPanel.RIGHT -> functionPoint = functionPoint(function, bounds.getRight(), centerY, side, incoming ? SectorRefactor.TYPE_END : SectorRefactor.TYPE_START);
            case com.ramussoft.pb.idef.visual.MovingPanel.TOP -> functionPoint = functionPoint(function, centerX, bounds.getTop(), side, incoming ? SectorRefactor.TYPE_END : SectorRefactor.TYPE_START);
            case com.ramussoft.pb.idef.visual.MovingPanel.BOTTOM -> functionPoint = functionPoint(function, centerX, bounds.getBottom(), side, incoming ? SectorRefactor.TYPE_END : SectorRefactor.TYPE_START);
            default -> throw new IllegalArgumentException("Неизвестная сторона IDEF0: " + side);
        }
        double borderX = side == com.ramussoft.pb.idef.visual.MovingPanel.TOP || side == com.ramussoft.pb.idef.visual.MovingPanel.BOTTOM ? centerX
                : side == com.ramussoft.pb.idef.visual.MovingPanel.RIGHT ? area.CLIENT_WIDTH - 10 - portOffset(port) : 10 + portOffset(port);
        double borderY = side == com.ramussoft.pb.idef.visual.MovingPanel.LEFT || side == com.ramussoft.pb.idef.visual.MovingPanel.RIGHT
                ? centerY : (side == com.ramussoft.pb.idef.visual.MovingPanel.TOP ? 10 + portOffset(port) : area.CLIENT_HEIGHT - 40 - portOffset(port));
        String boundaryIcom = switch (side) {
            case com.ramussoft.pb.idef.visual.MovingPanel.LEFT -> "input";
            case com.ramussoft.pb.idef.visual.MovingPanel.TOP -> "control";
            case com.ramussoft.pb.idef.visual.MovingPanel.BOTTOM -> "mechanism";
            default -> "output";
        };
        SectorRefactor.PerspectivePoint boundary = boundaryPoint(borderX, borderY, side,
                incoming ? SectorRefactor.TYPE_START : SectorRefactor.TYPE_END);
        Function active = (Function) function.getParentRow();
        if (tunnel.isEmpty()) {
            String inheritedName = parentStream.isBlank() ? stream.getName() : parentStream;
            boundary.crosspoint = inheritedCrosspoint(area.getDataPlugin(), active, inheritedName, side, incoming);
            if (!parentStream.isBlank() && boundary.crosspoint == null)
                throw new IllegalStateException("Не найден родительский поток ветви «" + stream.getName() + "»: " + parentStream);
        }
        // All branches of one interface enter/leave at the same boundary point.
        // This keeps the inherited resource/control interface visually joined.
        for (int i = 0; i < area.getRefactor().getSectorsCount(); i++) {
            PaintSector peer = area.getRefactor().getSector(i);
            if (peer.getStream() == null || !stream.getName().equals(peer.getStream().getName())) continue;
            var peerBorder = incoming ? peer.getSector().getStart() : peer.getSector().getEnd();
            if (peerBorder.getBorderType() != side) continue;
            Point peerPoint = incoming ? peer.getStartPoint() : peer.getEndPoint();
            boundary.x = peerPoint.getX(); boundary.y = peerPoint.getY();
            boundary.crosspoint = peerBorder.getCrosspoint();
            break;
        }
        createArrow(area,
                incoming ? boundary : functionPoint,
                incoming ? functionPoint : boundary,
                new Row[]{function}, stream, routeIndex, boundaryIcom, labelPositions, blockBounds, labeledStreams, style, color);
        PaintSector created = area.getRefactor().getSector(area.getRefactor().getSectorsCount() - 1);
        if ("soft".equals(tunnel)) {
            com.ramussoft.pb.data.negine.NSectorBorder border = incoming ? created.getSector().getStart() : created.getSector().getEnd();
            border.setTunnelSoft(com.ramussoft.pb.Crosspoint.TUNNEL_SOFT);
        }
    }

    private static com.ramussoft.pb.Crosspoint inheritedCrosspoint(DataPlugin data, Function active,
            String stream, int side, boolean incoming) {
        com.ramussoft.pb.Crosspoint shared = null;
        for (Sector candidate : data.getAllSectors()) {
            if (candidate.getStream() == null || !stream.equals(candidate.getStream().getName())) continue;
            com.ramussoft.pb.data.negine.NSectorBorder endpoint = incoming ? candidate.getEnd() : candidate.getStart();
            if (!active.equals(endpoint.getFunction()) || endpoint.getFunctionType() != side) continue;
            if (shared == null) shared = endpoint.getCrosspoint();
            else if (shared.getGlobalId() != endpoint.getCrosspoint().getGlobalId()) {
                endpoint.setCrosspointA(shared);
                endpoint.commit();
            }
        }
        return shared;
    }

    private static void createArrow(MovingArea area, SectorRefactor.PerspectivePoint start,
                                    SectorRefactor.PerspectivePoint end, Row[] rows, Stream stream, int routeIndex,
                                    String icom, List<FRectangle> labelBounds, List<FRectangle> blockBounds,
                                    java.util.Set<String> labeledStreams,
                                    String style, String requestedColor) {
        int previousSectorCount = area.getRefactor().getSectorsCount();
        area.setState(MovingArea.START_POINT_ADDING);
        area.getRefactor().setPoint(start);
        area.doSector();
        area.getRefactor().setPoint(end);
        area.doSector();
        SectorRefactor refactor = area.getRefactor();
        if (refactor.getSectorsCount() != previousSectorCount + 1)
            throw new IllegalStateException("Ramus не создал геометрию стрелки: " + stream.getName());
        PaintSector paintSector = refactor.getSector(refactor.getSectorsCount() - 1);
        preserveBoundaryPosition(paintSector.getStartPoint(), start);
        preserveBoundaryPosition(paintSector.getEndPoint(), end);
        // Each planned sector has its own explicit stream. Ramus' SIMPLE
        // replacement propagates across crosspoints and can overwrite the
        // parent or a sibling's renamed branch. Keep the native crosspoint,
        // but assign this sector only; every level is populated by the plan.
        ((com.ramussoft.pb.data.AbstractSector) paintSector.getSector()).setThisStream(stream);
        // createMiss also creates empty native stubs on the child's diagram.
        // Initialize those, without replacing already named parent/sibling sectors.
        for (var crosspoint : new com.ramussoft.pb.Crosspoint[]{paintSector.getStart(), paintSector.getEnd()}) {
            if (crosspoint == null) continue;
            for (Sector peer : crosspoint.getOppozite(paintSector.getSector()))
                if (peer.getStream() == null)
                    ((com.ramussoft.pb.data.AbstractSector) peer).setThisStream(stream);
        }
        paintSector.setShowText(true);
        paintSector.createTexts();
        paintSector.setRows(rows);
        paintSector.setColor(arrowColor(icom, requestedColor));
        paintSector.setFont(new Font(Font.DIALOG, Font.PLAIN, 8));
        paintSector.setStroke(arrowStroke(style, icom));
        routeOrthogonally(paintSector, routeIndex, labelBounds, blockBounds, labeledStreams, stream.getName());
        paintSector.saveVisual();
    }

    private static void preserveBoundaryPosition(Point point, SectorRefactor.PerspectivePoint boundary) {
        if (boundary.function != null || boundary.borderType < 0) return;
        // PointBuilder aligns a straight border arrow with its function port.
        // Detach that ordinate before restoring a shared boundary origin.
        if (boundary.borderType == com.ramussoft.pb.idef.visual.MovingPanel.TOP
                || boundary.borderType == com.ramussoft.pb.idef.visual.MovingPanel.BOTTOM) {
            Ordinate x = new Ordinate(Ordinate.TYPE_X); x.setPosition(boundary.x); point.setXOrdinate(x);
        } else {
            Ordinate y = new Ordinate(Ordinate.TYPE_Y); y.setPosition(boundary.y); point.setYOrdinate(y);
        }
    }

    private static Color arrowColor(String icom, String requested) {
        if (requested != null && requested.matches("#[0-9a-fA-F]{6}")) {
            Color selected = Color.decode(requested);
            if (selected.getRed() + selected.getGreen() + selected.getBlue() < 690) return selected;
        }
        if ("control".equalsIgnoreCase(icom)) return new Color(112, 83, 157);
        if ("mechanism".equalsIgnoreCase(icom)) return new Color(35, 126, 111);
        if ("output".equalsIgnoreCase(icom)) return new Color(49, 135, 91);
        if ("input".equalsIgnoreCase(icom)) return new Color(45, 105, 158);
        return new Color(92, 99, 110);
    }

    private static Stroke arrowStroke(String requested, String icom) {
        String style = requested == null ? "" : requested.toLowerCase(java.util.Locale.ROOT);
        if ("feedback".equals(style)) return new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        if (!List.of("solid", "dashed", "dotted", "lightning").contains(style)) style = "solid";
        if ("lightning".equals(style)) {
            WayStroke stroke = new WayStroke(1f);
            stroke.setType(1);
            return stroke;
        }
        if ("dashed".equals(style))
            return new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f,
                    new float[]{7f, 5f}, 0f);
        if ("dotted".equals(style))
            return new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f,
                    new float[]{1.5f, 4f}, 0f);
        return new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    /** Ramus stores arrow bends as points in the sector; use native IDEF0 orthogonal routing. */
    private static void routeOrthogonally(PaintSector sector, int routeIndex, List<FRectangle> labelPositions,
                                          List<FRectangle> blockBounds, java.util.Set<String> labeledStreams,
                                          String streamName) {
        Point start = sector.getStartPoint();
        Point end = sector.getEndPoint();
        double x1 = start.getX(), y1 = start.getY();
        double x2 = end.getX(), y2 = end.getY();
        com.ramussoft.pb.data.SectorBorder startBorder = sector.getSector().getStart();
        com.ramussoft.pb.data.SectorBorder endBorder = sector.getSector().getEnd();
        int startSide = startBorder.getFunction() != null ? startBorder.getFunctionType() : startBorder.getBorderType();
        int endSide = endBorder.getFunction() != null ? endBorder.getFunctionType() : endBorder.getBorderType();
        java.awt.geom.Point2D.Double startStub = stub(x1, y1, startSide, startBorder.getFunction() != null);
        java.awt.geom.Point2D.Double endStub = stub(x2, y2, endSide, endBorder.getFunction() != null);
        List<FRectangle> existingLines = new ArrayList<>();
        SectorRefactor drawing = sector.getMovingArea().getRefactor();
        for (int i = 0; i < drawing.getSectorsCount(); i++) {
            PaintSector other = drawing.getSector(i);
            if (other == sector) continue;
            Point[] line = other.getPoints();
            for (int j = 1; j < line.length; j++) existingLines.add(new FRectangle(
                    Math.min(line[j - 1].getX(), line[j].getX()), Math.min(line[j - 1].getY(), line[j].getY()),
                    Math.max(0.1, Math.abs(line[j].getX() - line[j - 1].getX())),
                    Math.max(0.1, Math.abs(line[j].getY() - line[j - 1].getY()))));
        }
        List<java.awt.geom.Point2D.Double> path = ArrowRouter.route(startStub, endStub, blockBounds, existingLines);
        List<Point> points = new ArrayList<>();
        points.add(start);
        for (int i = 0; i < path.size(); i++) {
            java.awt.geom.Point2D.Double p = path.get(i);
            java.awt.geom.Point2D.Double next = i + 1 < path.size() ? path.get(i + 1) : new java.awt.geom.Point2D.Double(x2, y2);
            addBend(points, p.x, p.y, Math.abs(p.x - next.x) < 0.01 ? Ordinate.TYPE_Y : Ordinate.TYPE_X);
        }
        points.add(end);
        for (int i = points.size() - 2; i > 0; i--) {
            Point a = points.get(i - 1), b = points.get(i), c = points.get(i + 1);
            if ((Math.abs(a.getX() - b.getX()) < 0.01 && Math.abs(b.getX() - c.getX()) < 0.01)
                    || (Math.abs(a.getY() - b.getY()) < 0.01 && Math.abs(b.getY() - c.getY()) < 0.01))
                points.remove(i);
        }
        // Ramus determines whether a segment is horizontal by shared
        // Ordinate identity, not just by equal coordinates.
        for (int i = 1; i < points.size() - 1; i++) {
            Point previous = points.get(i - 1), current = points.get(i);
            if (Math.abs(previous.getY() - current.getY()) < 0.01)
                current.setYOrdinate(previous.getYOrdinate());
            else current.setXOrdinate(previous.getXOrdinate());
        }
        Point beforeEnd = points.get(points.size() - 2);
        if (Math.abs(beforeEnd.getY() - end.getY()) < 0.01)
            beforeEnd.setYOrdinate(end.getYOrdinate());
        else beforeEnd.setXOrdinate(end.getXOrdinate());
        sector.setPoints(points.toArray(new Point[0]));
        positionLabel(sector, labelPositions, blockBounds, List.of(), labeledStreams, streamName);
        sector.savePointOrdinates();
    }

    private static java.awt.geom.Point2D.Double stub(double x, double y, int side, boolean isFunction) {
        int sign = isFunction ? 1 : -1;
        if (side == com.ramussoft.pb.idef.visual.MovingPanel.LEFT) x -= sign * 8;
        else if (side == com.ramussoft.pb.idef.visual.MovingPanel.RIGHT) x += sign * 8;
        else if (side == com.ramussoft.pb.idef.visual.MovingPanel.TOP) y -= sign * 8;
        else if (side == com.ramussoft.pb.idef.visual.MovingPanel.BOTTOM) y += sign * 8;
        return new java.awt.geom.Point2D.Double(x, y);
    }

    private static void positionLabel(PaintSector sector, List<FRectangle> occupied,
                                      List<FRectangle> blocks, List<FRectangle> lines, java.util.Set<String> labeledStreams,
                                      String streamName) {
        if (labeledStreams.contains(streamName)) {
            sector.setShowTilda(false);
            sector.setShowText(false);
            sector.setText(null);
            sector.saveVisual();
            return;
        }
        Point[] points = sector.getPoints();
        double longest = 0, anchorX = Double.NaN, anchorY = Double.NaN;
        for (int i = 1; i < points.length; i++) {
            Point a = points[i - 1], b = points[i];
            double length = Math.abs(b.getX() - a.getX()) + Math.abs(b.getY() - a.getY());
            if (length > 8) {
                if (length > longest) {
                    longest = length;
                    anchorX = (a.getX() + b.getX()) / 2;
                    anchorY = (a.getY() + b.getY()) / 2;
                }
            }
        }
        if (longest > 8) {
            if (sector.isOnMe(anchorX, anchorY) == null) {
                StringBuilder geometry = new StringBuilder(streamName + " anchor=" + anchorX + "," + anchorY);
                for (int i = 0; i < sector.getPinCount(); i++) {
                    PaintSector.Pin pin = sector.getPin(i);
                    geometry.append(" [").append(pin.getStart().getX()).append(",").append(pin.getStart().getY())
                            .append(" -> ").append(pin.getEnd().getX()).append(",").append(pin.getEnd().getY())
                            .append(" type=").append(pin.getType()).append("]");
                }
                throw new IllegalStateException("Не удалось привязать подпись: " + geometry);
            }
            sector.setTildaPos(anchorX, anchorY);
            com.ramussoft.pb.idef.visual.MovingText text = sector.getText();
            if (text == null) throw new IllegalStateException("Ramus не создал подпись стрелки: " + streamName);
            // MovingLabel derives its font from the sector, not from the text
            // object. Ask Ramus to measure the wrapped label before placement.
            sector.setFont(new Font(Font.DIALOG, Font.PLAIN, 7));
            sector.saveVisual();
            text.setBounds(new FRectangle(0, 0, 105, 60));
            text.resetBounds();
            FRectangle measured = text.getBounds();
            double width = Math.max(30, measured.getWidth()), height = Math.max(10, measured.getHeight());
            FRectangle labelRect = placeLabel(anchorX, anchorY, width, height, occupied, blocks, lines,
                    sector.getMovingArea().getDoubleWidth(), sector.getMovingArea().getDoubleHeight());
            text.setBounds(labelRect);
            double dx = Math.max(labelRect.getLeft() - anchorX, Math.max(0, anchorX - labelRect.getRight()));
            double dy = Math.max(labelRect.getTop() - anchorY, Math.max(0, anchorY - labelRect.getBottom()));
            sector.setShowTilda(Math.hypot(dx, dy) > 10);
            labeledStreams.add(streamName);
            occupied.add(labelRect);
        }
    }

    /** Place text independently of its tilda anchor; the native label otherwise
     * starts directly on the line and often covers a block or another label. */
    private static FRectangle placeLabel(double x, double y, double width, double height,
                                         List<FRectangle> occupied, List<FRectangle> blocks, List<FRectangle> lines,
                                         double pageWidth, double pageHeight) {
        double bestScore = Double.POSITIVE_INFINITY;
        FRectangle best = new FRectangle(24, 24, width, height);
        for (double leftCandidate = 12; leftCandidate <= pageWidth - width - 12; leftCandidate += 8) {
            for (double topCandidate = 12; topCandidate <= pageHeight - height - 12; topCandidate += 8) {
                    double left = leftCandidate;
                    double top = topCandidate;
                    // Keep a deliberate page margin instead of pinning labels
                    // to the corners when an anchor is near the canvas edge.
                    left = clamp(left, 12, pageWidth - width - 12);
                    top = clamp(top, 12, pageHeight - height - 12);
                    FRectangle candidate = new FRectangle(left, top, width, height);
                    double score = Math.abs((left + width / 2) - x) + Math.abs((top + height / 2) - y);
                    for (FRectangle block : blocks)
                        if (overlaps(candidate, block, 4)) score += 1000000;
                    for (FRectangle label : occupied)
                        if (overlaps(candidate, label, 4)) score += 1000000;
                    for (FRectangle line : lines)
                        if (overlaps(candidate, line, 2)) score += 10000;
                    if (score < bestScore) {
                        bestScore = score;
                        best = candidate;
                    }
            }
        }
        return best;
    }

    private static boolean overlaps(FRectangle a, FRectangle b, double padding) {
        return a.getLeft() < b.getRight() + padding && a.getRight() + padding > b.getLeft()
                && a.getTop() < b.getBottom() + padding && a.getBottom() + padding > b.getTop();
    }

    private static void addBend(List<Point> points, double x, double y, int outgoingAxis) {
        Point previous = points.get(points.size() - 1);
        if (Math.abs(previous.getX() - x) < 1 && Math.abs(previous.getY() - y) < 1) return;
        Ordinate xOrdinate = new Ordinate(Ordinate.TYPE_X);
        xOrdinate.setPosition(x);
        Ordinate yOrdinate = new Ordinate(Ordinate.TYPE_Y);
        yOrdinate.setPosition(y);
        Point bend = new Point(xOrdinate, yOrdinate);
        bend.setType(outgoingAxis);
        points.add(bend);
    }

    private static int boundarySide(String endpoint) {
        return switch (endpoint) {
            case "@LEFT" -> com.ramussoft.pb.idef.visual.MovingPanel.LEFT;
            case "@RIGHT" -> com.ramussoft.pb.idef.visual.MovingPanel.RIGHT;
            case "@TOP" -> com.ramussoft.pb.idef.visual.MovingPanel.TOP;
            case "@BOTTOM" -> com.ramussoft.pb.idef.visual.MovingPanel.BOTTOM;
            default -> throw new IllegalArgumentException("Неизвестная граница диаграммы: " + endpoint);
        };
    }

    private static SectorRefactor.PerspectivePoint boundaryPoint(double x, double y, int side, int endpointType) {
        SectorRefactor.PerspectivePoint result = new SectorRefactor.PerspectivePoint();
        result.x = x;
        result.y = y;
        result.borderType = side;
        result.type = endpointType;
        return result;
    }

    private static SectorRefactor.PerspectivePoint functionPoint(Function function, double x, double y, int side, int endpointType) {
        Ordinate xOrdinate = new Ordinate(Ordinate.TYPE_X);
        Ordinate yOrdinate = new Ordinate(Ordinate.TYPE_Y);
        xOrdinate.setPosition(x);
        yOrdinate.setPosition(y);
        Point point = new Point(xOrdinate, yOrdinate);
        SectorRefactor.PerspectivePoint result = new SectorRefactor.PerspectivePoint();
        result.point = point;
        result.setFunction(function, side);
        result.type = endpointType;
        return result;
    }

    private static void layout(Function function, int index) {
        function.setBounds(new FRectangle(330, 210, 140, 74));
        function.setFont(new Font(Font.DIALOG, Font.PLAIN, 8));
        function.setBackground(new Color(231, 239, 250));
        function.setForeground(new Color(37, 54, 77));
    }

    private static void layoutStage(Function function, int index, int count, int longestCaption) {
        count = Math.max(1, count);
        int lines = Math.max(1, (int) Math.ceil(longestCaption / 26.0));
        // Every sibling gets the same geometry: per-title widths made the
        // independently centered blocks overlap when captions had different lengths.
        double width = clamp(190 - count * 12, 138, 180);
        double height = clamp(54 + lines * 9, 68, 80);
        double gap = count <= 1 ? 0 : Math.min(96, Math.max(64, (760 - count * width) / (count - 1)));
        double rowWidth = count * width + (count - 1) * gap;
        double startX = Math.max(24, (800 - rowWidth) / 2);
        double top = count == 3 ? 110 + index * 98 : 190 + (index % 2) * 50;
        function.setBounds(new FRectangle(startX + index * (width + gap), top, width, height));
        int ownLines = Math.max(1, (int) Math.ceil(function.getName().length() / 26.0));
        function.setFont(new Font(Font.DIALOG, Font.PLAIN, ownLines > 2 ? 8 : 9));
        function.setBackground(blockColor(function.getName()));
        function.setForeground(new Color(45, 52, 63));
    }

    private static void layoutChild(Function function, int index, int count, int longestCaption) {
        count = Math.max(1, count);
        // Ramus numbers siblings by X position. Keep X increasing in process
        // order, while staggering Y to reserve space for orthogonal links.
        double width = clamp((690 - (count - 1) * 30.0) / count, 96, 150);
        double gap = count == 1 ? 0 : (720 - count * width) / (count - 1);
        double x = (800 - (count * width + (count - 1) * gap)) / 2 + index * (width + gap);
        double y = count == 1 ? 205 : 96 + index * (218.0 / (count - 1));
        int lines = Math.max(1, (int) Math.ceil(function.getName().length() / (width / 4.1)));
        double height = clamp(32 + lines * 10, 60, 78);
        function.setBounds(new FRectangle(x, y, width, height));
        function.setFont(new Font(Font.DIALOG, Font.PLAIN, width < 120 ? 8 : 9));
        function.setBackground(blockColor(function.getName()));
        function.setForeground(new Color(45, 52, 63));
    }

    /** Pale fills encode each block's IDEF0 role; arrow labels use matching hues. */
    private static Color blockColor(String name) {
        String text = normalize(name);
        if (text.contains("достав") || text.contains("разгруз")) return new Color(226, 243, 231);
        if (text.contains("транспорт") || text.contains("технич") || text.contains("охран")
                || text.contains("экспедитор")) return new Color(225, 242, 237);
        if (text.contains("логист") || text.contains("договор") || text.contains("документ")
                || text.contains("тариф") || text.contains("контрол") || text.contains("план"))
            return new Color(239, 233, 249);
        return new Color(227, 239, 250);
    }

    private static int nextPort(java.util.IdentityHashMap<Function, java.util.Map<Integer, Integer>> usage,
                                Function function, int side) {
        java.util.Map<Integer, Integer> sides = usage.computeIfAbsent(function, ignored -> new java.util.HashMap<>());
        int port = sides.getOrDefault(side, 0);
        sides.put(side, port + 1);
        return port;
    }

    private static double portOffset(int index) {
        if (index == 0) return 0;
        int magnitude = (index + 1) / 2;
        return (index % 2 == 1 ? 1 : -1) * magnitude * 14.0;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Row resolveFunction(java.util.Map<String, Row> functions, String requested) {
        Row exact = functions.get(requested);
        if (exact != null) return exact;
        String needle = normalize(requested);
        Row match = null;
        for (java.util.Map.Entry<String, Row> entry : functions.entrySet()) {
            String candidate = normalize(entry.getKey());
            if (candidate.equals(needle) || candidate.contains(needle) || needle.contains(candidate)) {
                if (match != null) return null; // ambiguous reference is unsafe
                match = entry.getValue();
            }
        }
        return match;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{Nd}]+", " ").trim().replaceAll("\\s+", " ");
    }

    public List<Element> createElements(Engine engine, String qualifierName, List<String> names) {
        Qualifier qualifier = engine.getQualifierByName(qualifierName);
        if (qualifier == null) throw new IllegalArgumentException("Unknown qualifier: " + qualifierName);
        Attribute nameAttribute = qualifier.getAttributeForName() < 0 ? null : engine.getAttribute(qualifier.getAttributeForName());
        List<Element> created = new ArrayList<>();
        for (String name : names) {
            Element element = engine.createElement(qualifier.getId());
            if (nameAttribute != null) engine.setAttribute(element, nameAttribute, name);
            created.add(element);
        }
        return created;
    }
}
