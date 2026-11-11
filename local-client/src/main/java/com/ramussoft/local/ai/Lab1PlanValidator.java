package com.ramussoft.local.ai;

import java.util.*;
import com.ramussoft.local.ai.AiPlan.AiOperation;

/** Checks the whole plan before the first Engine write; independent of business domain. */
public final class Lab1PlanValidator {
    public void validate(AiPlan plan) {
        if (plan == null || plan.operations().size() > 200) fail("Пустой или слишком большой план");
        Map<String, List<AiOperation>> levels = new LinkedHashMap<>();
        Set<String> paths = new HashSet<>();
        Map<String, List<AiOperation>> arrows = new LinkedHashMap<>();
        Set<String> uniqueArrows = new HashSet<>();
        for (AiOperation op : plan.operations()) {
            if (op.name().isBlank() || op.name().contains("::") || op.name().startsWith("@"))
                fail("Некорректное имя: " + op.name());
            if ("create_element".equals(op.tool())) {
                String parent = scope(op.qualifier(), "Работы");
                String path = parent.isEmpty() ? op.name() : parent + "::" + op.name();
                if (!paths.add(path)) fail("Повтор блока: " + path);
                if (path.split("::").length > 3) fail("Лабораторная №1: допускаются A-0, A0 и второй уровень");
                levels.computeIfAbsent(parent, key -> new ArrayList<>()).add(op);
            } else if ("create_stream".equals(op.tool())) {
                String parent = scope(op.qualifier(), "Потоки");
                if (!op.parentStream().isBlank() && parent.isEmpty()) fail("У A-0 нет родительского потока: " + op.name());
                String key = parent + "\n" + op.name() + "\n" + op.source() + "\n" + op.target() + "\n" + op.icom();
                if (!uniqueArrows.add(key)) fail("Повтор стрелки: " + op.name());
                arrows.computeIfAbsent(parent, ignored -> new ArrayList<>()).add(op);
            } else fail("Недопустимый инструмент: " + op.tool());
        }
        List<AiOperation> context = levels.getOrDefault("", List.of());
        if (context.size() != 1) fail("A-0 должна содержать ровно один контекстный блок");
        String root = context.get(0).name();
        if (!levels.containsKey(root)) fail("Отсутствует декомпозиция A0");
        if (levels.keySet().stream().noneMatch(p -> p.startsWith(root + "::")))
            fail("Отсутствует декомпозиция второго уровня");
        for (String path : paths) {
            int split = path.lastIndexOf("::");
            if (split >= 0 && !paths.contains(path.substring(0, split))) fail("Отсутствует родитель: " + path);
        }
        for (var entry : levels.entrySet()) {
            String parent = entry.getKey();
            if (!parent.isEmpty() && (entry.getValue().size() < 3 || entry.getValue().size() > 6))
                fail("На диаграмме нужно 3–6 работ: " + parent);
            Set<String> names = new HashSet<>();
            for (AiOperation op : entry.getValue()) names.add(op.name());
            List<AiOperation> streams = arrows.getOrDefault(parent, List.of());
            for (AiOperation op : streams) validateArrow(op, names);
            for (String name : names) {
                boolean out = streams.stream().anyMatch(a -> name.equals(a.source()));
                boolean control = streams.stream().anyMatch(a -> name.equals(a.target()) && "control".equals(a.icom()));
                boolean mechanism = streams.stream().anyMatch(a -> name.equals(a.target()) && "mechanism".equals(a.icom()));
                if (!out || !control || !mechanism)
                    fail("У блока «" + name + "» должны быть выход, управление и механизм");
            }
            if (!parent.isEmpty()) validateInterfaces(parent, arrows);
        }
        for (String scope : arrows.keySet()) if (!levels.containsKey(scope)) fail("Неизвестная диаграмма стрелок: " + scope);
        List<AiOperation> top = levels.get(root);
        Map<String, Integer> order = new HashMap<>();
        for (int i = 0; i < top.size(); i++) order.put(top.get(i).name(), i);
        boolean feedback = arrows.getOrDefault(root, List.of()).stream().anyMatch(a ->
                "control".equals(a.icom()) && order.containsKey(a.source()) && order.containsKey(a.target())
                        && order.get(a.source()) > order.get(a.target()));
        if (!feedback) fail("A0 должна содержать осмысленную обратную связь по управлению");
    }

