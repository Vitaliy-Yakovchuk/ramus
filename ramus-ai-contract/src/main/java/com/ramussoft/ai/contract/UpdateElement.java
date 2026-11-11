package com.ramussoft.ai.contract;

import java.util.Map;

public record UpdateElement(String operationId, String targetId, Map<String, String> fields, String reason)
        implements ChangeOperation { }
