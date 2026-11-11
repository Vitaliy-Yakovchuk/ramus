package com.ramussoft.ai.agent;

import java.util.List;

public record AnalysisResult(List<Finding> findings, String providerMessage) {
    public AnalysisResult { findings = List.copyOf(findings); }
    public record Finding(String severity, String elementId, String message) { }
}
