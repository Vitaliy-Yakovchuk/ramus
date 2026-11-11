package com.ramussoft.ai.contract;

public record AddConnection(String operationId, String targetId, String sourceId,
                            String destinationId, String reason) implements ChangeOperation { }
