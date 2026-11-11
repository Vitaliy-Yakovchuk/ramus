package com.ramussoft.local.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Read-only client for the separate localhost AI agent. */
public final class RamusAiClient {
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2)).build();
    private final URI baseUri;

    public RamusAiClient() { this(URI.create("http://127.0.0.1:37841")); }

    public RamusAiClient(URI baseUri) {
        if (!"http".equalsIgnoreCase(baseUri.getScheme())
                || !"127.0.0.1".equals(baseUri.getHost()))
            throw new IllegalArgumentException("AI agent must use localhost HTTP");
        this.baseUri = baseUri;
    }

    public String health() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("/v1/health"))
                .timeout(Duration.ofSeconds(3)).GET().build();
        return http.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public String analyze(String prompt, ModelSnapshot snapshot) throws IOException, InterruptedException {
        String body = "{\"prompt\":\"" + json(prompt) + "\",\"contextHash\":\""
                + snapshot.sha256() + "\",\"snapshot\":\"" + json(snapshot.content()) + "\"}";
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("/v1/analyze"))
                .timeout(Duration.ofSeconds(20)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        return http.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }
}
