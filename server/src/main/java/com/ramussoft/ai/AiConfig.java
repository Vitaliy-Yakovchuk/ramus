package com.ramussoft.ai;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration holder for the AI diagram service. Values can be provided via
 * environment variables or an external configuration file (conf/ramus-ai.conf).
 */
public class AiConfig {

    private static final String DEFAULT_BASE_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String DEFAULT_MODEL = "deepseek/deepseek-v3";
    private static final String DEFAULT_REFERER = "https://ramussoft.com";
    private static final String DEFAULT_TITLE = "Ramus AI Diagrammer";
    private static final long DEFAULT_TIMEOUT = 60000L;

    private static final String ENV_API_KEY = "OPENROUTER_API_KEY";
    private static final String ENV_MODEL = "OPENROUTER_MODEL";
    private static final String ENV_BASE_URL = "OPENROUTER_BASE_URL";
    private static final String ENV_REFERER = "OPENROUTER_REFERER";
    private static final String ENV_TITLE = "OPENROUTER_TITLE";
    private static final String ENV_CONNECT_TIMEOUT = "OPENROUTER_CONNECT_TIMEOUT";
    private static final String ENV_READ_TIMEOUT = "OPENROUTER_READ_TIMEOUT";

    private static final String PROP_API_KEY = "openrouter.apiKey";
    private static final String PROP_MODEL = "openrouter.model";
    private static final String PROP_BASE_URL = "openrouter.baseUrl";
    private static final String PROP_REFERER = "openrouter.referer";
    private static final String PROP_TITLE = "openrouter.title";
    private static final String PROP_CONNECT_TIMEOUT = "openrouter.connectTimeoutMillis";
    private static final String PROP_READ_TIMEOUT = "openrouter.readTimeoutMillis";

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final String referer;
    private final String title;
    private final long connectTimeoutMillis;
    private final long readTimeoutMillis;

