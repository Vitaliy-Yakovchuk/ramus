package com.ramussoft.local.ai;

import java.util.List;

public record AiPlan(String summary, List<AiOperation> operations) {
    public record AiOperation(String tool, String qualifier, String name, String source, String target,
                              String icom, String style, String color, String description, String tunnel,
                              String parentStream) {
        public AiOperation(String tool, String qualifier, String name, String source, String target,
                           String icom, String style, String color, String description, String tunnel) {
            this(tool, qualifier, name, source, target, icom, style, color, description, tunnel, "");
        }
        public AiOperation(String tool, String qualifier, String name, String source, String target,
                           String icom, String style, String color) {
            this(tool, qualifier, name, source, target, icom, style, color, "", "");
        }
    }
}
