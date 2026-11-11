package com.ramussoft.ai.agent;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Offline baseline provider. It is deterministic and never invents model identifiers. */
public final class RuleBasedAnalysisProvider implements AnalysisProvider {
    private static final Pattern ELEMENT = Pattern.compile("^element\\s+(\\d+)\\s+(\\d+)\\s*(.*)$", Pattern.MULTILINE);

    @Override public AnalysisResult analyze(String prompt, String snapshot) {
        ArrayList<AnalysisResult.Finding> findings = new ArrayList<>();
        Matcher matcher = ELEMENT.matcher(snapshot == null ? "" : snapshot);
        while (matcher.find()) {
            String name = matcher.group(3).replace("\\n", "").trim();
            if (name.isEmpty()) findings.add(new AnalysisResult.Finding("warning", matcher.group(1),
                    "Element has an empty name; propose a human-reviewed name."));
        }
        String message = findings.isEmpty() ? "No baseline issues found." :
                "Baseline checks completed; review findings before creating a proposal.";
        return new AnalysisResult(findings, message);
    }
}
