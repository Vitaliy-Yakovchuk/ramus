package com.ramussoft.local.ai;

import java.util.ArrayList;
import java.util.List;
import com.ramussoft.local.ai.AiPlan.AiOperation;

/** Explicit worked example; never silently replaces an arbitrary AI response. */
public final class TransportCompanyPlan {
    public static final String CONTEXT = "Оказать услуги по перевозке грузов";
    private static final String RULES = "Правила перевозки";
    private static final String RESOURCES = "Персонал и инфраструктура";
    private final List<AiOperation> operations = new ArrayList<>();

    public static AiPlan create() { return new TransportCompanyPlan().build(); }

    private AiPlan build() {
        String a1 = "Заключить договор", a2 = "Подготовить перевозку", a3 = "Доставить груз";
        function("", CONTEXT, "Перевозка груза от заявки клиента до передачи получателю в срок с обеспечением сохранности. Охрана и экспедитор — по договору.");
        function(CONTEXT, a1, "Уточнить характеристики, количество груза и адрес; согласовать тариф, срок, охрану и экспедитора; оформить договор и документы.");
        function(CONTEXT, a2, "Принять груз, при необходимости подготовить и хранить; спланировать логистику; предоставить технически исправный транспорт и погрузить груз.");
        function(CONTEXT, a3, "Перевезти груз, контролировать перемещение, обеспечить заказанные дополнительные услуги, разгрузить и подтвердить доставку.");
        String p1 = CONTEXT + "::" + a1, p2 = CONTEXT + "::" + a2, p3 = CONTEXT + "::" + a3;
        String[] contract = {"Уточнить параметры заказа", "Согласовать тариф и срок", "Согласовать дополнительные услуги", "Оформить договор и документы"};
        String[] preparation = {"Спланировать логистику", "Принять груз клиента", "Подготовить и хранить груз", "Предоставить исправный транспорт", "Погрузить груз"};
        String[] delivery = {"Перевезти груз", "Контролировать перемещение", "Разгрузить и передать груз", "Подтвердить исполнение договора"};
        functions(p1, contract, new String[]{
            "Зафиксировать характеристики и количество груза, пункт доставки и пожелания клиента.",
            "Согласовать тариф, срок доставки и условия сохранности груза.",
            "Зафиксировать необходимость охраны груза и/или сопровождения экспедитора. Услуги назначаются только по заказу клиента.",
            "Заключить договор с условиями перевозки и подготовить транспортные документы."});
        functions(p2, preparation, new String[]{
            "Составить маршрут, график подачи транспорта и план перевозки; корректировать план при отклонениях.",
            "Принять груз от клиента и сверить количество и характеристики с договором.",
            "При необходимости упаковать и подготовить груз. Организовать хранение до отправки; если оно не требуется, направить груз на погрузку.",
            "Выбрать подходящее транспортное средство, проверить техническое состояние и при необходимости провести техническое обслуживание.",
            "Погрузить подготовленный груз в предоставленный транспорт с соблюдением правил сохранности."});
        functions(p3, delivery, new String[]{
            "Доставить груз по адресу в срок, применяя охрану и сопровождение экспедитора только при наличии соответствующего условия договора.",
            "Отслеживать местоположение, состояние груза и соблюдение срока; передавать отклонения логисту.",
            "Выполнить разгрузку и передать груз получателю с проверкой количества и сохранности.",
            "Получить подтверждение приёмки груза и оформить закрывающие документы."});

        incoming("", "Заявка клиента", "@LEFT", CONTEXT);
        incoming("", "Груз клиента", "@LEFT", CONTEXT);
        incoming("", RULES, "@TOP", CONTEXT);
        incoming("", RESOURCES, "@BOTTOM", CONTEXT);
        outgoing("", "Груз у получателя", CONTEXT);
        outgoing("", "Подтверждение доставки", CONTEXT);

        incoming(CONTEXT, "Заявка клиента", "@LEFT", a1);
        incoming(CONTEXT, "Груз клиента", "@LEFT", a2);
        shared(CONTEXT, RULES, "@TOP", new String[]{a1, a2, a3});
        shared(CONTEXT, RESOURCES, "@BOTTOM", new String[]{a1, a2, a3});
        arrow(CONTEXT, "Договор", a1, a2, "control");
        arrow(CONTEXT, "Договор", a1, a3, "control");
        arrow(CONTEXT, "Груз к отправке", a2, a3, "input");
        arrow(CONTEXT, "План перевозки", a2, a3, "control");
        feedback(CONTEXT, "Отклонения перевозки", a3, a2);
        outgoing(CONTEXT, "Груз у получателя", a3);
        outgoing(CONTEXT, "Подтверждение доставки", a3);

        incoming(p1, "Заявка клиента", "@LEFT", contract[0]);
        shared(p1, RULES, "@TOP", contract);
        shared(p1, RESOURCES, "@BOTTOM", contract);
        arrow(p1, "Параметры заказа", contract[0], contract[1], "input");
        arrow(p1, "Согласованные условия", contract[1], contract[2], "input");
        arrow(p1, "Состав услуг", contract[2], contract[3], "input");
        outgoing(p1, "Договор", contract[3]);

        incoming(p2, "Груз клиента", "@LEFT", preparation[1]);
        shared(p2, "Договор", "@TOP", preparation);
        incoming(p2, RULES, "@TOP", preparation[3]);
        shared(p2, RESOURCES, "@BOTTOM", preparation);
        incoming(p2, "Отклонения перевозки", "@TOP", preparation[0]);
        arrow(p2, "Принятый груз", preparation[1], preparation[2], "input");
        arrow(p2, "Подготовленный груз", preparation[2], preparation[4], "input");
        arrow(p2, "Требования к транспорту", preparation[0], preparation[3], "control");
        arrow(p2, "Исправное ТС", preparation[3], preparation[4], "mechanism");
        arrow(p2, "График погрузки", preparation[0], preparation[4], "control");
        outgoing(p2, "Груз к отправке", preparation[4]);
        outgoing(p2, "План перевозки", preparation[0]);
        operations.add(new AiOperation("create_stream", streams(p2), "Заявка на ТО", preparation[3], "@RIGHT",
                "output", "solid", "", "Локальная заявка при выявлении неисправности. Деталь технического обслуживания не выводится на A0; круглый туннель выбран явно.", "soft"));

        incoming(p3, "Груз к отправке", "@LEFT", delivery[0]);
        shared(p3, "Договор", "@TOP", delivery);
        incoming(p3, "План перевозки", "@TOP", delivery[0]);
        incoming(p3, RULES, "@TOP", delivery[0]);
        shared(p3, RESOURCES, "@BOTTOM", delivery);
        arrow(p3, "Статус груза", delivery[0], delivery[1], "input");
        arrow(p3, "Доставленный груз", delivery[0], delivery[2], "input");
        feedback(p3, "Уточнённый график", delivery[1], delivery[0]);
        arrow(p3, "Акт приёмки", delivery[2], delivery[3], "input");
        outgoing(p3, "Отклонения перевозки", delivery[1]);
        outgoing(p3, "Груз у получателя", delivery[2]);
        outgoing(p3, "Подтверждение доставки", delivery[3]);
        AiPlan plan = new AiPlan("Лабораторная №1: транспортная компания — A-0, A0 (3 этапа), A1 (4 работы), A2 (5 работ), A3 (4 работы)", List.copyOf(operations));
        new Lab1PlanValidator().validate(plan);
        return plan;
    }

    private void function(String parent, String name, String description) {
        operations.add(new AiOperation("create_element", parent.isEmpty() ? "Работы" : "Работы::" + parent,
                name, "", "", "", "", "", description, ""));
    }
    private void functions(String parent, String[] names, String[] descriptions) {
        for (int i = 0; i < names.length; i++) function(parent, names[i], descriptions[i]);
    }
    private void shared(String path, String name, String side, String[] targets) {
        for (String target : targets) incoming(path, name, side, target);
    }
    private void incoming(String path, String name, String side, String target) {
        String icom = "@LEFT".equals(side) ? "input" : "@TOP".equals(side) ? "control" : "mechanism";
        arrow(path, name, side, target, icom);
    }
    private void outgoing(String path, String name, String source) { arrow(path, name, source, "@RIGHT", "output"); }
    private void arrow(String path, String name, String source, String target, String icom) {
        operations.add(new AiOperation("create_stream", streams(path), name, source, target, icom, "solid", ""));
    }
    private void feedback(String path, String name, String source, String target) {
        operations.add(new AiOperation("create_stream", streams(path), name, source, target, "control", "feedback", "#70539D"));
    }
    private static String streams(String path) { return path.isEmpty() ? "Потоки" : "Потоки::" + path; }
}
