package com.ramussoft.local.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Local OpenAI-compatible API, including Ollama's /v1 endpoint. */
final class LocalLlmClient {
    private final URI endpoint;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    LocalLlmClient(String base) {
        URI uri = URI.create(base.trim());
        if (!"http".equalsIgnoreCase(uri.getScheme()) || uri.getUserInfo() != null
                || uri.getQuery() != null || uri.getFragment() != null
                || !("127.0.0.1".equals(uri.getHost()) || "localhost".equalsIgnoreCase(uri.getHost())))
            throw new IllegalArgumentException("Локальная LLM должна использовать http://127.0.0.1 или http://localhost");
        String path = uri.getPath() == null ? "" : uri.getPath().replaceAll("/+$", "");
        if (path.isEmpty()) path = "/v1";
        if (!"/v1".equals(path)) throw new IllegalArgumentException("Укажите адрес локального API с путём /v1");
        endpoint = URI.create(uri.getScheme() + "://" + uri.getRawAuthority() + path + "/chat/completions");
    }

    String generatePlan(String prompt, ModelSnapshot snapshot, String model) throws Exception {
        if (model == null || model.isBlank()) throw new IllegalArgumentException("Укажите название установленной локальной модели");
        String system = Lab1Specification.SYSTEM_PROMPT;
        String body = "{\"model\":\"" + DeepSeekClient.esc(model) + "\",\"stream\":false,\"messages\":[{\"role\":\"system\",\"content\":\""
                + DeepSeekClient.esc(system) + "\"},{\"role\":\"user\",\"content\":\""
                + DeepSeekClient.esc(prompt + "\n\nСостояние проекта:\n" + snapshot.content()) + "\"}]}";
        HttpRequest request = HttpRequest.newBuilder(endpoint).timeout(Duration.ofMinutes(3))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2)
            throw new IllegalStateException("Локальная LLM вернула HTTP " + response.statusCode() + ". Проверьте адрес и модель.");
        return DeepSeekClient.content(response.body());
    }
}
