package com.ramussoft.ai.contract;

/** Typed, reviewable model change. Raw YAML and shell commands are intentionally absent. */
public sealed interface ChangeOperation permits UpdateElement, AddConnection, RemoveElement {
    String operationId();
    String targetId();
    String reason();
}
