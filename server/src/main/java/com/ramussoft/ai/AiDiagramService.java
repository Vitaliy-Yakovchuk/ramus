package com.ramussoft.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * HTTP client facade for communicating with the OpenRouter API using OkHttp 3.x
 * (compatible with Java 8) with JSON serialization support.
 */
public class AiDiagramService {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final String apiUrl;
    private final String apiKey;
    private final String referer;
    private final String title;
    private final ObjectMapper objectMapper;

    public AiDiagramService(String apiUrl, String apiKey, String referer, String title,
            long connectTimeoutMillis, long readTimeoutMillis) {
        this(buildClient(connectTimeoutMillis, readTimeoutMillis), apiUrl, apiKey, referer, title, new ObjectMapper());
    }

    public AiDiagramService(OkHttpClient httpClient, String apiUrl, String apiKey, String referer, String title) {
        this(httpClient, apiUrl, apiKey, referer, title, new ObjectMapper());
    }

    public AiDiagramService(OkHttpClient httpClient, String apiUrl, String apiKey, String referer, String title, ObjectMapper objectMapper) {
        if (httpClient == null) {
            throw new IllegalArgumentException("httpClient must not be null");
        }
        if (apiUrl == null) {
            throw new IllegalArgumentException("apiUrl must not be null");
        }
        if (apiKey == null) {
            throw new IllegalArgumentException("apiKey must not be null");
        }
        if (objectMapper == null) {
            throw new IllegalArgumentException("objectMapper must not be null");
        }
        this.httpClient = httpClient;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.referer = referer;
        this.title = title;
        this.objectMapper = objectMapper;
    }

    public AiDiagramService() {
        this(new ObjectMapper());
    }

    public AiDiagramService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = null;
        this.apiUrl = null;
        this.apiKey = null;
        this.referer = null;
        this.title = null;
    }

    private static OkHttpClient buildClient(long connectTimeoutMillis, long readTimeoutMillis) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (connectTimeoutMillis > 0) {
            builder.connectTimeout(connectTimeoutMillis, TimeUnit.MILLISECONDS);
        }
        if (readTimeoutMillis > 0) {
            builder.readTimeout(readTimeoutMillis, TimeUnit.MILLISECONDS);
            builder.writeTimeout(readTimeoutMillis, TimeUnit.MILLISECONDS);
        }
        return builder.build();
    }

    public String requestDiagram(String jsonBody) throws IOException {
        if (httpClient == null || apiUrl == null || apiKey == null) {
            throw new IllegalStateException("HTTP client not configured. Use constructor with HTTP parameters.");
        }
        if (jsonBody == null) {
            throw new IllegalArgumentException("jsonBody must not be null");
        }
        RequestBody requestBody = RequestBody.create(JSON_MEDIA_TYPE, jsonBody);
        Request.Builder requestBuilder = new Request.Builder().url(apiUrl).post(requestBody)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json");
        if (referer != null && referer.length() > 0) {
            requestBuilder.addHeader("HTTP-Referer", referer);
        }
        if (title != null && title.length() > 0) {
            requestBuilder.addHeader("X-Title", title);
        }
        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            ResponseBody responseBody = response.body();
            if (!response.isSuccessful()) {
                StringBuilder message = new StringBuilder();
                message.append("OpenRouter request failed with status ").append(response.code());
                if (responseBody != null) {
                    String errorBody = responseBody.string();
                    if (errorBody.length() > 0) {
                        message.append(": ").append(errorBody);
                    }
                }
                throw new IOException(message.toString());
            }
            if (responseBody == null) {
                return "";
            }
            return responseBody.string();
        }
    }

    public String createChatCompletionRequest(String model, List<OpenRouterMessage> messages) throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        payload.put("messages", messages);
        return objectMapper.writeValueAsString(payload);
    }

    public OpenRouterResponse parseResponse(String responseJson) throws IOException {
        if (responseJson == null) {
            return null;
        }
        return objectMapper.readValue(responseJson, OpenRouterResponse.class);
    }

    public String extractFirstMessageContent(String responseJson) throws IOException {
        OpenRouterResponse response = parseResponse(responseJson);
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return null;
        }
        OpenRouterChoice firstChoice = response.getChoices().get(0);
        if (firstChoice == null || firstChoice.getMessage() == null) {
            return null;
        }
        return firstChoice.getMessage().getContent();
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
