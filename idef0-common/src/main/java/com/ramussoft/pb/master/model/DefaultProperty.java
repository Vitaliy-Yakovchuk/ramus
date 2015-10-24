package com.ramussoft.pb.master.model;

public class DefaultProperty extends AbstractProperty {

    private final String key;

    private final int type;

    public DefaultProperty(final String key, final int type) {
        this.key = key;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return key + ": " + getValue();
    }
}
