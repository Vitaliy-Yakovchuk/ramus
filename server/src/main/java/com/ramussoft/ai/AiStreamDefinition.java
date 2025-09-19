package com.ramussoft.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Stream/arrow description returned by the AI assistant.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiStreamDefinition {

    private String id;

    private String name;

    private AiStreamEndpoint source;

    private AiStreamEndpoint target;

    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AiStreamEndpoint getSource() {
        return source;
    }

    public void setSource(AiStreamEndpoint source) {
        this.source = source;
    }

    public AiStreamEndpoint getTarget() {
        return target;
    }

    public void setTarget(AiStreamEndpoint target) {
        this.target = target;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
