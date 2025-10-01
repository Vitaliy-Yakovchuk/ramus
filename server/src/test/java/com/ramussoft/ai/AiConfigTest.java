package com.ramussoft.ai;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public class AiConfigTest {

    @Test
    public void usesDefaultsWhenNoConfigProvided() {
        AiConfig config = AiConfig.load(new HashMap<String, String>(), new Properties());

        assertEquals("https://openrouter.ai/api/v1/chat/completions", config.getBaseUrl());
        assertEquals("deepseek/deepseek-v3", config.getModel());
        assertEquals("https://ramussoft.com", config.getReferer());
        assertEquals("Ramus AI Diagrammer", config.getTitle());
        assertEquals(60000L, config.getConnectTimeoutMillis());
        assertEquals(60000L, config.getReadTimeoutMillis());
        assertFalse(config.isConfigured());
    }

    @Test
    public void loadsValuesFromPropertiesFile() {
        Properties properties = new Properties();
        properties.setProperty("openrouter.apiKey", "file-key");
        properties.setProperty("openrouter.model", "file-model");
        properties.setProperty("openrouter.baseUrl", "https://example.com/api");
        properties.setProperty("openrouter.referer", "https://example.com");
        properties.setProperty("openrouter.title", "Example Title");
        properties.setProperty("openrouter.connectTimeoutMillis", "45000");
        properties.setProperty("openrouter.readTimeoutMillis", "47000");

        AiConfig config = AiConfig.load(new HashMap<String, String>(), properties);

        assertEquals("file-key", config.getApiKey());
        assertEquals("file-model", config.getModel());
        assertEquals("https://example.com/api", config.getBaseUrl());
        assertEquals("https://example.com", config.getReferer());
        assertEquals("Example Title", config.getTitle());
        assertEquals(45000L, config.getConnectTimeoutMillis());
        assertEquals(47000L, config.getReadTimeoutMillis());
        assertTrue(config.isConfigured());
    }

    @Test
    public void environmentVariablesOverrideProperties() {
        Properties properties = new Properties();
        properties.setProperty("openrouter.apiKey", "file-key");
        properties.setProperty("openrouter.model", "file-model");

        Map<String, String> env = new HashMap<String, String>();
        env.put("OPENROUTER_API_KEY", "env-key");
        env.put("OPENROUTER_MODEL", "env-model");
        env.put("OPENROUTER_CONNECT_TIMEOUT", "90000");

        AiConfig config = AiConfig.load(env, properties);

        assertEquals("env-key", config.getApiKey());
        assertEquals("env-model", config.getModel());
        assertEquals(90000L, config.getConnectTimeoutMillis());
    }
}
