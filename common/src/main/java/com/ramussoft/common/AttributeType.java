package com.ramussoft.common;

import java.io.Serializable;
import java.util.Arrays;

import com.ramussoft.common.attribute.AttributePlugin;

public class AttributeType implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 2873611246615678282L;

    private String pluginName;

    private String typeName;

    private boolean comparable;

    private boolean light = true;

    private boolean historySupport = true;

    public AttributeType() {
    }

    public AttributeType(String pluginName, String typeName, boolean comparable) {
        this();
        this.pluginName = pluginName;
        this.typeName = typeName;
        this.comparable = comparable;
    }

    public AttributeType(String pluginName, String typeName) {
        this(pluginName, typeName, true);
    }

    public AttributeType(String pluginName, String typeName,
                         boolean comparable, boolean light, boolean historySupport) {
        this(pluginName, typeName, comparable);
        this.light = light;
        this.historySupport = historySupport;
    }

    /**
     * Return name of the plug-in, which uses this attribute.
     */

    public String getPluginName() {
        return pluginName;
    }

    /**
     * Return type name of the attribute.
     */

    public String getTypeName() {
        return typeName;
    }

    /**
     * <code>true</code> if attribute data can be compared, <code>false</code>
     * if attribute data can not be compared.
     */

    public boolean isComparable() {
        return comparable;
    }

    /**
     * @param pluginName the pluginName to set
     */
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * @param typeName the typeName to set
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * @param comparable the comparable to set
     */
    public void setComparable(boolean comparable) {
        this.comparable = comparable;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AttributeType) {
            AttributeType type = (AttributeType) obj;
            return pluginName.equals(type.pluginName)
                    && typeName.equals(type.typeName);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new String[]{pluginName, typeName});
    }

    @Override
    public String toString() {
        return pluginName + "." + typeName;
    }

    /**
     * Return <code>true</code> if attribute is light and can be loaded with
     * tables, etc.
     *
     * @return <code>true</code> is object is light, <code>false</code> if not.
     */

    public boolean isLight() {
        return light;
    }

    public boolean isHistorySupport() {
        return historySupport;
    }

    public AttributePlugin getAttributePlugin(Engine engine) {
        return (AttributePlugin) engine.getPluginProperty(pluginName, typeName
                + ".Plugin");
    }
}