    private static void validateArrow(AiOperation op, Set<String> names) {
        boolean in = op.source().startsWith("@"), out = op.target().startsWith("@");
        if (in && out || !in && !names.contains(op.source()) || !out && !names.contains(op.target()))
            fail("Неизвестная работа или межуровневая стрелка: " + op.name());
        if (op.source().equals(op.target())) fail("Стрелка замыкается на тот же блок: " + op.name());
        if (in) {
            String type = switch (op.source()) {
                case "@LEFT" -> "input"; case "@TOP" -> "control"; case "@BOTTOM" -> "mechanism";
                default -> "";
            };
            if (type.isEmpty() || !type.equals(op.icom())) fail("Неверная сторона входящей стрелки: " + op.name());
        } else if (out) {
            if (!"@RIGHT".equals(op.target()) || !"output".equals(op.icom())) fail("Выход должен идти справа: " + op.name());
        } else if (!Set.of("input", "control", "mechanism").contains(op.icom())) fail("Неизвестный ICOM: " + op.name());
        if (!Set.of("", "soft").contains(op.tunnel())) fail("Недопустимый туннель: " + op.name());
        if (!op.tunnel().isEmpty() && (!(in || out) || op.description().isBlank())) fail("Для локального туннеля нужно объяснение: " + op.name());
        if (!op.parentStream().isBlank() && (!(in || out) || !op.tunnel().isEmpty()))
            fail("parentStream допустим только для унаследованной граничной ветви: " + op.name());
    }

    private static void validateInterfaces(String path, Map<String, List<AiOperation>> arrows) {
        int split = path.lastIndexOf("::");
        String upper = split < 0 ? "" : path.substring(0, split);
        String name = split < 0 ? path : path.substring(split + 2);
        Set<String> expected = new HashSet<>(), actual = new HashSet<>();
        for (AiOperation a : arrows.getOrDefault(upper, List.of())) {
            if (a.target().equals(name)) expected.add(a.name() + "|" + a.icom());
            if (a.source().equals(name)) expected.add(a.name() + "|output");
        }
        for (AiOperation a : arrows.getOrDefault(path, List.of())) {
            if (!a.source().startsWith("@") && !a.target().startsWith("@")) continue;
            // A branch may have a more specific visible label while inheriting
            // the interface of the immediately enclosing parent function.
            String inheritedName = a.parentStream().isBlank() ? a.name() : a.parentStream();
            String signature = inheritedName + "|" + a.icom();
            if (a.tunnel().isEmpty()) actual.add(signature);
            else if (expected.contains(signature)) fail("Унаследованную стрелку нельзя скрывать туннелем: " + a.name());
        }
        if (!expected.equals(actual)) {
            Set<String> missing = new TreeSet<>(expected); missing.removeAll(actual);
            Set<String> extra = new TreeSet<>(actual); extra.removeAll(expected);
            fail("Нарушен баланс «" + path + "»: отсутствуют " + missing + "; лишние " + extra
                    + ". Для переименованной ветви укажите parentStream — точное имя потока у родителя; тип ICOM должен совпадать.");
        }
    }

    static String scope(String qualifier, String prefix) {
        if (qualifier.equals(prefix)) return "";
        if (qualifier.startsWith(prefix + "::") && qualifier.length() > prefix.length() + 2)
            return qualifier.substring(prefix.length() + 2);
        throw new IllegalArgumentException("Неверная область: " + qualifier);
    }

    private static void fail(String message) { throw new IllegalArgumentException("Лабораторная №1: " + message); }
}
