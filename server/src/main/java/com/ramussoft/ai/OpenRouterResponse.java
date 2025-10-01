package com.ramussoft.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenRouterResponse {
    private String id;
    private String model;
    private List<OpenRouterChoice> choices;

    public OpenRouterResponse() {
    }

    public OpenRouterResponse(String id, String model, List<OpenRouterChoice> choices) {
        this.id = id;
        this.model = model;
        this.choices = choices;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<OpenRouterChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<OpenRouterChoice> choices) {
        this.choices = choices;
    }
}
