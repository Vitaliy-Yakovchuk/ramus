package com.ramussoft.local.ai;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class LocalLlmClientTest {
    @Test public void sendsRequestAndReadsOpenAiCompatibleResponse() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(request.contains("llama3.2"));
            assertTrue(request.contains("перевозка"));
            assertTrue(request.contains("A-0"));
            assertTrue(request.contains("декомпозиция второго уровня"));
            byte[] response = ("{\"choices\": [{\"message\": {\"content\": \""
                    + DeepSeekClient.esc("{\"operations\":[{\"tool\":\"create_element\",\"qualifier\":\"Работы\",\"name\":\"Доставка\"}]}")
                    + "\"}}]}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (var out = exchange.getResponseBody()) { out.write(response); }
        });
        server.start();
        try {
            LocalLlmClient client = new LocalLlmClient("http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
            String answer = client.generatePlan("перевозка", new ModelSnapshot("модель", "hash", 0, 0), "llama3.2");
            assertEquals("Доставка", new AiPlanParser().parse(answer).operations().get(0).name());
        } finally { server.stop(0); }
    }

    @Test public void rejectsNonLocalOrCredentialedUrls() {
        for (String endpoint : new String[]{"http://example.com/v1", "https://127.0.0.1/v1", "http://user@127.0.0.1/v1"}) {
            try { new LocalLlmClient(endpoint); fail("Accepted " + endpoint); }
            catch (IllegalArgumentException expected) { }
        }
    }
}
