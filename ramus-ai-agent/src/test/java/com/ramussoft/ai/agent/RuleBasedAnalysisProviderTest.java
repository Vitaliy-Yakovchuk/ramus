package com.ramussoft.ai.agent;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class RuleBasedAnalysisProviderTest {
    @Test public void findsEmptyElementNames() {
        AnalysisResult result = new RuleBasedAnalysisProvider().analyze("check", "element 42 7\n");
        assertEquals(1, result.findings().size());
        assertEquals("42", result.findings().get(0).elementId());
    }
}
