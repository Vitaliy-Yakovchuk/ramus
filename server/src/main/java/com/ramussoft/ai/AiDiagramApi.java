package com.ramussoft.ai;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.List;

/**
 * Public functional interface for AI diagram operations exposed via the plugin system.
 */
public interface AiDiagramApi {

    String requestDiagram(String jsonBody) throws IOException;

    String createChatCompletionRequest(String model, List<OpenRouterMessage> messages) throws JsonProcessingException;

    OpenRouterResponse parseResponse(String responseJson) throws IOException;

    String extractFirstMessageContent(String responseJson) throws IOException;

    AiDiagramDefinition parseDiagramDefinition(String responseJson) throws IOException;
}


