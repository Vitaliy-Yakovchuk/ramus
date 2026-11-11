package com.ramussoft.local.ai;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class Lab1PlanValidatorTest {
    @Test public void acceptsDifferentDomainAndArbitraryOperationOrder() {
        List<AiPlan.AiOperation> other = new ArrayList<>(libraryPlan().operations());
        Collections.reverse(other);
        new Lab1PlanValidator().validate(new AiPlan("Другой вариант", other));
    }

    static AiPlan libraryPlan() {
        Map<String, String> names = new LinkedHashMap<>();
        AiPlan original = TransportCompanyPlan.create();
        for (var a : original.operations()) if ("create_element".equals(a.tool()))
            names.computeIfAbsent(a.name(), key -> "Операция библиотеки " + (names.size() + 1));
        names.put(TransportCompanyPlan.CONTEXT, "Обслужить читателя библиотеки");
        for (var a : original.operations()) if ("create_stream".equals(a.tool()))
            names.computeIfAbsent(a.name(), key -> "Данные библиотеки " + (names.size() + 1));
        List<AiPlan.AiOperation> result = new ArrayList<>();
        for (var a : original.operations()) {
            String[] parts = a.qualifier().split("::");
            for (int i = 1; i < parts.length; i++) parts[i] = names.get(parts[i]);
            result.add(new AiPlan.AiOperation(a.tool(), String.join("::", parts), names.get(a.name()),
                    names.getOrDefault(a.source(), a.source()), names.getOrDefault(a.target(), a.target()),
                    a.icom(), a.style(), a.color(), "Учебная операция другого варианта", a.tunnel()));
        }
        return new AiPlan("Независимая от терминологии структура", result);
    }

    @Test public void rejectsMissingBalanceAndCrossLevelReferences() {
        AiPlan original = TransportCompanyPlan.create();
        List<AiPlan.AiOperation> broken = new ArrayList<>(original.operations());
        broken.removeIf(a -> a.qualifier().equals("Потоки::" + TransportCompanyPlan.CONTEXT)
                && "Заявка клиента".equals(a.name()));
        rejected(broken, "баланс");
        broken = new ArrayList<>(original.operations());
        broken.add(new AiPlan.AiOperation("create_stream", "Потоки::" + TransportCompanyPlan.CONTEXT,
                "Плохая связь", "Заключить договор", "Погрузить груз", "input", "", ""));
        rejected(broken, "межуровневая");
    }

    @Test public void rejectsFlattenedOrMissingSecondLevel() {
        List<AiPlan.AiOperation> broken = new ArrayList<>(TransportCompanyPlan.create().operations());
        broken.removeIf(a -> a.qualifier().split("::").length > 2);
        rejected(broken, "второго уровня");
        try { new RamusToolExecutor().execute(null, null, new AiPlan("bad", broken)); fail("No preflight check"); }
        catch (IllegalArgumentException expected) { }
    }

    private void rejected(List<AiPlan.AiOperation> operations, String expected) {
        try { new Lab1PlanValidator().validate(new AiPlan("bad", operations)); fail("Accepted invalid model"); }
        catch (IllegalArgumentException error) { assertTrue(error.getMessage(), error.getMessage().contains(expected)); }
    }
}
