package com.ramussoft.pb.master.model;

import com.ramussoft.pb.master.Factory;

public abstract class AbstractProperty implements Property {

    protected Object value = null;

    public int getMax() {
        return -1;
    }

    public int getMin() {
        return -1;
    }

    public String getName() {
        return Factory.getString(getKey());
    }

    public Object getValue() {
        return value;
    }

    public boolean isReadOnly() {
        return false;
    }

    public void setValue(final Object value) {
        this.value = value;
    }

}
