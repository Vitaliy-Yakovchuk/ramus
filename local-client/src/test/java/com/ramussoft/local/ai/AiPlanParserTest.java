package com.ramussoft.local.ai;

import org.junit.Test;
import static org.junit.Assert.*;

public class AiPlanParserTest {
    @Test public void preservesParentStreamReferenceForRenamedBranch() {
        String json = "{\"operations\":[{\"tool\":\"create_stream\",\"qualifier\":\"Потоки::Контекст\","
                + "\"name\":\"Правила сборки и тестирования\",\"source\":\"@TOP\",\"target\":\"Сборка\","
                + "\"icom\":\"control\",\"parentStream\":\"Правила и процедуры\"}]}";
        assertEquals("Правила и процедуры", new AiPlanParser().parse(json).operations().get(0).parentStream());
    }
    @Test public void acceptsReorderedAndEscapedFields() {
        String json = "{\"operations\":[{\"name\":\"Груз \\\"А\\\"\",\"qualifier\":\"Работы\",\"tool\":\"create_element\"}]}";
        assertEquals("Груз \"А\"", new AiPlanParser().parse(json).operations().get(0).name());
    }

    @Test public void rejectsUnknownToolsAndUnfinishedPlan() {
        for (String input : new String[]{"{\"operations\":[{\"tool\":\"delete\",\"qualifier\":\"Работы\",\"name\":\"X\"}]}",
                "{\"operations\":[{\"tool\":\"create_element\",\"qualifier\":\"Работы\",\"name\":\"X\"}"}) {
            try { new AiPlanParser().parse(input); fail("Plan must be rejected"); }
            catch (IllegalArgumentException expected) { }
        }
    }
}
