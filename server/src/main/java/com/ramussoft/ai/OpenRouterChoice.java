package com.ramussoft.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenRouterChoice {
    private Integer index;
    private OpenRouterMessage message;
    @JsonProperty("finish_reason")
    private String finishReason;

    public OpenRouterChoice() {
    }

    public OpenRouterChoice(Integer index, OpenRouterMessage message, String finishReason) {
        this.index = index;
        this.message = message;
        this.finishReason = finishReason;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public OpenRouterMessage getMessage() {
        return message;
    }

    public void setMessage(OpenRouterMessage message) {
        this.message = message;
    }

    public String getFinishReason() {
        return finishReason;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }
}
