package com.ramussoft.ai.contract;

public record RemoveElement(String operationId, String targetId, String reason)
        implements ChangeOperation { }
