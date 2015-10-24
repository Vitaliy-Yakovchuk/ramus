package com.ramussoft.pb.master.model;

public class DefaultProperties extends AbstractProperties {

    private final Property[] properties;

    private final String describe;

    public DefaultProperties(final Property[] properties, final String describe) {
        this.properties = properties;
        this.describe = describe;
    }

    public Property getProperty(final int i) {
        return properties[i];
    }

    public int getPropertyCount() {
        return properties.length;
    }

    public String getDescribe() {
        return describe;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final Property p : properties) {
            sb.append(p.toString());
            sb.append("; ");
        }
        return sb.toString();
    }
}
