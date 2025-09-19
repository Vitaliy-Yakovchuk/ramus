package com.ramussoft.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.*;

public class AiDiagramServiceTest {

    private MockWebServer server;
    private AiDiagramService service;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        service = new AiDiagramService(new ObjectMapper());
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void sendsJsonBodyAndHeaders() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"result\":\"ok\"}"));

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(500, TimeUnit.MILLISECONDS)
                .readTimeout(500, TimeUnit.MILLISECONDS)
                .writeTimeout(500, TimeUnit.MILLISECONDS)
                .build();

        AiDiagramService httpService = new AiDiagramService(client, server.url("/v1/chat/completions").toString(),
                "api-key", "https://example.com", "RamusX");

        String response = httpService.requestDiagram("{\"prompt\":\"diagram\"}");
        assertEquals("{\"result\":\"ok\"}", response);

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("Bearer api-key", request.getHeader("Authorization"));
        assertEquals("https://example.com", request.getHeader("HTTP-Referer"));
        assertEquals("RamusX", request.getHeader("X-Title"));
        assertEquals("application/json; charset=utf-8", request.getHeader("Content-Type"));
        assertEquals("{\"prompt\":\"diagram\"}", request.getBody().readUtf8());
    }

    @Test
    public void doesNotAddOptionalHeadersWhenMissing() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

        AiDiagramService httpService = new AiDiagramService(new OkHttpClient(),
                server.url("/v1/chat/completions").toString(), "api-key", null, "");

        httpService.requestDiagram("{\"prompt\":\"diagram\"}");

        RecordedRequest request = server.takeRequest();
        assertEquals("Bearer api-key", request.getHeader("Authorization"));
        assertNull(request.getHeader("HTTP-Referer"));
        assertNull(request.getHeader("X-Title"));
    }

    @Test
    public void throwsIOExceptionOnFailure() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"error\":\"bad_request\"}"));

        AiDiagramService httpService = new AiDiagramService(new OkHttpClient(),
                server.url("/v1/chat/completions").toString(), "api-key", null, null);

        try {
            httpService.requestDiagram("{\"prompt\":\"diagram\"}");
            fail("Expected IOException");
        } catch (IOException ex) {
            assertFalse(ex.getMessage().isEmpty());
        }
    }

    @Test
    public void testCreateChatCompletionRequestSerializesMessages() throws Exception {
        OpenRouterMessage system = new OpenRouterMessage("system", "Maintain diagram context");
        OpenRouterMessage user = new OpenRouterMessage("user", "Generate a block diagram");

        String json = service.createChatCompletionRequest("openrouter/test-model", Arrays.asList(system, user));

        ObjectMapper mapper = new ObjectMapper();
        Map<?, ?> parsed = mapper.readValue(json, Map.class);

        assertEquals("openrouter/test-model", parsed.get("model"));

        List<?> messages = (List<?>) parsed.get("messages");
        assertEquals(2, messages.size());

        Map<?, ?> firstMessage = (Map<?, ?>) messages.get(0);
        assertEquals("system", firstMessage.get("role"));
        assertEquals("Maintain diagram context", firstMessage.get("content"));

        Map<?, ?> secondMessage = (Map<?, ?>) messages.get(1);
        assertEquals("user", secondMessage.get("role"));
        assertEquals("Generate a block diagram", secondMessage.get("content"));
    }

    @Test
    public void testParseResponseMapsDtoStructure() throws IOException {
        String responseJson = "{" +
                "\"id\":\"resp_123\"," +
                "\"model\":\"openrouter/test-model\"," +
                "\"choices\":[{" +
                "\"index\":0," +
                "\"message\":{\"role\":\"assistant\",\"content\":\"Diagram output\"}," +
                "\"finish_reason\":\"stop\"" +
                "}]" +
                "}";

        OpenRouterResponse response = service.parseResponse(responseJson);
        assertNotNull(response);
        assertEquals("resp_123", response.getId());
        assertEquals("openrouter/test-model", response.getModel());
        assertNotNull(response.getChoices());
        assertEquals(1, response.getChoices().size());

        OpenRouterChoice choice = response.getChoices().get(0);
        assertEquals(Integer.valueOf(0), choice.getIndex());
        assertEquals("stop", choice.getFinishReason());
        assertNotNull(choice.getMessage());
        assertEquals("assistant", choice.getMessage().getRole());
        assertEquals("Diagram output", choice.getMessage().getContent());
    }

    @Test
    public void testExtractFirstMessageContent() throws IOException {
        String responseJson = "{" +
                "\"choices\":[{" +
                "\"message\":{\"role\":\"assistant\",\"content\":\"Rendered diagram\"}" +
                "}]" +
                "}";

        String content = service.extractFirstMessageContent(responseJson);
        assertEquals("Rendered diagram", content);
    }

    @Test
    public void testExtractFirstMessageContentHandlesMissingData() throws IOException {
        assertNull(service.extractFirstMessageContent("{}"));
        assertNull(service.extractFirstMessageContent(null));
    }
}
