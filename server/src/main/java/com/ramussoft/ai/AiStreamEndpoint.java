package com.ramussoft.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Endpoint definition for a stream connection.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiStreamEndpoint {

    private String type;

    private String ref;

    private String side;

    private Double x;

    private Double y;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }
}
