package com.ramussoft.core.attribute.simple;

import com.ramussoft.common.attribute.AbstractAttributePlugin;

public abstract class SimpleAttributePlugin extends AbstractAttributePlugin {

    @Override
    public String getName() {
        return "Core";
    }

    @Override
    public boolean isComparable() {
        return true;
    }

    @Override
    public boolean isSystem() {
        return false;
    }

}