    private AiConfig(Builder builder) {
        this.apiKey = builder.apiKey;
        this.baseUrl = builder.baseUrl;
        this.model = builder.model;
        this.referer = builder.referer;
        this.title = builder.title;
        this.connectTimeoutMillis = builder.connectTimeoutMillis;
        this.readTimeoutMillis = builder.readTimeoutMillis;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getModel() {
        return model;
    }

    public String getReferer() {
        return referer;
    }

    public String getTitle() {
        return title;
    }

    public long getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public long getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public boolean isConfigured() {
        return apiKey != null && apiKey.length() > 0;
    }

    public static AiConfig load() {
        Properties fileProperties = loadPropertiesFile();
        return load(System.getenv(), fileProperties);
    }

    static AiConfig load(Map<String, String> environment, Properties fileProperties) {
        Map<String, String> env = environment;
        if (env == null) {
            env = Collections.emptyMap();
        }
        Properties props = fileProperties;
        if (props == null) {
            props = new Properties();
        }

        Builder builder = new Builder();
        builder.baseUrl(DEFAULT_BASE_URL);
        builder.model(DEFAULT_MODEL);
        builder.referer(DEFAULT_REFERER);
        builder.title(DEFAULT_TITLE);
        builder.connectTimeoutMillis(DEFAULT_TIMEOUT);
        builder.readTimeoutMillis(DEFAULT_TIMEOUT);

        applyProperty(builder, props.getProperty(PROP_BASE_URL), ValueType.STRING, PropertyTarget.BASE_URL);
        applyProperty(builder, props.getProperty(PROP_MODEL), ValueType.STRING, PropertyTarget.MODEL);
        applyProperty(builder, props.getProperty(PROP_API_KEY), ValueType.STRING, PropertyTarget.API_KEY);
        applyProperty(builder, props.getProperty(PROP_REFERER), ValueType.STRING, PropertyTarget.REFERER);
        applyProperty(builder, props.getProperty(PROP_TITLE), ValueType.STRING, PropertyTarget.TITLE);
        applyProperty(builder, props.getProperty(PROP_CONNECT_TIMEOUT), ValueType.LONG, PropertyTarget.CONNECT_TIMEOUT);
        applyProperty(builder, props.getProperty(PROP_READ_TIMEOUT), ValueType.LONG, PropertyTarget.READ_TIMEOUT);

        applyProperty(builder, env.get(ENV_BASE_URL), ValueType.STRING, PropertyTarget.BASE_URL);
        applyProperty(builder, env.get(ENV_MODEL), ValueType.STRING, PropertyTarget.MODEL);
        applyProperty(builder, env.get(ENV_API_KEY), ValueType.STRING, PropertyTarget.API_KEY);
        applyProperty(builder, env.get(ENV_REFERER), ValueType.STRING, PropertyTarget.REFERER);
        applyProperty(builder, env.get(ENV_TITLE), ValueType.STRING, PropertyTarget.TITLE);
        applyProperty(builder, env.get(ENV_CONNECT_TIMEOUT), ValueType.LONG, PropertyTarget.CONNECT_TIMEOUT);
        applyProperty(builder, env.get(ENV_READ_TIMEOUT), ValueType.LONG, PropertyTarget.READ_TIMEOUT);

        return builder.build();
    }

    private static void applyProperty(Builder builder, String value, ValueType type, PropertyTarget target) {
        if (value == null) {
            return;
        }
        String trimmed = value.trim();
        if (trimmed.length() == 0) {
            return;
        }
        switch (type) {
            case STRING:
                applyString(builder, trimmed, target);
                break;
            case LONG:
                try {
                    long longValue = Long.parseLong(trimmed);
                    applyLong(builder, longValue, target);
                } catch (NumberFormatException ex) {
                    // ignore malformed values but keep defaults intact
                }
                break;
            default:
                break;
        }
    }

    private static void applyString(Builder builder, String value, PropertyTarget target) {
        switch (target) {
            case API_KEY:
                builder.apiKey(value);
                break;
            case BASE_URL:
                builder.baseUrl(value);
                break;
            case MODEL:
                builder.model(value);
                break;
            case REFERER:
                builder.referer(value);
                break;
            case TITLE:
                builder.title(value);
                break;
            default:
                break;
        }
    }

    private static void applyLong(Builder builder, long value, PropertyTarget target) {
        switch (target) {
            case CONNECT_TIMEOUT:
                builder.connectTimeoutMillis(value);
                break;
            case READ_TIMEOUT:
                builder.readTimeoutMillis(value);
                break;
            default:
                break;
        }
    }

    private static Properties loadPropertiesFile() {
        File confFile = locateConfigFile("ramus-ai.conf");
        if (confFile == null || !confFile.exists()) {
            return null;
        }
        FileInputStream stream = null;
        try {
            Properties properties = new Properties();
            stream = new FileInputStream(confFile);
            properties.load(stream);
            return properties;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static File locateConfigFile(String fileName) {
        File confFile = null;
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase == null) {
            catalinaBase = System.getProperty("ramus.server.base");
        }
        if (catalinaBase != null) {
            File file = new File(catalinaBase, "conf");
            file = new File(file, fileName);
            if (file.exists()) {
                confFile = file;
            }
        }
        if (confFile == null) {
            File file = new File(fileName);
            if (file.exists()) {
                confFile = file;
            } else {
                file = new File("conf" + File.separator + fileName);
                if (file.exists()) {
                    confFile = file;
                }
            }
        }
        return confFile;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String apiKey;
        private String baseUrl;
        private String model;
        private String referer;
        private String title;
        private long connectTimeoutMillis = DEFAULT_TIMEOUT;
        private long readTimeoutMillis = DEFAULT_TIMEOUT;

        public Builder apiKey(String value) {
            this.apiKey = value;
            return this;
        }

        public Builder baseUrl(String value) {
            this.baseUrl = value;
            return this;
        }

        public Builder model(String value) {
            this.model = value;
            return this;
        }

        public Builder referer(String value) {
            this.referer = value;
            return this;
        }

        public Builder title(String value) {
            this.title = value;
            return this;
        }

        public Builder connectTimeoutMillis(long value) {
            this.connectTimeoutMillis = value;
            return this;
        }

        public Builder readTimeoutMillis(long value) {
            this.readTimeoutMillis = value;
            return this;
        }

        public AiConfig build() {
            if (baseUrl == null || baseUrl.length() == 0) {
                baseUrl = DEFAULT_BASE_URL;
            }
            if (model == null || model.length() == 0) {
                model = DEFAULT_MODEL;
            }
            if (referer == null || referer.length() == 0) {
                referer = DEFAULT_REFERER;
            }
            if (title == null || title.length() == 0) {
                title = DEFAULT_TITLE;
            }
            return new AiConfig(this);
        }
    }

    private enum ValueType {
        STRING,
        LONG
    }

    private enum PropertyTarget {
        API_KEY,
        BASE_URL,
        MODEL,
        REFERER,
        TITLE,
        CONNECT_TIMEOUT,
        READ_TIMEOUT
    }
}
