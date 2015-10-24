package com.ramussoft.eval;

public class ValueValue implements Value {

    private String name;

    private EObject value;

    public ValueValue(String name) {
        this.name = name;
    }

    @Override
    public EObject get() {
        return value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(EObject value) {
        this.value = value;
    }

    public EObject getValue() {
        return value;
    }

    @Override
    public void fill(StringBuffer sb) {
        sb.append(name);
    }

}
