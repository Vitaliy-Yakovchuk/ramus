package com.ramussoft.ai;

import java.util.concurrent.TimeUnit;

import com.ramussoft.common.AbstractPlugin;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;

import okhttp3.OkHttpClient;

/**
 * Plugin that exposes {@link AiDiagramService} to the engine runtime.
 */
public class AiDiagramPlugin extends AbstractPlugin {

    public static final String PLUGIN_NAME = "AiDiagram";

    private AiConfig config;
    private AiDiagramService service;

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public void init(Engine engine, AccessRules rules) {
        super.init(engine, rules);
        config = AiConfig.load();
        if (config.isConfigured()) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            long connectTimeout = config.getConnectTimeoutMillis();
            if (connectTimeout > 0) {
                builder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
            }
            long readTimeout = config.getReadTimeoutMillis();
            if (readTimeout > 0) {
                builder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
                builder.writeTimeout(readTimeout, TimeUnit.MILLISECONDS);
            }
            OkHttpClient client = builder.build();
            service = new AiDiagramService(client, config.getBaseUrl(), config.getApiKey(),
                    config.getReferer(), config.getTitle());
        } else {
            service = new AiDiagramService();
            System.err.println("AiDiagramPlugin: OpenRouter API key is not configured. Service will be disabled.");
        }
        engine.setPluginProperty(PLUGIN_NAME, "This", this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class getFunctionalInterface() {
        return AiDiagramApi.class;
    }

    @Override
    public Object createFunctionalInterfaceObject(Engine engine, IEngine iEngine) {
        return (AiDiagramApi) service;
    }

    public AiDiagramService getService() {
        return service;
    }

    public AiConfig getConfig() {
        return config;
    }

    public boolean isConfigured() {
        return config != null && config.isConfigured();
    }

    public static AiDiagramPlugin getPlugin(Engine engine) {
        if (engine == null) {
            return null;
        }
        Object value = engine.getPluginProperty(PLUGIN_NAME, "This");
        if (value instanceof AiDiagramPlugin) {
            return (AiDiagramPlugin) value;
        }
        return null;
    }

    public static AiDiagramService getService(Engine engine) {
        AiDiagramPlugin plugin = getPlugin(engine);
        if (plugin == null) {
            return null;
        }
        return plugin.getService();
    }
}
