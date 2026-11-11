package com.ramussoft.ai.agent;

public interface AnalysisProvider {
    AnalysisResult analyze(String prompt, String snapshot);
}
